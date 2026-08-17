const net = require('net')

let forwarders = []

function canConnectLocally(port) {
    return new Promise(resolve => {
        const socket = net.createConnection({ host: '127.0.0.1', port })
        let finished = false
        const finish = result => {
            if (finished) return
            finished = true
            socket.destroy()
            resolve(result)
        }
        socket.setTimeout(750)
        socket.once('connect', () => finish(true))
        socket.once('timeout', () => finish(false))
        socket.once('error', () => finish(false))
    })
}

function createForwarder(port) {
    return new Promise((resolve, reject) => {
        const server = net.createServer(localSocket => {
            const remoteSocket = net.createConnection({
                host: global.settings.game_host,
                port
            })
            localSocket.setNoDelay(true)
            remoteSocket.setNoDelay(true)
            localSocket.pipe(remoteSocket)
            remoteSocket.pipe(localSocket)
            localSocket.on('error', () => remoteSocket.destroy())
            remoteSocket.on('error', () => localSocket.destroy())
        })
        server.once('error', reject)
        server.listen(port, '127.0.0.1', () => {
            server.removeListener('error', reject)
            server.on('error', error => console.error(`Forwarder ${port} error:`, error.message))
            resolve(server)
        })
    })
}

async function startForwarders() {
    if (await canConnectLocally(global.settings.login_port)) {
        return { local: true, ports: [] }
    }

    const ports = [global.settings.login_port]
    for (let port = global.settings.channel_port_start; port <= global.settings.channel_port_end; port++) {
        ports.push(port)
    }

    try {
        for (const port of ports) {
            forwarders.push(await createForwarder(port))
        }
        return { local: false, ports }
    } catch (error) {
        stopForwarders()
        throw new Error(`Could not open the local game forwarders: ${error.message}`)
    }
}

function stopForwarders() {
    forwarders.forEach(server => {
        try { server.close() } catch (error) { }
    })
    forwarders = []
}

module.exports = { startForwarders, stopForwarders }
