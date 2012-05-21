
// Карта.

function Map(id, width, height, spriteLoader, spriteMap, accessMap, eventMap) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Идентификатор карты.
     */
    this.id = id

    // Ширина карты в клетках.
    this.w = width;

    // Высота карты в клетках.
    this.h = height;

    // Свой игрок. Ссылка на игрока, которым играют на клиенте. Он всегда
    // находится в центре экрана и поэтому рисование карты должно поисходить
    // так чтобы это всегда было верно.
    this.player = null
    
    /**
     * Ссылка на камеру, которая в данный момент показывает карту. Какую часть
     * карты рисовать на экране зависит от того, какая часть карты попадает в
     * объектив камеры.
     */
    this.camera = null

    // Загрузчик спрайтов.
    this.loader = spriteLoader;

    /**
     * Карта спрайтов.
     */
    this.spriteMap = spriteMap

    /**
     * Карта доступности.
     */
    this.accessMap = accessMap

    /**
     * Карта событий.
     */
    this.eventMap = eventMap

    /**
     * Флаг отображения карты доступности.
     */
    this.showAccessMap = false

    // Метод рисования карты.
    this.draw = function(context) {

        if (this.player == null) {
            console.error("У карты %s не установлен свой игрок! Карта "
                          + "не может быть отображена.", this.id)
            return
        }

        if (this.camera == undefined || this.camera == null) {
            console.error("У карты %s не установлена камера! Карта "
                          + "не может быть отображена.", this.id)
            return
        }

        // Вычислим, какие клетки карты помещаются на экран при текущем
        // положении нашего игрока. Например если наш игрок находится в клетке
        // 1, 1 то нам нужно нарисовать все клетки которые помещаются на экран
        // с клеткой 1, 1 в центре экрана.

        // j_from - индекс самой левой клетки, которая умещается на экран.
        //var j_from = this.player.j - Math.ceil(SCR_HW / CELL_W)
        var j_from
        if (this.camera.x < 0) {
            j_from = 0
        } else {
            j_from = Map.getCellJOnMap(this.camera.x)
        }
        /*if (j_from < 0) {
            j_from = 0
        }*/

        // j_to  - индекс самой правой клетки, которая умещается на экран.
        //var j_to = this.player.j + Math.ceil(SCR_HW / CELL_W)
        var j_to
        if (this.camera.x + SCR_W >= this.w * CELL_W) {
            j_to = this.w - 1
        } else {
            j_to = Map.getCellJOnMap(this.camera.x + SCR_W)
        }
        // Разрешение экрана может быть разное, поэтому при подсчете
        // Map.getCellJOnMap можем случайно вылететь за края карты.
        if (j_to >= this.w) {
            j_to = this.w - 1;
        }

        // i_from - индекс самой верхней клетки, которая умещается на экран.
        //var i_from = this.player.i - Math.ceil(SCR_HH / CELL_H) - 1
        var i_from = Map.getCellIOnMap(this.camera.y)
        if (this.camera.y < 0) {
            i_from = 0
        } else {
            i_from = Map.getCellIOnMap(this.camera.y)
        }
        /*if (i_from < 0) {
            i_from = 0;
        }*/

        // i_to - индекс самой нижней клетки, которая умещается на экран.
        //var i_to = this.player.i + Math.ceil(SCR_HH / CELL_H)
        var i_to = Map.getCellIOnMap(this.camera.y + SCR_H)
        if (this.camera.y + SCR_H >= this.h * CELL_H) {
            i_to = this.h - 1
        } else {
            i_to = Map.getCellIOnMap(this.camera.y + SCR_H)
        }
        // Разрешение экрана может быть разное, поэтому при подсчете
        // Map.getCellIOnMap можем случайно вылететь за края карты.
        if (i_to >= this.h) {
            i_to = this.h - 1;
        }

        var i, j, k,
            tAxis   // Translated axis. Map -> Camera.

        // Нарисуем клетки вычисленного региона карты.
        for (i = i_from; i <= i_to; i++) {
            for (j = j_from; j <= j_to; j++) {
                tAxis = this.camera.translateAxis(Map.getCellXOnMap(j), Map.getCellYOnMap(i))
                context.drawImage(this.spriteMap[i][j].ground, tAxis.x, tAxis.y)
                for (k = 0; k < this.spriteMap[i][j].objs.length; k++) {
                    context.drawImage(this.spriteMap[i][j].objs[k], tAxis.x, tAxis.y)
                }
                if (this.showAccessMap) {
                    context.fillText(this.accessMap[i][j], tAxis.x + CELL_W / 2, tAxis.y + CELL_H / 2)
                }
            }
        }

        // Если свой игрок движется, то нарисуем его путь.
        /*var sprId, path = this.player.pathC
        if (this.player.isMove && path != null) {
            for (i = 1; i < path.length; i++) {

                // Последная клетка пути или промежуточная
                if (i == path.length - 1) {
                    sprId = 5
                } else {
                    sprId = 4
                }

                if (path[i] == undefined) {
                    console.log("DEBUG", "Incorrect path or path cell index", path, i)
                }
                tAxis = this.camera.translateAxis(Map.getCellXOnMap(path[i][1]), Map.getCellYOnMap(path[i][0]))
                context.drawImage(this.loader.getSprite(sprId), tAxis.x, tAxis.y)
            }
        }*/
    }

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

    return this
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
