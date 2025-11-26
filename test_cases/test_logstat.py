
import time
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestLogStat(object):

    def test_logstat_command(self):

        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()
            
            home_page.terminal_input('logstat')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(1.0)

            terminal_text = home_page.get_terminal_text()
            
            assert len(terminal_text) > 0, "logstat应有输出"
            
            print(f"\n[测试通过] logstat命令执行成功")
            print(f"输出长度: {len(terminal_text)} 字符")
