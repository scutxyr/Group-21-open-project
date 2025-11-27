import time
import re
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestJad(object):
    def test_jad_mathgame_command(self):
        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()
            
            # 第一次执行 jad demo.MathGame 命令
            print(f"\n[第一次执行] jad demo.MathGame")
            start_time_1 = time.time()
            home_page.terminal_input('jad demo.MathGame')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(2.5)  # 等待反编译完成
            end_time_1 = time.time()
            time1_seconds = end_time_1 - start_time_1
            
            # 获取第一次执行的输出
            terminal_text_1 = home_page.get_terminal_text()
            print(f"第一次执行耗时: {time1_seconds:.3f} 秒")
            print(f"输出长度: {len(terminal_text_1)} 字符")
            
            # 验证输出包含 MathGame
            assert 'MathGame' in terminal_text_1, "第一次输出应包含 MathGame"
            
            time.sleep(1.0)
            
            # 第二次执行 jad demo.MathGame 命令
            print(f"\n[第二次执行] jad demo.MathGame")
            start_time_2 = time.time()
            home_page.terminal_input('jad demo.MathGame')
            home_page.terminal_input(Keys.ENTER)
            time.sleep(2.5)  # 等待反编译完成
            end_time_2 = time.time()
            time2_seconds = end_time_2 - start_time_2
            
            # 获取第二次执行的输出
            terminal_text_2 = home_page.get_terminal_text()
            print(f"第二次执行耗时: {time2_seconds:.3f} 秒")
            print(f"输出长度: {len(terminal_text_2)} 字符")
            
            # 验证输出包含 MathGame
            assert 'MathGame' in terminal_text_2, "第二次输出应包含 MathGame"
            
            # 验证第二次执行时间短于第一次（缓存优化）
            print(f"\n[性能验证] 第一次: {time1_seconds:.3f} 秒, 第二次: {time2_seconds:.3f} 秒")
            assert time1_seconds > time2_seconds, \
                f"第二次执行应该更快（缓存优化），但实际: {time1_seconds:.3f}s <= {time2_seconds:.3f}s"
            
            improvement = ((time1_seconds - time2_seconds) / time1_seconds) * 100
            print(f"[测试通过] 第二次执行快了 {improvement:.2f}%，验证缓存优化成功")

