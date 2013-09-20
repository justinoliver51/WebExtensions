var url = 'http://localhost:8080/IBTradingServer/RemoteProcedureCallsServlet?traderID=robertmaxwell&newTrade=';

// SOCKETS!
var socket = io.connect('http://localhost:8081');
socket.send('New message1');

socket.on('connect', function () { // TIP: you can avoid listening on `connect` and listen on events directly too!
    socket.emit('login', '\nWHOOP!\n', function (data) {
      console.log(data); // data will be 'woot'
    });
});

socket.on('disconnect', function() {
	console.log('disconnected');
});

socket.on('error', function (e) {
	console.log('System', e ? e : 'A unknown error occurred');
});

chrome.runtime.onMessage.addListener(
	function(request,sender,senderResponse)
	{
		// If we've received a trade update, let the server know!
		if(request.msg==="TradeUpdate")
		{
				console.log("Sending data to socket: " + request.text);
				socket.emit('data', request.text, function (data) {
					console.log(data); // data will be 'woot'
				});

				var newURL = url.concat(request.text);
                                $.getJSON( newURL ,function(data){
   		                    console.log(data);
   		                });
		}
	}
);
