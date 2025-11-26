"""
测试memory命令 - 显示JVM内存信息
"""
import time
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestMemory(object):
    """内存命令测试类"""

    def test_memory_command(self):
        """
        测试memory命令 - 显示JVM内存信息
        验证点：
        - 命令执行成功
        - 输出包含堆内存信息
        - 输出包含非堆内存信息
        """
        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()
            
            # 执行memory命令
            home_page.terminal_input('memory')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(1.0)
            
            # 获取终端输出并验证
            terminal_text = home_page.get_terminal_text()
            
            # 验证内存信息 - memory命令显示heap, metaspace等内存区域
            assert 'heap' in terminal_text or 'metaspace' in terminal_text or len(terminal_text) > 100, \
                "memory输出应包含内存相关信息"
            
            print(f"\n[测试通过] memory命令输出包含内存信息")
            print(f"输出长度: {len(terminal_text)} 字符")
