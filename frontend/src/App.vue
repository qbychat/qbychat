<script setup>

</script>

<template>
<h1> {{ $t('message.hello') }}</h1>
</template>

<script>
import {loadLanguageAsync} from "@/main.js";

export default {
  created() {
    this.setInitialLanguage();
  },
  methods: {
    setInitialLanguage() {
      const browserLanguage = navigator.language;
      loadLanguageAsync(browserLanguage)
    },
  },
};
</script>

<style scoped>

</style>
