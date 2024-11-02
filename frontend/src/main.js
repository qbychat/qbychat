import {createApp} from 'vue'
import {createI18n} from 'vue-i18n'
import en from './locales/en-US.json';
import zh from './locales/zh-CN.json';

import App from './App.vue'

const i18n = createI18n({
    locale: 'en', // 设置默认语言
    messages: {
        en: en,
        zh: zh,
    },
})

let app = createApp(App);
app.use(i18n)
app.mount('#app')
