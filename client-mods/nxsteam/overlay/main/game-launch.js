const { app, BrowserWindow } = require('electron')
const { spawn } = require('child_process')
const path = require('path')
const { startForwarders, stopForwarders } = require('./tcp-forwarder')

let gameProcess = null

async function gameLaunch(token) {
    if (gameProcess) {
        throw new Error('MapleStory is already running from this launcher.')
    }
    if (typeof token !== 'string' || token.length === 0) {
        throw new Error('The server did not return a login token.')
    }

    await startForwarders()
    try {
        gameProcess = spawn(global.settings.app.exec_path, ['WebStart', token], {
            cwd: path.dirname(global.settings.app.exec_path),
            shell: false,
            windowsHide: false,
            stdio: 'ignore'
        })
    } catch (error) {
        stopForwarders()
        gameProcess = null
        throw error
    }

    if (!gameProcess.pid) {
        stopForwarders()
        gameProcess = null
        throw new Error('Windows could not start MapleStory.exe.')
    }

    gameProcess.once('error', error => {
        console.error('MapleStory launch error:', error.message)
        stopForwarders()
        gameProcess = null
        const mainWindow = BrowserWindow.getAllWindows().find(win => win.name === 'main')
        if (mainWindow) {
            mainWindow.show()
            mainWindow.webContents.send('launchError', error.message)
        }
    })
    gameProcess.once('close', () => {
        stopForwarders()
        gameProcess = null
        app.quit()
    })

    return true
}

module.exports = { gameLaunch }
