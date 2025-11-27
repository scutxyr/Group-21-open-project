"""
测试help命令 - 显示所有可用命令的帮助信息
"""
import time
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestHelp(object):
    """Help命令测试类"""

    def test_help_command(self):
        """
        测试help命令 - 显示所有可用的Arthas命令
        验证点：
        - 命令执行成功
        - 输出包含命令列表
        - 输出包含新增的JVM诊断命令描述
        """
        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()
            home_page.terminal_input('help')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(1.0)
            home_page.image_operate(tc.data_dir + "\\help_command_result.png")
            
            print(f"\n[测试通过] help命令输出与预期图片匹配")
