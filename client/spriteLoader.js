
// Загрузчик спрайтов.

function SpriteLoader(sprites) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    // Уже загруженные спрайты.
    if (arguments.callee.loaded == undefined) {
        arguments.callee.loaded = {};
    }

    // Ссылка на набор спрайтов из ресурсов.
    this.sprites = sprites;

    /**
     * Возвращает спрайт по его секции и имени.
     * @param section Имя секции.
     * @param name Имя спрайта.
     */
    this.getSprite = function(section, name) {

        // Уже загруженные спрайты.
        if (arguments.callee.loaded == undefined) {
            arguments.callee.loaded = {};
        }

        // Создадим секцию, если нужно.
        if (arguments.callee.loaded[section] == undefined) {
            arguments.callee.loaded[section] = {};
        }

        // Если спрайт уже не был загружен.
        if (arguments.callee.loaded[section][name] == undefined) {
            arguments.callee.loaded[section][name] = new Image();
            arguments.callee.loaded[section][name].src
                = this.sprites[section][name].src;
        }

        return arguments.callee.loaded[section][name];
    }

    return this;
}
