(function () {
    'use strict'

    const usernameForm = document.getElementById('username-form')
    const passwordForm = document.getElementById('password-form')
    const registerForm = document.getElementById('register-form')
    const usernameInput = document.getElementById('username')
    const passwordInput = document.getElementById('password')
    const newPasswordInput = document.getElementById('new-password')
    const verifyPasswordInput = document.getElementById('verify-password')
    const message = document.getElementById('message')
    let username = ''

    function show(form) {
        ;[usernameForm, passwordForm, registerForm].forEach(item => item.classList.add('hidden'))
        form.classList.remove('hidden')
    }

    function setMessage(text, type) {
        message.textContent = text || ''
        message.className = type || ''
    }

    function setBusy(form, busy) {
        Array.prototype.forEach.call(form.querySelectorAll('button,input'), control => { control.disabled = busy })
    }

    function errorMessage(error) {
        if (!error) return 'Unexpected launcher error.'
        if (error.message) return error.message
        if (error.error && error.error.message) return error.error.message
        return String(error)
    }

    async function launch(result) {
        if (!result || !result.token) throw new Error('The server did not return a login token.')
        setMessage('Login successful. Starting MapleStory...', 'success')
        await window.comfyLauncher.request('launchGame', { token: result.token })
    }

    usernameForm.addEventListener('submit', async event => {
        event.preventDefault()
        username = usernameInput.value.trim()
        if (username.length < 4) {
            setMessage('Username must be at least 4 characters.', 'error')
            return
        }
        setBusy(usernameForm, true)
        setMessage('Checking account...')
        try {
            const status = await window.comfyLauncher.request('accountStatus', { username })
            setMessage('')
            if (status.exists) {
                document.getElementById('existing-username').textContent = username
                show(passwordForm)
                passwordInput.focus()
            } else {
                document.getElementById('new-username').textContent = username
                show(registerForm)
                newPasswordInput.focus()
            }
        } catch (error) {
            setMessage(errorMessage(error), 'error')
        } finally {
            setBusy(usernameForm, false)
        }
    })

    passwordForm.addEventListener('submit', async event => {
        event.preventDefault()
        setBusy(passwordForm, true)
        setMessage('Signing in...')
        try {
            const result = await window.comfyLauncher.request('login', { username, password: passwordInput.value })
            await launch(result)
        } catch (error) {
            setMessage(errorMessage(error), 'error')
            setBusy(passwordForm, false)
            passwordInput.select()
        }
    })

    registerForm.addEventListener('submit', async event => {
        event.preventDefault()
        if (newPasswordInput.value !== verifyPasswordInput.value) {
            setMessage('The passwords do not match.', 'error')
            verifyPasswordInput.select()
            return
        }
        setBusy(registerForm, true)
        setMessage('Creating account...')
        try {
            const result = await window.comfyLauncher.request('register', { username, password: newPasswordInput.value })
            await launch(result)
        } catch (error) {
            setMessage(errorMessage(error), 'error')
            setBusy(registerForm, false)
        }
    })

    Array.prototype.forEach.call(document.querySelectorAll('.back'), button => {
        button.addEventListener('click', () => {
            passwordInput.value = ''
            newPasswordInput.value = ''
            verifyPasswordInput.value = ''
            setMessage('')
            show(usernameForm)
            usernameInput.focus()
        })
    })

    document.getElementById('close').addEventListener('click', () => window.comfyLauncher.close())
    document.getElementById('minimize').addEventListener('click', () => window.comfyLauncher.minimize())
    window.comfyLauncher.on('launchError', text => {
        show(passwordForm)
        setBusy(passwordForm, false)
        setMessage(text, 'error')
    })
    window.comfyLauncher.on('fatalError', text => setMessage(text, 'error'))
})()
