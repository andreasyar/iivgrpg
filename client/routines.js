
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
