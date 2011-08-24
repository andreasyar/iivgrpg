
// Игрок.

function Player(id, j, i, name, spriteLoader, speed) {

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
    this.name_width = null;

    // Загрузчик спрайтов.
    this.loader = spriteLoader;

    // Скорость передвижения в клетках в секунду.
    this.speed = speed;

    // Флаг движения.
    this.is_move = false;

    // Путь движения.
    this.path = null;

    /**
     * Рисует игрока.
     * @param context Контекст для рисования.
     */
    this.draw = function(context) {

        // По своим координатам на карте получим координаты в кадре камеры
        // (на холсте).
        var tAxis
        
        if (this.is_move) {
            var curPos = this.movement.getCurPos()
            tAxis = camera.translateAxis(curPos.x, curPos.y)
        } else {
            tAxis = camera.translateAxis(this.x, this.y)
        }

        // Нарисуем игрока.
        context.drawImage(this.loader.getSprite(3), tAxis.x, tAxis.y)

        // Нарисуем имя.
        if (this.name_width == null) {
            this.name_width = context.measureText(this.name).width;
        }
        context.fillText(
            this.name,
            tAxis.x + CELL_HW - this.name_width / 2,
            tAxis.y + CELL_H + 10
        )
    }

    // Запускает движение в заданную клетку.
    this.move = function(path) {
        this.path = path;

        // Запускаем движение.
        this.step.run = true;
        var a = this;
        setTimeout(
            function () {return function(b) {b.step()}(a)},
            1000 / this.speed
        );
    };

    // Останавливает движение.
    this.stop = function() {
        this.step.run = false;
    };

    // Тред движения. Перемещает игрока по клеткам в направлении его движения.
    this.step = function() {

        // Пока не дошли до конца пути.
        if (this.path.length > 1) {

            this.is_move = true;

            // Индекс 0 в пути всегда имеет клетка на которой мы стоим.
            // Соответственно индекс 1 имеет клетка на которю мы идём.
            this.i = this.path[1][0];
            this.j = this.path[1][1];

            // Обновим так же и координаты игрока на карте.
            this.x = this.getX()
            this.y = this.getY()

            // Удалим из пути пойденную клетку.
            this.path.splice(0, 1);

            // Обновим путь для отрисовки на карте.
            map.setPath(this.path);

            // Делаем следующий шаг.
            if (arguments.callee.run) {
                var a = this;
                setTimeout(
                    function () {return function(b) {b.step()}(a)},
                    1000 / this.speed
                );
            }
        } else {
            // Очистим путь.
            this.path = null

            // Обновим путь для отрисовки на карте.
            map.setPath(this.path)

            this.is_move = false
            arguments.callee.run = false
            return
        }
    };

    /**
     * Возвращает x координату игрока на карте.
     */
    this.getX = function() {
        return Map.getCellXOnMap(this.j)
    }

    /**
     * Возвращает y координату игрока на карте.
     */
    this.getY = function() {
        return Map.getCellYOnMap(this.i)
    }

    /**
     * Положение на карте по оси x.
     */
    this.x = this.getX()

    /**
     * Положение на карте по оси y.
     */
    this.y = this.getY()

    this.movement = {
        pathC: null,    // Путь движения по клеткам.
        pathP: null,    // Путь движения по точкам.
        speedC: null,   // Скорость в клетках в секунду.
        begTime: null,  // Время начала движения.
        start: function(path, speed, begTime) {
            this.pathC = path
            this.begTime = begTime
            this.speedC = speed
            this.createWaypoints(path, begTime)
            console.log(this.pathP)
        },
        stop: function() {},
        getCurPos: function() {

            // Найдём где сейчас находится игрок.

            var t = GAMETIME.get()
            var p1, p2, s, S, ds, dx, dy, _dx, _dy, x, y

            // Движение закончено?
            if (this.pathP[this.pathP.length - 1][3] <= t) {
                console.log("Movement complete")
                return {
                    x: this.pathP[this.pathP.length - 1][0],
                    y: this.pathP[this.pathP.length - 1][1]
                }
            }

            for (var i = 1; i < this.pathP.length; i++) {
                p1 = this.pathP[i - 1]
                p2 = this.pathP[i]

                if (t >= p1[3] && t < p2[3]) {
                    break
                }
            }

            // Пройденное расстояние между точками.
            s = p2[2] / 1000 * (t - p1[3])

            // Общее расстояение между точками.
            S = p2[4] ? Math.sqrt(2) * CELL_W : CELL_W

            //console.log("time", t, p1[3], p2[3], "move", p1[0], p1[1], "->", p2[0], p2[1], "var", s, S)
                    
            ds = s / S

            dx = p2[0] - p1[0]
            dy = p2[1] - p1[1]
            _dx = ds * dx
            _dy = ds * dy
            x = p1[0] + _dx
            y = p1[1] + _dy

            return {x: x, y: y}
        },
        createWaypoints: function(path, begTime) {
            
            // path - массив, содержащий клетки:
            // 0 - i индекс клетки пути.
            // 1 - j индекс клетки.
            // Пример: [[0, 0], [0, 1]]. Путь из клетки 0,0 вправо в клетку 0,1.

            // Скорость движения по горизонтали в пикселях в секунду.
            var hSpeed = this.speedC * CELL_W

            // Скорость движения по диагонали.
            var dSpeed = hSpeed * Math.sqrt(2)

            // Время движения между соседними клетками в милисекундах.
            var t = Math.ceil((1 / this.speedC) * 1000)

            this.pathP = new Array(new Array(
                Map.getCellXOnMap(path[0][1]),
                Map.getCellYOnMap(path[0][0]),
                null,
                begTime
            ))

            var с1, с2, isDiag

            for (var i = 1; i < path.length; i++) {
                с1 = path[i - 1]
                с2 = path[i]
                isDiag = this.isDiagonalCells(с1[0], с1[1], с2[0], с2[1])
                this.pathP.push(new Array(
                    Map.getCellXOnMap(с2[1]),
                    Map.getCellYOnMap(с2[0]),
                    isDiag ? dSpeed : hSpeed,
                    this.pathP[this.pathP.length - 1][3] + t,
                    isDiag
                ))
            }
        },
        isDiagonalCells: function(i1, j1, i2, j2) {
            if (Math.abs(i1 - i2) == Math.abs(j1 - j2)) {
                return true
            }

            return false
        },
        printWaypoints: function() {
            console.log("Waypoints")
            for (var i in this.pathP) {
                console.log(this.pathP[i][0], this.pathP[i][1],
                            this.pathP[i][2], this.pathP[i][3],
                            this.pathP[i][4])
            }
        },
        printCellPath: function() {
            console.log("Cell path")
            for (var i in this.pathC) {
                console.log(this.pathC[i][0], this.pathC[i][1],
                            Map.getCellXOnMap(this.pathC[i][0]),
                            Map.getCellXOnMap(this.pathC[i][1]))
            }
        }
    }

    return this;
}
