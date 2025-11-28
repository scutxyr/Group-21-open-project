import time
import re
import allure
from selenium.webdriver.common.keys import Keys
from test_cases.conftest import *


class TestJad(object):
    def test_jad_mathgame_command(self):
        with goto_tool(tc.Tool) as home_page:
            time.sleep(0.5)
            home_page.click_controller()
            
            # 第一次执行
            with allure.step("第一次执行 jad demo.MathGame"):
                start_time_1 = time.time()
                home_page.terminal_input('jad demo.MathGame')
                home_page.terminal_input(Keys.ENTER)
                time.sleep(2.5)
                end_time_1 = time.time()
                time1_seconds = end_time_1 - start_time_1
                
                allure.attach(f"第一次执行耗时: {time1_seconds:.3f} 秒", 
                            name="第一次执行时间", 
                            attachment_type=allure.attachment_type.TEXT)
            
            time.sleep(1.0)
            
            # 第二次执行
            with allure.step("第二次执行 jad demo.MathGame"):
                start_time_2 = time.time()
                home_page.terminal_input('jad demo.MathGame')
                home_page.terminal_input(Keys.ENTER)
                time.sleep(2.5)
                end_time_2 = time.time()
                time2_seconds = end_time_2 - start_time_2
                
                allure.attach(f"第二次执行耗时: {time2_seconds:.3f} 秒", 
                            name="第二次执行时间", 
                            attachment_type=allure.attachment_type.TEXT)
            
            # 验证第二次执行时间短于第一次（缓存优化）
            with allure.step("验证缓存性能优化"):
                improvement = ((time1_seconds - time2_seconds) / time1_seconds) * 100
                allure.attach(
                    f"第一次: {time1_seconds:.3f} 秒\n第二次: {time2_seconds:.3f} 秒\n性能提升: {improvement:.2f}%", 
                    name="性能对比结果", 
                    attachment_type=allure.attachment_type.TEXT
                )
                
                assert time1_seconds > time2_seconds, \
                    f"缓存优化失败：第二次执行({time2_seconds:.3f}s)应快于第一次({time1_seconds:.3f}s)"
