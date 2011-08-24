
// Объект для работы с игровым временем.

var GAMETIME = {

    time: 0,

    /**
     * Устанавливает игровое время.
     */
    set: function(time) {
        this.time = (new Date()).getTime() - time
    },

    /**
     * Возвращает текущее игровое время.
     */
    get: function() {
        return (new Date()).getTime() - this.time
    }
}
