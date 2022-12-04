var ws = null;
var ws = null;
var playerId = null;

function setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
}

function setGameOptionsEnabled(enabled) {
    //document.getElementById('stay').disabled = !enabled;
    //document.getElementById('hit').disabled = !enabled;
    //document.getElementById('split').disabled = !enabled;
}

function setAdmin(enabled) {
    document.getElementById('open').disabled = !enabled;
    document.getElementById('shutdown').disabled = !enabled;
    document.getElementById('numberPlayers').disabled = !enabled;
}

function enableStart(enabled) {
    //document.getElementById('start').disabled = !enabled;
}

function setUID(uid) {
    if (uid != null) {
        var stripped = uid.replace(/\./g, ' ');
        document.getElementById('consoleText').innerHTML = 'Console (UID: ' + stripped + ')';
        playerId = uid;
    } else {
        document.getElementById('consoleText').innerHTML = 'Console';
        playerId = '';
    }
}
/**
 * Connect to the server.
 */
function connect() {
    // hardcoded endpoint, oh no!
    ws = new SockJS('/game');
    ws.onopen = function () {
        setConnected(true);
        clientLog('Connection opened.');
    };
    ws.onmessage = function (event) {
        dispatch(event.data);
    };
    ws.onclose = function () {
        setUID();
        clientLog('Connection closed.');
        disconnect();
    };
}

/**
 * Disconnect from the server.
 */
function disconnect() {

    // Before we disconnect let them know we're leaving if its our turn
    if (document.getElementById('stay').disabled === false) {
        ws.send('LEAVING');
    }

    if (ws != null) {
        ws.close();
        ws = null;
    }
    setConnected(false);
    setGameOptionsEnabled(false);
    resetYourText();
    resetDealerText();
    resetOtherText();
    setAdmin(false);
    //enableStart(false);
    removeCards();
}

/**
 * Determine what to do with the message.
 *
 * @param message the message.
 */
function dispatch(message) {
    // split message into three: [SENDER, KEY, PAYLOAD]
    var split = message.split('|');
    var logMessage = split[0].concat(split[2]);
    console.log(split);
    switch (split[1]) {
        case 'OTHER+DISCONNECTED':
        case 'OTHER+READY+TO+START':
        case 'OTHER+CONNECTED':
        case 'CONNECTED':
            log(logMessage);
            var connectedMessage = logMessage.split(' ');
            var last = connectedMessage[connectedMessage.length - 1];
            if (split[1] === 'CONNECTED') {
                setUID(last);
            }
            break;
        case 'NOT+ACCEPTING':
            log(logMessage);
            //disconnect(); for now done by the server...this is the work around
            break;
        case 'ADMIN':
            log(logMessage);
            setAdmin(true);
            enableStart(false);
            break;
        case 'GAME+START':
            log(logMessage);
            break;

/**
 * Send option chosen back.
 */
function game_option(option) {
    ws.send('GAME_'.concat(option));
    console.log('Sent ' + option);
    clientLog('You decided to ' + option + '. Sending to server - please wait for your next turn.');
    setGameOptionsEnabled(false);
}

/**
 * Send the start message.
 */
function start() {
    ws.send('START_GAME');
    //enableStart(false);
    removeCards();
}

/**
 * Open connections for other players.
 */
function acceptOthers() {
    if (ws != null) {
        var numP = document.getElementById('numberPlayers').value;

        clientLog('Opening the lobby with specified settings. When the correct number of players have connected, the start button will become available.');
        var send = 'ACCEPT|' + numP;
        ws.send(send);
        document.getElementById('open').disabled = true;
        document.getElementById('numberPlayers').disabled = true;

    } else {
        alert('Connection not established, please connect.');
    }
}

//GETTING CARD INDEX SPECIFIC IN A PLAYERS HAND
function removeAndPlayCard() {

    var numP = document.getElementById('card-to-play').value;

    clientLog('You are playing a card: ' + numP);
    var send = 'PLAY|' + numP;
    ws.send(send);

}

/**
 * Log from the client.
 * @param message the message.
 */
function clientLog(message) {
    var pad = '00';
    var date = new Date();
    var hour = "" + date.getHours();
    var hourPad = pad.substring(0, pad.length - hour.length) + hour;
    var min = "" + date.getMinutes();
    var minPad = pad.substring(0, pad.length - min.length) + min;
    var hourMin = hourPad + ':' + minPad;
    var prefix = '<strong>' + hourMin + ' Client' + '</strong>: ';
    log(prefix + message);
}

/**
 * Log to the console
 * @param message the message.
 */
function log(message) {
    var console = document.getElementById('console');
    var p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.innerHTML = message;

    console.appendChild(p);
    while (console.childNodes.length > 25) {
        console.removeChild(console.firstChild);
    }
    console.scrollTop = console.scrollHeight;
}


function shutdown() {
    clientLog("Drawing a card...");
    var send = 'DRAW';
    ws.send(send);
}
