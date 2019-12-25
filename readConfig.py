import configparser
import glob
import os
import shutil

"""
读取配置文件信息
"""

class ConfigParser():

    def get_value(self, opt, keys_n):
        config = configparser.ConfigParser()
        config.read('config.ini', encoding='utf8')
        value = config.get(opt,keys_n)
        return value

    config_dic = {}
    @classmethod
    def get_config(cls, sector, item):
        value = None
        try:
            value = cls.config_dic[sector][item]
        except KeyError:
            cf = configparser.ConfigParser()
            cf.read('config.ini', encoding='utf8')  #注意setting.ini配置文件的路径
            value = cf.get(sector, item)
            cls.config_dic = value
        finally:
            return value

def mkdir(path):
    '''
    创建文件夹的函数
    '''
    folder = os.path.exists(path)

    if not folder:  # 判断是否存在文件夹如果不存在则创建为文件夹
        os.makedirs(path)  # makedirs 创建文件时如果路径不存在会创建这个路径
        print
        "---  new folder...  ---"
        print
        "---  OK  ---"
    else:
        print
        "---  There is this folder!  ---"

if __name__ == '__main__':
    com = ConfigParser()
    buildCommand = com.get_value('config', 'buildCommand')
    print(buildCommand)
    archives_debug = com.get_value('config', 'archives_debug')
    print(archives_debug)
    com = ConfigParser()
    buildCommand = com.get_config('config', 'buildCommand')
    ahives_release = com.get_config('config', 'ahives_release')
    publishDir = com.get_config('public', 'zgq_publishDir')
    archivesDir = com.get_config('public', 'archivesDir')
    print('buildCommand is ', buildCommand, 'ahives_release is ', ahives_release, 'publishDir is ', publishDir)
# def files(curr_dir = '.', ext = '*.txt'):
#     """当前目录下的文件"""
#     for i in glob.glob(os.path.join(curr_dir, ext)):
#         yield i
#
# def all_files(rootdir, ext):
#     """当前目录下以及子目录的文件"""
#     for name in os.listdir(rootdir):
#         if os.path.isdir(os.path.join(rootdir, name)):
#             try:
#                 for i in all_files(os.path.join(rootdir, name), ext):
#                     yield i
#             except:
#                 pass
#     for i in files(rootdir, ext):
#         yield i
#
# def remove_files(rootdir, ext, tar, show = False):
#     """删除rootdir目录下的符合的文件"""
#     for i in files(rootdir, ext):
#         if show:
#             print(i)
#         #os.remove(i)
#         shutil.copy(i, tar)
#
# def remove_all_files(rootdir, ext, show = False):
#     """删除rootdir目录下以及子目录下符合的文件"""
#     for i in all_files(rootdir, ext):
#         if show:
#             print(i)
#         #os.remove(i)
#         #shutil.copy(i, '123.txt')
#
# if __name__ == '__main__':
#     rootdir = 'd1\d2\d3'
#     tar = "targetFiles"
#     mkdir(tar)
#     #remove_all_files('.', '*.o', show = True)
#     # remove_all_files('.', '*.exe', show = True)
#     remove_files(rootdir, '*.apk', tar, show = True)
#     # for i in files('.','*.c'):
#         # print i
