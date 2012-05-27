
// Сообщения дла взаимодействия с сервером.

// <- ["CONFIRM", step]
// <- ["STATE", step, [["UNIT", ownerId, id, j, i], ["UNIT", ownerId, id, j, i], ...], ["MOVE", unitId, j, i], ["MOVE", unitId, j, i], ...]
// -> ["STATE", step, [["UNIT", ownerId, id, j, i], ["UNIT", ownerId, id, j, i], ...], ["MOVE", unitId, j, i], ["MOVE", unitId, j, i], ...]

// ["UNIT", ownerId, id, j, i]
// ["MOVE", unitId, j, i]

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

/**
 * Требование хода.
 */
function MoveToMessage(j, i) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "MOVETO";

    /**
     * j-индекс клетки назначения.
     */
    this.j = j;

    /**
     * i-индекс клетки назначения.
     */
    this.i = i;

    /**
     * Преобразование в JSON.
     */
    this.toJSON = function() {
        return JSON.stringify(new Array(this.type, {x:this.j,y:this.i}))
    }

    return this;
}

/**
 * Требование хода.
 */
function MoveMessage2() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "MOVE";

    /**
     * j-индекс клетки назначения.
     */
    this.j = Math.floor(Math.random() * 100);

    /**
     * i-индекс клетки назначения.
     */
    this.i = Math.floor(Math.random() * 100);

    /**
     * Преобразование в JSON.
     */
    this.toJSON = function() {
        return JSON.stringify(new Array(this.type, {j:this.j,i:this.i}))
    }

    return this;
}

/**
 * Запрос для синхронизации игрового времени.
 */
function PingMessage() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!")
    }

    /**
     * Тип сообщения.
     */
    this.type = "PING"

    /**
     * Преобразование в JSON.
     */
    this.toJSON = function() {
        return JSON.stringify(new Array(this.type))
    }

    return this
}

/**
 * Отчёт о состоянии клиента.
 */
function StateReportMessage(step, units, actions) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Номер шага.
     */
    this.step = step;

    /**
     * Список юнитов.
     */
    this.units = units;

    /**
     * Список действий.
     */
    this.actions = actions;

    /**
     * Преобразование в JSON.
     */
    this.toJSON = function() {
        return "[\"STATE\", " + this.step + ", " + this.units + ", " + this.actions + "]";
    }
}

/**
 * Сообщение с состоянием юнита.
 */
