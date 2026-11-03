function Movement(player, beg_time, path) {
    this.unit = player
    this.beg_time = beg_time
    this.path = path
    this.step = function() {

        // Текущее игровое время.
        var t = GAMETIME.get()

        // Соседние точки пути, между которыми находится игрок.
        var p1, p2

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

        // Движение завершено?
        if (this.path[this.path.length - 1].t <= t) {
            console.log("Движение завершено.", t)

            this.unit.x = this.path[this.path.length - 1].x
            this.unit.y = this.path[this.path.length - 1].y
            this.unit.j = this.path[this.path.length - 1].j
            this.unit.i = this.path[this.path.length - 1].i
            this.isMove = false

            return
        }

        // Найдём две соседние клетки пути, между которыми находится игрок.
        for (i = 1; i < this.path.length; i++) {
            p1 = this.path[i - 1]
            p2 = this.path[i]

            if (t >= p1.t && t < p2.t) {
                break
            }
        }

        s = p2.v * (t - p1.t)
        S = p2.diag ? Math.sqrt(2) * CELL_W : CELL_W
        ds = s / S
        dx = p2.x - p1.x
        dy = p2.y - p1.y
        _dx = ds * dx
        _dy = ds * dy

        this.x = p1.x + _dx
        this.y = p1.y + _dy
        newi = GameMap.getCellIOnMap(this.y + CELL_HH)
        newj = GameMap.getCellJOnMap(this.x + CELL_HW)
        if (newi != this.i || newj != this.j) {
            this.j = newj
            this.i = newi
            this.pathC = this.pathC.slice(1)
        }

        return
    }
    
    this.updatePath = function() {}
    
    return this
}