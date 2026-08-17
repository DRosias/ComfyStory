const https = require('https')
const { URL } = require('url')

function postJson(route, body) {
    const endpoint = new URL(global.settings.api_base_url + route)
    const payload = Buffer.from(JSON.stringify(body), 'utf8')

    return new Promise((resolve, reject) => {
        const request = https.request({
            protocol: endpoint.protocol,
            hostname: endpoint.hostname,
            port: endpoint.port || 443,
            path: endpoint.pathname + endpoint.search,
            method: 'POST',
            rejectUnauthorized: true,
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': payload.length,
                'User-Agent': 'ComfyStory-Launcher/1.0'
            }
        }, response => {
            let responseBody = ''
            response.setEncoding('utf8')
            response.on('data', chunk => { responseBody += chunk })
            response.on('end', () => {
                let parsed = {}
                if (responseBody) {
                    try {
                        parsed = JSON.parse(responseBody)
                    } catch (error) {
                        parsed = {}
                    }
                }
                if (response.statusCode >= 200 && response.statusCode < 300) {
                    resolve(parsed)
                    return
                }
                const message = parsed.err_msg || parsed.message || parsed.error ||
                    (response.statusCode === 403 ? 'Incorrect username or password.' : 'The server rejected the request.')
                const requestError = new Error(message)
                requestError.status = response.statusCode
                reject(requestError)
            })
        })

        request.setTimeout(global.settings.request_timeout_ms, () => {
            request.destroy(new Error('The server did not respond in time.'))
        })
        request.on('error', reject)
        request.write(payload)
        request.end()
    })
}

module.exports = { postJson }
