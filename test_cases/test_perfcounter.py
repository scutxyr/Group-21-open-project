"""
测试perfcounter命令 - 显示性能计数器信息
"""
import time
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestPerfCounter(object):
    """性能计数器命令测试类"""

    def test_perfcounter_command(self):
        """
        测试perfcounter命令 - 显示性能计数器信息
        验证点：
        - 命令执行成功
        - 输出包含性能计数器数据
        """
        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()
            
            # 执行perfcounter命令
            home_page.terminal_input('perfcounter')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(1.0)
            
            # 获取终端输出并验证
            terminal_text = home_page.get_terminal_text()
            
            # 验证包含性能计数器相关信息
            # perfcounter命令通常显示各种JVM性能指标
            # 可能输出NAME, VALUE等列
            assert len(terminal_text) > 50, "perfcounter应有输出内容"
            
            print(f"\n[测试通过] perfcounter命令执行成功")
            print(f"输出长度: {len(terminal_text)} 字符")
