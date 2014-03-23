'''
Created on Mar 20, 2014

@author: justinoliver
'''

import requests
from xml.etree import ElementTree
import urllib

class TDAmeritradeAPI:
    def __init__(self):
        self.debugFlag      = True
        self.userID         = ""
        self.password       = ""
        self.sourceID       = ""
        self.versionID      = "1.0"
        self.cookies        = { }
        
        # CONSTANTS
        self.bassURL        = "https://apis.tdameritrade.com/apps/"
   
# More information on requests
# http://docs.python-requests.org/en/latest/api/
 
    def initiateGetRequest(self, url, data):
        response = requests.get(url, params=data)
        root = ElementTree.fromstring(response.content)
        
        # Get the cookies
        if r.cookies["JSESSIONID"] != None:
            self.cookies["JSESSIONID"] = r.cookies["JSESSIONID"]
        
        return root
    
    def initiatePostRequest(self, url, data):
        headers = {"content-type": "application/x-www-form-urlencoded"}
        response = requests.post(url, data=data, headers=headers, cookies=self.cookies)
        root = ElementTree.fromstring(response.content)

        #if self.debugFlag == True:
        #    history = response.history
        #    request = history[0]
        #    print request.url
        
        for child in root:
            print child.tag, child.attrib
        
        return root

    def loginRequest(self):
        url = self.bassURL + "300/LogIn?"
        paramDic  = {
                     "sourceID":    self.sourceID, 
                     "version":     self.versionID
                    }
        postData  = {
                     "userid":      self.userID, 
                     "password":    self.password, 
                     "sourceID":    self.sourceID, 
                     "version":     self.versionID
                    }
        url += urllib.urlencode(paramDic)

        # Start the request
        tree = self.initiatePostRequest(url, postData)

        # Verify results
        return True

# Initialize TDAmeritrade API
api = TDAmeritradeAPI()
api.loginRequest()

# Content-Type: application/x-www-form-urlencoded