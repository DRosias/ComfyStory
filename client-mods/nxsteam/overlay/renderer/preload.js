const { ipcRenderer } = require('electron')
const ipcPromise = require('ipc-promise')

window.comfyLauncher = Object.freeze({
    request(topic, data) {
        return ipcPromise.send(topic, data || {})
    },
    close() {
        return ipcPromise.send('window', 'close')
    },
    minimize() {
        return ipcPromise.send('window', 'minimize')
    },
    on(topic, callback) {
        ipcRenderer.on(topic, (event, message) => callback(message))
    }
})
