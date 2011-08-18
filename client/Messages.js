
// Сообщения дла взаимодействия с сервером.

/**
 * Сообщение входа в игру игрока.
 */
function LoginMessage(login, password) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "LOGIN";

    /**
     * Логин.
     */
    this.login = login;

    /**
     * Пароль.
     */
    this.password = password;

    /**
     * Преобразование в JSON. Временно.
     */
    this.toJSON = function() {
        return JSON.stringify(new Array(this.type, {login:this.login, password:this.password}))
    }

    return this;
}

/**
 * Сообщение о выходе игрока из игры.
 */
function LogoutMessage() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "LOGOUT";

    /**
     * Преобразование в JSON. Временно.
     */
    this.toJSON = function() {
        return JSON.stringify(new Array(this.type))
    }

    return this;
}

/**
 * Сообщение о выходе игрока из игры.
 */
function NewchatMessage(message) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "NEWCHAT";

    /**
     * Сообщение
     */
    this.message = message;

    /**
     * Преобразование в JSON. Временно.
     */
    this.toJSON = function() {
        return JSON.stringify(new Array(this.type, {message:this.message}))
    }

    return this;
}

// Обработчики сообщений.
var MESSAGE_HANDLERS = {
    "HELLO": function(msg) {
        // ["HELLO",{"id":"идентификатор игрока","name":"имя игрока","servertime":"100500"}]
        GAMETIME.set(msg[1].servertime)

        if (self_player != undefined) {
            console.log("Клиент уже вошел в игру. Сообщение будет проигнорировано.")
            return
        }

        arguments.callee.selfPlayerId = msg[1].id
    },
    "WRONGPASS": function(msg) {
        // ["WRONGPASS"]
        alert("Неверный пароль для пользователя " + getLogin())
    },
    "NEWPLAYER": function(msg) {
        // ["NEWPLAYER",{"id":"идентификатор игрока","name":"имя игрока","j":"индекс","i":"индекс","speed":"3", "spriteCategory":"player"}]
        var p = new Player(msg[1].id, msg[1].j, msg[1].i, msg[1].name,
                           new SpriteLoader(SPRITES), msg[1].speed)
        if (MESSAGE_HANDLERS["HELLO"].selfPlayerId == msg[1].id) {
            if (self_player != undefined) {
                console.log("Свой игрок уже создан. Сообщение будет проигнорировано.")
            } else {
                self_player = p
                map = new Map(100, 100, self_player, new SpriteLoader(SPRITES))
                camera = new Camera(self_player)

                // Запускаем главный цикл.
                setTimeout("mainLoop();", 1000 / FPS)

                hideLoginForm()
                showChat()
                scr.addEventListener("click", scrOnClick, false)
            }

            return
        }

        players_list.push(p)
    },
    "DELPLAYER": function(msg) {
        // ["DELPLAYER",{"id":"идентификатор игрока"}]
        if (self_player.id == msg[1].id) {
            // TODO отключение игрока от сервера
            return
        }

        for (var i in players_list) {
            if (players_list[i].id == msg[1].id) {
                players_list.splice(i, 1)
                break
            }
        }
    },
    "CHAT": function(msg) {
        // ["CHAT,{"name":"отправитель","time":"время","type":"1","message":"сообщение"}]
        var c
        
        switch (msg[1].type) {
            case 0:
                c = private_chat
                break
            case 1:
                c = common_chat
                break
            default:
                return
        }
        
        if (c.length > 100) {
            c.splice(0, c.length - 100)
        }
        c.push({name:msg[1].name, message:msg[1].message})

        if (cur_chat_type == msg[1].type) {
            var t = document.getElementById("chat_field")
            var input = document.getElementById("chat_input")
            t.value += msg[1].name + ": " + msg[1].message + "\n"
            setCaretToPos(t, t.value.length)
            setCaretToPos(input, input.value.length)
        }
    },
    "ERROR": function(msg) {
        // ["ERROR",{"id":"идентификатор ошибки","description":"описание","additionalDescription":"ещё описание"}]
        alert("Error.\nid: " + msg[1].id +
              "\ndescription: " + msg[1].description +
              "\nmore info: " + msg[1].additionalDescription)
    }
};
