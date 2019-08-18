var app = require('express')();
var server = require('http').Server(app);
// 这里服务端用了ws这个库。相比熟悉的socket.io，ws实现更轻量，更适合学习的目的。
var WebSocket = require('ws');

var wss = new WebSocket.Server({ port: 8080 });

wss.on('connection', function connection(ws) {
    console.log('server: receive connection.');
    
    ws.on('message', function incoming(message) {
        console.log('server: received %s', message);
        ws.send('server: reply');
    });

    // 接收方->发送方：pong
    ws.on('pong', () => {
        console.log('server: received pong from client');
    });

    ws.send('world');
    
    // 发送方->接收方：ping
    // setInterval(() => {
    //   ws.ping('', false, true); // 心跳
    // }, 10000);
});

app.get('/', function (req, res) {
  res.sendfile(__dirname + '/index.html');
});

app.listen(3000);
