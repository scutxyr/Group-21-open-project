import os
import time

from selenium.webdriver.common.keys import Keys

from test_cases.conftest import *

class TestTool(object):
    def test_to_home_page(self):
        with (goto_tool(tc.Tool) as home_page):
            time.sleep(0.5)
            home_page.click_controller()
            home_page.terminal_input('help')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(0.5)
            assert 'Display JVM startup time and application uptime statistics' in home_page.get_terminal_text()
            assert 'Display jvm memory info' in home_page.get_terminal_text()
            home_page.image_operate(tc.data_dir + "\\help_result.png")
    def test_terminal_input_startup_time(self):
        with (goto_tool(tc.Tool) as home_page):
            time.sleep(0.5)
            home_page.click_controller()
            home_page.terminal_input('startup-time')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(0.5)
            home_page.image_operate(tc.data_dir + "\\startup_time_result.png")
            home_page.elements.controller.send_keys(Keys.CONTROL, 'z')

