#encoding=utf-8
buildCommand = './gradlew clean build lint'
mappingFile = 'app/build/outputs/apk/release/mapping.txt'
archives_debug = 'app/build/outputs/apk/debug/*.apk'
ahives_release = 'app/build/outputs/apk/release/*.apk'
publishDir = "/var/lib/jenkins/Publish/tempApks"
emailList = "shengyan.liang@ubtrobot.com,guoqiang.zhou@ubtrobot.com"
testt = 'ls -a'