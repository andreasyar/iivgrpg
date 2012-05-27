function Unit(ownerId, id, j, i) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    this.ownerId = ownerId;
    this.id = id;
    this.j = j;
    this.i = i;
    
    this.getOwnerId = function() {
        return this.ownerId;
    }
    this.getId = function() {
        return this.id;
    }
    this.getJ = function() {
        return this.j;
    }
    this.getI = function() {
        return this.i;
    }
}
