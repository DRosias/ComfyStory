const fs = require('fs')
const loadSettings = require('./load-settings')

module.exports = async function initialize() {
    const settings = loadSettings()
    if (!fs.existsSync(settings.app.exec_path)) {
        throw new Error('MapleStory.exe was not found beside the nxsteam folder.')
    }
    global.settings = settings
    require('./main-events-handler')
}
