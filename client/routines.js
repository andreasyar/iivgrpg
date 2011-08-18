
// Различные функции.
// requied Constants.js

// http://stackoverflow.com/questions/2936112/text-wrap-in-a-canvas-element
// Разбивает фразу на отдельные слова и возвращает массив подфраз фразы phrase,
// влезающих в ширину maxPxLength при выводе на экран.
function getLines(ctx, phrase, maxPxLength, textFont) {
    var wa = phrase.split(" "),
        phraseArray = [],
        lastPhrase = "",
        l = maxPxLength,
        measure = 0;   // Length of word tail in percent.
    ctx.font = textFont;
    for (var i = 0; i < wa.length; i++) {
        var w = wa[i];
        measure = ctx.measureText(lastPhrase + w).width;
        if (measure < l) {
            lastPhrase += (w + " ");
        } else {
            phraseArray.push(lastPhrase);
            lastPhrase = w;
        }
        if (i === wa.length - 1) {
            phraseArray.push(lastPhrase);
            break;
        }
    }
    return phraseArray;
}

// Скрывает форму ввода логина и пароля для входа в игру.
function hideLoginForm() {
    document.getElementById('login_form').style.visibility = 'hidden';
}
// Отображает форму ввода логина и пароля для входа в игру.
function showLoginForm() {
    var login_form = document.getElementById('login_form');

    // Сделаем форму видимой и отцентируем форму логина по середине конвы.
    login_form.style.visibility = 'visible';
    login_form.style.left = (SCR_W / 2 - login_form.offsetWidth / 2) + "px";
    login_form.style.top = (SCR_H / 2  - login_form.offsetHeight / 2) + "px";
}
/**
 * Возвращает логин, введённый в форме ввода логина пароля.
 */
function getLogin() {
    return document.getElementById('login_fild').value
}
/**
 * Возвращает пароль, введённый в форме ввода логина пароля.
 */
function getPassword() {
    return document.getElementById('password_fild').value
}

// Поиск пути на карте.
function Cell(i, j, g, h, parent) {
    this.i = i
    this.j = j
    this.f = g + h
    this.g = g
    this.h = h
    this.parent = parent
    this.equal = function(i, j) {
        return this.i == i && this.j == j
    }
    this.toString = function() {
        return "[" + this.j + ", " + this.i + "]"
    }
    return this
}
function cost(cur, i, j) {
    var g = cur.g
    if ( (cur.i - 1 == i && cur.j == j)
            || (cur.i + 1 == i && cur.j == j)
            || (cur.i == i && cur.j - 1 == j)
            || (cur.i == i && cur.j + 1 == j)) {

        g += 10
    } else {
        g += 14
    }
    return g
}
function heuristic(i, j, k, l) {
    return 10 * (Math.abs(j - l) + Math.abs(i - k))
}
function getByIndex(list, i, j) {
    for (var n = 0; n < list.length; n++)
        if (list[n].i == i && list[n].j == j)
            return list[n]
    return null
}
function isInList(list, i, j) {
    for (var n = 0; n < list.length; n++)
        if (list[n].i == i && list[n].j == j)
            return true
    return false
}
function throughCorner(cur, i, j) {

    // Клетки не лежат на диагонали
    if (cur.i - i == 0 || cur.j - j == 0) {
        return false
    }

    //[c][ ]
    //[ ][n]
    if (i > cur.i && j > cur.j) {
        return SPRITES[GEODATA[i - 1][j]].acc == 0
                || SPRITES[GEODATA[i][j - 1]].acc == 0;
    }

    //[n][ ]
    //[ ][c]
    if (i < cur.i && j < cur.j) {
        return SPRITES[GEODATA[i + 1][j]].acc == 0
                || SPRITES[GEODATA[i][j + 1]].acc == 0;
    }

    //[ ][n]
    //[c][ ]
    if (i < cur.i && j > cur.j) {
        return SPRITES[GEODATA[i + 1][j]].acc == 0
                || SPRITES[GEODATA[i][j - 1]].acc == 0;
    }

    //[ ][c]
    //[n][ ]
    if (i > cur.i && j < cur.j) {
        return SPRITES[GEODATA[i - 1][j]].acc == 0
                || SPRITES[GEODATA[i][j + 1]].acc == 0;
    }

    return false
}
function getPath(beg, end) {
    var open = new Array()
    var closed = new Array()
    var i, j, g, h, cur, nei, n, rows = new Array(3), cols = new Array(3)
    var limit = 10000
    var stat = {
        cur: 0,
        beg: 0,
        onmap: 0,
        geo: 0,
        close: 0,
        corner: 0
    }

    if (!map.isOnMap(end.i, end.j) || SPRITES[GEODATA[end.i][end.j]].acc == 0) {
        return false
    }

    open.push(beg)

    while(open.length != 0 && limit-- > 0) {

        // Текущей будет клетка из списка открытых клеток с нимимальным f.
        cur = null
        for (n = 0; n < open.length; n++) {
            if (cur == null) {
                cur = open[n]
            } else if (open[n].f < cur.f) {
                cur = open[n]
            }
        }

        //console.log("Current cell " + cur)

        // Поместим текущую клтку в список закрытых и удалим из списка
        // открытых клеток.
        closed.push(cur)
        for (n = 0; n < open.length; n++) {
            if (open[n].equal(cur.i, cur.j)) {
                open.splice(n, 1)
            }
        }

        // Обойдём всех соседей текущей клетки.
        rows[0] = cur.i - 1
        rows[1] = cur.i
        rows[2] = cur.i + 1
        cols[0] = cur.j - 1
        cols[1] = cur.j
        cols[2] = cur.j + 1
        for (var k in rows) {
            for (var l in cols) {
                i = rows[k]
                j = cols[l]

                // Текущую клетку не рассматриваем.
                if (cur.equal(i, j)) {
                    stat.cur++
                    continue
                }

                // Начальную клетку не рассматриваем.
                if (beg.equal(i, j)) {
                    stat.beg++
                    continue
                }

                // Клетку вне карты не рассматриваем.
                if (!map.isOnMap(i, j)) {
                    stat.onmap++
                    continue
                }

                // Клетку, в которую нельзя ходить, не рассматриваем.
                if (SPRITES[GEODATA[i][j]].acc == 0) {
                    stat.geo++
                    continue
                }

                // Клетку из списка закрытых не рассматриваем.
                if (isInList(closed, i, j)) {
                    stat.close++
                    continue
                }

                // Клетку в которую из текущей можно попасть лишь срезав угол
                // непроходимой клетки, также не рассматриваем.
                if (throughCorner(cur, i, j)) {
                    stat.corner++
                    continue
                }

                if (!isInList(open, i, j)) {
                    g = cost(cur, i, j)
                    h = heuristic(i, j, end.i, end.j)
                    nei = new Cell(i, j, g, h, cur)

                    if (nei.equal(end.i, end.j)) {
                        console.log(stat)
                        return nei  // Путь найден!
                    } else {
                        open.push(nei)
                    }
                } else {
                    nei = getByIndex(open, i, j)
                    g = cost(cur, nei.i, nei.j)
                    if (nei.g > g) {
                        nei.parent = cur
                        nei.g = g
                        nei.f = nei.g + nei.h
                    }
                }
            }
        }
    }

    if (limit <= 0) {
        alert('Search limit reached.')
    }

    console.log(stat)
    return false    // Нет пути
}

