import os
import time

from test_cases.conftest import *

class TestTool(object):
    def test_to_home_page(self):
        with goto_tool(tc.Tool) as home_page:
            home_page.click_variable_button()
            time.sleep(200)
