import logging
import os
import re
import time

from selenium.common.exceptions import NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webelement import WebElement


from infrastructure.driver import DriverWrapper
from infrastructure.system import OpenFileDialog, System
from infrastructure.utils import ReUtils, WebDriverUtils


class _Locator(object):
    loc_root = "//div[@id='root']"
    loc_controller = "//textarea[@class='xterm-helper-textarea']"
    loc_terminal = "//div[@id='terminal']"


class _HomePageElements(object):
    def __init__(self):
        self.page = DriverWrapper()

    @property
    def title(self) -> WebElement:
        return self.page.find_element(_Locator.loc_root)

    @property
    def controller(self) -> WebElement:
        return self.page.find_element(_Locator.loc_controller)

    @property
    def terminal(self) -> WebElement:
        return self.page.find_elements(_Locator.loc_terminal)

class HomePage(object):
    def __init__(self):
        self.page = DriverWrapper()
        # self.page.wait.wait_page_loading()
        self.elements = _HomePageElements()

    def click_controller(self):
        self.page.click(self.elements.controller)

    def get_terminal_text(self) -> str:
        return self.page.get_canvas_content(self.elements.terminal)
