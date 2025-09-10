import datetime
import logging
import os.path
import time
from contextlib import contextmanager
from pathlib import Path

import allure
import pytest
from PIL import ImageGrab

from infrastructure.driver import DriverWrapper
from infrastructure.envmgr import *
from page_objects.common.login import LoginPage
from page_objects.home_page import HomePage


class TestEnvError(Exception):
    def __init__(self, unsupported_env):
        self.unsupported_env = unsupported_env
        self.msg = f'{unsupported_env} is not supported. please use one of ["QA", "UAT", "SI", "PROD"]'


def pytest_addoption(parser):
    """
    add option --test_env for pytest
    :param parser:
    :return:
    """
    parser.addoption('--test_env', action='store', default='UAT',
                     help='test environment, one of [QA, UAT, SI, CI, PROD]')


@pytest.fixture(scope='session')
def parse_test_env(request):
    test_env = request.config.getoption('--test_env')
    if test_env not in ('QA', 'UAT', 'SI', 'SI', 'PROD'):
        raise TestEnvError(test_env)
    return test_env


@pytest.fixture(scope='session', autouse=True)
def reload_config_for_env(parse_test_env):
    EnvMgr.set_env_and_reload(parse_test_env)


@pytest.fixture(scope='session', autouse=True)
def driver_wrapper():
    dw_instance = DriverWrapper()
    dw_instance.get(tc.url)
    yield dw_instance
    dw_instance.close()


@pytest.hookimpl(tryfirst=True, hookwrapper=True)
def pytest_runtest_makereport(item):
    """
    pytest 钩子，用于在测试失败时进行截图
    """
    outcome = yield
    report = outcome.get_result()
    error_handled = read_error_handle_flag()
    if report.when == 'call' and report.failed:
        if error_handled == str(False):
            test_name = item.function.__name__
            screenshot = capture_screenshot(test_name)
            attach_screenshot_to_allure_report(screenshot)
    set_error_handle_flag(False)


def capture_screenshot(test_name=''):
    screenshot_file_path = ''
    with allure.step(f"Capture screenshot on failure: {test_name}"):
        try:
            # 确保截图目录存在
            dir_path = Path(tc.screenshot_dir)
            if not dir_path.exists():
                dir_path.mkdir(parents=True, exist_ok=True)

            # 生成截图文件名
            sc_file_name = f'{test_name}_{datetime.datetime.now().strftime("%Y%m%d%H%M%S")}.png'
            screenshot_file_path = os.path.join(tc.screenshot_dir, sc_file_name)

            # 截图并保存
            screenshot = ImageGrab.grab()
            screenshot.save(screenshot_file_path)
        except Exception as ex:
            logging.warning(f'Take screenshot error. ex: {str(ex)}')
    return screenshot_file_path


def attach_screenshot_to_allure_report(sc_full_path):
    # 将截图附加到 Allure 报告
    with open(sc_full_path, "rb") as image_file:
        allure.attach(image_file.read(), name="failure screenshot", attachment_type=allure.attachment_type.PNG)


def set_error_handle_flag(handled):
    flag_file = os.path.join(os.path.abspath(os.path.dirname(os.path.dirname(__file__))), 'error_handled_flag.txt')
    with open(flag_file, 'w') as flag:
        flag.write(str(handled))


def read_error_handle_flag():
    flag_file = os.path.join(os.path.abspath(os.path.dirname(os.path.dirname(__file__))), 'error_handled_flag.txt')
    if not os.path.isfile(flag_file):
        set_error_handle_flag(False)
        return False
    else:
        with open(flag_file, 'rb') as flag:
            return flag.read()


def login(app, max_retries=3):
    retry_count = 0
    login_page = LoginPage(tc.url)
    while retry_count <= max_retries:
        try:
            # login_page.page.wait.wait_page_loading()
            break
        except Exception:
            retry_count += 1
            time.sleep(1)
            continue
    home_page = None
    if app == tc.Tool:
        home_page = HomePage()
    return home_page


# with goto_tool(app)
@contextmanager
def goto_tool(app):
    home = login(app)
    try:
        yield home
    except Exception:
        screenshot = capture_screenshot()
        attach_screenshot_to_allure_report(screenshot)
        set_error_handle_flag(True)
        raise
    finally:
        # time.sleep(1000)
        retry_count = 0
        while retry_count < 3:
            try:
                # DriverWrapper.wait.wait_page_loading()
                break
            except Exception:
                retry_count += 1
                time.sleep(1)
                continue
