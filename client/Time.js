
// Объект для работы с игровым временем.

var GAMETIME = {

    time: 0,

    /**
     * Устанавливает игровое время.
     */
    set: function(time) {
        this.time = (new Date).getMilliseconds() - time
    },

    /**
     * Возвращает текущее игровое время.
     */
    get: function() {
        return (new Date).getMilliseconds() - this.time
    }
}
