import subprocess 

def runCommand(): 
    env = {'PYTHONIOENCODING': 'MacRoman', 'VERSIONER_PYTHON_PREFER_32_BIT': 'no', 'LOGNAME': 'justinoliver', 'USER': 'justinoliver', 'PATH': '/usr/bin:/bin:/usr/sbin:/sbin', 'HOME': '/Users/justinoliver', 'com.apple.java.jvmMode': 'client', 'SHELL': '/bin/bash', 'VERSIONER_PYTHON_VERSION': '2.7', 'PYDEV_CONSOLE_ENCODING': 'MacRoman', 'com.apple.java.jvmTask': 'JNI', 'PYTHONPATH': '/Users/justinoliver/Desktop/Developer/DevTools/eclipse_javaEE/eclipse/plugins/org.python.pydev_2.8.2.2013090511/pysrc/pydev_sitecustomize:/Users/justinoliver/Desktop/Developer/WebExtensions/IBTrading/EmailMonitor/src:/System/Library/Frameworks/Python.framework/Versions/2.7/lib/python27.zip:/System/Library/Frameworks/Python.framework/Versions/2.7/lib/python2.7:/System/Library/Frameworks/Python.framework/Versions/2.7/lib/python2.7/plat-darwin:/System/Library/Frameworks/Python.framework/Versions/2.7/lib/python2.7/plat-mac:/System/Library/Frameworks/Python.framework/Versions/2.7/lib/python2.7/plat-mac/lib-scriptpackages:/System/Library/Frameworks/Python.framework/Versions/2.7/Extras/lib/python:/System/Library/Frameworks/Python.framework/Versions/2.7/lib/python2.7/lib-tk:/System/Library/Frameworks/Python.framework/Versions/2.7/lib/python2.7/lib-old:/System/Library/Frameworks/Python.framework/Versions/2.7/lib/python2.7/lib-dynload:/System/Library/Frameworks/Python.framework/Versions/2.7/Extras/lib/python/PyObjC:/Library/Python/2.7/site-packages', 'SSH_AUTH_SOCK': '/tmp/launch-PIREfB/Listeners', 'Apple_PubSub_Socket_Render': '/tmp/launch-Jtm6Rj/Render', 'APP_ICON_537': '../Resources/Eclipse.icns', 'JAVA_STARTED_ON_FIRST_THREAD_537': '1', 'TMPDIR': '/var/folders/j_/yqxyp3qd1q30hz7dhkhnw4xh0000gn/T/', 'PYDEV_COMPLETER_PYTHONPATH': '/Users/justinoliver/Desktop/Developer/DevTools/eclipse_javaEE/eclipse/plugins/org.python.pydev_2.8.2.2013090511/pysrc', '__CF_USER_TEXT_ENCODING': '0x1F5:0:0', 'Apple_Ubiquity_Message': '/tmp/launch-wdiRk7/Apple_Ubiquity_Message', 'DJANGO_SETTINGS_MODULE': 'EmailMonitor.settings', 'COMMAND_MODE': 'unix2003', 'DISPLAY':':0.0'}
    command = '/Users/justinoliver/Desktop/Developer/DevTools/Sikuli/runScript -r /Users/justinoliver/Desktop/Developer/WebExtensions/setupTWS/setupTWS.skl -f /Users/justinoliver/Desktop/Developer/WebExtensions/setupTWS/dailyLog.log -d 3 -c'
    p = subprocess.Popen(command, shell=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            env=env)
    return p.communicate()


### MAIN ###
data = runCommand()
print data