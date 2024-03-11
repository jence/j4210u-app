const net = require('net');
const time = require('timers');

const PORT = 3000;
let ENTRY_DATA = [];

// Incoming scan data handler
function dataAction(data) {
    // take any action with the received Tag data
    ENTRY_DATA.push(data);
}

/* Socket data handler: 
        raw json that comes through the socket from the UHF desktop app 
        will be handled by this function 
*/
function sockDataHandler(data) {
    try {
        const jsonData = JSON.parse(data.toString());
        console.log('Received JSON:', jsonData);
        dataAction(jsonData);
    } catch (error) {
        console.error('Error parsing JSON:', error.message);
        return;
    }
}

function sockDisconnectHandler(data) {
    console.log('Client disconnected');
}

const server = net.createServer(socket => {
    console.log('Client connected');

    socket.on('data', sockDataHandler);

    socket.on('end', sockDisconnectHandler);
});

server.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}`);
});
