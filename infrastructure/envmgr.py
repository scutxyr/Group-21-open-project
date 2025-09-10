import os
from configparser import ConfigParser
import _testconfig as tc
from importlib import reload


class EnvMgr(object):
    @classmethod
    def set_env_and_reload(cls, env):
        tc.set_env_under_test(env)
        tc.update_data_dir_with_env(env)
        reload(tc)

