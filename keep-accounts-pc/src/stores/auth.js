import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('admin_user') || 'null'))
  const permissions = ref([])
  const menus = ref([])

  const isLoggedIn = () => !!token.value

  function login(username, password) {
    return new Promise((resolve) => {
      setTimeout(() => {
        const t = 'mock-admin-jwt-token'
        const user = {
          id: 1,
          username,
          nickname: '超级管理员',
          avatar: '',
          roles: ['ROLE_SUPER_ADMIN'],
        }
        token.value = t
        userInfo.value = user
        permissions.value = ['sys:user:list', 'sys:user:create', 'sys:user:update', 'sys:user:delete',
          'sys:role:list', 'sys:role:create', 'sys:role:update', 'sys:role:delete',
          'sys:menu:list', 'sys:menu:create', 'sys:menu:update', 'sys:menu:delete',
          'app:user:list', 'app:record:list', 'app:category:list', 'app:log:list']
        localStorage.setItem('admin_token', t)
        localStorage.setItem('admin_user', JSON.stringify(user))
        resolve({ token: t, userInfo: user, permissions: permissions.value, menus: menus.value })
      }, 800)
    })
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    menus.value = []
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
  }

  return { token, userInfo, permissions, menus, isLoggedIn, login, logout }
})
