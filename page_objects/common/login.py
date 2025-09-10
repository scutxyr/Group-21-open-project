import logging
from logging import exception

from infrastructure import utils
from infrastructure.driver import DriverWrapper
from selenium.common.exceptions import NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebElement


class _Locator(object):
    loc_root = "//div[@id='root']"


class _LoginPageWebElements(object):
    def __init__(self, page):
        self.page = page


class LoginPage(object):
    def __init__(self, url: str, max_retries=3):
        self.page = DriverWrapper()
        retry_count = 0
        while retry_count <= max_retries:
            try:
                self.page.get(url)
                break
            except exception as e:
                retry_count += 1
        # self.page.wait.wait_page_loading()
        self.elements = _LoginPageWebElements(self.page)
