const http = require('http');
const url = require('url');

const PORT = 3000;
let ENTRY_DATA = [];

// Incoming scan data handler
function dataAction(data) {
    // take any action with the received Tag data
    ENTRY_DATA.push(data);
}

/* Request handler: 
        Http request that comes from the UHF desktop app via socket
        will be handled by this function 
*/

function httpStringHandler(req, res) {
    const parsedUrl = url.parse(req.url, true);
    const jsonParam = parsedUrl.query.json;

    let jsonData;
    try {
        jsonData = JSON.parse(jsonParam);
    } catch (error) {
        console.error('Error parsing JSON parameter:', error);
        res.writeHead(400, { 'Content-Type': 'text/plain' });
        res.end('Bad Request');
        return;
    }

    let data = {
        EPC: jsonData.EPC, // EPC Data of the tag
        EPCLength: jsonData.EPCLength, // EPC lenght of the tag
        Ant: jsonData.Ant, // used antenna
        RSSI: jsonData.RSSI, // signal strength of the tags response
        Count: jsonData.Count, // Number of times the tag responded in a singla scan
        Timestamp: jsonData.Timestamp, // Time of the scan
    };

    dataAction(data);
    console.log('Data is...', data);

    res.writeHead(200, { 'Content-Type': 'text/plain' });
    res.end('Request handled successfully');
}

const server = http.createServer((req, res) => {
    httpStringHandler(req, res);
});

server.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
