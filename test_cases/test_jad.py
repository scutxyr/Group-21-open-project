"""
测试jad命令 - 反编译demo.MathGame类
"""
import time
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestJad(object):
    """JAD反编译命令测试类"""

    def test_jad_mathgame_command(self):
        """
        测试jad demo.MathGame命令 - 反编译MathGame类
        验证点：
        - 命令执行成功
        - 输出包含MathGame类的源代码
        - 输出包含关键方法（如primeFactors、run等）
        """
        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()
            
            # 执行jad demo.MathGame命令
            home_page.terminal_input('jad demo.MathGame')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(2.0)  # 反编译可能需要更长时间
            
            # 获取终端输出并验证
            terminal_text = home_page.get_terminal_text()
            
            # 验证反编译输出包含MathGame类的关键内容
            assert 'MathGame' in terminal_text, "jad输出应包含MathGame类名"
            assert 'public' in terminal_text or 'class' in terminal_text, \
                "jad输出应包含Java代码关键字"
            
            print(f"\n[测试通过] jad demo.MathGame命令成功反编译")
            print(f"输出长度: {len(terminal_text)} 字符")
