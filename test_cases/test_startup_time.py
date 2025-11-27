"""
测试startup-time命令 - 显示JVM启动时间和应用运行时长统计
"""
import time
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestStartupTime(object):
    """Startup-Time命令测试类"""

    def test_startup_time_command(self):
        """
        测试startup-time命令 - JVM启动时间统计
        验证点：
        - 命令执行成功
        - 输出包含启动时间信息
        - 输出包含运行时长统计
        """
        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()
            
            # 执行startup-time命令
            home_page.terminal_input('startup-time')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(1.0)
            home_page.image_operate(tc.data_dir + "\\startup_time_command_result.png")
            
            print(f"\n[测试通过] startup-time命令输出与预期图片匹配")
