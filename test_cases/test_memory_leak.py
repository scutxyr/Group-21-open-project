"""
测试memory-leak命令 - 检测潜在内存泄漏问题
"""
import time
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestMemoryLeak(object):
    """Memory-Leak命令测试类"""

    def test_memory_leak_command(self):
        """
        测试memory-leak命令 - 内存泄漏检测
        验证点：
        - 命令执行成功
        - 输出包含内存分析结果
        - 输出包含可疑对象信息
        """
        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()
            
            # 执行memory-leak命令
            home_page.terminal_input('memory-leak')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(2.0)  # 内存分析可能需要更长时间
            
            # 使用图片比对验证输出结果（比OCR更可靠）
            home_page.image_operate(tc.data_dir + "\\memory_leak_result.png")
            
            print(f"\n[测试通过] memory-leak命令输出与预期图片匹配")
