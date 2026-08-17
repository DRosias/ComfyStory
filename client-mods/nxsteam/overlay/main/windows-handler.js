if (global._windows) return

const { BrowserWindow } = require('electron')
const path = require('path')
const url = require('url')

global._windows = {}

function openWindow(name) {
    if (global._windows[name]) {
        global._windows[name].show()
        return global._windows[name]
    }
    if (name !== 'main') return null

    const window = new BrowserWindow({
        width: 520,
        height: 500,
        frame: false,
        show: false,
        center: true,
        resizable: false,
        maximizable: false,
        backgroundColor: '#121720',
        webPreferences: {
            nodeIntegration: false,
            webSecurity: true,
            preload: path.join(__dirname, '../renderer/preload.js')
        }
    })
    window.name = name
    window.loadURL(url.format({
        pathname: path.join(__dirname, '../template/login.html'),
        protocol: 'file:',
        slashes: true
    }))
    window.once('ready-to-show', () => window.show())
    window.on('closed', () => { delete global._windows[name] })
    global._windows[name] = window
    return window
}

module.exports = { openWindow }
