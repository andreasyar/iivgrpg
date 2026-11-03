
// Игрок.

function Player(id, mapId, j, i, name, spriteLoader, speed) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Идентификатор игрока.
     */
    this.id = id;

    // Положение относительно карты в клетках по ширине.
    this.j = j;

    // Положение относительно карты в клетках по высоте.
    this.i = i;

    /**
     * Имя игрока.
     */
    this.name = name;

    /**
     * Длина имени игрока в пикселях.
     */
    this.nameWidth = null;

    // Загрузчик спрайтов.
    this.loader = spriteLoader;

    // Скорость передвижения в клетках в секунду.
    this.speed = speed;

    // Флаг движения.
    this.isMove = false

    // Путь движения.
    this.path = null

    // Путь движения в клетках для отрисовки на карте.
    this.pathC = null

    /**
     * Идентификатр карты, на котрой находится игрок.
     */
    this.mapId = mapId
    
    /**
     * Запрос на новое движение. Костыль.
     */
    this.movementRequest = null

    /**
     * Рисует игрока.
     * @param context Контекст для рисования.
     * @param camera Камера, в объективе которой находится игрок.
     */
    this.draw = function(context, camera) {

        // Игроков, которые находятся на другой карте, не рисуем.
        if (this.mapId != self_player.mapId) {
            return
        }

        if (this.isMove) {
            this.updatePos()
        }

        var tAxis = camera.translateAxis(this.x, this.y)

        // Нарисуем игрока.
        context.drawImage(this.loader.getSprite(3), tAxis.x, tAxis.y)

        // Нарисуем имя.
        if (this.nameWidth == null) {
            this.nameWidth = context.measureText(this.name).width;
        }
        context.fillText(this.name, tAxis.x + CELL_HW - this.nameWidth / 2, tAxis.y + CELL_H + 10)
    }
    
    /**
     * Рисует путь игрока.
     * @param context Контекст для рисования.
     * @param camera Камера, в объективе которой находится игрок.
     */
    this.drawPath = function(context, camera) {
        if (this.path == undefined || this.path == null) {
            return
        }
        
        var sprId, tAxis, showpath = false
        var t = GAMETIME.get()
        
        for (i = 0; i < this.path.length; i++) {
            if (showpath) {
                // Последная клетка пути или промежуточная
                if (i == this.path.length - 1) {
                    sprId = 5
                } else {
                    sprId = 4
                }
                tAxis = camera.translateAxis(this.path[i].x, this.path[i].y)
                context.drawImage(this.loader.getSprite(sprId), tAxis.x, tAxis.y)
            }
            if (this.path[i].t > t) {
                showpath = true
            }
        }
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
            GameMap.getCellXOnMap(path[0][1]),
            GameMap.getCellYOnMap(path[0][0]),
            null,
            GAMETIME.get(),
            null
        ))

        // Добавим остальные точки пути.
        for (i = 1; i < path.length; i++) {
            с1 = path[i - 1]
            с2 = path[i]
            isDiag = GameMap.isDiagonalCells(с1[0], с1[1], с2[0], с2[1])
            this.path.push(new WayPoint(
                GameMap.getCellXOnMap(с2[1]),
                GameMap.getCellYOnMap(с2[0]),
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
        // TODO Из пути криво удаляются пройденные точки

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
            newi = GameMap.getCellIOnMap(this.y)
            newj = GameMap.getCellJOnMap(this.x)
            if (newi != this.i || newj != this.j) {
                this.j = newj
                this.i = newi
                this.pathC = this.pathC.slice(1)
            }
            this.isMove = false
            
            if (this.movementRequest != undefined && this.movementRequest != null) {
                this.movementRequest.execute()
                delete this.movementRequest
            }

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
        newi = GameMap.getCellIOnMap(this.y + CELL_HH)
        newj = GameMap.getCellJOnMap(this.x + CELL_HW)
        if (newi != this.i || newj != this.j) {
            this.j = newj
            this.i = newi
            this.pathC = this.pathC.slice(1)
            
            if (this.movementRequest != undefined && this.movementRequest != null) {
                this.movementRequest.execute()
                delete this.movementRequest
                this.isMove = false
            }
        }

        return
    }
    
    this.addMovementRequest = function (movementRequest) {
        this.movementRequest = movementRequest
    }

    /**
     * Выводит на консоль точки пути.
     */
    this.printWaypoints = function() {
        console.log("Точки пути:")
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

    /**
     * Положение на карте по оси x.
     */
    this.x = GameMap.getCellXOnMap(this.j)

    /**
     * Положение на карте по оси y.
     */
    this.y = GameMap.getCellYOnMap(this.i)

    return this;
}

/**
 *  Точка пути.
 */
function WayPoint(x, y, v, t, diag) {
    this.x = x          // X координата точки пути.
    this.y = y          // Y координата точки пути.
    this.v = v          // Скорость движения в эту точку пути из предыдущей точки пути в пикселях в милисекунду.
    this.t = t          // Время прибытия в эту точку пути.
    this.diag = diag    // Лежит ли эта точка пути и предыдущая точка пути на диагонали.

    return this
}
