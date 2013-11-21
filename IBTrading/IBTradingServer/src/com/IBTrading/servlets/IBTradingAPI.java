package com.IBTrading.servlets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JFrame;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientErrors;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.EWrapperMsgGenerator;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;
import com.IBTrading.servlets.OrderStatus;

public class IBTradingAPI extends JFrame implements EWrapper
{
	private EClientSocket m_client = new EClientSocket(this);
	private EClientSocket m_client_simulation = new EClientSocket(this);
	private final String orderIDPath = "/Users/justinoliver/Desktop/Developer/WebExtensions/orderID.txt";
	
	public boolean  m_bIsFAAccount = false;
	private boolean m_disconnectInProgress = false;

	// Personal variables
	private static int orderID;	 // If this value is not updated, we may simply never get a response...
	private static int tickerID = 0; 
	private static HashMap<String,OrderStatus> orderStatusHashMap = new HashMap<String,OrderStatus>();
	private static HashMap<Integer,HashMap<String,Double>> marketDataHashMap = new HashMap<Integer,HashMap<String,Double>>();
	private static double totalCash = 0;
	private static double totalCashSimulation = 0;
	private static boolean purchasingFlag = false;
	
	public IBTradingAPI()
	{
		// Get the current orderID
		Scanner sc;
		try 
		{
			sc = new Scanner(new File(orderIDPath));
			orderID = sc.nextInt() + 100;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			orderID = 1000000;
		}
	}
	
	public synchronized void connect() 
	{
		m_bIsFAAccount = false;

        // connect to TWS
        m_disconnectInProgress = false;
        
        if(m_client_simulation.isConnected() == false)
        {
        	m_client_simulation.eConnect(null, 7496, 0);
            if (m_client_simulation.isConnected()) {
                System.out.println("Connected to the TWS server, simulation!");
            }
        }
        if(m_client.isConnected() == false)
        {
        	m_client.eConnect(null, 7495, 0);
            if (m_client.isConnected()) {
                System.out.println("Connected to the TWS server!");
            }
        }
    }
	
	public synchronized void disconnect() 
	{
        // disconnect from TWS
        m_disconnectInProgress = true;
        m_client.eDisconnect();
        m_client_simulation.eDisconnect();
    }
	
	public OrderStatus getOrderStatus(int orderId)
	{
		return orderStatusHashMap.get(Integer.toString(orderId));
	}

    public synchronized OrderStatus placeOrder(String orderAction, String symbol, int quantity, boolean isSimulation, OrderStatus orderStatus) 
    {
    	int orderId = orderStatus == null ? orderID : orderStatus.orderId;
    	
    	// Check parameters
    	if( (orderAction == null) || (symbol == null) || (quantity == 0) )
    	{
    		System.out.println("Invalid parameters to placeOrder");
    		return null;
    	}
    	
        Order order = new Order();
        Contract contract = new Contract();
        
        setDefaultsOrder(order);
        setDefaultsContract(contract);
        
        contract.m_symbol = symbol;
        order.m_totalQuantity = quantity;
        order.m_action = orderAction;
        
        // Connect to TWS
    	connect();
        if( ((m_client.isConnected() == false) && (isSimulation == false)) || ((m_client_simulation.isConnected() == false) && (isSimulation == true)) )
        {
        	System.out.println("Unable to connect to TWS...");
        	return null;
        }
        
        // Place order
        if(isSimulation)
        	m_client_simulation.placeOrder( orderId, contract, order );
        else
        	m_client.placeOrder( orderId, contract, order );
        
        // Do not make a new purchase until we have finished selling from our last order
        // FIXME: May want a timeout
        //while(orderAction.equalsIgnoreCase("BUY") && (purchasingFlag == true)){}
        
        // Set the purchasing flag which prevents any other orders to occur until this
        // one is complete
        purchasingFlag = true;
        
        // Log time
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa");
		Date date = new Date();
		System.out.println("Order number " + orderId + " placed at: " + dateFormat.format(date));
		
		// Add the new order to our hash map
		OrderStatus newOrder;
		if(orderStatus == null)
		{
			newOrder =  new OrderStatus();
			orderStatusHashMap.put(Integer.toString(orderID), newOrder);
			
			// Update the orderID for the next order
			orderID++;
		}
		// Otherwise, we are modifying an existing order
		else
		{
			newOrder = orderStatus;
		}
		
		return newOrder;
    }
    
