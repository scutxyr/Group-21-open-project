from datetime import datetime, timedelta
import warnings
import time

from infrastructure.system import System
from infrastructure.utils import *

from selenium.common.exceptions import TimeoutException
from selenium.common.exceptions import NoSuchElementException
from selenium.common.exceptions import StaleElementReferenceException
from selenium.webdriver.common.action_chains import *
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebElement
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait


class Wait(object):
    def __init__(self, driver):
        self.driver = driver

    def wait_until_element_visible(self, target, timeout=5):
        msg = "Element is still not visible in %s seconds" % timeout
        if isinstance(target, WebElement):
            msg = "%s. [Element text]: %s" % (msg, target.text)
            element_if_visible = WebDriverWait(self.driver, timeout).until(EC.visibility_of(target), msg)
        elif isinstance(target, tuple):
            target = WebDriverUtils.transform_xpath_locator(*target)
            msg = "%s. [locator]: %s" % (msg, target)
            element_if_visible = WebDriverWait(self.driver, timeout).until(EC.visibility_of_element_located(target), msg)
        else:
            raise TypeError("supported type is either WebElement or tuple (locator).")
        return element_if_visible

    def wait_until_element_invisible(self, target, timeout=5):
        invisible = None
        msg = "Element is still visible in %s seconds" % timeout
        if isinstance(target, WebElement):
            msg = "%s. [Element text]: %s" % (msg, target.text)
            invisible = WebDriverWait(self.driver, timeout).until(EC.invisibility_of_element(target), msg)
        elif isinstance(target, tuple):
            target = WebDriverUtils.transform_xpath_locator(*target)
            msg = "%s. [locator]: %s" % (msg, target)
            invisible = WebDriverWait(self.driver, timeout).until(EC.invisibility_of_element_located(target), msg)
        else:
            raise TypeError("supported type is either WebElement or tuple (locator).")
        return invisible

    def wait_until_element_clickable(self, *target, timeout=5):
        msg = "Element is not clickable in %s seconds" % timeout
        clickable = False
        if not isinstance(target[0], WebElement):
            target = WebDriverUtils.transform_xpath_locator(*target)
            msg = "%s. [locator]: %s" % (msg, target)
            WebDriverWait(self.driver, timeout).until(EC.element_to_be_clickable(target), msg)
            time.sleep(0.1)
        else:
            target = target[0]
            start = datetime.now()
            while datetime.now() - start < timedelta(seconds=timeout):
                x = target.rect.get("x") if "x" in target.rect.keys() else -9999
                y = target.rect.get("y") if "y" in target.rect.keys() else -9999
                width = target.rect.get("width") if "width" in target.rect.keys() else -9999
                height = target.rect.get("height") if "height" in target.rect.keys() else -9999
                rect_locatable = x >= 0 and y >= 0 and width >= 0 and height >= 0
                # For element like checkbox, .is_displayed() always return false, use rect_locatable instead.
                if (target.is_displayed() or rect_locatable) and target.is_enabled():
                    clickable = True
                    break
                else:
                    time.sleep(0.1)
            timed_out = datetime.now() - start > timedelta(seconds=timeout)
            if not clickable and timed_out:
                raise TimeoutException("%s. [Element text]: %s" % (msg, target.text))
            else:
                time.sleep(0.1)

    def wait_until_element_exist(self, *target, timeout=5):
        element_exist = False
        msg = "Element still not exist in %s seconds" % timeout

        start = datetime.now()
        while (datetime.now() - start).seconds < timeout:
            try:
                target = WebDriverUtils.transform_xpath_locator(*target)
                msg = "%s. [locator]: %s" % (msg, target)
                self.driver.find_element(*target)
                element_exist = True
                break
            except NoSuchElementException as ex:
                time.sleep(0.1)
        if not element_exist and datetime.now() - start > timedelta(seconds=timeout):
            raise TimeoutException("%s. [Element]: %s" % (msg, target))
        return element_exist

    def wait_until_element_gone(self, target, timeout=5):
        if isinstance(target, tuple):
            try:
                target = WebDriverUtils.transform_xpath_locator(*target)
                target = self.driver.find_element(*target)
            except NoSuchElementException as ex:
                return
            except StaleElementReferenceException as ex:
                return
        elif isinstance(target, WebElement):
            try:
                rect = target.rect
            except StaleElementReferenceException as ex:
                return
        else:
            raise TypeError("supported type is either WebElement or tuple (locator).")
        msg = "Element is still alive after %s seconds." % timeout
        WebDriverWait(self.driver, timeout).until(EC.staleness_of(target), msg)

    def wait_until_element_contains_text(self, target, text, timeout=5, wait_before_element_exist=120):
        msg = "Text is not shown on element in %s seconds" % timeout
        if not (isinstance(target, WebElement) or isinstance(target, tuple)):
            raise TypeError("supported type is either WebElement or tuple (locator).")
        if not isinstance(target, WebElement):
            self.wait_until_element_exist(target, wait_before_element_exist)
            target = WebDriverUtils.transform_xpath_locator(*target)
            target = self.driver.find_element(*target)
        contains = False
        start = datetime.now()
        while (datetime.now() - start).seconds < timeout:
            if text in target.text.strip() or text in target.get_attribute('innerText').strip():
                contains = True
                break
            else:
                time.sleep(0.1)
        if not contains and datetime.now() - start > timedelta(seconds=timeout):
            raise TimeoutException("%s. [Element tag]: %s" % (msg, target.tag_name))

    def wait_page_loading(self, before_wait=0.1, timeout=60):
        time.sleep(before_wait)
        loading_completed = False
        # Todo
        loc_mask = ""

        try:
            mask = self.driver.find_element(By.XPATH, loc_mask)
        except NoSuchElementException as ex:
            warnings.warn("Did not find page loader element, please confirm if page design changed.")
            return

        start = datetime.now()
        while datetime.now() - start < timedelta(seconds=timeout):
            try:
                if "visible" not in mask.get_attribute("class"):
                    loading_completed = True
                    break
                else:
                    time.sleep(0.1)
            except StaleElementReferenceException as sere:
                mask = self.driver.find_element(By.XPATH, loc_mask)
                time.sleep(0.1)
        timed_out = datetime.now() - start > timedelta(seconds=timeout)
        if not loading_completed and timed_out:
            raise TimeoutException("Page did not load completed in %d seconds" % timeout)

    def wait_until_element_disappear(self, locator, before_wait=0.1, timeout=30):
        time.sleep(before_wait)
        loading_completed = False
        try:
            target = self.driver.find_element(By.XPATH, locator)
        except NoSuchElementException as ex:
            warnings.warn("Did not find page loader element, please confirm if page design changed.")
            return

        start = datetime.now()
        while datetime.now() - start < timedelta(seconds=timeout):
            try:
                if "visible" not in target.get_attribute("class"):
                    loading_completed = True
                    break
                else:
                    time.sleep(0.1)
            except StaleElementReferenceException as sere:
                target = self.driver.find_element(By.XPATH, locator)
                time.sleep(0.1)
        timed_out = datetime.now() - start > timedelta(seconds=timeout)
        if not loading_completed and timed_out:
            raise TimeoutException("Page did not load completed in %d seconds" % timeout)

    @classmethod
    def wait_file_download_completed(cls, expected_count, timeout=30):
        actual_count = System.count_download_files()
        start = datetime.now()
        while datetime.now() - start < timedelta(seconds=timeout):
            if actual_count < expected_count:
                actual_count = System.count_download_files()
                time.sleep(0.1)
            else:
                break
        return actual_count

    def wait_until_element_not_disabled(self, target, timeout=30):
        start = datetime.now()
        abled = False
        while datetime.now() - start < timedelta(seconds=timeout):
            if 'disabled' in target.get_attribute('class') or target.get_attribute('aria-disabled') == 'true' or target.get_attribute('disabled') == 'true':
                time.sleep(0.1)
            else:
                abled = True
                break
        if abled:
            pass
        else:
            msg = "Element is not abled in %s seconds" % timeout
            raise TimeoutException("%s. [Element text]: %s" % (msg, target.text))