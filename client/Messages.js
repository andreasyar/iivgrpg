
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
        
        player.move(path, data.time)
    }
}

function UNITSTATUS_PLAYER(player) {
    var i, j, k, o, tl, sloader, mapw, maph, grnd, objs

    if (self_player == undefined) {

        // Создадим своего игрока.
        if (MESSAGE_HANDLERS["HELLO"].selfPlayerId == player.id) {
            self_player = new Player(
                player.id,
                player.x,
                player.y,
                player.name,
                new SpriteLoader(SPRITES),
                player.speed
            )
            loadMapObjects()
            loadGrounds()
            loadMap(player.mapId)
            maph = MAPS[player.mapId].tiles.length
            mapw = MAPS[player.mapId].tiles[0].length
            map = new Map(
                mapw,
                maph,
                self_player,
                new SpriteLoader(SPRITES)
            )
            camera = new Camera(self_player)

            grnd = MAPS[player.mapId].tiles
            objs = MAPS[player.mapId].objects
            sloader = new SpriteLoader(SPRITES)
            spriteMap = new Array(maph)
            accessMap = new Array(maph)
            eventMap = new Array(maph)
            for (i = 0; i < maph; i++) {
                spriteMap[i] = new Array(mapw)
                accessMap[i] = new Array(mapw)
                eventMap[i] = new Array(mapw)
                for (j = 0; j < mapw; j++) {
                    spriteMap[i][j] = {ground: sloader.getSprite(GROUNDS[grnd[i][j]].sprId), objs: new Array()}
                    accessMap[i][j] = parseInt(GROUNDS[grnd[i][j]].acc)
                }
            }
            for (k in objs) {
                o = objs[k]
                tl = MAPOBJECTS[o.type].tiles
                for (i in tl) {
                    i = parseInt(i)
                    for (j in tl[i]) {
                        j = parseInt(j)
                        if (tl[i][j] != null) {
                            spriteMap[o.y + i][o.x + j].objs.push(sloader.getSprite(tl[i][j].sprId))
                            accessMap[o.y + i][o.x + j] = parseInt(tl[i][j].acc)
                            if (tl[i][j].acc == 2) {
                                var id = o.objid
                                eventMap[o.y + i][o.x + j] = function() { return function(id) { console.log("Entering", id) }(id) }
                            }
                        }
                    }
                }
            }

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
        player.x,
        player.y,
        player.name,
        new SpriteLoader(SPRITES),
        player.speed
    ))
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
    if (self_player.id == id) {
        // TODO отключение игрока от сервера
        return
    }

    for (var i in players_list) {
        if (players_list[i].id == id) {
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
