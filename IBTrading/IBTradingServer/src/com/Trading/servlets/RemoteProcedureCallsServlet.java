package com.Trading.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.Trading.ib.HistoricalDataCollector;
import com.Trading.ib.IBTradingAPI;
import com.Trading.ib.MarketDepthCollector;
import com.google.gson.Gson;

/**
 * Servlet implementation class RemoteProcedureCallsServlet
 */
@WebServlet("/RemoteProcedureCallsServlet")
//http://localhost:8080/TradingServer/RemoteProcedureCallsServlet?startUp=true
//http://localhost:8080/TradingServer/RemoteProcedureCallsServlet?historicalDataSym=GOOG&historicalDataTimestamp=1395316152000

public class RemoteProcedureCallsServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private IBTradingAPI tradingAPI;
	private TradeCenter tradeCenter;
	
	// Database
	private static final String dbClassName = "com.mysql.jdbc.Driver";
	private static final String PATH = "jdbc:mysql://127.0.0.1/IBTradingDB";
	private static final String DEFAULT_USERNAME = "justinoliver51";;
	private static final String DEFAULT_PASSWORD = "utredhead51";
	private static final int 	MAX_ACTIVE_CONNECTIONS = 500;
	private static final int MAX_SESSIONS_PER_USER = 3;
	private static BasicDataSource ConnPool;
	private Database DB;
	
	public static BasicDataSource setupDataSource() 
	{
		ConnPool = new BasicDataSource();
		ConnPool.setMaxActive(MAX_ACTIVE_CONNECTIONS);
		ConnPool.setDriverClassName(dbClassName);
		ConnPool.setUsername(DEFAULT_USERNAME);
		ConnPool.setPassword(DEFAULT_PASSWORD);
		ConnPool.setUrl(PATH);
		ConnPool.setValidationQuery("select 1 as dbcp_connection_test");
		//ConnPool.setValidationQueryTimeout(2);
		ConnPool.setTestOnBorrow(true);
		ConnPool.setTestWhileIdle(true);
		System.out.println("Max active connections: "+ ConnPool.getMaxActive());
		
		return ConnPool;
	}
	
	public static BasicDataSource getConnectionPool() 
	{
		if(ConnPool == null)
			setupDataSource();
		
		return ConnPool;
	}
	
	private String getUsername(HttpServletRequest req)
	{
		HttpSession s = req.getSession();
		return (String)s.getAttribute("username");
	}	
	
	private boolean authenticate(HttpServletRequest req)
	{
		HttpSession s = req.getSession();
		Object loggedIn = s.getAttribute("logon.isDone");
		if(loggedIn == null){
			return false;
		}
		else{
			return true;
		}			
	}
	
	private String outputTradeInfo(HashMap<String,Object> tradeInfo)
	{
		String output = "";
		
		String stockSymbol = (String) tradeInfo.get(Database.STOCKSYMBOL);
		double averageBuyingPrice = (Double) tradeInfo.get(Database.AVERAGEBUYINGPRICE);
		double averageSellingPrice = (Double) tradeInfo.get(Database.AVERAGESELLINGPRICE);
		int numberOfShares = (Integer) tradeInfo.get(Database.NUMBEROFSHARES);
		boolean debugFlag = (Boolean) tradeInfo.get(Database.DEBUGFLAG);
		
		output += "Simulation: " + debugFlag
				+ "\nStock: " + stockSymbol
				+ "\nShares: " + numberOfShares
				+ "\nBuying Price: " + averageBuyingPrice
				+ "\nSelling Price: " + averageSellingPrice
				+ "\nNet Value: " + numberOfShares * (averageBuyingPrice - averageSellingPrice)
				+ "\nTotal funds: " + tradingAPI.getAvailableFunds(debugFlag);
		
		return output;
	}
       
    /**
     * @see HttpServlet#HttpServlet()
     */
	
	public void init() throws ServletException 
	{
		try 
		{
			Class.forName(dbClassName);
		} catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		setupDataSource();		
	}	
	
	public RemoteProcedureCallsServlet() 
    {
        super();

        // Initialize the trading API connection
        tradingAPI = new IBTradingAPI();
        tradingAPI.connect();
        
        // Initializes the Trade Center
        tradeCenter = new TradeCenter(tradingAPI);
        
        // Subscribe to updates from my account
        boolean isSimulation = true;
        tradingAPI.initializeAvailableFunds();
        //tradingAPI.getMarketData("DGLY", false);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		HttpSession session = request.getSession(true);
		PrintWriter out = response.getWriter();
		response.setHeader("Access-Control-Allow-Origin", "*");
        System.out.println("-----------------------\ndoGet() called!  :)");
        //out.println("-----------------------\ndoGet() called");
        
        synchronized (session)
        {
        	DB = new Database(ConnPool);
        }
        
        String startUp 					= request.getParameter("startUp");
        String historicalDataSym 		= request.getParameter("historicalDataSym");
        String historicalDataTimestamp	= request.getParameter("historicalDataTimestamp");
		String traderID 				= request.getParameter("traderID");
		String newTrade 				= request.getParameter("newTrade");
		String debug 					= request.getParameter("debug");
		String realTimeSystem 			= request.getParameter("realTimeSystem");
		String marketDepthSym			= request.getParameter("marketDepthSym");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa");
		Date date = new Date();
		
		System.out.println("traderID: " + traderID + "\ntrade: " + newTrade + "\nrealTimeSystem: " + realTimeSystem + "\non date: " + dateFormat.format(date));
		
		// If we are just starting up, exit!
		if(debug != null)
		{
			System.out.println("Debug called!");
			tradingAPI.requestOpenOrders();
			
			return;
		}
		
		// If we are just starting up, exit!
		if(startUp != null)
		{
			System.out.println("System started up!");
			out.println("System started up!");
			return;
		}
		
		// If historical data has been requested, return it
		if( (historicalDataSym != null) && (historicalDataTimestamp != null) )
		{
			System.out.println("Historical Data requested!");
			
			Gson gson = new Gson();
			HistoricalDataCollector dataCollector = new HistoricalDataCollector(tradingAPI);
			boolean formatData = true;
			HashMap<String, Object> returnMap = dataCollector.getHistoricalDataOverLast30Minutes(historicalDataSym, historicalDataTimestamp, formatData);
			out.println(gson.toJson(returnMap));
			
			return;
		}
		
		if(marketDepthSym != null)
		{
			System.out.println("Market Depth data requested!");
			
			Gson gson = new Gson();
			MarketDepthCollector dataCollector = new MarketDepthCollector(tradingAPI);
			HashMap<String, Object> returnMap = dataCollector.collectMarketDepthForTenMin(marketDepthSym);
			out.println(gson.toJson(returnMap));
			
			return;
		}
		
		/*
		if(debug != null)
		
		{
			JythonObjectFactory factory = JythonObjectFactory.getInstance();
			TDAmeritradeAPI api = (TDAmeritradeAPI) factory.createObject(
					TDAmeritradeAPI.class, "TD Ameritrade API");
			api.loginRequest();
			return;
		}
		*/
		
		out.println("traderID: " + traderID + "\ntrade: " + newTrade + "\nrealTimeSystem: " + realTimeSystem + "\non date: " + dateFormat.format(date));
		
		// Invalid parameters
		if( (traderID == null) || (newTrade == null) || (realTimeSystem == null) || 
				((realTimeSystem.equalsIgnoreCase("email") == false) && 
						(realTimeSystem.equalsIgnoreCase("websiteMonitor") == false)) )
		{
			System.out.println("Invalid parameters...");
			out.println("Invalid parameters: \ntraderID = " + traderID 
					+ "\nnewTrade = " + newTrade);
			return;
		}
		
		// Send the trade to the TradeCenter to be evaluated
		String tradeError = tradeCenter.newTrade(traderID, newTrade, realTimeSystem);
		if(tradeError != null)
		{
			System.out.println("Invalid parameters, " + traderID + ", " + newTrade);
			out.println("Error: " + tradeError);
			return;
		}

		// The new trade has found a trader.  
		// If the trade is successful, send the all clear!
		tradeError = tradeCenter.trade();
		if(tradeError != null)
		{
			System.out.println("Invalid trade!\nError: " + tradeError);
			out.println("Invalid Trade!\nError: " + tradeError);
			return;
		}

		// Valid trade, write to the database
		//tradeCenter.getOrderIds();
		System.out.println("Valid trade initiated!");
		out.println("Valid trade!");
		
		// If we have trade info, save it to the database
		HashMap<String,Object> tradeInfo = tradeCenter.getTradeInfo();
		if(tradeInfo == null)
		{
			System.out.println("No tradeInfo to save to database.");
			return;
		}
		
		// Send the trade info to the client
		out.println(outputTradeInfo(tradeInfo));
		
		// Save the tradeInfo to the database
		DB.NewTrade(tradeInfo);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doPost(request, response);
	}
}