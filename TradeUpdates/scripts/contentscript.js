// Element class names
var commentsClassName = '.comments';
var newsClassName = '.newsletter';
var connectedClassName = '.green';
var chatGuruClassName = '.chatWidgetText chatWidgetComm'; //class="chatWidgetText chatWidgetGuru"
var chatRoomClassName = '.dijitContentPane chatWidgetMessages chatWidget-child chatWidget-dijitContentPane chatWidgetPane dijitAlignCenter';
var chatRoomID = 'dijit_layout_ContentPane_3';

// Time related variables
var tenthSeconds = 0;
var numMilliseconds = 100;

// Counters
var numItems = $(commentsClassName).length;
var numComments;
var numTradeAlerts;
var numChats = 0;

var lastTradeAlert;
var lastComment;

// Debug
var debug = true;
var initiateTrade = false;

/*
// Injecting a small amount of code to add the necessary settings to the log
var updatingLogCode = [	'console.logCopy = console.log.bind(console);',
                       	' console.log = function(data)',
                       	' {',
                       		' var currentDate = \'[\' + new Date().toUTCString() + \'] \';',
                       		' this.logCopy(currentDate, data);'
                       	' }'].join('\n');

// Inject the script
var script = document.createElement('script');
script.textContent = updatingLogCode;
(document.head||document.documentElement).appendChild(script);
script.parentNode.removeChild(script);
*/

var script = document.createElement('script');
script.setAttribute("type", "text/javascript");
script.setAttribute("async", true);
script.setAttribute("src", chrome.extension.getURL("scripts/profitlyInjectedScript.js")); //Assuming your host supports both http and https
var head = document.head || document.getElementsByTagName( "head" )[0] || document.documentElement;
head.insertBefore(script, head.firstChild)


// Add a listener for the "Update Received" message
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

// Main function
function go () {
	// Log number of seconds every five minutes
	if((tenthSeconds % 3000) == 0)
	{
		console.log("Time: " + new Date());
		
		// Print useful debug information
		if(debug == true)
		{
			console.log("numTradeAlerts = ", numTradeAlerts);
			console.log("numComments = ", numComments);
			console.log("lastTradeAlert = ", lastTradeAlert);
			
			if(tradeArray != null)
			{
				console.log("The first trade alert is: ", tradeArray[0].innerHTML.split("<a")[0]);
				console.log("The first comment is: ", tradeArray[numTradeAlerts].innerHTML.split("<a")[0]);
			}
		}
	}

	// Get the trade array and update the time
	var tradeArray = $(commentsClassName);
	var newsArray = $(newsClassName);
	var chatRoom = $('#dijit_layout_ContentPane_3');

	// INITIALIZATION
	if( (numItems == 0) && (tradeArray.length > 0) )
	{
		numItems = tradeArray.length;
		numComments = 10;
		numTradeAlerts = numItems - numComments;
		lastComment = tradeArray[numTradeAlerts].innerHTML.split("<a")[0];
		lastTradeAlert = tradeArray[0].innerHTML.split("<a")[0];
		console.log(lastComment);

		if(initiateTrade == true)
		{
			numItems = 0; // Instigating an 'Added'
			lastTradeAlert = ""; //Instigating a 'Bought'
			numTradeAlerts = numTradeAlerts - 1; // Subtracts an alert because there really isn't a new one
		}
	}
	
	// CHAT ROOM
	if( (chatRoom != null) && (tenthSeconds % 30 ) )
	{
		var chatRoomArray = chatRoom.children();
		
		// Initialization
		if( (chatRoomArray.length > 0) && (numChats == 0) )
		{
			numChats = chatRoomArray.length;
		}
		
		// New chat
		if(chatRoomArray.length != numChats)
		{
			//console.log("New chat at time: " + new Date());
	
			
			// When a new chat is posted, it is in the following format:
			// newChat[0] == Time
			// newChat[1] == Username
			// newChat[2] == chat text
			// newChat[5] == br
            var username;
            var chatText;
            var counter = 0
            
            // Loop through the new chat room post, grabbing everything important
			for(var i = numChats; i < chatRoomArray.length; i++, counter++)
			{
                var newChat = chatRoomArray[i];
                
                if(newChat.className == "chatWidgetComm chatWidgetUserBold")
                	username = newChat.innerText;
                else if(newChat.className == "chatWidgetText chatWidgetComm")
                	chatText = newChat.innerText;
                
                // If there has been a new trade, break
                if( ((username == "timothysykes") || (username == "super_trades")) && (chatText != null)  && ((chatText.indexOf("[trade]") >= 0) || ((chatText.indexOf("[commentary]") >= 0))) )
                	break;
			}
			
			// If this was a useful chat, log it and send it to the server
            if( (username == "timothysykes") || (username == "super_trades") )
            {
            	console.log(username + " " + chatText + " at time: " + new Date());
            	chrome.runtime.sendMessage({msg:username,text:chatText},function(response){});
            }
            
            // Debug
            if(username == "justinoliver51")
            	console.log(username + " " + chatText + " at time: " + new Date());
			
			// Update the length
			numChats = chatRoomArray.length;
			//console.log(chatRoom.outerText + " at time: " + new Date());
		}
	}

	/*
	// TRADE ALERTS
	// If there has been a new trade alert or comment
	if(tradeArray.length > numItems)
	{
		// local variables
		var tradeUpdate;
		var trader = null;

		// Set the trader
		trader = newsArray[0].innerHTML.split("<a")[0];

		// Update the number of items
		numItems = tradeArray.length;

		// Is this a new trade
		if(lastTradeAlert != tradeArray[0].innerHTML.split("<a")[0])
		{
			console.log("New trade alert! Time: " + new Date());

			// Update the global variables
			lastTradeAlert = tradeArray[0].innerHTML.split("<a")[0]; // Grabs the newest alert
			numTradeAlerts = numTradeAlerts + 1;
			numComments = numItems - numTradeAlerts; // Some alerts are posted to both
			
			// Access the latest trade text
			splitHTML = tradeArray[numTradeAlerts].innerHTML.split("<a"); 
			tradeUpdate = splitHTML[0];
			console.log(tradeUpdate);

			// Send message to the background with the data
			chrome.runtime.sendMessage({msg:trader,text:tradeUpdate},function(response){});
		}
		// Is this an 'Added' comment
		else if(tradeArray[numTradeAlerts].innerHTML.split("<a")[0].indexOf("Added") >= 0)
		{
			console.log("Added comment! Time: " + new Date());

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
		if(debug == true)
		{
			console.log("numTradeAlerts = ", numTradeAlerts);
			console.log("numComments = ", numComments);

			console.log("The first trade alert is: ", tradeArray[0].innerHTML.split("<a")[0]);
			console.log("The first comment is: ", tradeArray[numTradeAlerts].innerHTML.split("<a")[0]);
		}
	}*/

	tenthSeconds++;
	setTimeout(go, numMilliseconds);
	
/*	var connected = $(connectedClassName)[0].innerHTML;
	
	
	console.log($(connectedClassName)[0]);
	console.log(connected);

	// Monitor the page's connection
	if(connected.search("NOT CONNECTED") != -1)
	{
		chrome.runtime.sendMessage({msg:"Refresh",text:tradeUpdate},function(response){});
	}
*/
}
go()