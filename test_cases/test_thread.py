"""
测试thread命令 - 显示线程信息
"""
import time
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestThread(object):
    """Thread命令测试类"""

    def test_thread_command(self):
        """
        测试thread命令 - 显示所有线程信息
        验证点：
        - 命令执行成功
        - 输出包含线程列表
        - 输出包含线程状态信息
        """
        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()
            
            # 执行thread命令
            home_page.terminal_input('thread')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(1.5)
            home_page.image_operate(tc.data_dir + "\\thread_result.png")
            
            print(f"\n[测试通过] thread命令输出与预期图片匹配")
