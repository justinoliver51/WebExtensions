package com.IBTrading.servlets;

import javax.swing.JFrame;

import TestJavaClient.ConnectDlg;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.EWrapperMsgGenerator;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;

public class IBTradingAPI extends JFrame implements EWrapper
{

	private EClientSocket   m_client = new EClientSocket( this);
	private static int orderID = 20;
	
	public boolean  m_bIsFAAccount = false;
	private boolean m_disconnectInProgress = false;
	
	
	public synchronized void connect() 
	{
        m_bIsFAAccount = false;

        // connect to TWS
        m_disconnectInProgress = false;
        
        m_client.eConnect(null, 7496, 0);
        if (m_client.isConnected()) {
            System.out.println("Connected to the TWS server!");
        }
    }
	
	public synchronized void disconnect() 
	{
        // disconnect from TWS
        m_disconnectInProgress = true;
        m_client.eDisconnect();
    }

    public synchronized void placeOrder(String symbol, int quantity) 
    {

        Order order = new Order();
        Contract contract = new Contract();
        
        setDefaultsOrder(order);
        setDefaultsContract(contract);
        
        contract.m_symbol = symbol;
        order.m_totalQuantity = quantity;
        
        // If we have disconnected, reestablish the connection
        if (m_client.isConnected() == false) {
        	m_client.eConnect(null, 7496, 0);
            if (m_client.isConnected() == false) {
            	System.out.println("Unable to connect to TWS...");
            	
            }
        }
        
        // place order
        m_client.placeOrder( orderID, contract, order );
    }
    
    @Override
    public void orderStatus( int orderId, String status, int filled, int remaining,
			 double avgFillPrice, int permId, int parentId,
			 double lastFillPrice, int clientId, String whyHeld) 
    {
		// received order status
		String msg = EWrapperMsgGenerator.orderStatus( orderId, status, filled, remaining,
		avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);
		
		System.out.println(msg);
		
		// make sure id for next order is at least orderId+1
		orderID =  orderId + 1;
	}
    
    void setDefaultsContract(Contract m_contract)
    {
        // Set useful contract fields
    	m_contract.m_secType = "STK";
    	m_contract.m_strike = 0;
    	m_contract.m_exchange = "SMART";
    	m_contract.m_primaryExch = "ISLAND"; // FIXME: Justin, what is this?
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
    
	@Override
	public void error(Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(String str) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionClosed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickPrice(int tickerId, int field, double price,
			int canAutoExecute) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickOptionComputation(int tickerId, int field,
			double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta,
			double undPrice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureExpiry, double dividendImpact, double dividendsToExpiry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order,
			OrderState orderState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openOrderEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAccountValue(String key, String value, String currency,
			String accountName) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execDetailsEnd(int reqId) {
		// TODO Auto-generated method stub
		
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