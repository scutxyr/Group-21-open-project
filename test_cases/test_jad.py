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
            
            # 使用图片比对验证输出结果（比OCR更可靠）
            home_page.image_operate(tc.data_dir + "\\jad_result.png")
            
            print(f"\n[测试通过] jad demo.MathGame命令输出与预期图片匹配")
