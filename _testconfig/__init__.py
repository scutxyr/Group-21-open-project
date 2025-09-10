import getopt
import os
import sys
from configparser import ConfigParser


# section name is case sensitive, it must be strictly same with that in testconfig.ini
__env_under_test_section = 'EnvUnderTest'
__selenium_config_section = 'SeleniumConfig'
__const_section = 'Const'
__parser = ConfigParser()
configfile = os.path.join(os.path.abspath(os.path.dirname(__file__)), 'testconfig.ini')
__parser.read(configfile, encoding='utf-8')
__env_section = env_name = __parser.get(__env_under_test_section, 'env_name')

# environment
domain = __parser.get(__env_section, 'domain')
url = __parser.get(__env_section, 'url')
# sql_server = __parser.get(__env_section, 'sql_server')
# sql_database = __parser.get(__env_section, 'sql_database')
# sql_username = __parser.get(__env_section, 'sql_username')
# sql_password = __parser.get(__env_section, 'sql_password')

# const
Tool = __parser.get(__const_section, 'tool')
locale = __parser.get(__const_section, 'locale')
time_zone = __parser.get(__const_section, 'time_zone')

# selenium config
download_dir = __parser.get(__selenium_config_section, 'download_dir')
if not os.path.isabs(download_dir):
    download_dir = os.path.normpath(os.path.join(os.path.abspath(os.path.dirname(__file__)), download_dir))

data_dir = __parser.get(__selenium_config_section, 'data_dir')
if not os.path.isabs(data_dir):
    data_dir = os.path.normpath(os.path.join(os.path.abspath(os.path.dirname(__file__)), data_dir))

screenshot_dir = __parser.get(__selenium_config_section, 'screenshot_dir')
if not os.path.isabs(screenshot_dir):
    screenshot_dir = os.path.normpath(os.path.join(os.path.abspath(os.path.dirname(__file__)), screenshot_dir))

failures_file = __parser.get(__selenium_config_section, 'failures_file')
if not os.path.isabs(failures_file):
    failures_file = os.path.normpath(os.path.join(os.path.abspath(os.path.dirname(__file__)), failures_file))

debug_address = __parser.get(__selenium_config_section, 'debug_address')
fallback_strategy = __parser.get(__selenium_config_section, 'fallback_strategy')


def set_env_under_test(env):
    __parser.set(__env_under_test_section, 'env_name', env)
    __parser.write(open(file=configfile, mode='r+'))


def update_data_dir_with_env(env):
    __parser.set(__selenium_config_section, 'data_dir', '..\\data\\' + env)
    __parser.write(open(file=configfile, mode='r+'))
