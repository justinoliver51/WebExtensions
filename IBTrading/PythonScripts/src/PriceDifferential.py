'''
Created on March 31, 2014

@author: justinoliver
'''
from HTMLParser import HTMLParser
from BeautifulSoup import BeautifulSoup

class IbReportParser(HTMLParser):
    def __init__(self):
        HTMLParser.HTMLParser.__init__(self)
        self.recording = 0
        self.data = []

    def handle_starttag(self, tag, attributes):
        if tag != 'div':
            return
        if self.recording:
            self.recording += 1
            return
        for name, value in attributes:
            if name == 'id' and value == 'tblTransactions_U1257707Body':
                break
        else:
            return
        self.recording = 1
        
    def handle_endtag(self, tag):
        if tag == 'div' and self.recording:
            self.recording -= 1
    
    def handle_data(self, data):
        if self.recording:
            self.data.append(data)

# <TH align="left" class="nobottomborder">Symbol</TH>
# <TH align="left" class="nobottomborder">Date/Time</TH>
# <TH align="left" class="nobottomborder">Exchange</TH>
# <TH align="right" class="nobottomborder">Quantity</TH>
# <TH align="right" class="nobottomborder">T. Price</TH>
# <TH align="right" class="nobottomborder">C. Price</TH>
# <TH align="right" class="nobottomborder">Proceeds</TH>
# <TH align="right" class="nobottomborder">Comm/Tax</TH>
# <TH align="right" class="nobottomborder">Basis</TH>
# <TH align="right" class="nobottomborder">Realized P/L</TH>
# <TH align="right" class="nobottomborder">MTM P/L</TH>
# <TH align="right" class="nobottomborder">Code</TH>



### MAIN ###
tradingData = ''
recording = False
ibStartingLine = '<DIV id="tblTransactions_U1257707Body" style="position: absolute; display: none">'
ibEndingLine = 'grid_12'

# Open the Interactive Brokers Trading report and save the trades information
with open('/Users/justinoliver/Desktop/Developer/WebExtensions/TradesData/U1257707 Activity Statement September 24, 2013 - March 28, 2014 - Interactive Brokers.html') as fp:
    for line in fp:
        if line.find(ibStartingLine) >= 0:
            recording = True
        elif line.find(ibEndingLine) >= 0:
            recording = False
        
        if recording == True:
            tradingData += line

soup = BeautifulSoup(tradingData)

#print soup('tr', {'class': 'summaryRow'})

# <TBODY>
# <TR class="summaryRow">
# <TD class="noleftborder"><span>+</span>ANR</TD>
# <TD>2013-12-02, 15:15:09</TD>
# <TD>-</TD>
# <TD align="right">3,508</TD>
# <TD align="right">6.8700</TD>
# <TD align="right">6.8000</TD>
# <TD align="right">-24,099.96</TD>
# <TD align="right">-17.24</TD>
# <TD align="right">24,117.20</TD>
# <TD align="right">0.00</TD>
# <TD align="right">-245.56</TD>
# <TD align="right">P;O</TD>
# </TR>
# </TBODY>

for tradeData in soup('tr', {'class': 'summaryRow'}):
    print tradeData('td', {'class': 'noleftborder'})[0]
    for row in tradeData:
        print row
    break

for row in soup('tr', {'class': 'summaryRow'}):#[0].tbody(''):
    stuff = BeautifulSoup(str(row('td', {'class': 'noleftborder'})[0]))#.tr.string
    print stuff
    print '----'
    print stuff.td.string
    print '===='
    tds = row('td')
    print tds[0].string, tds[1].string
    # will print date and sunrise




# -*- coding: utf-8 -*-
#
# Copyright (C) 2013 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Command-line skeleton application for Drive API.
Usage:
  $ python sample.py

You can also get help on all the command-line flags the program understands
by running:

  $ python sample.py --help

