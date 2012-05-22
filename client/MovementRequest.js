function MovementRequest(server, moveMsg) {
    this.server = server
    this.moveMsg = moveMsg
    this.execute = function () {
        this.server.sendMessage(this.moveMsg)
    }
}
