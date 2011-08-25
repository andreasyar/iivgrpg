
// Камера.

/**
 * Камера. "Показывает" место, которое будет нарисовано на основном холсте.
 */
function Camera(self_player) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!")
    }

    /**
     * Ссылка на своего игрока, чтобы камера всегда могла ориентироваться на
     * него.
     */
    this.self_player = self_player

    /**
     * Проверяет, попадат ли объект с заданными границами в объектив.
     */
    this.isOnView = function(x, y, w, h) {

        // Обновим координаты камеры на карте.
        this.x = this.self_player.x - SCR_W / 2
        this.y = this.self_player.y - SCR_H / 2

        // Объект находится целиком справа или слева за кадром.
        if (x > this.x || x + w < this.x) {
            return false
        }

        // Объект находится целиком снизу или сверху за кадром.
        if (y > this.y || y + h < this.y) {
            return false
        }

        return true
    }

    /**
     * Возвращает координаты внутри кадра по координатам на карте. Этот метод
     * будет вызывать любой объект чтобы узнать координаты где его нужно
     * нарисовать.
     */
    this.translateAxis = function(x, y) {

        // Обновим координаты камеры на карте.
        this.x = this.self_player.x - SCR_W / 2
        this.y = this.self_player.y - SCR_H / 2

        return {x: x - this.x, y: y - this.y}
    }

    /**
     * Возвращает координаты на карте по координатам внутри кадра.
     */
    this.reTranslateAxis = function(x, y) {

        // Обновим координаты камеры на карте.
        this.x = this.self_player.x - SCR_W / 2
        this.y = this.self_player.y - SCR_H / 2

        return {x: x + this.x, y: y + this.y}
    }

    return this
}
