import logging
import os
import re
import time
import timeit
import airtest.aircv as airtest
import numpy as np
import cv2

from PIL import Image
from io import BytesIO
import base64
import pytesseract
from configparser import ConfigParser

from selenium import webdriver
from selenium.common.exceptions import ElementClickInterceptedException, InvalidArgumentException, \
    ElementNotInteractableException, StaleElementReferenceException
from selenium.webdriver import ActionChains, Keys
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service as ChromeService
from selenium.webdriver.remote.webelement import WebElement
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.chrome.service import Service

import _testconfig as tc
from infrastructure.system import System
from infrastructure.utils import WebDriverUtils
from infrastructure.wait import Wait


def measure_time(func):
    def wrapper(*args, **kwargs):
        start_time = timeit.default_timer()
        result = func(*args, **kwargs)
        end_time = timeit.default_timer()
        logging.info(f"Function {func.__name__} executed in {end_time - start_time:.4f} seconds")
        return result

    return wrapper


class DriverWrapper(object):
    _instance = driver = init_handle = wait = None
    _action_chains = None

    def __new__(cls, debug_mode=False):
        if cls._instance is None:
            cls._instance = super(DriverWrapper, cls).__new__(cls)
            cls.start(debug_mode)
        return cls._instance

    @classmethod
    def start(cls, debug_mode):
        if cls.driver is None:
            options = Options()
            prefs = {'profile.default_content_settings.popups': 0, 'download.default_directory': tc.download_dir}
            options.add_argument('--ignore-certificate-errors')
            if debug_mode:
                options.add_experimental_option('debuggerAddress', tc.debug_address)
            else:
                options.add_experimental_option('prefs', prefs)
                options.add_experimental_option('excludeSwitches', ['enable-automation'])
            try:
                cdm = ChromeDriverManager()
                chrome_version = cdm.driver.get_browser_version_from_os()
                driver_path = System.get_cached_chromedriver_path(chrome_version)
                if not (driver_path and os.path.exists(driver_path)):
                    driver_path = cdm.install()
                service = Service(driver_path)
                cls.driver = webdriver.Chrome(service=service, options=options)
                cls.driver.implicitly_wait(10)
                cls.driver.maximize_window()
                cls.init_handle = cls.driver.current_window_handle
                cls.wait = Wait(cls.driver)
            except Exception as e:
                print(f"Error initializing ChromeDriver: {e}")
                raise

    @classmethod
    def close(cls):
        if cls.driver is not None:
            cls.driver.quit()
            cls.driver = None
        cls._instance = None

    def get(self, url):
        self.driver.get(url)

    def refresh(self):
        self.driver.refresh()
        # self.wait.wait_page_loading()

    def back(self):
        self.driver.back()

    def max(self):
        self.driver.maximize_window()

    def min(self):
        self.driver.minimize_window()

    @classmethod
    def open_new_tab(cls, url):
        """
        打开一个新的标签页并切换到该页面。

        :param url: 要在新标签页中打开的 URL。
        """
        # 使用 JavaScript 打开一个新的标签页
        cls.driver.execute_script("window.open('');")
        # 切换到新打开的标签页
        cls.driver.switch_to.window(cls.driver.window_handles[-1])
        # 在新标签页中打开指定的 URL
        cls.driver.get(url)

    @classmethod
    def close_tab(cls, url, back_to_init_page=True):
        for handle in cls.driver.window_handles:
            if url == cls.driver.current_url:
                cls.driver.close()
                break
        if back_to_init_page:
            cls.driver.switch_to.window(cls.init_handle)
        else:
            cls.driver.switch_to.window(cls.driver.window_handles[-1])

    def find_element(self, *loc, scope: WebElement = None):
        loc_x = WebDriverUtils.transform_xpath_locator(*loc)
        if scope is None:
            self.wait.wait_until_element_exist(*loc_x)
            return self.driver.find_element(*loc_x)
        else:
            if isinstance(scope, WebElement):
                self.wait.wait_until_element_exist(*loc_x)
                return scope.find_element(*loc_x)
            else:
                raise ValueError("scope must be a WebElement.")

    def find_elements(self, *loc, scope: WebElement = None):
        loc_x = WebDriverUtils.transform_xpath_locator(*loc)
        if scope is None:
            self.wait.wait_until_element_exist(*loc_x)
            return self.driver.find_elements(*loc_x)
        else:
            if isinstance(scope, WebElement):
                self.wait.wait_until_element_exist(*loc_x)
                return scope.find_elements(*loc_x)
            else:
                raise ValueError("scope must be a WebElement.")

    def click(self, *target, timeout=5):
        if not isinstance(target[0], WebElement):
            self.wait.wait_until_element_clickable(*target, timeout=timeout)
            target = self.find_element(*target)
        else:
            target = target[0]
        try:
            self.scroll_into_view(target)
            target.click()
        except (ElementClickInterceptedException, ElementNotInteractableException, InvalidArgumentException) as ex:
            # loc = target.location_once_scrolled_into_view
            try:
                ActionChains(self.driver).move_to_element(target).click(target).perform()
            except ElementClickInterceptedException as cie:
                self.js_click(target)

    def js_click(self, target):
        if not isinstance(target, WebElement):
            target = self.find_element(*target)
        self.driver.execute_script("arguments[0].click();", target)

    def click_and_wait(self, target, wait_time):
        self.click(target)
        time.sleep(wait_time)

    def double_click(self, *target, timeout=5):
        if not isinstance(target[0], WebElement):
            self.wait.wait_until_element_clickable(*target, timeout)
            target = self.find_element(*target)
        else:
            target = target[0]
        ActionChains(self.driver).double_click(target).perform()

    def input_by_loc(self, method, locator, value, timeout=5):
        target = self.find_element(method, locator)
        self.input(target, value, timeout)

    def input(self, target, value, timeout=5):
        self.wait.wait_until_element_clickable(target, timeout)
        try:
            self.clear_up_input(target)
        except:
            pass
        finally:
            target.clear()

        target.send_keys(value)

    @staticmethod
    def clear_up_input(target):
        target.send_keys(Keys.CONTROL + 'a')
        target.send_keys(Keys.DELETE)
        # self.driver.execute_script("arguments[0].value = '';", target)

    def get_text(self, target):
        if not isinstance(target, WebElement):
            target = self.find_element(target)
        return target.text if target.text != '' else target.get_attribute('innerText')

    def get_value(self, target):
        if not isinstance(target, WebElement):
            target = self.find_element(target)
        return target.get_attribute('value')

    @property
    def action_chains(self):
        if self._action_chains is None:
            self._action_chains = ActionChains(self.driver)
        return self._action_chains

    def scroll_into_view(self, target):
        if not isinstance(target, WebElement):
            target = self.find_element(target)
        self.driver.execute_script('arguments[0].scrollIntoView({block: "center"});', target)

    def scroll_down(self, v_pixel):
        # js = "var q=document.documentElement.scrollTop=10000"
        # self.driver.execute_script(js)
        self.driver.execute_script(f'window.scrollBy(0,{v_pixel})')

    def scroll_to(self, v_pixel):
        self.driver.execute_script(f'window.scrollTo(0,{v_pixel})')

    def get_canvas_content(self, target, threshold=127):
        """
        OCR提取pdf文件中的文本内容（canvas元素）
        目前OCR识别准确率不高，只能用来判断其中是否包含某些关键内容，如果断言失败，需人工复验
        :param target: canvas元素，或用于定位canvas的locator
        :return: canvas中的文本内容
        """
        content = ''
        for t in target:
            canvas_data = t.screenshot_as_base64
            canvas_image = Image.open(BytesIO(base64.b64decode(canvas_data))).convert("RGB")
            inverted_image = Image.eval(canvas_image, lambda x: 255 - x)
            gray_image = inverted_image.convert('L')
            binary_image = gray_image.point(lambda p: 255 if p > threshold else 0)
            extracted_text = pytesseract.image_to_string(binary_image, lang='eng')
            c = re.sub(r'\n+', '\n', extracted_text)
            content += c + '\n'
        return content

    def operate_by_image(self, target, operation='CheckElement', threshold=0.8, auto_capture=True):
        screenshot_bytes = self.driver.get_screenshot_as_png()
        screenshot_array = np.frombuffer(screenshot_bytes, dtype=np.uint8)
        screen = cv2.imdecode(screenshot_array, cv2.IMREAD_COLOR)
        
        # 检查目标图片是否存在
        import os
        if not os.path.exists(target):
            if auto_capture:
                # 自动保存当前屏幕截图作为参考图片
                print(f"\n[自动截图] 参考图片不存在，自动保存: {target}")
                cv2.imwrite(target, screen)
                print(f"[自动截图] 已保存屏幕截图 ({screen.shape[1]}x{screen.shape[0]})")
                print(f"[自动截图] 跳过此次验证，下次运行将使用此图片进行比对\n")
                return
            else:
                raise FileNotFoundError(f"参考图片不存在: {target}")
        
        target_img = airtest.imread(target)

        if operation == 'CheckElement':
            try:
                result = airtest.find_template(screen, target_img, threshold=threshold)
                assert result is not None, f"图片匹配失败: {target}"
            except Exception as e:
                # 如果匹配失败且开启自动捕获，更新参考图片
                if auto_capture and "bigger than im_source" in str(e):
                    print(f"\n[自动截图] 参考图片尺寸过大，自动更新: {target}")
                    cv2.imwrite(target, screen)
                    print(f"[自动截图] 已更新屏幕截图 ({screen.shape[1]}x{screen.shape[0]})")
                    print(f"[自动截图] 跳过此次验证，下次运行将使用新图片进行比对\n")
                    return
                raise
        elif operation == 'ClickElement':
            click_x, click_y = airtest.find_template(screen, target_img, threshold=threshold)['result']
            ActionChains(self.driver).move_by_offset(click_x, click_y).click().perform()
        elif operation == 'RightClick':
            click_x, click_y = airtest.find_template(screen, target_img, threshold=threshold)['result']
            ActionChains(self.driver).move_by_offset(click_x, click_y).context_click().perform()
        else:
            raise ValueError(f"Invalid operation：{operation}")

