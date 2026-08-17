const { app, BrowserWindow, shell } = require('electron')
const bootup = require('./main/init')
const { openWindow } = require('./main/windows-handler')

global.application = { errors: [] }

function appReady() {
    if (app.isReady()) {
        return Promise.resolve()
    }
    return new Promise(resolve => app.once('ready', resolve))
}

process.on('uncaughtException', error => {
    console.error(error)
    global.application.errors.push(error.message || String(error))
    const mainWindow = BrowserWindow.getAllWindows().find(win => win.name === 'main')
    if (mainWindow) {
        mainWindow.webContents.send('fatalError', error.message || String(error))
    }
})

appReady()
    .then(bootup)
    .then(() => openWindow('main'))
    .catch(error => {
        console.error('Launcher initialization failed:', error)
        app.quit()
    })

app.on('web-contents-created', (event, contents) => {
    contents.on('new-window', (openEvent, targetUrl) => {
        openEvent.preventDefault()
        if (/^https:\/\//i.test(targetUrl)) {
            shell.openExternal(targetUrl)
        }
    })
})

app.on('window-all-closed', () => app.quit())
