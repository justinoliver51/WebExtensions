// Element class names
var commentsClassName = '.comments';
var newsClassName = '.newsletter';
var connectedClassName = '.green';

// Time related variables
var seconds = 0;
var numMilliseconds = 1000;

// Counters
var numItems = $(commentsClassName).length;
var numComments;
var numTradeAlerts;


var lastTradeAlert;
var lastComment;

// Debug
var debug = 1;

chrome.runtime.onMessage.addListener(
	function(request,sender,senderResponse)
	{
		// If we've received a trade update, let the server know!
		if(request.msg==="Update Received")
		{
			console.log(request.text);
		}
	}
);

function go () {
	// Log number of seconds every five minutes
	if((seconds % 300) == 0)
		console.log(seconds);

	// Get the trade array and update the time
	var tradeArray = $(commentsClassName);
	var newsArray = $(newsClassName);
/*	var connected = $(connectedClassName)[0].innerHTML;
	
	
	console.log($(connectedClassName)[0]);
	console.log(connected);

	// Monitor the page's connection
	if(connected.search("NOT CONNECTED") != -1)
	{
		chrome.runtime.sendMessage({msg:"Refresh",text:tradeUpdate},function(response){});
	}
*/

	// If we have just started running this script, initialize our variables
	if( (numItems == 0) && (tradeArray.length > 0) )
	{
		numItems = tradeArray.length;
		numComments = 10;
		numTradeAlerts = numItems - numComments;
		lastComment = tradeArray[numTradeAlerts].innerHTML.split("<a")[0];
		lastTradeAlert = tradeArray[0].innerHTML.split("<a")[0];
		console.log(lastComment);

		//numItems = 0; // Comment out for production - for instigating an 'Added'
		//lastTradeAlert = ""; // Comment out for production - for instigating a 'Bought'
	}

	// If there has been a new trade alert or comment
	if(tradeArray.length > numItems)
	{
		// local variables
		var tradeUpdate;
		var trader = null;
		
		// Set the trader
		if (newsArray[0].innerHTML.split("<a")[0].indexOf("Super") >= 0)
			trader = "superman";
		else if (newsArray[0].innerHTML.split("<a")[0].indexOf("Tim") >= 0)
			trader = "sykes";
		else
			trader = "unknown";

		// Update the number of items
		numItems = tradeArray.length;

		// Is this a new trade
		if(lastTradeAlert != tradeArray[0].innerHTML.split("<a")[0])
		{
			console.log("New trade alert!");

			// Access the latest trade text
			splitHTML = tradeArray[0].innerHTML.split("<a");
			tradeUpdate = splitHTML[0];
			console.log(tradeUpdate);

			// 
			lastTradeAlert = tradeUpdate;
			numTradeAlerts = numTradeAlerts + 1;
			
			// Send message to the background with the data
			chrome.runtime.sendMessage({msg:trader,text:tradeUpdate},function(response){});
		}
		// Is this an 'Added' comment
		else if(tradeArray[numTradeAlerts].innerHTML.split("<a")[0].indexOf("Added") >= 0)
		{
			console.log("Added comment!");

			// Access the latest comment text
			splitHTML = tradeArray[numTradeAlerts].innerHTML.split("<a");
			tradeUpdate = splitHTML[0];
			console.log(tradeUpdate);

			//
			lastComment = tradeUpdate;
			numComments = numComments + 1;
			
			// Send message to the background with the data
			chrome.runtime.sendMessage({msg:trader,text:tradeUpdate},function(response){});
		}
		// This is nothing
		else
		{
			console.log("False alarm!");
			numComments = numComments + 1;
		}

		// Print useful debug information
		if(debug == 1)
		{
			console.log("numTradeAlerts = ", numTradeAlerts);
			console.log("numComments = ", numComments);
			
			console.log("The first trade alert is: ", tradeArray[0].innerHTML.split("<a")[0]);
			console.log("The first comment is: ", tradeArray[numTradeAlerts].innerHTML.split("<a")[0]);
		}
	}
	
	seconds++;
	setTimeout(go, numMilliseconds);
}
go();
