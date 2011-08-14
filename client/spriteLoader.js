
// Загрузчик спрайтов.

function SpriteLoader(sprites) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    // Ссылка на набор спрайтов из ресурсов.
    this.sprites = sprites;

    /**
     * Возвращает спрайт по его id.
     * @param id Имя спрайта.
     */
    this.getSprite = function(id) {
        
        // Если спрайт уже не был загружен.
        if (this.sprites[id].image == undefined) {
            this.sprites[id].image = new Image();
            this.sprites[id].image.src = this.sprites[id].src;
        }

        return this.sprites[id].image;
    }

    return this;
}