function UnitMessage(ownerId, id, j, i) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "UNIT";

    /**
     * Идентификатор владельца (клиента) юнита.
     */
    this.ownerId = ownerId;

    /**
     * Идентификатор юнита.
     */
    this.id = id;

    /**
     * Номер клетки на карте по ширине, где находится юнит.
     */
    this.j = j;

    /**
     * Номер клетки на карте по высоте, где находится юнит.
     */
    this.i = i;
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
        console.warn("Устаревший тип сообщения: NEWPLAYER.")
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
            t.value += msg[1].name + ": " + msg[1].message + "\n"
            t.scrollTop = t.scrollHeight;
        }
    },
    "ERROR": function(msg) {
        // ["ERROR",{"id":"идентификатор ошибки","description":"описание","additionalDescription":"ещё описание"}]
        alert("Error.\nid: " + msg[1].id +
              "\ndescription: " + msg[1].description +
              "\nmore info: " + msg[1].additionalDescription)
    },
    "UNITSTATUS": function(msg) {
        // ["UNITSTATUS",{"type":"PLAYER","unit":{"id":"35","mapid":"1","x":"5","y":"1","name":"test","spriteCategory":"player","speed":"13"}}]

        switch (msg[1].type) {
            case "PLAYER":
                UNITSTATUS_PLAYER(msg[1].unit)
                break
            case "NPC":
                UNITSTATUS_NPC(msg[1].unit)
                break
            default:
                console.log("Неизвестный тип юнита " + msg[1].type)
                break
        }
    },
    "UNITUPD": function(msg) {
        // ["UNITUPD",{"type":"PLAYER","unit":{"id":"35","spriteCategory":"player2","speed":"6"}}]

        switch (msg[1].type) {
            case "PLAYER":
                UNITUPD_PLAYER(msg[1].unit)
                break
            case "NPC":
                UNITUPD_NPC(msg[1].unit)
                break
            default:
                console.log("Неизвестный тип юнита " + msg[1].type)
                break
        }
    },
    "UNITSLIST": function(msg) {
        // ["UNITSLIST",{"units":[{"type":"PLAYER","unit":{"name":"test","id":"35","speed":"13","mapid":"1","x":"5","y":"1","spriteCategory":"player"}},{"type":"NPC","unit":{"id":"669","speed":"5","mapid":"1","x":"55","y":"152","spriteCategory":"woolf"}}]}]

        for (var i in msg[1].units) {
            switch (msg[1].units[i].type) {
                case "PLAYER":
                    UNITSTATUS_PLAYER(msg[1].units[i].unit)
                    break
                case "NPC":
                    UNITSTATUS_NPC(msg[1].units[i].unit)
                    break
                default:
                    console.log("Неизвестный тип юнита " + msg[1].units[i].type)
                    break
            }
        }
    },
    "DELUNIT": function(msg) {
        // ["DELUNIT",{"type":"PLAYER","id":"идентификатор игрока"}]

        DELUNIT_PLAYER(msg[1].id)
        DELUNIT_NPC(msg[1].id)
    },
    "MOVE": function(msg) {
        // ["MOVE",{"id":id_юнита,"time":"время начала движения",way:[{"x":x1,"y":y1},{"x":x2,"y":y2},...,{"x":xn,"y":yn}]}]
        var data = msg[1]
        var path = new Array()
        var i
        var player

        if (data.way.length < 2) {
            console.log("Неверный путь движения:", data.way)
            return
        }

        // Считаем путь.
        for (i in data.way) {
            path.push(new Array(data.way[i].y, data.way[i].x))
        }

        // Пока только для игроков.

        if (data.id == self_player.id) {
            player = self_player
        } else {
            for (i in players_list) {
                if (data.id == players_list[i].id) {
                    player = players_list[i]
                }
            }
        }

        if (player.i != data.way[0].y || player.j != data.way[0].x) {
            console.log("ВНИМАНИЕ:", "Обнаружена разсинхронизация клиента и сервера. Игрок должен начать движение не из той клетки, в которой находится.")
            path.unshift(new Array(player.i, player.j))
            data.time -= (1 / player.speed) * 1000
        }

        player.move(path, data.time)
    },
    "PONG": function(msg) {
        // ["PONG",{"time":1050}]
        var netDiff = ((new Date()).getTime() - timeSync.requestTime) / 2
        GAMETIME.set(msg[1].time + Math.ceil(netDiff))
    },
    "WELCOME": function (msg) {
        // ["WELCOME", clientId]
        clientId = msg[1];
        setTimeout("syncLoop();", 100);
        scr.addEventListener("click", scrOnClick, false);
        hideLoginForm();
    },
    "CONFIRM": function (msg) {
        // ["CONFIRM", step]
        if (step == msg[1]) {
            awaiting_confirm = false;
            setTimeout("syncLoop();", 0);
        } else {
            console.log("Cant wake up. Step", msg[1], "confirmed but we are waiting for confirmation of step", step);
        }
    },
    "STATE": function (msg) {
        // ["STATE", step, [{"type":"UNIT","ownerId":1,"id":2,"j":4,"i":4},{"type":"UNIT","ownerId":1,"id":3,"j":5,"i":5}], ["MOVE", unitId, j, i], ["MOVE", unitId, j, i], ...]
        self_units_list = new Array();
        units_list = new Array();
        actions = new Array();
        
        step = msg[1];
        var units = msg[2];
        var cur_units_list;
            
        for (var i in units) {
            if (clientId == units[i].ownerId) {
                cur_units_list = self_units_list;
            } else {
                cur_units_list = units_list;
            }
            cur_units_list.push(new Unit(units[i].ownerId, units[i].id, units[i].j, units[i].i));
        }
        
        // Пока пропустим действия.
    }
}

