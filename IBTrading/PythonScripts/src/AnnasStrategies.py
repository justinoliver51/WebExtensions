import scipy as sp
import numpy as np

import MySQLdb

import json
import simplejson
import matplotlib.pyplot as plt

import sys, datetime, argparse

from databases import TradingDataDB

################# COMMAND LINE ARGUMENTS #################
my_parser = argparse.ArgumentParser( )
my_parser.add_argument('-verbose', action='store_true',
                   help='if used, print more debug information')
my_parser.add_argument('--file', dest='file_exists',
                   help='full file path')
        
################# FUNCTIONS #################
def get_data(db): 
    # Collect the trade alert data
    query = '''
            SELECT TradeID, PredictorName, Time, Symbol, Quantity, Price 
            FROM TradeAlerts 
            ORDER BY TradeID ASC
            '''
    trade_alerts = db.query(query, None)

    # Collect the historical data from the trades
    query = '''
            SELECT TradeID, Date, Open, High, Low, Close, BarCount, Volume, WAP 
            FROM HistoricalData 
            ORDER BY TradeID,Date ASC
            '''
    historical_data = db.query(query, None)
    
    return (trade_alerts, historical_data)

def format_historical_data(trade_alerts, historical_data):
    
    # Put the information in an easy-to-read hash map
    historical_data_map = {}
    current_trade_id = '0'
    historical_data_map[current_trade_id] = []
    for data_entry in historical_data:
        if current_trade_id != str(data_entry['TradeID']):
            current_trade_id = str(data_entry['TradeID'])
            historical_data_map[current_trade_id] = []
        historical_data_map[current_trade_id].append(data_entry)
        
    for trade_alert in trade_alerts:
        trade_alert['Time'] = trade_alert['Time'].strftime("%Y-%m-%d %H:%M:%S")
        
    return historical_data_map

def print_trade_alerts(trade_alerts):
    print(simplejson.dumps(trade_alerts, sort_keys=True, indent=4 * ' '))
    
def print_historical_data(historical_data):
    print(simplejson.dumps(historical_data, sort_keys=True, indent=4 * ' '))

################# MAIN FUNCTION #################
def main(argv):
    flags = my_parser.parse_args(argv[1:])
    args = vars(flags)
    
    if args['file_exists']:
        file_name  = args['file_exists']
    else:
        file_name = None
    
    db = TradingDataDB()
    (trade_alerts, historical_data) = get_data(db)
    historical_data_map = format_historical_data(trade_alerts, historical_data)
    
    if args['verbose']:
        print_trade_alerts(trade_alerts)
        #print_historical_data(historical_data_map)
    
# Entry point
if __name__ == '__main__':
    main(sys.argv)


     