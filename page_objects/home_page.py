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
    loc_variable_button = "//a[@data-value='variables']"


class _HomePageElements(object):
    def __init__(self):
        self.page = DriverWrapper()

    @property
    def title(self) -> WebElement:
        return self.page.find_element(_Locator.loc_root)

    @property
    def variables_button(self):
        return self.page.find_element(_Locator.loc_variable_button)

class HomePage(object):
    def __init__(self):
        self.page = DriverWrapper()
        # self.page.wait.wait_page_loading()
        self.elements = _HomePageElements()

    def click_variable_button(self):
        self.page.click(self.elements.variables_button)