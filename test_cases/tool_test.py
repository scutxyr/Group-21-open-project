import os
import time

from selenium.webdriver.common.keys import Keys

from test_cases.conftest import *

class TestTool(object):
    def test_to_home_page(self):
        with (goto_tool(tc.Tool) as home_page):
            time.sleep(0.5)
            home_page.click_controller()
            home_page.elements.controller.send_keys('help')
            home_page.elements.controller.send_keys(Keys.ENTER)
            time.sleep(0.5)
            assert 'Display JVM startup time and runtime statistics' in home_page.get_terminal_text()
            assert 'Detect potential memory leaks' in home_page.get_terminal_text()
