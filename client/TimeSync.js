var timeSync = {

    requestTime: 0,

    timeout: null,

    stoped: false,

    run: function(timeout) {
        this.timeout = timeout
        this.stoped = false
        this.tick()
    },

    tick: function() {
        this.requestTime = (new Date()).getTime()
        server.sendMessage(new PingMessage())

        var a = this
        setTimeout(function(){return function(b){b.tick()}(a)}, this.timeout)
    },

    stop: function() {
        this.stoped = true
    }
}
