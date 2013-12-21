package com.IBTrading.servlets;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

public class Database 
{
	// CONSTANT STRINGS WITH MYSQL INFORMATION
	private static final String dbClassName = "com.mysql.jdbc.Driver";
	private static final String PATH = "jdbc:mysql://127.0.0.1/IBTradingDB";
	private static final String DEFAULT_USERNAME = "justinoliver51";;
	private static final String DEFAULT_PASSWORD = "utredhead51";
	
	// CONSTANT STRINGS FOR TABLE NAMES
	// Basic trade information
	public static final String STOCKSYMBOL = "StockSymbol";
	public static final String AVERAGEBUYINGPRICE = "AverageBuyingPrice";
	public static final String AVERAGESELLINGPRICE = "AverageSellingPrice";
	public static final String NUMBEROFSHARES = "NumberOfShares";
	
	// Minute granularity of volume
	public static final String INITIALVOLUME = "InitialVolume";
	public static final String VOLUMEAFTERPURCHASE = "VolumeAfterPurchase";
	public static final String FINALVOLUME = "FinalVolume";
	
	// Daily granularity of VWAP
	public static final String CASHTRADEDYESTERDAY = "CashTradedYesterday";
	public static final String CASHTRADEDLASTWEEK = "CashTradedLastWeek";
	public static final String CASHTRADEDLASTMONTH = "CashTradedLastMonth";
	
	// Specifies whether this trade was just debug or real
	public static final String DEBUGFLAG = "DebugFlag";
	
	// PRIVATE VARIABLES
	private BasicDataSource DBConnection = null;
	
	
	/******* USEFUL MYSQL COMMANDS *******/
	/*
 		LOGIN:
		mysql -u justinoliver51 -p'utredhead51' IBTradingDB
		
		GET CREATE TABLE INFO:
		show create table TradeData;
	 */
	
	/******* THESE ARE THE STATEMENTS TO CREATE MY TABLES *******/
	/*
		//`MarketDepth` int(11) DEFAULT NULL,  reqMktDepth(), updateMktDepth(), updateMktDepthL2()
	
		CREATE TABLE `TradeData` (
		  `StockSymbol` varchar(10) DEFAULT NULL,
		  `AverageBuyingPrice` double DEFAULT NULL,
		  `AverageSellingPrice` double DEFAULT NULL,
		  `NumberOfShares` int(11) DEFAULT NULL,
		  `InitialVolume` int(11) DEFAULT NULL,
		  `VolumeAfterPurchase` int(11) DEFAULT NULL,
		  `FinalVolume` int(11) DEFAULT NULL,
		  `CashTradedYesterday` double DEFAULT NULL,
		  `CashTradedLastWeek` double DEFAULT NULL,
		  `CashTradedLastMonth` double DEFAULT NULL,
		  `DebugFlag` tinyint DEFAULT NULL,
		  `Time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
		  `TradeID` int(11) NOT NULL AUTO_INCREMENT,
		  PRIMARY KEY (`TradeID`)
		) ENGINE=MyISAM DEFAULT CHARSET=latin1;
	*/

	Database(BasicDataSource BDS) 
	{
		DBConnection = BDS;
	}

	private synchronized Connection Connect(String username, String password) 
	{
		// This will load the MySQL driver, each DB has its own driver
		try {
			Class.forName(dbClassName);
		} catch (ClassNotFoundException e) {
			return null;
		}
		Properties p = new Properties();
		p.put("user", username);
		p.put("password", password);

		try {
			return DriverManager.getConnection(PATH, p);
		} catch (SQLException e) {
			return null;
		}
	}

	private synchronized Connection Connect() 
	{
		return Connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
	}
	
	void closeConnection(PreparedStatement ps, Connection conn, ResultSet rs)
	{
	    // A couple of things to note, here:
	    //   1. Close objects in the proper order: result, then statement,
	    //      then connection.
	    //   2. Each close gets its own try/catch block. You don't want
	    //      the connection to be leaked just because the result set
	    //      failed to close properly.
	    //   3. Don't throw any exceptions in a finally block. If there is
	    //      already an exception "in the air", you'll shoot it down
	    //      and replace it with a new one. The original exception is
	    //      almost certainly more useful.
	    //   4. NEVER swallow an exception. At least log the error.
	    //   5. This cleanup code has whitespace removed for brevity.
	    //   6. This cleanup code lends itself to being put into a separate
	    //      method. I usually have a 'close' method that takes 3 arguments:
	    //      Connection, Statement, ResultSet and does the same thing.

	    if(null != rs) try { rs.close(); } catch (SQLException sqle)
	        {  sqle.printStackTrace(); }
	    if(null != ps) try { ps.close(); } catch (SQLException sqle)
	        {  sqle.printStackTrace(); }
	    if(null != conn) try { conn.close(); } catch (SQLException sqle)
	        {  sqle.printStackTrace(); }
	}
	
	/******** FUNCTIONS THAT READ/WRITE TO THE DATABASE **********/
	
	public synchronized void NewTrade(HashMap<String,Object> tradeInfo)  
	{
		// Get parameters from the trade
		String stockSymbol = (String) tradeInfo.get(STOCKSYMBOL);
		double averageBuyingPrice = (Double) tradeInfo.get(AVERAGEBUYINGPRICE);
		double averageSellingPrice = (Double) tradeInfo.get(AVERAGESELLINGPRICE);
		int numberOfShares = (Integer) tradeInfo.get(NUMBEROFSHARES);
		int initialVolume = (Integer) tradeInfo.get(INITIALVOLUME);
		int volumeAfterPurchase = (Integer) tradeInfo.get(VOLUMEAFTERPURCHASE);
		int finalVolume = (Integer) tradeInfo.get(FINALVOLUME);
		double cashTradedYesterday = (Double) tradeInfo.get(CASHTRADEDYESTERDAY);
		double cashTradedLastWeek = (Double) tradeInfo.get(CASHTRADEDLASTWEEK);
		double cashTradedLastMonth = (Double) tradeInfo.get(CASHTRADEDLASTMONTH);
		boolean debugFlag = (Boolean) tradeInfo.get(DEBUGFLAG);
		
		Connection con = null;
		PreparedStatement p = null;
		try {
			con = DBConnection.getConnection();
			
			String statement = "INSERT INTO TradeData "
					+ "(StockSymbol,AverageBuyingPrice,AverageSellingPrice,"
					+ "NumberOfShares,InitialVolume,VolumeAfterPurchase,FinalVolume,"
					+ "CashTradedYesterday,CashTradedLastWeek,CashTradedLastMonth,"
					+ "DebugFlag) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
				
				p = con.prepareStatement(statement);
				p.setString(1, stockSymbol);
				p.setDouble(2, averageBuyingPrice);
				p.setDouble(3, averageSellingPrice);
				p.setInt(4, numberOfShares);
				p.setInt(5, initialVolume);
				p.setInt(6, volumeAfterPurchase);
				p.setInt(7, finalVolume);
				p.setDouble(8, cashTradedYesterday);
				p.setDouble(9, cashTradedLastWeek);
				p.setDouble(10, cashTradedLastMonth);
				p.setBoolean(11, debugFlag);

				System.out.println(p);
				p.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			closeConnection(p, con, null);
		}
		
		return;
	}
}
