
// Наброски для своего GUI.
// Сейчас не используется.

function IIWindow(title, x, y, w, h, hasTopBar) {
    this.title = title;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.hasTopBar = hasTopBar;
    this.visible = true;
    this.closeButton = null;
    this.elements = new Array();
    this.contains = function(x, y) {
        if (!this.visible) {
            return false;
        }
        return x >= this.x && x <= (this.x + this.w)
            && y >= this.y && y <= (this.y + this.h);
    };
    this.onPress = function (x, y) {
        for (var i in this.elements) {
            if (this.elements[i].contains(x, y)) {
                this.elements[i].onPress(x, y);
                return;
            }
        }
        if (this.hasTopBar) {
            if (this.closeButton.contains(x, y)) {
                this.closeButton.onPress(x, y);
                return;
            }
        }
    };
    if (this.hasTopBar) {
        this.closeButton = new IIButton("x", x + w - 30, y + 10, 20, 20, this);
        this.closeButton.onPress = function(x, y) {
            //alert(this.text);
            if (this.rootWindow != null) {
                this.rootWindow.close();
            }
        };
    }
    this.draw = function(context) {
        if (!this.visible) {
            return;
        }

        if (context == undefined) {
            context = gui_buffer.getContext('2d');
        }

        context.lineWidth = 0.5;
        context.fillStyle = 'grey';
        context.strokeStyle = 'black';
        context.fillRect(this.x, this.y, this.w, this.h);
        context.strokeRect(this.x, this.y, this.w, this.h);

        if (this.hasTopBar) {
            context.fillStyle = 'black';
            context.fillText(this.title, this.x + 10, this.y + 10, this.w - 40);
            this.closeButton.draw(context);
        }

        for (var i in this.elements) {
            this.elements[i].draw(context);
        }
    };
    this.close = function () {
        this.visible = false;
    };
    this.addElement = function (e) {
        this.elements.push(e);
    }
}

function IIButton(text, x, y, w, h, rootWindow) {
    this.text = text;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.rootWindow = null;
    if (rootWindow != undefined) {
        this.rootWindow = rootWindow;
    }
    this.onPress = function(x, y) {};
    this.contains = function(x, y) {
        return x >= this.x && x <= (this.x + this.w)
            && y >= this.y && y <= (this.y + this.h);
    };
    this.draw = function(context) {
        if (context == undefined) {
            context = gui_buffer.getContext('2d');
        }

        context.fillStyle = 'cyan';
        context.fillRect(this.x, this.y, this.w, this.h);
        context.fillStyle = 'black';
        context.fillText(this.text, this.x + 3, this.y + 10, this.w - 3);
    };
}

function IITextArea(text, x, y, w, h) {
    this.text = text;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.contains = function(x, y) {
        return x >= this.x && x <= (this.x + this.w)
            && y >= this.y && y <= (this.y + this.h);
    };
    this.onPress = function(x, y) {};
    this.draw = function(context) {
        if (context == undefined) {
            context = gui_buffer.getContext('2d');
        }

        context.fillStyle = 'black';
        var words = getLines(context, this.text, this.w, context.font);
        for (var i = 0; i < words.length; i++) {
            context.fillText(words[i], this.x, this.y + 10 + (10 * i), this.w);
        }
    };
}
