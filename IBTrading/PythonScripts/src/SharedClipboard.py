'''
Created on Sep 18, 2013

@author: justinoliver
'''
import os
import sys
import time

import subprocess 

def getClipboardData(): 
    p = subprocess.Popen(['pbpaste'], stdout=subprocess.PIPE) 
    retcode = p.wait() 
    data = p.stdout.read() 
    
    return data 
    
def setClipboardData(data): 
    p = subprocess.Popen(['pbcopy'], stdin=subprocess.PIPE) 
    p.stdin.write(data) 
    p.stdin.close() 
    retcode = p.wait()

def monitorClipboard(clipboard_file):
    prev_data = ''

    while (True):
        time.sleep(1)

        try:
            data = getClipboardData()
            print 'data: ', data
            if data and data != prev_data:
                open(clipboard_file, 'w').write(data)
                print 'writing %s to file' % data
                prev_data = data

        except Exception, e:
            print e
            pass

clipboard_file = "/Users/justinoliver/Desktop/Developer/WebExtensions/JasonBonds/clipboard.txt"
            
def main():
    usage = \
"""Usage: python SharedClipboard.py <shared clipboard filename>

The filename should refer to a writeable exisiting file. The file
should be on a shared location visible and (writeable) to all the
shared clipboard instances on all machines.
"""

    #clipboard_file = sys.argv[1]
    print "os.path.isfile(clipboard_file) = ", os.path.isfile(clipboard_file)
    monitorClipboard(clipboard_file)

if __name__=='__main__':
    main()
    