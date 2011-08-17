
// Игрок.
// requied map.js, Constants.js

// Для работы методов необходимо загрузить скрипт с картой.
// Для работы методов необходимо загрузить скрипт с константами.
function Player(id, x, y, j, i, name, spriteLoader, speed) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Идентификатор игрока.
     */
    this.id = id;

    // Положение относительно конвы в пикселах по оси Х.
    this.x = x;

    // Положение относительно конвы в пикселах по оси Y.
    this.y = y;

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
        var x = Map.getCellXOnScreen(this.j, self_player.x, self_player.j);
        var y = Map.getCellYOnScreen(this.i, self_player.y, self_player.i);
        context.drawImage(this.loader.getSprite(3), x, y);

        // Нарисуем имя.
        if (this.name_width == null) {
            this.name_width = context.measureText(this.name).width;
        }
        context.fillText(this.name, x + CELL_HW - this.name_width / 2,
                         y + CELL_H + 10);
    };

    // Запускает движение в заданную клетку.
    this.move = function(path) {
        this.path = path;

        // Запускаем движение.
        this.step.run = true;
        var a = this;
        setTimeout(
            function () { return function(b) { b.step() }(a) },
            1000 / this.speed
        );
    };

    // Останавливает движение.
    this.stop = function() {
        this.step.run = false;
    };

    // Тред движения. Перемещает игрока по клеткам в направлении его движения.
    this.step = function() {

        // Здесь нет ни каких проверок, игрок просто тупо движется в заданную
        // точку и всё.

        // Пока не дошли до конца пути.
        if (this.path.length > 1) {

            this.is_move = true;

            // Индекс 0 в пути всегда имеет клетка на которой мы стоим.
            // Соответственно индекс 1 имеет клетка на которю мы идём.
            this.i = this.path[1][0];
            this.j = this.path[1][1];

            // Удалим из пути пойденную клетку.
            this.path.splice(0, 1);

            // Обновим путь для отрисовки на карте.
            map.setPath(this.path);

            // Делаем следующий шаг.
            if (arguments.callee.run) {
                var a = this;
                setTimeout(
                    function () { return function(b) { b.step() }(a) },
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

    return this;
}
