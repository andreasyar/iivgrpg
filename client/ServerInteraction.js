
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

        // Если веб сокеты поддерживаются.
        if (window.WebSocket) {
            this.socket = new WebSocket(addr);
            this.socket.onmessage = function(event) {
                //alert(event.data);
                document.getElementById('messages').innerHTML = event.data;

                var msg = JSON.parse(event.data);
                for (var key in MESSAGE_HANDLERS) {
                    if (msg.type == key) {
                        MESSAGE_HANDLERS[key](msg);
                        return;
                    }
                }
                alert('Wrong message type ' + msg.type);
            };
            this.socket.onopen = function(event) {
                //alert("Web Socket opened!");

                // Отправим сообщения, которые были положены в очередь во время
                // установления соединения и затем очистим очередь.
                for (var i = 0; i < this.msgQueue.length; i++) {
                    this.sendMessage(this.msgQueue[i]);
                }
                this.msgQueue = new Array();
            };
            this.socket.onclose = function(event) {
                alert("Web Socket closed.");
            };
            return true;
        } else {
            alert("Your browser does not support Web Socket.");
            return false;
        }
    };

<<<<<<< .mine
    // Отправляет сообщение.
    this.sendMessage = function(obj) {
=======
        // Обработчик получения сообщения от сервера.
        this.socket.onmessage = function(event) {
            console.log("<- " + event.data)
            var obj = JSON.parse(event.data)
            for (var key in MESSAGE_HANDLERS) {
                if (obj[0] != undefined && obj[0] == key) {
                    MESSAGE_HANDLERS[key](obj)
                    break
                }
            }
        }
>>>>>>> .r100

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
