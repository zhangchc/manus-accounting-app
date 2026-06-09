<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

onMounted(() => {
  if (auth.isLoggedIn() && auth.menus.length === 0) {
    auth.fetchUserInfo()
  }
})
</script>

<template>
  <router-view />
</template>
