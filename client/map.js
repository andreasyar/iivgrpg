
// Карта.

// Для работы методов класса необходимо снчала загрузить скрипт с константами.
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
        var j_from = this.player.j
                     - Math.ceil((this.player.x - CELL_HW) / CELL_W);
        if (j_from < 0) {
            j_from = 0;
        }

        // j_to  - индекс самой правой клетки, которая умещается на экран.
        var j_to = this.player.j
                   + Math.ceil((this.player.x + CELL_HW) / CELL_W);
        if (j_to >= this.w) {
            j_to = this.w - 1;
        }

        // i_from - индекс самой верхней клетки, которая умещается на экран.
        var i_from = this.player.i
                     - Math.ceil((this.player.y - CELL_HH) / CELL_H);
        if (i_from < 0) {
            i_from = 0;
        }

        // i_to - индекс самой нижней клетки, которая умещается на экран.
        var i_to = this.player.i
                   + Math.ceil((this.player.y + CELL_HH) / CELL_H);
        if (i_to >= this.h) {
            i_to = this.h - 1;
        }

        // Нарисуем клетки вычисленного региона карты.
        //var x, y;
        for (var i = i_from; i < i_to; i++) {
            for (var j = j_from; j < j_to; j++) {
                //x = Map.getCellXOnScreen(j, this.player.x, this.player.j);
                //y = Map.getCellXOnScreen(i, this.player.y, this.player.i);
                context.drawImage(
                    this.loader.getSprite('map', 'grass'),
                    Map.getCellXOnScreen(j, this.player.x, this.player.j),
                    Map.getCellXOnScreen(i, this.player.y, this.player.i));
                
                // Нарисуем номера клеток. Для отладки.
                //context.fillText(j + ',' + i, x + 2, y + 10);
            }
        }
    };

    return this;
}
// Возвращает x-координату в конве клетки с индексом j. px - координата x
// своего игрока, pj - индекс j клетки где находится свой игрок.
Map.getCellXOnScreen = function(j, px, pj) {
    return (px - CELL_HW) + (j - pj) * CELL_W;
};
// Возвращает y-координату в конве клетки с индексом i. py - координата y
// своего игрока, pi - индекс i клетки где находится свой игрок.
Map.getCellYOnScreen = function(i, py, pi) {
    return (py - CELL_HH) + (i - pi) * CELL_H;
};
// Возвращает индекс j клетки, которая имеет в конве координату x.
// px - координата x своего игрока, pj - индекс j клетки где находится свой
// игрок.
Map.getCellJFromScreen = function(x, px, pj) {
    var _;
    if (x < px) {
        _ = pj - Math.abs(Math.ceil((px - CELL_HW - x) / CELL_W));
    } else if (x > px) {
        _ = pj + Math.abs(Math.ceil((x - (px + CELL_HW)) / CELL_W));
    } else {
        _ = pj;
    }
    return _;
};
// Возвращает индекс i клетки, которая имеет в конве координату y.
// py - координата y своего игрока, pi - индекс i клетки где находится свой
// игрок.
Map.getCellIFromScreen = function(y, py, pi) {
    var _;
    if (y < py) {
        _ = pi - Math.abs(Math.ceil((py - CELL_HH - y) / CELL_H));
    } else if (y > py) {
        _ = pi + Math.abs(Math.ceil((y - (py + CELL_HH)) / CELL_H));
    } else {
        _ = pi;
    }
    return _;
};
