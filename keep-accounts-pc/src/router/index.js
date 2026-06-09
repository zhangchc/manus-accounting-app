import { createRouter, createWebHashHistory } from 'vue-router'
import AdminLayout from '@/layout/AdminLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { hidden: true },
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' },
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '管理员管理', icon: 'User' },
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' },
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu' },
      },
      {
        path: 'app/user',
        name: 'AppUser',
        component: () => import('@/views/app/user/index.vue'),
        meta: { title: '小程序用户', icon: 'Avatar' },
      },
      {
        path: 'app/record',
        name: 'AppRecord',
        component: () => import('@/views/app/record/index.vue'),
        meta: { title: '记账记录', icon: 'Collection' },
      },
      {
        path: 'app/category',
        name: 'AppCategory',
        component: () => import('@/views/app/category/index.vue'),
        meta: { title: '分类管理', icon: 'Grid' },
      },
      {
        path: 'app/log',
        name: 'AppLog',
        component: () => import('@/views/app/log/index.vue'),
        meta: { title: '操作日志', icon: 'Document' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

// Auth guard
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
