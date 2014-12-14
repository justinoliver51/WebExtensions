'''
Created on Jul 14, 2014

@author: justinoliver
'''

import sys
import time
import re
import csv
from GoogleDriveAPI import GoogleDrive

class MyGoogleDriveAPI(GoogleDrive):
    def __init__(self, flags):
        GoogleDrive.__init__(self, flags)
        
    def createNewCsv(self, filePath, trades):
        with open(filePath, 'wb') as csvfile:
            csvwriter  = csv.writer(
                                    csvfile, delimiter=',',
                                    quotechar='"', quoting=csv.QUOTE_MINIMAL
                                    )
            # The top row contains the titles for each of the columns
            columnNames = ['Ticker', 'Date']
            csvwriter.writerow(columnNames)
            
            # For each venue, collect the information and place it in the csv file
            for trade in trades:
                row = []
                for column in columnNames:
                    if column in trade:
                        row.append(trade[column])
                    else:
                        row.append('')
                
                csvwriter.writerow(row)

class AnnouncementHistoryParser:
    announcements = [ ]
    
    def __init__(self):
        return
    
    def parseAnnoucements(self, file_name):
        f = open(file_name, 'r')
        date_string = ''
        
        for line in f:
            try:
                if len(line) == 10:
                    date_string = line[:-2]
                    date = time.strptime(date_string, "%m/%d/%y")
                    continue
                
                if (line.find('jason bon') > 0) and (line.lower().find('bought') > 0):
                    self.announcements.append(date_string + ' ' + line)
            except:
                pass
            
        f.close()
        return self.announcements
    
    def printAnnouncements(self):
        for announcement in self.announcements:
            print announcement
            
    def build_history(self):
        data_array = [ ]
        for announcement in self.announcements:
            i = -1
            words = re.split(':| ',announcement)
            data = {'Date' : words[0]}
            for word in words:
                if i >= 0:
                    i += 1
                    
                if i == 2:
                    data['Ticker'] = word
                    break
                
                if word.lower() == 'bought':
                    i += 1
            data_array.append(data)
        return data_array

def output_trade_history(parser):
    data = parser.build_history()
    google_drive_api = MyGoogleDriveAPI(None)
    
    # Create a temporary file and store the data
    csv_file_path = '/Users/justinoliver/Desktop/Developer/Java/SceneCheck/SceneCheckScripts/src/Resources/TempSpreadsheet.csv'
    google_drive_api.createNewCsv(csv_file_path, data)
        
    # Build the spreadsheet
    google_drive_api.buildSpreadsheet('Announcement History Jason Bonds', data, '0B-rBnjbP89nxN0RVSS1GekQ4WVE', csv_file_path) 

def main(argv):
    path = '/Users/justinoliver/Desktop/Developer/Trading/TradingScripts/src/Resources/'
    file1 = 'Announcement History 2013.txt'
    file2 = 'Announcement History 2014.txt'
    parser = AnnouncementHistoryParser()
    parser.parseAnnoucements(path + file1)
    parser.parseAnnoucements(path + file2)
    #parser.printAnnouncements()
    
    #output_trade_history(parser)
    
    print 'done!'
if __name__ == '__main__':
    main(sys.argv)