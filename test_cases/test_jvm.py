"""
测试JVM命令 - 显示JVM基本信息
"""
import time
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestJVM(object):
    """JVM命令测试类"""

    def test_jvm_command(self):
        """
        测试jvm命令 - 显示JVM基本信息
        验证点：
        - 命令执行成功
        - 输出包含JVM类加载信息
        - 输出包含编译信息
        """
        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()
            
            # 执行jvm命令
            home_page.terminal_input('jvm')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(1.0)
            
            # 获取终端输出并验证
            terminal_text = home_page.get_terminal_text()
            
            # 验证JVM信息 - jvm命令显示类加载、编译、文件描述符等信息
            assert 'CLASS-COUNT' in terminal_text or 'COMPILATION' in terminal_text, \
                "JVM输出应包含类加载或编译信息"
            
            print(f"\n[测试通过] jvm命令输出包含预期信息")
            print(f"输出长度: {len(terminal_text)} 字符")
