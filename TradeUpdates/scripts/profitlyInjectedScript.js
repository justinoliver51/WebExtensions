// Update the log with the desired settings
//alert('injected');

var initializedJquery = false;
var url = 'http://localhost:8080/IBTradingServer/RemoteProcedureCallsServlet?';
var logFlag = false;

// MAIN
startLoop();

// 
function example() 
{
	alert('injected');
}

function newConsoleLog()
{
    var old_console_log = console.log;
    var logged = [];
    console.log = function() 
    {
    	
    	var args = Array.prototype.slice.call(arguments);
    	
        // Determine if we need to start a console group
    	if( (args[0].indexOf('pre goToLastMessage') >= 0)
    			|| (args[0].indexOf('chat history') >= 0)
    			|| (args[0].indexOf('userlist')  >= 0) 
    			|| (args[0].indexOf('_chatClicked')  >= 0)
    			|| ((args[0].indexOf('_clientJoin') >= 0)
    				&& (args[0].indexOf('chat: true') >= 0))
    		)
    	{
    		if(logFlag == false)
    			console.groupCollapsed('Useless Comments');
    		
    		logFlag = true;
    	}
    	else 
    	{
    		if(logFlag == true)
    			console.groupEnd();
    		
    		logFlag = false;
    	}
        
        //passNotification(args);
        
        args.unshift('[' + new Date() + '] ');
        logged.push(args);
        old_console_log.apply(console, args);
    }
}

function passNotification(notification)
{
	if( (notification.indexOf('Notification') >= 0) && (initializedJquery == true) )
	{
		sendTradeAlert('timothysykes', notification)
	}
}

//
function startLoop() 
{
	var jq = document.createElement('script');
    jq.src = '//ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js';
    document.getElementsByTagName('head')[0].appendChild(jq);
    newConsoleLog();
    setTimeout(testURL, 3000);
    
    //setTimeout(loop, 2000);
}

function testURL()
{
	var params = 
	{
		startUp: 'Start!',
	};
	
	encodedURL = url + $.param(params);
	
    $.getJSON( encodedURL ,function(data)
    	    {
    			console.log(data);
    			console.log('Time received response from server: ' + new Date());
    		});
}

/*
//Element class names
var commentsClassName = '.comments';
var newsClassName = '.newsletter';
var connectedClassName = '.green';
var chatGuruClassName = '.chatWidgetText chatWidgetComm'; //class='chatWidgetText chatWidgetGuru'
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

// Main function
function loop () {
	initializedJquery = true;

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
		lastComment = tradeArray[numTradeAlerts].innerHTML.split('<a')[0];
		lastTradeAlert = tradeArray[0].innerHTML.split('<a')[0];
		console.log(lastComment);

		if(initiateTrade == true)
		{
			numItems = 0; // Instigating an 'Added'
			lastTradeAlert = ''; //Instigating a 'Bought'
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
			//console.log('New chat at time: ' + new Date());
	
			
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
                
                if(counter == 1)
                	username = newChat.innerText;
                else if(counter == 2)
                	chatText = newChat.innerText;
			}
			
			// If this was a useful chat, log it and send it to the server
            if( (username == 'timothysykes') || (username == 'super_trades') )
            {
            	console.log(username + ' ' + chatText + ' at time: ' + new Date());
            	chrome.runtime.sendMessage({msg:username,text:chatText},function(response){});
            }
            
            // Debug
            if(username == 'justinoliver51')
            	console.log(username + ' ' + chatText + ' at time: ' + new Date());
			
			// Update the length
			numChats = chatRoomArray.length;
			//console.log(chatRoom.outerText + ' at time: ' + new Date());
		}
	}

	// TRADE ALERTS
	// If there has been a new trade alert or comment
	if(tradeArray.length > numItems)
	{
		// local variables
		var tradeUpdate;
		var trader = null;

		// Set the trader
		trader = newsArray[0].innerHTML.split('<a')[0];

		// Update the number of items
		numItems = tradeArray.length;

		// Is this a new trade
		if(lastTradeAlert != tradeArray[0].innerHTML.split('<a')[0])
		{
			console.log('New trade alert! Time: ' + new Date());

			// Update the global variables
			lastTradeAlert = tradeArray[0].innerHTML.split('<a')[0]; // Grabs the newest alert
			numTradeAlerts = numTradeAlerts + 1;
			numComments = numItems - numTradeAlerts; // Some alerts are posted to both
			
			// Access the latest trade text
			splitHTML = tradeArray[numTradeAlerts].innerHTML.split('<a'); 
			tradeUpdate = splitHTML[0];
			console.log(tradeUpdate);

			// Send message to the background with the data
			//chrome.runtime.sendMessage({msg:trader,text:tradeUpdate},function(response){});
		}
		// Is this an 'Added' comment
		else if(tradeArray[numTradeAlerts].innerHTML.split('<a')[0].indexOf('Added') >= 0)
		{
			console.log('Added comment! Time: ' + new Date());

			// Access the latest comment text
			splitHTML = tradeArray[numTradeAlerts].innerHTML.split('<a');
			tradeUpdate = splitHTML[0];
			console.log(tradeUpdate);

			//
			lastComment = tradeUpdate;
			numComments = numComments + 1;

			// Send message to the background with the data
			//chrome.runtime.sendMessage({msg:trader,text:tradeUpdate},function(response){});
		}
		// This is nothing
		else
		{
			console.log('False alarm!');
			numComments = numComments + 1;
		}

		// Print useful debug information
		if(debug == true)
		{
			console.log('numTradeAlerts = ', numTradeAlerts);
			console.log('numComments = ', numComments);

			console.log('The first trade alert is: ', tradeArray[0].innerHTML.split('<a')[0]);
			console.log('The first comment is: ', tradeArray[numTradeAlerts].innerHTML.split('<a')[0]);
		}
	}

	tenthSeconds++;
	setTimeout(go, numMilliseconds);

}*/

function sendTradeAlert (traderID, newTrade) 
{
	console.log('Time sent to server: ' + new Date());
			
	var encodedURL;
	var params = {
					traderID: request.msg,
					newTrade: request.text
				 };
			
	encodedURL = url + $.param(params);
	console.log(encodedURL); 

	// Make the call to the server
	$.getJSON( encodedURL ,function(data)
	{
		console.log(data);
		console.log('Time received response from server: ' + new Date());
	});
}