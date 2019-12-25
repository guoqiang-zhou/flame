#encoding=utf-8
import os
import glob
import configparser
import shutil


def mkdir(path):
    '''
    创建文件夹的函数
    '''
    folder = os.path.exists(path)

    if not folder:  # 判断是否存在文件夹如果不存在则创建为文件夹
        os.makedirs(path)  # makedirs 创建文件时如果路径不存在会创建这个路径
        print("---  new folder...  ---")
        print("---  OK  ---")
    else:
        print("---  There is this folder!  ---")


class ConfigParser():
    '''
    读取配置文件中给的参数
    '''
    def get_value(self, opt, keys_n):
        config = configparser.ConfigParser()
        config.read('config.ini', encoding='utf8')
        value = config.get(opt,keys_n)
        return value

def files(curr_dir = '.', ext = '*.txt'):
    """当前目录下的文件"""
    for i in glob.glob(os.path.join(curr_dir, ext)):
        yield i


def copy_files(rootdir, ext, tar, show = True):
    """复制rootdir目录下的符合的文件"""
    for i in files(rootdir, ext):
        if show:
            print(i)
        #os.remove(i)
        shutil.copy(i, tar)
        print("文件已经复制！")

if __name__ == '__main__':
    com = ConfigParser()
    buildCommand = com.get_value('config', 'buildCommand')
    tfs = com.get_value('config', 'ahives_release')
    publishDir = com.get_value('public', 'zgq_publishDir')
    archivesDir = com.get_value('public', 'archivesDir')
    print('buildCommand is: ', buildCommand, '\ttfs is: ',
          tfs, '\tpublishDir is: ', publishDir,
          '\tarchivesDir is: ', archivesDir)
    os.system("chmod +x gradlew")
    os.system(buildCommand)
    mkdir(archivesDir)
    os.system("git log --name-status -5 --graph --decorate >> ${archivesDir}/${JOB_NAME}.txt")
    copy_files(tfs, '*.apk', archivesDir, show=True)
    print("复制文件完成！")




