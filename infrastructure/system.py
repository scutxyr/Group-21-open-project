# -*- coding: utf-8 -*-
import time
import _testconfig as tc
from datetime import datetime, timedelta
from pywinauto import mouse
from pywinauto import keyboard
from pywinauto.application import *
from pywinauto.findwindows import *

from datetime import datetime, timedelta
import time
from pywinauto.application import Application
from pywinauto.findwindows import ElementNotFoundError, find_windows
from pywinauto.timings import TimeoutError
import pywinauto


class System(object):

    @staticmethod
    def start(cmd):
        os.popen(cmd)

    @staticmethod
    def set_app_foreground(handle):
        win32gui.SetForegroundWindow(handle)

    @staticmethod
    def get_foreground_app():
        return win32gui.GetForegroundWindow()

    @staticmethod
    def switch_to_recent_app():
        keyboard.send_keys("%{TAB}")

    @staticmethod
    def count_file(root_dir, type_dict, rec=False):
        file_count = 0
        file_list = os.listdir(root_dir)
        for file_name in file_list:
            if os.path.isdir(os.path.join(root_dir, file_name)):
                type_dict.setdefault("folder", 0)
                type_dict["folder"] += 1
                if rec:
                    p_dir = os.path.join(root_dir, file_name)
                    System.count_file(p_dir, type_dict)
            else:
                ext = os.path.splitext(file_name)[1]
                type_dict.setdefault(ext, 0)
                type_dict[ext] += 1
                file_count += 1
        return file_count, type_dict

    @staticmethod
    def count_download_files(ext='.xlsx'):
        type_dict = dict()
        file_count, type_dict = System.count_file(tc.download_dir, type_dict)
        return type_dict[ext] if ext in type_dict.keys() else 0

    @staticmethod
    def get_modification_time(file_or_dir_path):
        return os.path.getmtime(file_or_dir_path)

    @staticmethod
    def get_cached_chromedriver_path(chrome_version, filename='chromedriver.exe'):
        # 获取用户主目录
        user_home = os.path.expanduser("~")
        # 构建 WebDriverManager 的缓存目录路径
        wdm_cache_dir = os.path.join(user_home, r".wdm\drivers\chromedriver")
        # 正则表达式匹配 chromedriver 版本目录
        chromedriver_pattern = re.compile(f"{chrome_version}.*")

        for root, dirs, files in os.walk(wdm_cache_dir):
            dirs = [os.path.join(root, s_dir) for s_dir in dirs]
            dirs.sort(key=System.get_modification_time, reverse=True)
            for dir_name in dirs:
                dir_name = os.path.basename(dir_name)
                if chromedriver_pattern.match(dir_name):
                    version_dir = os.path.join(root, dir_name)
                    for version_root, sub_dirs, inner_files in os.walk(version_dir):
                        if filename in inner_files:
                            driver_path = os.path.join(version_root, filename)
                            return driver_path
        return None


def escape_brackets(file_path):
    # 替换左括号 '(' 为 '{(}'
    file_path = file_path.replace('(', '{(}')
    # 替换右括号 ')' 为 '{)}'
    file_path = file_path.replace(')', '{)}')
    return file_path


class OpenFileDialog(object):
    @staticmethod
    def upload_file_by_path(file_path):
        """Upload file via the open file windows dialog
        :param file_path: String, file path to open
        """
        app = Application(backend="win32")
        start = datetime.now()

        # 等待文件选择对话框弹出
        while datetime.now() - start < timedelta(seconds=10):
            try:
                app.connect(title_re='Open|打开', class_name='#32770')  # 添加更多可能的标题
                print("Connected to the open file dialog.")
                break
            except ElementNotFoundError:
                print("waiting for open file dialog to open.")
                time.sleep(1)
        else:
            raise TimeoutError("Failed to find the open file dialog within the timeout period.")

        # 列出所有打开的窗口，检查文件选择对话框是否存在
        windows = find_windows()
        for win in windows:
            try:
                title = app.window(handle=win).window_text()
                print(f"Window handle: {win}, Window title: {title}")
            except Exception as e:
                print(f"Could not get window text for handle {win}: {e}")

        # 确保应用程序已连接
        if not app.is_process_running():
            raise pywinauto.application.AppNotConnected("Failed to connect to the open file dialog.")

        file_path = escape_brackets(file_path)
        # 执行文件上传操作
        try:
            # 使用正则表达式来匹配窗口名称
            app.window(title_re='Open|打开')["ComboBoxEx32"].type_keys(file_path, with_spaces=True)
            time.sleep(1)
            app.window(title_re='Open|打开').Button1.click()
            time.sleep(0.5)
        except Exception as e:
            print(f"An error occurred while uploading the file: {e}")
            raise

