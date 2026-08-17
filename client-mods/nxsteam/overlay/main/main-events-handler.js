const { app, BrowserWindow } = require('electron')
const ipcPromise = require('ipc-promise')
const { postJson } = require('./api-client')
const { gameLaunch } = require('./game-launch')

function publicError(error) {
    return {
        message: error && error.message ? error.message : 'Unexpected launcher error.',
        status: error && error.status ? error.status : 0
    }
}

ipcPromise.on('accountStatus', params =>
    postJson('/launcher/account-status', { username: params.username }).catch(error => Promise.reject(publicError(error)))
)

ipcPromise.on('login', params =>
    postJson('/login', { username: params.username, password: params.password }).catch(error => Promise.reject(publicError(error)))
)

ipcPromise.on('register', params =>
    postJson('/launcher/register', { username: params.username, password: params.password }).catch(error => Promise.reject(publicError(error)))
)

ipcPromise.on('launchGame', async params => {
    try {
        await gameLaunch(params.token)
        const mainWindow = BrowserWindow.getAllWindows().find(win => win.name === 'main')
        if (mainWindow) mainWindow.hide()
        return true
    } catch (error) {
        return Promise.reject(publicError(error))
    }
})

ipcPromise.on('window', action => {
    const mainWindow = BrowserWindow.getAllWindows().find(win => win.name === 'main')
    if (action === 'minimize' && mainWindow) mainWindow.minimize()
    if (action === 'close') app.quit()
    return Promise.resolve(true)
})
