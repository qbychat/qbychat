<script setup>

</script>

<template>

</template>

<script>
export default {
  created() {
    this.setInitialLanguage();
  },
  methods: {
    setInitialLanguage() {
      const browserLanguage = navigator.language;
      console.log(browserLanguage);
      const supportedLanguages = ['en-US', 'zh-CN'];
      const defaultLanguage = 'en-US';

      this.$i18n.locale = supportedLanguages.includes(browserLanguage) ? browserLanguage : defaultLanguage;
    },
  },
};
</script>

<style scoped>

</style>
