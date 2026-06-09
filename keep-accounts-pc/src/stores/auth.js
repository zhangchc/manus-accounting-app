import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/auth'
import { addMenuRoutes } from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('admin_user') || 'null'))
  const permissions = ref(JSON.parse(localStorage.getItem('admin_permissions') || '[]'))
  const menus = ref(JSON.parse(localStorage.getItem('admin_menus') || '[]'))

  const isLoggedIn = () => !!token.value

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const info = await getUserInfo()
      menus.value = info.menus || []
      permissions.value = info.permissions || []
      localStorage.setItem('admin_menus', JSON.stringify(menus.value))
      localStorage.setItem('admin_permissions', JSON.stringify(permissions.value))
      addMenuRoutes(menus.value)
      userInfo.value = { id: info.userId, username: info.username, nickname: info.nickname }
      localStorage.setItem('admin_user', JSON.stringify(userInfo.value))
    } catch (e) {
      logout()
    }
  }

  async function login(username, password) {
    const res = await loginApi({ username, password })
    token.value = res.token
    userInfo.value = { id: res.userId, username: res.username, nickname: res.nickname }
    localStorage.setItem('admin_token', res.token)
    localStorage.setItem('admin_user', JSON.stringify(userInfo.value))

    const info = await getUserInfo()
    menus.value = info.menus || []
    permissions.value = info.permissions || []
    localStorage.setItem('admin_menus', JSON.stringify(menus.value))
    localStorage.setItem('admin_permissions', JSON.stringify(permissions.value))
    addMenuRoutes(menus.value)
    userInfo.value = { id: info.userId, username: info.username, nickname: info.nickname }
    localStorage.setItem('admin_user', JSON.stringify(userInfo.value))

    return { token: token.value, userInfo: userInfo.value, permissions: permissions.value, menus: menus.value }
  }

  function hasPermission(code) {
    return permissions.value.includes(code)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    menus.value = []
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
    localStorage.removeItem('admin_menus')
    localStorage.removeItem('admin_permissions')
  }

  return { token, userInfo, permissions, menus, isLoggedIn, hasPermission, login, fetchUserInfo, logout }
})