    public void cancelOrder(OrderStatus order, boolean isSimulation)
    {
        // Cancel order
        if(isSimulation)
        	m_client_simulation.cancelOrder(order.orderId);
        else
        	m_client.cancelOrder(order.orderId);
    }
    
    @Override
    public void orderStatus( int orderId, String status, int filled, int remaining,
			 double avgFillPrice, int permId, int parentId,
			 double lastFillPrice, int clientId, String whyHeld) 
    {
		// Received order status
		String msg = EWrapperMsgGenerator.orderStatus(orderId, status, filled, remaining,
		avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);
		
		// Get the date
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa");
		Date date = new Date();
		System.out.println(msg + " " + dateFormat.format(date));
		
		// 
		OrderStatus order;
		if(orderStatusHashMap.containsKey(Integer.toString(orderId)))
		{
			order = orderStatusHashMap.get(Integer.toString(orderId));
			order.updateOrder(orderId, status, filled, remaining,
					avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);
		}
		else
		{
			System.out.println("Unknown order, orderID: " + orderId);
			order = new OrderStatus(orderId, status, filled, remaining,
				avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);
			orderStatusHashMap.put(Integer.toString(orderId), order);
		}
		
		// make sure id for next order is at least orderId+1
		orderID++;
		
		// Set the orderID in the file
		try {
			PrintWriter writer = new PrintWriter(orderIDPath, "UTF-8");
			writer.println(orderID);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    void setDefaultsContract(Contract m_contract)
    {
        // Set useful contract fields
    	m_contract.m_secType = "STK";
    	m_contract.m_strike = 0;
    	m_contract.m_exchange = "SMART";
    	m_contract.m_primaryExch = "ISLAND"; 
    	m_contract.m_currency = "USD";
    	
    	// Set other fields to 0
    	m_contract.m_conId = 0;
    	m_contract.m_strike = 0.0;
    	m_contract.m_right = null;
    	m_contract.m_multiplier = null;
    	m_contract.m_localSymbol = null;
    	m_contract.m_includeExpired = false;
    	m_contract.m_secIdType = null;
    	m_contract.m_secId = null;
    }
    
    void setDefaultsOrder(Order m_order)
    {
        // set order fields
    	
    	// main order fields
        m_order.m_action = "BUY";
        m_order.m_orderType = "MKT";
        m_order.m_orderId = orderID;
        m_order.m_clientId = 0;
        /*
        public int 		m_clientId;
        public int  	m_permId;
        public double 	m_lmtPrice;
        public double 	m_auxPrice;
        
        // SMART routing only
        public double   m_discretionaryAmt;
        public boolean  m_eTradeOnly;
        public boolean  m_firmQuoteOnly;
        public double   m_nbboPriceCap;
        public boolean  m_optOutSmartRouting;
        */
        /*
        m_order.m_orderType = m_orderType.getText();
        m_order.m_lmtPrice = parseStringToMaxDouble( m_lmtPrice.getText());
        m_order.m_auxPrice = parseStringToMaxDouble( m_auxPrice.getText());
        m_order.m_goodAfterTime = m_goodAfterTime.getText();
        m_order.m_goodTillDate = m_goodTillDate.getText();

        m_order.m_faGroup = m_faGroup;
        m_order.m_faProfile = m_faProfile;
        m_order.m_faMethod = m_faMethod;
        m_order.m_faPercentage = m_faPercentage;

        // set historical data fields
        m_backfillEndTime = m_BackfillEndTime.getText();
        m_backfillDuration = m_BackfillDuration.getText();
        m_barSizeSetting = m_BarSizeSetting.getText();
        m_useRTH = Integer.parseInt( m_UseRTH.getText() );
        m_whatToShow = m_WhatToShow.getText();
        m_formatDate = Integer.parseInt( m_FormatDate.getText() );
        m_exerciseAction = Integer.parseInt( m_exerciseActionTextField.getText() );
        m_exerciseQuantity = Integer.parseInt( m_exerciseQuantityTextField.getText() );
        m_override = Integer.parseInt( m_overrideTextField.getText() );;

        // set market depth rows
        m_marketDepthRows = Integer.parseInt( m_marketDepthRowTextField.getText() );
        m_genericTicks = m_genericTicksTextField.getText();
        m_snapshotMktData = m_snapshotMktDataTextField.isSelected();
        
        m_marketDataType = m_marketDataTypeCombo.getSelectedIndex() + 1;
    	*/
    }
    
    public int subscribeToMarketData(String symbol, boolean isSimulation)
    {
    	Contract contract = new Contract();
    	String genericTicklist = null;
    	boolean snapshot = true;
    	
    	// Set up the contract
    	setDefaultsContract(contract);
    	contract.m_symbol = symbol;
    	
    	if(isSimulation)
    		m_client_simulation.reqMktData(tickerID, contract, genericTicklist, snapshot);
    	else
    		m_client.reqMktData(tickerID, contract, genericTicklist, snapshot);
    	
    	// Add a new hash map to market data for this stock
    	marketDataHashMap.put(tickerID, new HashMap<String,Double>());
    	
    	return tickerID++;
    }
    
    public double getAvailableFunds(boolean isSimulation)
    {
    	if(isSimulation)
    		return totalCashSimulation;
    	else
    		return totalCash;
    }
    
    public double getMarketData(int tickerId, String marketInfo)
    {
    	if(marketDataHashMap.get(tickerId) == null)
    		return 0.0;
    	else if(marketDataHashMap.get(tickerId).get(marketInfo) == null)
    		return 0.0;
    	else
    		return marketDataHashMap.get(tickerId).get(marketInfo);
    }
    
    public void initializeAvailableFunds()
    {
    	m_client_simulation.reqAccountUpdates(true, "DU170967");
    	m_client.reqAccountUpdates(true, "U1257707");
    }
    
    public boolean isStillPurchasing()
    {
    	return purchasingFlag;
    }
    
	@Override
	public void error(Exception e) {
		System.out.println(EWrapperMsgGenerator.error(e));
	}

	@Override
	public void error(String str) {
		System.out.println(EWrapperMsgGenerator.error(str));
	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
		System.out.println(EWrapperMsgGenerator.error(id, errorCode, errorMsg));
	}

	@Override
	public void connectionClosed() {
		// TODO Auto-generated method stub
		
	}

	/*
		-1	Not applicable.	--
		0	BID_SIZE	tickSize()
		1	BID_PRICE	tickPrice()
		2	ASK_PRICE	tickPrice()
		3	ASK_SIZE	tickSize()
		4	LAST_PRICE	tickPrice()
		5	LAST_SIZE	tickSize()
		6	HIGH	tickPrice()
		7	LOW	tickPrice()
		8	VOLUME	tickSize()
		9	CLOSE_PRICE	tickPrice()
		11	ASK_OPTION_COMPUTATION	tickOptionComputation()
		12	LAST_OPTION_COMPUTATION	tickOptionComputation()
		13	MODEL_OPTION_COMPUTATION	tickOptionComputation()
		14	OPEN_TICK	tickPrice()
		15	LOW_13_WEEK	tickPrice()
		16	HIGH_13_WEEK	tickPrice()
		17	LOW_26_WEEK	tickPrice()
		18	HIGH_26_WEEK	tickPrice()
		19	LOW_52_WEEK	tickPrice()
		20	HIGH_52_WEEK	tickPrice()
		21	AVG_VOLUME	tickSize()
		22	OPEN_INTEREST	tickSize()
		23	OPTION_HISTORICAL_VOL	tickGeneric()
		24	OPTION_IMPLIED_VOL	tickGeneric()
		25	OPTION_BID_EXCH	NOT USED
		26	OPTION_ASK_EXCH	NOT USED
		27	OPTION_CALL_OPEN_INTEREST	tickSize()
		28	OPTION_PUT_OPEN_INTEREST	tickSize()
		29	OPTION_CALL_VOLUME	tickSize()
		30	OPTION_PUT_VOLUME	tickSize()
		31	INDEX_FUTURE_PREMIUM	tickGeneric()
		32	BID_EXCH	tickString()
		33	ASK_EXCH	tickString()
		34	AUCTION_VOLUME	tickSize()
		35	AUCTION_PRICE	tickPrice()
		36	AUCTION_IMBALANCE	tickSize()
		37	MARK_PRICE	tickPrice()
		38	BID_EFP_COMPUTATION	tickEFP()
		39	ASK_EFP_COMPUTATION	tickEFP()
		40	LAST_EFP_COMPUTATION	tickEFP()
		41	OPEN_EFP_COMPUTATION	tickEFP()
		42	HIGH_EFP_COMPUTATION	tickEFP()
		43	LOW_EFP_COMPUTATION	tickEFP()
		44	CLOSE_EFP_COMPUTATION	tickEFP()
		45	LAST_TIMESTAMP	tickString()
		46	SHORTABLE	tickString()
		47	FUNDAMENTAL_RATIOS	tickString()
		48	RT_VOLUME	tickGeneric()
		49	HALTED	See Note 2 below.
		50	BIDYIELD	tickPrice()
		51	ASKYIELD	tickPrice()
		52	LASTYIELD	tickPrice()
		53	CUST_OPTION_COMPUTATION	tickOptionComputation()
		54	TRADE_COUNT	tickGeneric()
		55	TRADE_RATE	tickGeneric()
		56	VOLUME_RATE	tickGeneric()
	*/
	@Override
	public void tickPrice(int tickerId, int field, double price,
			int canAutoExecute) {
		System.out.println(EWrapperMsgGenerator.tickPrice(tickerId, field, price, canAutoExecute));
		
		String marketInfo = null;
		if(field == 0)
			marketInfo = "BID_SIZE";
		else if(field == 1)
			marketInfo = "BID_PRICE";
		else if(field == 2)
			marketInfo = "ASK_PRICE";
		else if(field == 3)
			marketInfo = "ASK_SIZE";
		else if(field == 4)
			marketInfo = "LAST_PRICE";
		else if(field == 5)
			marketInfo = "LAST_SIZE";
		else if(field == 6)
			marketInfo = "HIGH";
		else if(field == 7)
			marketInfo = "LOW";
		else if(field == 8)
			marketInfo = "VOLUME";
		else if(field == 9)
			marketInfo = "CLOSE_PRICE";
		else if(field == 11)
			marketInfo = "ASK_OPTION_COMPUTATION";
		else if(field == 12)
			marketInfo = "LAST_OPTION_COMPUTATION";
		else if(field == 13)
			marketInfo = "MODEL_OPTION_COMPUTATION";
		else if(field == 14)
			marketInfo = "OPEN_TICK";
		else if(field == 15)
			marketInfo = "LOW_13_WEEK";
		else if(field == 16)
			marketInfo = "HIGH_13_WEEK";
		else if(field == 17)
			marketInfo = "LOW_26_WEEK";
		else if(field == 18)
			marketInfo = "HIGH_26_WEEK";
		else if(field == 19)
			marketInfo = "LOW_52_WEEK";
		else if(field == 20)
			marketInfo = "HIGH_52_WEEK";
		else if(field == 21)
			marketInfo = "AVG_VOLUME";
		else if(field == 22)
			marketInfo = "OPEN_INTEREST";
		else if(field == 23)
			marketInfo = "OPTION_HISTORICAL_VOL";
		else if(field == 24)
			marketInfo = "OPTION_IMPLIED_VOL";
		else if(field == 25)
			marketInfo = "OPTION_BID_EXCH";
		else if(field == 26)
			marketInfo = "OPTION_ASK_EXCH";
		else if(field == 27)
			marketInfo = "OPTION_CALL_OPEN_INTEREST";
		else if(field == 28)
			marketInfo = "OPTION_PUT_OPEN_INTEREST";
		else if(field == 29)
			marketInfo = "OPTION_CALL_VOLUME";
		else if(field == 30)
			marketInfo = "OPTION_PUT_VOLUME";
		else if(field == 31)
			marketInfo = "INDEX_FUTURE_PREMIUM";
		else if(field == 32)
			marketInfo = "BID_EXCH";
		else if(field == 33)
			marketInfo = "ASK_EXCH";
		else if(field == 34)
			marketInfo = "AUCTION_VOLUME";
		else if(field == 35)
			marketInfo = "AUCTION_PRICE";
		else if(field == 36)
			marketInfo = "AUCTION_IMBALANCE";
		else if(field == 37)
			marketInfo = "MARK_PRICE";
		else if(field == 38)
			marketInfo = "BID_EFP_COMPUTATION";
		else if(field == 39)
			marketInfo = "ASK_EFP_COMPUTATION";
		else if(field == 40)
			marketInfo = "LAST_EFP_COMPUTATION";
		else if(field == 41)
			marketInfo = "OPEN_EFP_COMPUTATION";
		else if(field == 42)
			marketInfo = "HIGH_EFP_COMPUTATION";
		else if(field == 43)
			marketInfo = "LOW_EFP_COMPUTATION";
		else if(field == 44)
			marketInfo = "CLOSE_EFP_COMPUTATION";
		else if(field == 45)
			marketInfo = "LAST_TIMESTAMP";
		else if(field == 46)
			marketInfo = "SHORTABLE";
		else if(field == 47)
			marketInfo = "FUNDAMENTAL_RATIOS";
		else if(field == 48)
			marketInfo = "RT_VOLUME";
		else if(field == 49)
			marketInfo = "HALTED";
		else if(field == 50)
			marketInfo = "BIDYIELD";
		else if(field == 51)
			marketInfo = "ASKYIELD";
		else if(field == 52)
			marketInfo = "LASTYIELD";
		else if(field == 53)
			marketInfo = "CUST_OPTION_COMPUTATION";
		else if(field == 54)
			marketInfo = "TRADE_COUNT";
		else if(field == 55)
			marketInfo = "TRADE_RATE";
		else if(field == 56)
			marketInfo = "VOLUME_RATE";
		else
			marketInfo = "UNKNOWN";
		
		HashMap<String,Double> temp = marketDataHashMap.get(tickerId);
		marketDataHashMap.get(tickerId).put(marketInfo, price);
	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
		System.out.println(EWrapperMsgGenerator.tickSize(tickerId, field, size));
	}

	@Override
	public void tickOptionComputation(int tickerId, int field,
			double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta,
			double undPrice) {
		System.out.println(EWrapperMsgGenerator.tickOptionComputation(tickerId, field, impliedVol, delta, optPrice, pvDividend, gamma, vega, theta, undPrice));
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		System.out.println(EWrapperMsgGenerator.tickGeneric(tickerId, tickType, value));
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		System.out.println(EWrapperMsgGenerator.tickString(tickerId, tickType, value));
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureExpiry, double dividendImpact, double dividendsToExpiry) {
		System.out.println(EWrapperMsgGenerator.tickEFP(tickerId, tickType, basisPoints, formattedBasisPoints, impliedFuture, holdDays, futureExpiry, dividendImpact, dividendsToExpiry));
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order,
			OrderState orderState) {
		System.out.println(EWrapperMsgGenerator.openOrder(orderId, contract, order, orderState));
	}

	@Override
	public void openOrderEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAccountValue(String key, String value, String currency,
			String accountName) {
		// TODO Auto-generated method stub
		String msg = EWrapperMsgGenerator.updateAccountValue(key, value, currency, accountName);
		
		// Only get the information regarding the current funds in the account
		if(key.equalsIgnoreCase("AvailableFunds"))
		{
			double cash = Double.parseDouble(value);
			
			// Simulation account
			if(accountName.equalsIgnoreCase("DU170967"))
				totalCashSimulation = cash;
			// Real money account
			else if(accountName.equalsIgnoreCase("U1257707"))
				totalCash = cash;
			
			System.out.println(msg);
		}
	}

	@Override
	public void updatePortfolio(Contract contract, int position,
			double marketPrice, double marketValue, double averageCost,
			double unrealizedPNL, double realizedPNL, String accountName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAccountTime(String timeStamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accountDownloadEnd(String accountName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nextValidId(int orderId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contractDetailsEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		//System.out.println(EWrapperMsgGenerator.execDetails(reqId, contract, execution));
		// execution.m_shares
		// execution.m_orderId
		
		System.out.println("Order, " + execution.m_orderId + " executed " + execution.m_cumQty);
		
		
		// FIXME: We want to wait until after both the 
		OrderStatus orderStatus = orderStatusHashMap.get(Integer.toString(execution.m_orderId));
		
		// If we have completed the order, give the signal
		if( ((orderStatus.status.equalsIgnoreCase("Cancelled") == true) ||
				(orderStatus.status.equalsIgnoreCase("Filled") == true) )
			&& execution.m_side.equalsIgnoreCase("SELL"))
		{
			purchasingFlag = false;
		}
	}

	@Override
	public void execDetailsEnd(int reqId) {
		System.out.println(EWrapperMsgGenerator.execDetailsEnd(reqId));
	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation,
			int side, double price, int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMktDepthL2(int tickerId, int position,
			String marketMaker, int operation, int side, double price, int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message,
			String origExchange) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void managedAccounts(String accountsList) {
		// TODO Auto-generated method stub
		String msg = EWrapperMsgGenerator.managedAccounts(accountsList);
		System.out.println(msg);
	}

	@Override
	public void receiveFA(int faDataType, String xml) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalData(int reqId, String date, double open,
			double high, double low, double close, int volume, int count,
			double WAP, boolean hasGaps) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scannerParameters(String xml) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scannerData(int reqId, int rank,
			ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scannerDataEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void realtimeBar(int reqId, long time, double open, double high,
			double low, double close, long volume, double wap, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void currentTime(long time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fundamentalData(int reqId, String data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deltaNeutralValidation(int reqId, UnderComp underComp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickSnapshotEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {
		// TODO Auto-generated method stub
		
	}
	
}
