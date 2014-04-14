import os
import sys
import time
import thread
#from sikuli.Sikuli import *

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
  count = 0
  prev_data = ''
  clipboard_file = "C:\Users\B40904\Documents\Personal\clipboard.txt"
  print "os.path.isfile(clipboard_file) = ", os.path.isfile(clipboard_file)

  while (True):
    time.sleep(1)

    try:
      data = getClipboardData()
      print 'data: ' % data
      if data and data != prev_data:
        open(clipboard_file, 'w').write(data)
        print 'writing %s to file' % data
        prev_data = data

    except Exception, e:
      print e
      pass
  
def monitorChatroom():
    
    #while 
    click(None)
    
    return
      
def main():
  usage = \
"""Usage: python SharedClipboard.py <shared clipboard filename>

The filename should refer to a writeable exisiting file. The file
should be on a shared location visible and (writeable) to all the
shared clipboard instances on all machines.
"""
  # Spawn a thread that listens to the clipboard for changes
  #clipboard_file = sys.argv[1]
  #thread.start_new_thread( monitorClipboard, (clipboard_file) )

  # Begin the infinite loop of looking for new data
  monitorChatroom()
if __name__=='__main__':
  main()
  
