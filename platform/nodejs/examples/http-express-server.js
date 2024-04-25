const express = require('express');
const http = require('http');

const app = express();

const ENTRY_DATA = [];

// Incoming scan data handler
function dataAction(data) {
    // Take any action with the received Tag data
    console.log(data);
    ENTRY_DATA.push(data);
}

/* Request handler: 
        Http request that comes from the UHF desktop app via socket
        will be handled by this function 
*/

function httpStringHandler(req, res) {
    // Retrieve query parameters
    const jsonParam = req.query.json;

    // Parse the JSON parameter
    let jsonData;
    try {
        jsonData = JSON.parse(jsonParam);
    } catch (error) {
        console.error('Error parsing JSON parameter:', error);
        return res.status(400).send('Bad Request');
    }

    // Accessing individual properties
    const antValue = jsonData.Ant;
    const rssiValue = jsonData.RSSI;
    const countValue = jsonData.Count;
    const epcLengthValue = jsonData.EPCLength;
    const epcValue = jsonData.EPC;
    const timestampValue = jsonData.Timestamp;

    let data = {
        antValue,
        rssiValue,
        countValue,
        epcLengthValue,
        epcValue,
        timestampValue,
    };

    // Take your own action here. [e.g: querying a database or sending a notification]
    dataAction(data);

    // Sending a response
    res.send('Request handled successfully');
}

// Attach the request handler to both HTTP and HTTPS servers
app.get('*', httpStringHandler);

// Create an HTTP server and an HTTPS server
const httpServer = http.createServer(app);

// Listen on ports 3000 HTTP
const HTTP_PORT = 3000;

httpServer.listen(HTTP_PORT, () => {
    console.log(`HTTP server is running on port ${HTTP_PORT}`);
});
