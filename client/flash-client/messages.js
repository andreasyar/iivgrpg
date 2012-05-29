
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
     * Преобразование в JSON.
     */
    this.toJSON = function() {
        return new Array(this.type, {login:this.login, password:this.password});
    }

    return this;
}

/**
 * Пакет сообщений. Обёртка для одновременной отправки массива сообщений.
 * @param messages Массив сообщений.
 */
function MessagePacket(messages) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Массив сообщений.
     */
    this.messages = messages;

    /**
     * Преобразование в JSON.
     */
    this.toJSON = function() {
        return new Array(this.messages);
    }

    return this;
}
