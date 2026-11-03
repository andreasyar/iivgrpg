function Player(id, mapId, j, i, name, spriteLoader, speed) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    this.id = id;

    this.name = name;

    this.nameWidth = null;

    /**
     * Номер клетки по ширине, где находится игрок.
     */
    this.j = j;

    /**
     * Номер клетки по высоте, где находится игрок.
     */
    this.i = i;

    /**
     * Положение на карте по оси 0x, в пикселах.
     */
    this.x = Map.getCellXOnMap(this.j);

    /**
     * Положение на карте по оси 0y, в пикселах.
     */
    this.y = Map.getCellYOnMap(this.i);

    /**
     * Скорость передвижения в клетках в секунду.
     */
    this.speed = speed;

    this.isMove = false;

    /**
     * Путь движения.
     */
    this.path = null;

    /**
     * Путь движения в клетках.
     */
    this.pathC = null;

    /**
     * Загрузчик спрайтов.
     */
    this.loader = spriteLoader;

    this.mapId = mapId;

    /**
     * Рисует игрока.
     * @param context Контекст для рисования.
     */
    this.draw = function(context) {

        // Игроков, которые находятся на другой карте, не рисуем.
        if (this.mapId != self_player.mapId) {
            return
        }

        if (this.isMove) {
            this.updatePos()
        }

        // По своим координатам на карте получим координаты в кадре камеры
        // (на холсте).
        var tAxis = camera.translateAxis(this.x, this.y)

        // Нарисуем игрока.
        context.drawImage(this.loader.getSprite(3), tAxis.x, tAxis.y)

        // Нарисуем имя.
        if (this.nameWidth == null) {
            this.nameWidth = context.measureText(this.name).width;
        }
        context.fillText(
            this.name,
            tAxis.x + CELL_HW - this.nameWidth / 2,
            tAxis.y + CELL_H + 10
        )
    }

    /**
     * Запускает движение по заданному пути.
     * @param path Путь из клеток.
     * @param begTime Игровое время начала движения.
     */
    this.move = function(path, begTime) {

        // path - массив, содержащий клетки пути:
        // 0 - i индекс клетки пути.
        // 1 - j индекс клетки.
        // Пример: [[0, 0], [0, 1]]. Путь из клетки 0,0 вправо в клетку 0,1.

        // Скорость движения по горизонтали в пикселях в милисекунду.
        var hSpeed

        // Скорость движения по диагонали в пикселях в милисекунду.
        var dSpeed

        // Время движения между соседними клетками в милисекундах.
        var t

        // Лежат ли клетки на диагонали.
        var isDiag

        // Временные переменные.
        var с1, с2, i

        // Скорость анимации движения игрока. Необходима для плавного движения по пути.
        var movementAdjustedSpeed

        this.pathC = path

        movementAdjustedSpeed = (path.length * 1000) / ((begTime + ((path.length / this.speed) * 1000)) - GAMETIME.get())

        console.log("Speed updated:", this.speed, "->", movementAdjustedSpeed)

        hSpeed = (movementAdjustedSpeed * CELL_W) / 1000
        dSpeed = hSpeed * Math.sqrt(2)
        t = Math.ceil((1 / movementAdjustedSpeed) * 1000)

        if (movementAdjustedSpeed <= 0) {
            console.log("Движение уже закончилось.")
            return
        }

        // Добавим отправную точку пути.
        this.path = new Array(new WayPoint(
            Map.getCellXOnMap(path[0][1]),
            Map.getCellYOnMap(path[0][0]),
            null,
            GAMETIME.get(),
            null
        ))

        // Добавим остальные точки пути.
        for (i = 1; i < path.length; i++) {
            с1 = path[i - 1]
            с2 = path[i]
            isDiag = Map.isDiagonalCells(с1[0], с1[1], с2[0], с2[1])
            this.path.push(new WayPoint(
                Map.getCellXOnMap(с2[1]),
                Map.getCellYOnMap(с2[0]),
                isDiag ? dSpeed : hSpeed,
                this.path[this.path.length - 1].t + t,
                isDiag
            ))
        }

        // Отметим, что движение началось.
        this.isMove = true

        console.log("Начато движение из клетки", path[0][1], path[0][0],
                    "в клетку", path[path.length - 1][1],
                    path[path.length - 1][0])
    }

    /**
     * Обновляет позицию игрока, если он движется.
     */
    this.updatePos = function() {

        if (!this.isMove) {
            return
        }

        // Текущее игровое время.
        var t = GAMETIME.get()

        // Соседние точки пути, между которыми находится игрок.
        var wp1, wp2

        // Пройденное расстояние между соседними точками пути, где находится
        // игрок.
        var s

        // Полное расстояние между соседними точками пути, где находится
        // игрок.
        var S

        // Отношение пройденного расстояния к полному расстоянию.
        var ds

        // Разность соответствующих координат соседних точек пути, между
        // которыми находится игрок.
        var dx, dy

        // Координаты текущей точки, где находится игрок, относительно начальной
        // точки пути.
        var _dx, _dy

        var i, newi, newj

        // Движение закончено?
        if (this.path[this.path.length - 1].t <= t) {
            console.log("Движение завершено.", t)

            this.x = this.path[this.path.length - 1].x
            this.y = this.path[this.path.length - 1].y
            newi = Map.getCellIOnMap(this.y)
            newj = Map.getCellJOnMap(this.x)
            if (newi != this.i || newj != this.j) {
                this.j = newj
                this.i = newi
                this.pathC = this.pathC.slice(1)
                if (this == self_player
                        && maps[self_player.mapId].eventMap[this.i][this.j] != undefined) {

                    setTimeout(maps[self_player.mapId].eventMap[this.i][this.j], 0)
                }
            }
            this.isMove = false

            return
        }

        // Найдём две соседние точки пути, между которыми находится игрок.
        for (i = 1; i < this.path.length; i++) {
            wp1 = this.path[i - 1]
            wp2 = this.path[i]

            if (t >= wp1.t && t < wp2.t) {
                break
            }
        }

        s = wp2.v * (t - wp1.t)
        S = wp2.diag ? Math.sqrt(2) * CELL_W : CELL_W
        ds = s / S
        dx = wp2.x - wp1.x
        dy = wp2.y - wp1.y
        _dx = ds * dx
        _dy = ds * dy

        this.x = wp1.x + _dx
        this.y = wp1.y + _dy
        newi = Map.getCellIOnMap(this.y + CELL_HH)
        newj = Map.getCellJOnMap(this.x + CELL_HW)
        if (newi != this.i || newj != this.j) {
            this.j = newj
            this.i = newi
            this.pathC = this.pathC.slice(1)
            if (this == self_player
                    && maps[self_player.mapId].eventMap[this.i] != undefined
                    && maps[self_player.mapId].eventMap[this.i][this.j] != undefined) {

                setTimeout(maps[self_player.mapId].eventMap[this.i][this.j], 0)
            }
        }

        return
    }

    /**
     * Выводит на консоль точки пути.
     */
    this.printWaypoints = function() {
        console.log("Точки пути")
        for (var i in this.path) {
            console.log(this.path[i].x, this.path[i].y, this.path[i].v,
                        this.path[i].t, this.path[i].diag)
        }
    }

    /**
     * Выводит на консоль путь в клетках.
     */
    this.printCellPath = function() {
        console.log("Клетки пути:")
        for (var i in this.pathC) {
            console.log(this.pathC[i][1], this.pathC[i][0])
        }
    }

    /**
     * Останавливает движение.
     */
    this.stop = function() {
        if (this.isMove) {
            this.updatePos()
            this.isMove = false
            console.log("Движение остановлено.")
        } else {
            console.log("Игрок уже не движется.")
        }
    }

    return this;
}

/**
 *  Точка пути.
 */
function WayPoint(x, y, v, t, diag) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    this.x = x          // X координата точки пути.
    this.y = y          // Y координата точки пути.
    this.v = v          // Скорость движения в эту точку пути из предыдущей точки пути в пикселях в милисекунду.
    this.t = t          // Время прибытия в эту точку пути.
    this.diag = diag    // Лежит ли эта точка пути и предыдущая точка пути на диагонали.

    return this
}
