import time
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestMemory(object):

    def test_memory_command(self):

        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()

            home_page.terminal_input('memory')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(1.0)

            # 使用图片比对验证输出结果（比OCR更可靠）
            home_page.image_operate(tc.data_dir + "\\memory_result.png")
            
            print(f"\n[测试通过] memory命令输出与预期图片匹配")
