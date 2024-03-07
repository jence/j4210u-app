const express = require('express');
const https = require('https');
const fs = require('fs');
const path = require('path');
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

// ENTER YOUR OWN SSL certificate here if you wanna use https. Current keys are demo keys and will not work.
const passphrase = '1234';
const sslKeyPath = path.join(__dirname, 'ssl', 'private.key');
const sslCertPath = path.join(__dirname, 'ssl', 'certificate.crt');

// Read SSL certificate and private key
const privateKey = fs.readFileSync(sslKeyPath, 'utf8');
const certificate = fs.readFileSync(sslCertPath, 'utf8');
const credentials = {
    key: privateKey,
    cert: certificate,
    passphrase: passphrase,
};

// Create an HTTP server and an HTTPS server
const httpServer = http.createServer(app);
const httpsServer = https.createServer(credentials, app);

// Listen on ports 3000 and 3443 for HTTP and HTTPS, respectively
const HTTP_PORT = 3000;
const HTTPS_PORT = 3443;

httpServer.listen(HTTP_PORT, () => {
    console.log(`HTTP server is running on port ${HTTP_PORT}`);
});

httpsServer.listen(HTTPS_PORT, () => {
    console.log(`HTTPS server is running on port ${HTTPS_PORT}`);
});
