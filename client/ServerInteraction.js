
// Взаимодействите с сервером.
// require Messages.js

// Отправка и приём сообщений с сервера.
function ServerInteraction() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    // Поддерживаются ли веб сокеты браузером клиента?
    if (!window.WebSocket) {
        throw new Error("Your browser does not support Web Socket!");
    }

    // Веб сокет для обмена сообщениями.
    this.socket = null;

    // Очередь сообщений для отправки.
    this.msgQueue = new Array();

    // Очередь полученных сообщений.
    this.outQueue = new Array();

    // Очередь полученных сообщений.
    this.inQueue = new Array();

    // Соединяется с серевером.
    this.connect = function(addr) {
        console.log('Connect to ' + addr);

        this.socket = new WebSocket(addr);
        this.socket.parent = this;

        // Обработчик получения сообщения от сервера.
        this.socket.onmessage = function(event) {
            var obj = JSON.parse(event.data);
            console.log("<- " + event.data);
            for (var key in MESSAGE_HANDLERS) {
                if (obj[0] != undefined && obj[0] == key) {
                    MESSAGE_HANDLERS[key](obj);
                    break;
                }
            }
        };

        // Обработчик открытия соединения с сервером.
        this.socket.onopen = function(event) {
            console.log("Web Socket opened. Send login message");
            this.parent.sendMessage(new LoginMessage(
                getLogin(),
                getPassword()
            ));
        };

        // Обработчик закрытия соединения с сервером.
        this.socket.onclose = function(event) {
            console.log("Web Socket closed.");
        };
    };

    // Добавляет сообщение в очередь исходящих сообщений.
    this.addMessage = function(obj) {
        this.outQueue.push(obj);
    };

    // Отправляет все сообщения из очереди исходящих сообщений.
    this.sendMessages = function() {
        if (this.socket == null || this.socket.readyState != WebSocket.OPEN) {
            console.log("Cant send messages. Connection not established.");
            return false;
        } else if (this.outQueue.length > 0) {
            var msg, obj;
            while ( (obj = this.outQueue.shift()) != undefined) {
                msg = obj.toJSON()//JSON.stringify(obj);
                console.log("-> " + msg);
                this.socket.send(msg);
            }
        }

        return true;
    }

    // Обрабатывает все сообщения из очереди входящих сообщений.
    this.processMessages = function() {
        if (this.inQueue.length > 0) {
            var msg, obj;
            while ( (msg = this.inQueue.shift()) != undefined) {
                obj = JSON.parse(msg);
                for (var key in MESSAGE_HANDLERS) {
                    if (obj.type != undefined && obj.type == key) {
                        console.log("<- " + msg);
                        MESSAGE_HANDLERS[key](obj);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Отправляет сообщение.
     * @param msg Сообщение.
     */
    this.sendMessage = function(msg) {
        if (this.socket == null || this.socket.readyState != WebSocket.OPEN) {
            throw new Error("Cant send message. Connection not established.")
        } else {
            msg = msg.toJSON()
            console.log("-> " + msg);
            this.socket.send(msg);
        }

        return true;
    }
}
