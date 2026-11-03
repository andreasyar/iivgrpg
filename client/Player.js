
// Игрок.

<<<<<<< .mine
// Для работы методов необходимо загрузить скрипт с картой.
// Для работы методов необходимо загрузить скрипт с константами.
function Player(x, y, j, i, name, spriteLoader) {
=======
function Player(id, j, i, name, spriteLoader, speed) {
>>>>>>> .r56

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

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
    this.speed = 0;

    // Флаг движения.
    this.is_move = false;

    // Индексы конечной клетки движения.
    this.dest_j = null;
    this.dest_i = null;

    /**
     * Рисует игрока.
     * @param context Контекст для рисования.
     */
    this.draw = function(context) {
<<<<<<< .mine
        var x = Map.getCellXOnScreen(this.j, this.x, this.j);
        var y = Map.getCellYOnScreen(this.i, this.y, this.i);
        context.drawImage(this.loader.getSprite('player', 'stand_s'), x, y);
=======
>>>>>>> .r56

        // По своим координатам на карте получим координаты в кадре камеры
        // (на холсте).
        var tAxis = camera.translateAxis(this.x, this.y)

        // Нарисуем игрока.
        context.drawImage(this.loader.getSprite(3), tAxis.x, tAxis.y)

        // Нарисуем имя.
        if (this.name_width == null) {
            this.name_width = context.measureText(this.name).width;
        }
<<<<<<< .mine
        context.fillText(this.name, x + CELL_HW - this.name_width / 2,
                         y + CELL_H);
    };
=======
        context.fillText(
            this.name,
            tAxis.x + CELL_HW - this.name_width / 2,
            tAxis.y + CELL_H + 10
        )
    }
>>>>>>> .r56

    // Запускает движение в заданную клетку.
    this.move = function(j, i) {
        this.dest_j = j;
        this.dest_i = i;

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

        // Пока не дошли до точки назначения.
        if (this.dest_j != null && this.dest_i != null
                && (this.j != this.dest_j || this.i != this.dest_i)) {

            this.is_move = true;

            if (this.j < this.dest_j) {
                this.j++;
            } else if (this.j > this.dest_j) {
                this.j--;
            }
            if (this.i < this.dest_i) {
                this.i++;
            } else if (this.i > this.dest_i) {
                this.i--;
            }

<<<<<<< .mine
=======
            // Обновим так же и координаты игрока на карте.
            this.x = this.getX()
            this.y = this.getY()

            // Удалим из пути пойденную клетку.
            this.path.splice(0, 1);

            // Обновим путь для отрисовки на карте.
            map.setPath(this.path);

>>>>>>> .r56
            // Делаем следующий шаг.
            if (arguments.callee.run) {
                var a = this;
                setTimeout(
                    function () {return function(b) {b.step()}(a)},
                    1000 / this.speed
                );
            }
        } else {
            this.is_move = false;
            arguments.callee.run = false;
            this.dest_j = null;
            this.dest_i = null;
            return;
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

    return this;
}
