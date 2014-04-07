# http://nighttimecuriosities.blogspot.com/2012/08/historical-intraday-stock-price-data.html
### Example on how to use and plot this data
import matplotlib.pyplot as plt
import matplotlib.font_manager as font_manager
import matplotlib.dates as mdates
import matplotlib.ticker as mticker
import numpy as np

import urllib2
import urllib
import numpy as np
import datetime

import matplotlib.pyplot as plt
from matplotlib.dates import  DateFormatter, WeekdayLocator, HourLocator, \
     DayLocator, MONDAY
from matplotlib.finance import quotes_historical_yahoo, candlestick,\
     plot_day_summary, candlestick2

import sys

def sophisticatedGraphingExample():
    urldata = {}
     
    urldata['q'] = ticker = 'JPM'       # stock symbol
    urldata['x'] = 'NYSE'               # exchange symbol
    urldata['i'] = '60'                 # interval
    urldata['p'] = '1d'                 # number of past trading days (max has been 15d)
    urldata['f'] = 'd,o,h,l,c,v'        # requested data d is time, o is open, c is closing, h is high, l is low, v is volume
     
    url_values = urllib.urlencode(urldata)
    url = 'http://www.google.com/finance/getprices'
    full_url = url + '?' + url_values
    req = urllib2.Request(full_url)
    response = urllib2.urlopen(req).readlines()
    getdata = response
    del getdata[0:7]
    numberoflines = len(getdata)
    returnMat = np.zeros((numberoflines, 5))
    timeVector = []
     
    index = 0
    for line in getdata:
        line = line.strip('a')
        listFromLine = line.split(',')
        returnMat[index,:] = listFromLine[1:6]
        timeVector.append(int(listFromLine[0]))
        index += 1
     
    # convert Unix or epoch time to something more familiar
    for x in timeVector:
        if x > 500:
            z = x
            timeVector[timeVector.index(x)] = datetime.datetime.fromtimestamp(x)
        else:
            y = z+x*60 # multiply by interval
            timeVector[timeVector.index(x)] = datetime.datetime.fromtimestamp(y)
     
    print timeVector
    tdata = np.array(timeVector)
    time = tdata.reshape((len(tdata),1))
    intradata = np.concatenate((time, returnMat), axis=1) # array of all data with th
     
    def relative_strength(prices, n=14):
     
        deltas = np.diff(prices)
        seed = deltas[:n+1]
        up = seed[seed>=0].sum()/n
        down = -seed[seed<0].sum()/n
        rs = up/down
        rsi = np.zeros_like(prices)
        rsi[:n] = 100. - 100./(1.+rs)
     
        for i in range(n, len(prices)):
            delta = deltas[i-1] # cause the diff is 1 shorter
     
            if delta>0:
                upval = delta
                downval = 0.
            else:
                upval = 0.
                downval = -delta
     
            up = (up*(n-1) + upval)/n
            down = (down*(n-1) + downval)/n
     
            rs = up/down
            rsi[i] = 100. - 100./(1.+rs)
     
        return rsi
     
    def moving_average(p, n, type='simple'):
        """
        compute an n period moving average.
     
        type is 'simple' | 'exponential'
     
        """
        p = np.asarray(p)
        if type=='simple':
            weights = np.ones(n)
        else:
            weights = np.exp(np.linspace(-1., 0., n))
     
        weights /= weights.sum()
     
     
        a =  np.convolve(p, weights, mode='full')[:len(p)]
        a[:n] = a[n]
        return a
     
    def moving_average_convergence(p, nslow=26, nfast=12):
        """
        compute the MACD (Moving Average Convergence/Divergence) using a fast and slow exponential moving avg'
        return value is emaslow, emafast, macd which are len(p) arrays
        """
        emaslow = moving_average(p, nslow, type='exponential')
        emafast = moving_average(p, nfast, type='exponential')
        return emaslow, emafast, emafast - emaslow
     
    plt.rc('axes', grid=True)
    plt.rc('grid', color='0.75', linestyle='-', linewidth=0.5)
     
    textsize = 9
    left, width = 0.1, 0.8
    rect1 = [left, 0.7, width, 0.2]
    rect2 = [left, 0.3, width, 0.4]
    rect3 = [left, 0.1, width, 0.2]
     
     
    fig = plt.figure(facecolor='white')
    axescolor  = '#f6f6f6'  # the axies background color
     
    ax1 = fig.add_axes(rect1, axisbg=axescolor)  #left, bottom, width, height
    ax2 = fig.add_axes(rect2, axisbg=axescolor, sharex=ax1)
    ax2t = ax2.twinx()
    ax3  = fig.add_axes(rect3, axisbg=axescolor, sharex=ax1)
     
    ### plot the relative strength indicator
    prices = intradata[:,4]
    rsi = relative_strength(prices)
    t = intradata[:,0]
    fillcolor = 'darkgoldenrod'
     
    ax1.plot(t, rsi, color=fillcolor)
    ax1.axhline(70, color=fillcolor)
    ax1.axhline(30, color=fillcolor)
    ax1.fill_between(t, rsi, 70, where=(rsi>=70), facecolor=fillcolor, edgecolor=fillcolor)
    ax1.fill_between(t, rsi, 30, where=(rsi<=30), facecolor=fillcolor, edgecolor=fillcolor)
    ax1.text(0.6, 0.9, '>70 = overbought', va='top', transform=ax1.transAxes, fontsize=textsize)
    ax1.text(0.6, 0.1, '<30 = oversold', transform=ax1.transAxes, fontsize=textsize)
    ax1.set_ylim(0, 100)
    ax1.set_yticks([30,70])
    ax1.text(0.025, 0.95, 'RSI (14)', va='top', transform=ax1.transAxes, fontsize=textsize)
    ax1.set_title('%s daily'%ticker)
     
    ### plot the price and volume data
    low = intradata[:,3]
    high = intradata[:,2]
     
    deltas = np.zeros_like(prices)
    deltas[1:] = np.diff(prices)
    up = deltas>0
    ax2.vlines(t[up], low[up], high[up], color='black', label='_nolegend_')
    ax2.vlines(t[~up], low[~up], high[~up], color='black', label='_nolegend_')
    ma5 = moving_average(prices, 5, type='simple')
    ma50 = moving_average(prices, 50, type='simple')
     
    linema5, = ax2.plot(t, ma5, color='blue', lw=2, label='MA (5)')
    linema50, = ax2.plot(t, ma50, color='red', lw=2, label='MA (50)')
     
    props = font_manager.FontProperties(size=10)
    leg = ax2.legend(loc='center left', shadow=True, fancybox=True, prop=props)
    leg.get_frame().set_alpha(0.5)
     
    volume = (intradata[:,4]*intradata[:,5])/1e6  # dollar volume in millions
    vmax = volume.max()
    poly = ax2t.fill_between(t, volume, 0, label='Volume', facecolor=fillcolor, edgecolor=fillcolor)
    ax2t.set_ylim(0, 5*vmax)
    ax2t.set_yticks([])
     
    ### compute the MACD indicator
    fillcolor = 'darkslategrey'
    nslow = 26
    nfast = 12
    nema = 9
    emaslow, emafast, macd = moving_average_convergence(prices, nslow=nslow, nfast=nfast)
    ema9 = moving_average(macd, nema, type='exponential')
    ax3.plot(t, macd, color='black', lw=2)
    ax3.plot(t, ema9, color='blue', lw=1)
    ax3.fill_between(t, macd-ema9, 0, alpha=0.5, facecolor=fillcolor, edgecolor=fillcolor)
     
     
    ax3.text(0.025, 0.95, 'MACD (%d, %d, %d)'%(nfast, nslow, nema), va='top',
             transform=ax3.transAxes, fontsize=textsize)
     
    # turn off upper axis tick labels, rotate the lower ones, etc
    for ax in ax1, ax2, ax2t, ax3:
        if ax!=ax3:
            for label in ax.get_xticklabels():
                label.set_visible(False)
        else:
            for label in ax.get_xticklabels():
                label.set_rotation(30)
                label.set_horizontalalignment('right')
     
        ax.fmt_xdata = mdates.DateFormatter('%Y-%m-%d')
     
    class MyLocator(mticker.MaxNLocator):
        def __init__(self, *args, **kwargs):
            mticker.MaxNLocator.__init__(self, *args, **kwargs)
     
        def __call__(self, *args, **kwargs):
            return mticker.MaxNLocator.__call__(self, *args, **kwargs)
     
    # at most 5 ticks, pruning the upper and lower so they don't overlap
    # with other ticks
    ax2.yaxis.set_major_locator(MyLocator(5, prune='both'))
    ax3.yaxis.set_major_locator(MyLocator(5, prune='both'))
     
    plt.show()

