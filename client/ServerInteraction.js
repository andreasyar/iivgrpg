
// Взаимодействите с сервером.

// Отправка и приём сообщений с сервера.
function ServerInteraction() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    // Веб сокет для обмена сообщениями.
    this.socket = null;

    // Очередь сообщений для отправки.
    this.msgQueue = new Array();

    // Соединяется с серевером.
    this.connect = function(addr) {
        console.log('Connect to ' + addr);

        // Если веб сокеты поддерживаются.
        if (window.WebSocket) {
            this.socket = new WebSocket(addr);
            this.socket.parent = this;

            this.socket.onmessage = function(event) {
                document.getElementById('messages').innerHTML = event.data;
                console.log("<- " + event.data);

                var msg = JSON.parse(event.data);
                for (var key in MESSAGE_HANDLERS) {
                    if (msg.type == key) {
                        MESSAGE_HANDLERS[key](msg);
                        return;
                    }
                }
                console.log('Wrong message type ' + msg.type);
            };

            this.socket.onopen = function(event) {
                console.log("Web Socket opened!");

                // Отправим сообщения, которые были положены в очередь во время
                // установления соединения и затем очистим очередь.
                for (var i = 0; i < this.parent.msgQueue.length; i++) {
                    console.log("-> " + this.parent.msgQueue[i].type);
                    this.parent.sendMessage(this.parent.msgQueue[i]);
                }
                this.parent.msgQueue = new Array();
            };

            this.socket.onclose = function(event) {
                console.log("Web Socket closed.");
            };
            
            return true;
        } else {
            alert("Your browser does not support Web Socket.");
            return false;
        }
    };

    // Отправляет сообщение.
    this.sendMessage = function(obj) {

        // Если веб сокеты поддерживаются.
        if (window.WebSocket) {
            if (this.socket == null
                    || this.socket.readyState != WebSocket.OPEN) {

                // Если соединение ещё не установлено, то поставим сообщение в
                // очередь.
                this.msgQueue.push(obj);
            } else {
                this.socket.send(JSON.stringify(obj));
            }
            return true;
        } else {
            alert("Your browser does not support Web Socket.");
            return false;
        }
    };
}
