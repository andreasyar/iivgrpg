
// Карта.

function Map(width, height, selfPlayer, spriteLoader) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    // Ширина карты в клетках.
    this.w = width;

    // Высота карты в клетках.
    this.h = height;

    // Свой игрок. Ссылка на игрока, которым играют на клиенте. Он всегда
    // находится в центре экрана и поэтому рисование карты должно поисходить
    // так чтобы это всегда было верно.
    this.player = selfPlayer;

    // Загрузчик спрайтов.
    this.loader = spriteLoader;

    // Метод рисования карты.
    this.draw = function(context) {

        // Вычислим, какие клетки карты помещаются на экран при текущем
        // положении нашего игрока. Например если наш игрок находится в клетке
        // 1, 1 то нам нужно нарисовать все клетки которые помещаются на экран
        // с клеткой 1, 1 в центре экрана.

        // j_from - индекс самой левой клетки, которая умещается на экран.
        var j_from = this.player.j - Math.ceil(SCR_HW / CELL_W) - 1
        if (j_from < 0) {
            j_from = 0;
        }

        // j_to  - индекс самой правой клетки, которая умещается на экран.
        var j_to = this.player.j + Math.ceil(SCR_HW / CELL_W) + 1
        if (j_to >= this.w) {
            j_to = this.w - 1;
        }

        // i_from - индекс самой верхней клетки, которая умещается на экран.
        var i_from = this.player.i - Math.ceil(SCR_HH / CELL_H) - 1
        if (i_from < 0) {
            i_from = 0;
        }

        // i_to - индекс самой нижней клетки, которая умещается на экран.
        var i_to = this.player.i + Math.ceil(SCR_HH / CELL_H) + 1
        if (i_to >= this.h) {
            i_to = this.h - 1;
        }

        var i, j, k,
            tAxis   // Translated axis. Map -> Camera.

        // Нарисуем клетки вычисленного региона карты.
        for (i = i_from; i < i_to; i++) {
            for (j = j_from; j < j_to; j++) {
                tAxis = camera.translateAxis(Map.getCellXOnMap(j),
                                             Map.getCellYOnMap(i))
                context.drawImage(
                    spriteMap[i][j].ground,
                    tAxis.x,
                    tAxis.y
                )
                for (k = 0; k < spriteMap[i][j].objs.length; k++) {
                    context.drawImage(
                        spriteMap[i][j].objs[k],
                        tAxis.x,
                        tAxis.y
                    )
                }
            }
        }

        // Если есть путь на карте, то нарисуем его.
        var sprId
        if (this.path != null) {
            for (i = 1; i < this.path.length; i++) {

                // Последная клетка пути или промежуточная
                if (i == this.path.length - 1) {
                    sprId = 5
                } else {
                    sprId = 4
                }

                tAxis = camera.translateAxis(Map.getCellXOnMap(this.path[i][1]),
                                             Map.getCellYOnMap(this.path[i][0]))
                context.drawImage(
                    this.loader.getSprite(sprId),
                    tAxis.x,
                    tAxis.y
                )
            }
        }
    };

    /**
     * Проверяет, принадлежит ли клетка карте.
     * @param i i-индекс клетки.
     * @param j j-индекс клетки.
     */
    this.isOnMap = function(i, j) {
        return i >= 0 && j >= 0 && i < this.h && j < this.w
    }

    /**
     * Путь на карте.
     */
    this.path = null;

    /**
     * Сохраняет путь на карте. Чтобы удалить путь установите его равным null.
     * @param path Путь.
     */
    this.setPath = function(path) {
        this.path = path
    }

    return this;
}
/**
 * Возвращает координату x карты, которую имеет клетка с индексом j.
 */
Map.getCellXOnMap = function(j) {
    return j * CELL_W
};
/**
 * Возвращает координату y карты, которую имеет клетка с индексом i.
 */
Map.getCellYOnMap = function(i) {
    return i * CELL_H
};
/**
 * Возвращает j индекс клетки по x координате карты.
 */
Map.getCellJOnMap = function(x) {
    return Math.floor(x / CELL_W)
};
/**
 * Возвращает i индекс клетки по y координате карты.
 */
Map.getCellIOnMap = function(y) {
    return Math.floor(y / CELL_H)
};
/**
 * Проверяет, лежат ли клетки на диагонали.
 * @param i1 i индекс первой клетки.
 * @param j1 j индекс первой клетки.
 * @param i2 i индекс второй клетки.
 * @param j2 j индекс второй клетки.
 * @return Возвращает true, если клетки лежат на диагонали и false в
 * противном случае.
 */
Map.isDiagonalCells = function(i1, j1, i2, j2) {
    if (Math.abs(i1 - i2) == Math.abs(j1 - j2)) {
        return true
    }

    return false
}