def candlestickExample():
    # (Year, month, day) tuples suffice as args for quotes_historical_yahoo
    date1 = ( 2004, 2, 1)
    date2 = ( 2004, 4, 12 )
    
    
    mondays = WeekdayLocator(MONDAY)        # major ticks on the mondays
    alldays    = DayLocator()              # minor ticks on the days
    weekFormatter = DateFormatter('%b %d')  # e.g., Jan 12
    dayFormatter = DateFormatter('%d')      # e.g., 12
    
    quotes = quotes_historical_yahoo('INTC', date1, date2)
    if len(quotes) == 0:
        raise SystemExit
    
    fig, ax = plt.subplots()
    fig.subplots_adjust(bottom=0.2)
    ax.xaxis.set_major_locator(mondays)
    ax.xaxis.set_minor_locator(alldays)
    ax.xaxis.set_major_formatter(weekFormatter)
    #ax.xaxis.set_minor_formatter(dayFormatter)
    
    #plot_day_summary(ax, quotes, ticksize=3)
    candlestick(ax, quotes, width=0.6)
    
    ax.xaxis_date()
    ax.autoscale_view()
    plt.setp( plt.gca().get_xticklabels(), rotation=45, horizontalalignment='right')
    
    print quotes
    plt.show()
    
def candlestickIntradayExample():
    try:
        urlToVisit = 'http://chartapi.finance.yahoo.com/instrument/1.0/OPTT/chartdata;type=quote;range=1d/csv'
        stockFile = []
        
        try:
            sourceCode = urllib2.urlopen(urlToVisit).read()
            splitSource = sourceCode.split('\n')
            for eachLine in splitSource:
                splitLine = eachLine.split(',')
                
                fixMe = splitLine[0]
                
                if len(splitLine) == 6:
                    if 'values' not in eachLine:
                        fixed = eachLine.replace(fixMe, datetime.datetime.fromtimestamp(int(fixMe)).strftime('%Y-%m-%d %H:%M:%S'))
                        stockFile.append(fixed)
        except Exception, e:
            print str(e), 'failed to organized pulled data.'
    except Exception, e:
        print str(e), 'failed to pull pricing data'
    
    print stockFile
    try:
        date, closep, highp, lowp, openp, volume = np.loadtxt(stockFile, delimiter=',', unpack=True, converters={0: mdates.strpdate2num('%Y-%m-%d %H:%M:%S')})

        x = 0
        y = len(date)
        newAr = []
        while x < y:
            appendLine = date[x],openp[x],closep[x],highp[x],lowp[x],volume[x]
            newAr.append(appendLine)
            x += 1
        
        print newAr
        SP = len(date)
        fig = plt.figure(facecolor='#07000d')
        
        ax1 = plt.subplot2grid((6,4), (1,0), rowspan=4, colspan=4, axisbg='#07000d')
        candlestick(ax1, newAr[-SP:], width=.0005, colorup='#53c156', colordown='#ff1717')
        
        # CUSTOMIZE AXES 1
        ax1.grid(True)
        ax1.xaxis.set_major_formatter(mdates.DateFormatter('%Y-%m-%d'))
        ax1.xaxis.set_major_locator(mticker.MaxNLocator(10))
        plt.ylabel('Stock price and Volume')
        
        # Colors
        ax1.yaxis.label.set_color('w')
        ax1.spines['bottom'].set_color('#5998ff')
        ax1.spines['left'].set_color('#5998ff')
        ax1.spines['right'].set_color('#5998ff')
        ax1.spines['top'].set_color('#5998ff')
        ax1.tick_params(axis='y', colors='w')
        ax1.tick_params(axis='x', colors='w')
        
        for label in ax1.xaxis.get_ticklabels():
            label.set_rotation(45)
        
        volumeMin = 0
        
        ax1v = ax1.twinx()
        ax1v.axes.yaxis.set_ticklabels([])
        ax1v.grid(False)
        ax1v.set_ylim(0, 3*volume.max())
        
        # Colors
        ax1v.spines['bottom'].set_color('#5998ff')
        ax1v.spines['left'].set_color('#5998ff')
        ax1v.spines['right'].set_color('#5998ff')
        ax1v.spines['top'].set_color('#5998ff')
        ax1v.tick_params(axis='y', colors='w')
        ax1v.tick_params(axis='x', colors='w')
        ax1v.bar(date, volume, color='w', width=.001)
        
        #ax2 = plt.subplot2grid((4,4), (5,0), rowspan=2, colspan=4) # 3 rows down
        #ax2.axes.yaxis.set_ticklabels([])
        #ax2.grid(True)
        #plt.ylabel('Volume')
        
        # Formats the plot
        plt.subplots_adjust(left=.09, bottom=.18, right=.94, top=.95, wspace=.20, hspace=0)
        plt.setp(ax1.get_xticklabels(), visible=False)
        plt.xlabel('Date')
        
        plt.show()
    except Exception, e:
        print str(e), 'failed to plot data'

def main(args):
    candlestickIntradayExample()

if __name__ == '__main__':
  main(sys.argv)

# vim: ts=8 et sw=4 sts=4 background=light