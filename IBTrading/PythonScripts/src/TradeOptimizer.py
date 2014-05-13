import scipy as sp
import numpy as np

import MySQLdb

import json
import matplotlib.pyplot as plt

################# DATABASE #################
class Database:
    host        = '192.168.1.20'       # 'scenecheckdb.ccwd1ptkptjf.us-east-1.rds.amazonaws.com'
    user        = 'justinoliver51'  # 'justinoliver51'
    password    = 'utredhead51'     # 'annais2AWESOME!'
    db          = 'IBTradingDB'     # 'SceneCheckDB'

    def __init__(self):
        self.connection = MySQLdb.connect(self.host, self.user, self.password, self.db)
        self.cursor = self.connection.cursor()

    def insert(self, query, values):
        try:
            self.cursor.execute(query, values)
            self.connection.commit()
        except Exception as inst:
            print type(inst)     # the exception instance
            print inst           # __str__ allows args to printed directly
            self.connection.rollback()

        return self.cursor.lastrowid
    
    def query_with_values(self, query, values):
        cursor = self.connection.cursor( MySQLdb.cursors.DictCursor )
        cursor.execute(query, values)

        return cursor.fetchall()
    
    def query(self, query):
        cursor = self.connection.cursor( MySQLdb.cursors.DictCursor )
        cursor.execute(query)

        return cursor.fetchall()

    def getLastRowID(self):
        return self.connection.insert_id()

    def __del__(self):
        self.connection.close()
        
################# FUNCTIONS #################
def getStatsOnMoneyMoved(db):
    stats               = {}
    money_moved_list    = []
    time_interval_list  = []
    initial_index       = 10
    max_index_offset    = 5
    
    # Collect the trade alert data
    query = '''
            SELECT TradeID, PredictorName, Time, Symbol, Quantity, Price 
            FROM TradeAlerts 
            ORDER BY TradeID ASC
            '''
    trade_alerts = db.query(query)
    
    
    
    # Collect the historical data from the trades
    query = '''
                SELECT TradeID, Date, Open, High, Low, Close, BarCount, Volume, WAP 
                FROM HistoricalData 
                ORDER BY TradeID,Date ASC
            '''
    historical_data = db.query(query)
    
    # Put the information in an easy-to-read hash map
    historical_data_map = {}
    current_trade_id = '0'
    for data_entry in historical_data:
        if current_trade_id != str(data_entry['TradeID']):
            current_trade_id = str(data_entry['TradeID'])
            historical_data_map[current_trade_id] = []
        historical_data_map[current_trade_id].append(data_entry)
        
    # For each trade, find the number of minutes the price continued
    # to rise after the 20 minute marker.
    # NOTE: The 20 minute marker is the minute in which we received
    #       the trade alert
    for trade_alert in trade_alerts:
        trade_id                    = str(trade_alert['TradeID'])
        trade_alert_historical_data = historical_data_map[trade_id]
        original_price              = trade_alert['Price']
        close_price_list            = []
        index                       = 0
        price                       = original_price
        total_cash                  = 0
        average_cash                = 0
        
        # Calculate the average money moved without the predictor
        for index in range(0, initial_index):
            close_price_list.append(trade_alert_historical_data[index]['WAP'] * trade_alert_historical_data[index]['Volume'] * 100)
        average_cash = sp.mean(close_price_list, dtype=np.float64)
        
        # As long as the price continues to increase, count the interval
        index                       = initial_index
        while trade_alert_historical_data[index]['Close'] > price:
            total_cash += trade_alert_historical_data[index]['WAP'] * trade_alert_historical_data[index]['Volume'] * 100
            price = trade_alert_historical_data[index]['Close']
            index += 1
            
            # If we have reached our cutoff time, get out
            if index - initial_index >= max_index_offset:
                break
        
        time_interval = index - initial_index
        time_interval_list.append(time_interval)
        
        if (time_interval > 0) and (float(total_cash) - time_interval * average_cash > 0):
            money_moved_list.append(float(total_cash) - time_interval * average_cash)

    # Do a statistical analysis on the new list
    time_interval_array = np.array(time_interval_list, dtype=np.float64)
    money_moved_array   = np.array(money_moved_list, dtype=np.float64)
    stats['MoneyMoved'] = {
                           'mean'       : sp.mean(money_moved_array, dtype=np.float64),
                           'std_dev'    : sp.std(money_moved_array, dtype=np.float64),
                           'median'     : sp.median(money_moved_array),
                           'data'       : money_moved_list
                           }
    
    stats['TimeInterval'] = {
                           'mean'       : sp.mean(time_interval_array, dtype=np.float64),
                           'std_dev'    : sp.std(time_interval_array, dtype=np.float64),
                           'median'     : sp.median(time_interval_array),
                           'data'       : time_interval_list
                           }

    # Build list of profts vs percentage price difference
    #build_plot(time_interval_array, money_moved_array)

    return stats

def buildPlot(xAxis, yAxis):
    plt.plot(xAxis, yAxis, 'ro')
    plt.axis([0, 5, 0, 10000000])
    #plt.axis([-1, 1, -1200, 1200])
    plt.show()

################# MAIN FUNCTION #################
def main():
    db = Database()
    stats = getStatsOnMoneyMoved(db)
    
    print json.dumps(stats, indent=2)
    
if __name__ == '__main__':
  main()