// Отображает форму чата.
function showChat() {
    var chat = document.getElementById('chat');

    // Сделаем форму видимой и отцентируем форму логина по середине конвы.
    chat.style.visibility = 'visible';
    chat.style.left = (SCR_W + 20) + "px";
    chat.style.top = (20) + "px";
    document.getElementById("chat_bar_common").style.color = "blue"

    chatToggle(cur_chat_type)
}
// Выводит содержимое чата.
function chatToggle(type) {
    var c, e,
        t = document.getElementById("chat_field"),
        input = document.getElementById("chat_input")

    switch (type) {
        case 0:
            e = document.getElementById("chat_bar_private")
            c = private_chat
            cur_chat_type = 0

            e.style.color = "blue"
            document.getElementById("chat_bar_common").style.color = "black"
            break
        case 1:
            e = document.getElementById("chat_bar_common")
            c = common_chat
            cur_chat_type = 1

            e.style.color = "blue"
            document.getElementById("chat_bar_private").style.color = "black"
            break
    }

    var tmp = ""
    for (var i in c) {
        tmp += c[i].name + ": " + c[i].message + "\n"
    }
    t.value = tmp
    setCaretToPos(t, t.value.length)
    setCaretToPos(input, input.value.length)
}
// Отправляет сообщение на сервер.
function chatSend() {
    var input = document.getElementById("chat_input")
    server.sendMessage(new NewchatMessage(input.value))
    setCaretToPos(input, input.value.length)
    input.value = ""
}
// http://stackoverflow.com/questions/499126/jquery-set-cursor-position-in-text-area
function setSelectionRange(input, selectionStart, selectionEnd) {
  if (input.setSelectionRange) {
    input.focus();
    input.setSelectionRange(selectionStart, selectionEnd);
  }
  else if (input.createTextRange) {
    var range = input.createTextRange();
    range.collapse(true);
    range.moveEnd('character', selectionEnd);
    range.moveStart('character', selectionStart);
    range.select();
  }
}
function setCaretToPos(input, pos) {
  setSelectionRange(input, pos, pos);
}
