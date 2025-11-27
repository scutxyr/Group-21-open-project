import time
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestJVM(object):

    def test_jvm_command(self):

        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()
            
            home_page.terminal_input('jvm')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(1.0)
            home_page.image_operate(tc.data_dir + "\\jvm_result.png")
            
            print(f"\n[测试通过] jvm命令输出与预期图片匹配")
