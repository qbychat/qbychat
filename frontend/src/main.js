import {createApp} from 'vue'
import {createI18n} from 'vue-i18n'

import App from './App.vue'
import axios from "axios";

export const i18n = createI18n({
    locale: 'en-US',
    fallbackLocale: 'en-US',
    messages: {
        "en-US": {
            message: {
                hello: 'hello world'
            }
        },
        "ja": {
            message: {
                hello: 'こんにちは、世界'
            }
        },
        "zh-CN": {
            "message": {
                "hello": "你好，世界！"
            }
        }
    }
})

const loadedLanguages = ['en-US']

function setI18nLanguage(lang) {
    i18n.locale = lang
    axios.defaults.headers.common['Accept-Language'] = lang
    document.querySelector('html').setAttribute('lang', lang)
    return lang
}

export function loadLanguageAsync(lang) {
    if (i18n.locale === lang) {
        return Promise.resolve(setI18nLanguage(lang))
    }

    if (loadedLanguages.includes(lang)) {
        return Promise.resolve(setI18nLanguage(lang))
    }

    return import(/* webpackChunkName: "lang-[request]" */ `@/i18n/messages/${lang}.js`).then(
        messages => {
            i18n.setLocaleMessage(lang, messages.default)
            loadedLanguages.push(lang)
            return setI18nLanguage(lang)
        }
    )
}

let app = createApp(App);
app.use(i18n)
app.mount('#app')