function UNITSTATUS_PLAYER(player) {
    var i, chat_field

    if (self_player == undefined) {

        // Создадим своего игрока.
        if (MESSAGE_HANDLERS["HELLO"].selfPlayerId == player.id) {
            self_player = new Player(
                player.id,
                player.mapId,
                player.x,
                player.y,
                player.name,
                new SpriteLoader(SPRITES),
                player.speed
            )
            camera = new Camera(self_player)
            for (i in maps) {
                maps[i].player = self_player
                maps[i].camera = camera
            }

            // Запускаем синхронизацию игрового времени.
            timeSync.run(5000)

            // Запускаем главный цикл.
            setTimeout("mainLoop();", 1000 / FPS)

            hideLoginForm()
            showChat()
            scr.addEventListener("click", scrOnClick, false)
        } else {
            console.log("Свой игрок уже создан. Сообщение будет "
                        + "проигнорировано.")
        }
        return
    } else if (self_player.id == player.id) {

        // Изменим атрибуты своего игрока.
        self_player.j = player.x
        self_player.i = player.y
        self_player.name = player.name
        //players_list[i].spriteCategory = player.spriteCategory
        self_player.speed = player.speed
        return
    }

    for (i in players_list) {

        // Изменим атрибуты существующего игрока.
        if (players_list[i].id == player.id) {

            players_list[i].j = player.x
            players_list[i].i = player.y
            players_list[i].name = player.name
            //players_list[i].spriteCategory = player.spriteCategory
            players_list[i].speed = player.speed
            return
        }
    }

    // Если игрок не найден, то добавим нового игрока.
    players_list.push(new Player(
        player.id,
        player.mapId,
        player.x,
        player.y,
        player.name,
        new SpriteLoader(SPRITES),
        player.speed
    ))
    chat_field = document.getElementById("chat_field")
    chat_field.value += "Player " + player.name + " logged in.\n"
    chat_field.scrollTop = chat_field.scrollHeight
}
function UNITSTATUS_NPC(npc) {
    for (var i in npc_list) {

        // Изменим атрибуты существующего NPC
        if (npc_list[i].id == npc.id) {

            npc_list[i].j = npc.x
            npc_list[i].i = npc.y
            npc_list[i].name = npc.name
            //npc_list[i].spriteCategory = npc.spriteCategory
            npc_list[i].speed = npc.speed
            return
        }
    }

    // Если NPC не найден, то добавим нового NPC.
    npc_list.push(new NPC(
        npc.id,
        npc.x,
        npc.y,
        npc.name,
        new SpriteLoader(SPRITES),
        npc.speed
    ))
}

function UNITUPD_PLAYER(player) {
    for (var i in players_list) {

        // Изменим атрибуты существующего NPC
        if (players_list[i].id == player.id) {

            //players_list[i].spriteCategory = player.spriteCategory
            players_list[i].speed = player.speed
            return
        }
    }
}
function UNITUPD_NPC(npc) {
    for (var i in npc_list) {

        // Изменим атрибуты существующего NPC
        if (npc_list[i].id == npc.id) {

            //npc_list[i].spriteCategory = npc.spriteCategory
            npc_list[i].speed = npc.speed
            return
        }
    }
}

function DELUNIT_PLAYER(id) {
    var chat_field
    
    if (self_player.id == id) {
        // TODO отключение игрока от сервера
        return
    }

    for (var i in players_list) {
        if (players_list[i].id == id) {
            chat_field = document.getElementById("chat_field")
            chat_field.value += "Player " + players_list[i].name + " logged out.\n"
            chat_field.scrollTop = chat_field.scrollHeight
            players_list.splice(i, 1)
            break
        }
    }
}
function DELUNIT_NPC(id) {
    for (var i in npc_list) {
        if (npc_list[i].id == id) {
            npc_list.splice(i, 1)
            break
        }
    }
}
