const fs = require('fs')
const path = require('path')

const GAME_HOST = 'danny-games.servegame.com'
const LOGIN_PORT = 8484
const CHANNEL_PORT_START = 8585
const CHANNEL_PORT_END = 8604

function findGameExecutable() {
    const suppliedPath = process.argv.slice(1).find(arg =>
        typeof arg === 'string' && arg.toLowerCase().endsWith('.exe') && fs.existsSync(arg)
    )
    if (suppliedPath) {
        return path.resolve(suppliedPath)
    }
    return path.resolve(process.resourcesPath, '..', '..', 'MapleStory.exe')
}

module.exports = function loadSettings() {
    return {
        app: {
            title: 'ComfyStory',
            exec_path: findGameExecutable()
        },
        api_base_url: `https://${GAME_HOST}/api`,
        game_host: GAME_HOST,
        login_port: LOGIN_PORT,
        channel_port_start: CHANNEL_PORT_START,
        channel_port_end: CHANNEL_PORT_END,
        request_timeout_ms: 10000
    }
}
