var app = require('http').createServer(handler).listen(8080);
var io = require('socket.io').listen(app);

function handler(req,res){
    console.log(req.url);
    res.writeHead(200, {'Content-Type':'text/plain'});
    res.end('Hello Node\n You are really really awesome!');
}

io.sockets.on('connection', function (socket) {
  socket.on('login', function (info, fn) {
    console.log(info);
		fn('Logged in!');
  });
	
	socket.on('data', function (data, fn) {
    console.log(data);
		fn('Received data!');
  });
});