"""

import argparse
import httplib2
import os
import sys
import json

import csv
import MySQLdb

from apiclient import discovery
from oauth2client import file
from oauth2client import client
from oauth2client import tools

# Parser for command-line arguments.
parser = argparse.ArgumentParser(
    description=__doc__,
    formatter_class=argparse.RawDescriptionHelpFormatter,
    parents=[tools.argparser])


# CLIENT_SECRETS is name of a file containing the OAuth 2.0 information for this
# application, including client_id and client_secret. You can see the Client ID
# and Client secret on the APIs page in the Cloud Console:
# <https://cloud.google.com/console#/project/305573432144/apiui>
CLIENT_SECRETS = os.path.join(os.path.dirname(__file__), 'client_secrets.json')

# Set up a Flow object to be used for authentication.
# Add one or more of the following scopes. PLEASE ONLY ADD THE SCOPES YOU
# NEED. For more information on using scopes please see
# <https://developers.google.com/+/best-practices>.
FLOW = client.flow_from_clientsecrets(CLIENT_SECRETS,
  scope=[
      'https://www.googleapis.com/auth/drive',
      'https://www.googleapis.com/auth/drive.appdata',
      'https://www.googleapis.com/auth/drive.apps.readonly',
      'https://www.googleapis.com/auth/drive.file',
      'https://www.googleapis.com/auth/drive.metadata.readonly',
      'https://www.googleapis.com/auth/drive.readonly',
      'https://www.googleapis.com/auth/drive.scripts',
    ],
    message=tools.message_if_missing(CLIENT_SECRETS))

def retrieve_all_files(service):
  """Retrieve a list of File resources.

  Args:
    service: Drive API service instance.
  Returns:
    List of File resources.
  """
  result = []
  page_token = None
  while True:
    try:
      param = {}
      if page_token:
        param['pageToken'] = page_token
      
      files = service.files().list(**param).execute()
      result.extend(files['items'])
      
      page_token = files.get('nextPageToken')
      if not page_token:
        break
    except errors.HttpError, error:
      print 'An error occurred: %s' % error
      break

  return result

def retrieve_all_folders(service):
  """Retrieve a list of File resources.

  Args:
    service: Drive API service instance.
  Returns:
    List of File resources.
  """
  result = []
  page_token = None
  while True:
    try:
      param = {}
      if page_token:
        param['pageToken'] = page_token

      files = service.files().list(**param).execute()
      result.extend(files['items'])
      page_token = files.get('nextPageToken')
      
      if not page_token:
        break
    except errors.HttpError, error:
      print 'An error occurred: %s' % error
      break
  return result

def get_files_in_folder(service, folder_id):
  """Returns files belonging to a folder.

  Args:
    service: Drive API service instance.
    folder_id: ID of the folder to print files from.
  """
  page_token = None
  files = []
  while True:
    try:
      param = {}
      if page_token:
        param['pageToken'] = page_token
      
      children = service.children().list(
          folderId=folder_id, **param).execute()

      for child in children.get('items', []):
        files.append(child)
        
      page_token = children.get('nextPageToken')
      if not page_token:
        break
    except errors.HttpError, error:
      print 'An error occurred: %s' % error
      break

  return files

def insertIntoDatabase(database_table, csv_data):
    csv_data = csv_data.split('\n')
    
    mydb = MySQLdb.connect(host='scenecheckdb.ccwd1ptkptjf.us-east-1.rds.amazonaws.com',
                           user='justinoliver51',
                           passwd='annais2AWESOME!',
                           db='SceneCheckDB')
    cursor = mydb.cursor()

    # The first row contains the columns
    firstRow = True
    columns = ""
    for row in csv_data:
        # Grab the columns of the table, but skip inserting data
        if firstRow:
            columns = row
            firstRow = False
            continue
        
        row = "\'" + row
        row = row.replace(',', "\',\'")
        row += "\'"
        
        query = 'INSERT INTO ' + database_table + '(' + columns + ') \
              VALUES(' + row + ')'
        print query
        
        try:
            cursor.execute(query)
        except:
            self.connection.rollback()
    #close the connection to the database.
    mydb.commit()
    cursor.close()
    print "Done"
    
    return


def main(argv):
  # Parse the command-line flags.
  flags = parser.parse_args(argv[1:])

  # If the credentials don't exist or are invalid run through the native client
  # flow. The Storage object will ensure that if successful the good
  # credentials will get written back to the file.
  storage = file.Storage('sample.dat')
  credentials = storage.get()
  if credentials is None or credentials.invalid:
    credentials = tools.run_flow(FLOW, storage, flags)

  # Create an httplib2.Http object to handle our HTTP requests and authorize it
  # with our good Credentials.
  http = httplib2.Http()
  http = credentials.authorize(http)

  # Construct the service object for the interacting with the Drive API.
  service = discovery.build('drive', 'v2', http=http)

  try:
    print "Success! Now add code here."    

    files = retrieve_all_files(service)

    for theFile in files:
        if( (theFile['mimeType'] == 'application/vnd.google-apps.folder') and
           (theFile['title'] == 'DallasTX') ):
            
            # Gets the files
            newVenueFiles = get_files_in_folder(service, theFile['id'])
            break

    for spreadsheet in newVenueFiles:
        theFile = service.files().get(fileId=spreadsheet['id']).execute()
        
        #print json.dumps(theFile, indent=2)
        #print theFile['title']
        
        # If this isn't a spreadsheet
        if(theFile['mimeType'] != 'application/vnd.google-apps.spreadsheet'):
            continue
        
        # If the files name doesn't match
        if( (theFile['title'] != 'Venues') and (theFile['title'] != 'VenueOwners') 
            and (theFile['title'] != 'VenueScenes') and (theFile['title'] != 'VenueTags')):
            continue
        
        # TEMP FIXME!
        if(theFile['title'] != 'Venues'):
            continue
        
        url = theFile['exportLinks']['application/pdf']
        url = url[:-4] + "=csv" + "&gid=0"
        #print url
        response, content = http.request(url)
        #print str(content)

        # Verify content
        
        
        # Insert into databse
        insertIntoDatabase('Temp', str(content))

  except client.AccessTokenRefreshError:
    print ("The credentials have been revoked or expired, please re-run"
      "the application to re-authorize")



# Search my latest downloaded statement.  Get the following data for each trade:
# - Date/Time
# - Number of shares 
# - Price
# - Symbol


# Search through emails. Get the following data for each Jason Bonds trade:
# - Date/Time
# - Number of shares 
# - Price
# - Symbol


# Plot a graph of profits vs percentage difference
