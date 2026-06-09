import { createRouter, createWebHashHistory } from 'vue-router'
import AdminLayout from '@/layout/AdminLayout.vue'
import { useAuthStore } from '@/stores/auth'

const viewModules = import.meta.glob('../views/**/*.vue')

function resolveComponent(componentPath) {
  return viewModules[`../${componentPath}.vue`]
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { hidden: true },
  },
  {
    path: '/',
    name: 'layout',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

export function addMenuRoutes(menus) {
  if (!menus || !menus.length) return

  function walk(items) {
    for (const item of items) {
      if (item.type === 'menu' && item.path && item.component) {
        const component = resolveComponent(item.component)
        if (component) {
          router.addRoute('layout', {
            path: item.path,
            name: item.name,
            component,
            meta: { title: item.name, icon: item.icon },
          })
        }
      }
      if (item.children) walk(item.children)
    }
  }
  walk(menus)
}

// Auth guard
router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.isLoggedIn()) {
    next('/login')
  } else if (to.path === '/login' && auth.isLoggedIn()) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
