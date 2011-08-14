
// Сообщения дла взаимодействия с сервером.
// require Player.js, Constants.js, SpriteLoader.js, resources.js

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

    return this;
}

/**
 * Сообщение об успешном входе в игру.
 */
function HelloMessage(id, name) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "HELLO";

    /**
     * Id игрока.
     */
    this.id = id;

    /**
     * Имя игрока.
     */
    this.name = name;

    return this;
}

// Обработчики сообщений.
var MESSAGE_HANDLERS = {
    "LOGIN": function(msg) {},
    "HELLO": function(msg) {
        self_player = new Player(
            SCR_W / 2,
            SCR_H / 2,
            5,
            5,
            msg.name,
            new SpriteLoader(SPRITES)
        );
        map = new Map(100, 100, self_player, new SpriteLoader(SPRITES));
    }
};
