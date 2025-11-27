"""
测试classloader命令 - 显示类加载器信息
"""
import time
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestClassLoader(object):
    """ClassLoader命令测试类"""

    def test_classloader_command(self):
        """
        测试classloader命令 - 类加载器信息
        验证点：
        - 命令执行成功
        - 输出包含类加载器列表
        - 输出包含已加载类的统计信息
        """
        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()
            home_page.terminal_input('classloader')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(1.5)
            home_page.image_operate(tc.data_dir + "\\classloader_result.png")
            
            print(f"\n[测试通过] classloader命令输出与预期图片匹配")
