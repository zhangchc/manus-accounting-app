<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const expanded = ref([])
const userMenuOpen = ref(false)

const flatPages = computed(() => {
  const map = {}
  if (auth.menus && auth.menus.length) {
    function walk(items) {
      for (const m of items) {
        if (m.type === 'menu' && m.path) {
          map[m.path] = m.name
        }
        if (m.children) walk(m.children)
      }
    }
    walk(auth.menus)
  }
  return map
})

const navGroups = computed(() => {
  const result = [
    { key: 'dashboard', label: '仪表盘', route: '/dashboard', icon: 'DataAnalysis' }
  ]
  if (auth.menus && auth.menus.length) {
    for (const m of auth.menus) {
      if (m.type === 'dir') {
        result.push({
          key: String(m.id),
          label: m.name,
          icon: m.icon,
          children: (m.children || []).map(c => ({
            key: String(c.id),
            label: c.name,
            icon: c.icon,
            route: c.path,
          }))
        })
      } else if (m.type === 'menu') {
        result.push({
          key: String(m.id),
          label: m.name,
          route: m.path,
          icon: m.icon,
        })
      }
    }
  }
  return result
})

const pageNames = computed(() => {
  const map = { '/dashboard': '仪表盘' }
  if (auth.menus && auth.menus.length) {
    function walk(items) {
      for (const m of items) {
        if (m.path) map[m.path] = m.name
        if (m.children) walk(m.children)
      }
    }
    walk(auth.menus)
  }
  return map
})

function isActive(routePath) { return route.path === routePath }
function isGroupActive(group) { return group.children?.some(c => isActive(c.route)) }

function toggle(key) {
  expanded.value = expanded.value.includes(key)
    ? expanded.value.filter(k => k !== key)
    : [...expanded.value, key]
}

function navTo(path) { router.push(path) }

const breadcrumbs = computed(() => {
  const label = pageNames.value[route.path] || ''
  return { label }
})

const userInitial = computed(() => {
  const name = auth.userInfo?.nickname || auth.userInfo?.username || 'A'
  return name[0]
})

function handleLogout() {
  auth.logout()
  router.push('/login')
}

watch(() => auth.menus, (menus) => {
  if (menus && menus.length) {
    expanded.value = navGroups.value.filter(g => g.children).map(g => g.key)
  }
}, { immediate: true })
</script>

<template>
  <div style="display:flex;height:100vh;overflow:hidden;font-family:system-ui,-apple-system,sans-serif;">
    <!-- Sidebar -->
    <div style="width:220px;min-width:220px;background:linear-gradient(180deg,#252D52 0%,#222948 50%,#1F2542 100%);display:flex;flex-direction:column;overflow:hidden;box-shadow:3px 0 16px rgba(37,45,82,0.18);">
      <!-- Logo -->
      <div style="padding:20px 16px 16px;flex-shrink:0;">
        <div style="display:flex;align-items:center;gap:10px;">
          <div style="width:38px;height:38px;flex-shrink:0;background:linear-gradient(135deg,#667EEA,#764BA2);border-radius:11px;display:flex;align-items:center;justify-content:center;font-size:19px;box-shadow:0 4px 14px rgba(102,126,234,0.4);">🐜</div>
          <div>
            <div style="color:#fff;font-size:15px;font-weight:700;line-height:1.2;">蚂蚁记账</div>
            <div style="color:rgba(255,255,255,0.38);font-size:11px;margin-top:2px;letter-spacing:0.05em;">ADMIN CONSOLE</div>
          </div>
        </div>
        <div style="height:1px;background:linear-gradient(90deg,rgba(255,255,255,0.1),transparent);margin-top:16px;"></div>
      </div>

      <!-- Nav -->
      <nav style="flex:1;overflow-y:auto;padding:4px 0 12px;">
        <template v-for="item in navGroups" :key="item.key">
          <!-- Single item -->
          <div v-if="!item.children" style="padding:2px 8px;">
            <button @click="navTo(item.route)" :style="{
              width:'100%',display:'flex',alignItems:'center',gap:'8px',
              padding:'9px 10px',
              background: isActive(item.route) ? 'rgba(255,255,255,0.1)' : 'transparent',
              border:'none',cursor:'pointer',fontSize:'13px',textAlign:'left',transition:'all 0.18s',boxSizing:'border-box',
              color: isActive(item.route) ? '#fff' : 'rgba(255,255,255,0.48)',
              borderRadius:'10px',position:'relative',
            }"
              @mouseenter="e => { if (!isActive(item.route)) { e.currentTarget.style.background = 'rgba(255,255,255,0.06)'; e.currentTarget.style.color = 'rgba(255,255,255,0.82)' } }"
              @mouseleave="e => { if (!isActive(item.route)) { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = 'rgba(255,255,255,0.48)' } }"
            >
              <span v-if="isActive(item.route)" style="position:absolute;left:0;top:50%;transform:translateY(-50%);width:3px;height:16px;border-radius:0 2px 2px 0;background:linear-gradient(180deg,#A5B4FC,#C084FC);"></span>
              <el-icon v-if="item.icon" style="font-size:14px;flex-shrink:0;"><component :is="item.icon" /></el-icon>
              <span style="line-height:1;font-weight:400;" :style="{fontWeight: isActive(item.route) ? 600 : 400}">{{ item.label }}</span>
            </button>
          </div>

          <!-- Group -->
          <div v-else style="padding:2px 8px 2px;">
            <button @click="toggle(item.key)" :style="{
              width:'100%',display:'flex',alignItems:'center',
              padding:'8px 8px',background:'transparent',border:'none',cursor:'pointer',
              color: isGroupActive(item) ? 'rgba(255,255,255,0.75)' : 'rgba(255,255,255,0.3)',
              fontSize:'13px',fontWeight:500,letterSpacing:'0',
              justifyContent:'space-between',boxSizing:'border-box',transition:'color 0.18s',
            }"
              @mouseenter="e => { e.currentTarget.style.color = 'rgba(255,255,255,0.65)' }"
              @mouseleave="e => { e.currentTarget.style.color = isGroupActive(item) ? 'rgba(255,255,255,0.75)' : 'rgba(255,255,255,0.3)' }"
            >
              <span style="display:flex;align-items:center;gap:6px;">
                <el-icon v-if="item.icon" style="font-size:12px;"><component :is="item.icon" /></el-icon>
                {{ item.label }}
              </span>
              <el-icon :style="{transform: expanded.includes(item.key) ? 'rotate(0deg)' : 'rotate(-90deg)',transition:'transform 0.22s',flexShrink:0,opacity:0.5,fontSize:'11px'}"><ArrowDown /></el-icon>
            </button>
            <div v-if="expanded.includes(item.key)" style="margin-bottom:4px;">
              <div v-for="child in item.children" :key="child.key" style="padding:0;">
                <button @click="navTo(child.route)" :style="{
                  width:'100%',display:'flex',alignItems:'center',gap:'8px',
                  padding:'8px 10px 8px 32px',
                  background: isActive(child.route) ? 'rgba(255,255,255,0.1)' : 'transparent',
                  border:'none',cursor:'pointer',
                  fontSize:'13px',textAlign:'left',transition:'all 0.18s',boxSizing:'border-box',
                  color: isActive(child.route) ? '#fff' : 'rgba(255,255,255,0.48)',
                  borderRadius:'10px',position:'relative',
                }"
                  @mouseenter="e => { if (!isActive(child.route)) { e.currentTarget.style.background = 'rgba(255,255,255,0.06)'; e.currentTarget.style.color = 'rgba(255,255,255,0.82)' } }"
                  @mouseleave="e => { if (!isActive(child.route)) { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = 'rgba(255,255,255,0.48)' } }"
                >
                  <span v-if="isActive(child.route)" style="position:absolute;left:0;top:50%;transform:translateY(-50%);width:3px;height:16px;border-radius:0 2px 2px 0;background:linear-gradient(180deg,#A5B4FC,#C084FC);"></span>
                  <el-icon v-if="child.icon" style="font-size:14px;flex-shrink:0;"><component :is="child.icon" /></el-icon>
                  <span style="font-size:13px;line-height:1;" :style="{fontWeight: isActive(child.route) ? 600 : 400}">{{ child.label }}</span>
                </button>
              </div>
            </div>
          </div>
        </template>
      </nav>

      <!-- Bottom user card -->
      <div style="padding:12px;border-top:1px solid rgba(255,255,255,0.07);flex-shrink:0;">
        <div style="display:flex;align-items:center;gap:9px;padding:9px 10px;border-radius:12px;background:rgba(255,255,255,0.06);border:1px solid rgba(255,255,255,0.08);">
          <div style="width:30px;height:30px;border-radius:8px;background:linear-gradient(135deg,#667EEA,#764BA2);display:flex;align-items:center;justify-content:center;color:#fff;font-size:12px;font-weight:700;flex-shrink:0;">{{ userInitial }}</div>
          <div style="flex:1;min-width:0;">
            <div style="color:#fff;font-size:12px;font-weight:600;line-height:1.3;">{{ auth.userInfo?.username || '-' }}</div>
            <div style="color:rgba(255,255,255,0.35);font-size:11px;margin-top:1px;">{{ auth.userInfo?.nickname || '' }}</div>
          </div>
          <button @click="handleLogout" title="退出登录"
            style="background:none;border:none;cursor:pointer;color:rgba(255,255,255,0.25);padding:4px;display:flex;border-radius:6px;transition:color 0.15s;"
            @mouseenter="e => e.currentTarget.style.color = '#F87171'"
            @mouseleave="e => e.currentTarget.style.color = 'rgba(255,255,255,0.25)'"
          >
            <el-icon style="font-size:14px;"><SwitchButton /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <!-- Main -->
    <div style="flex:1;display:flex;flex-direction:column;overflow:hidden;min-width:0;">
      <!-- Topbar -->
      <div style="height:56px;background:#fff;border-bottom:1px solid #EAECF0;display:flex;align-items:center;padding:0 24px;justify-content:space-between;flex-shrink:0;box-shadow:0 1px 4px rgba(0,0,0,0.04);">
        <div style="display:flex;align-items:center;gap:6px;font-size:13px;color:#94A3B8;">
          <span>首页</span>
          <el-icon style="font-size:11px;"><ArrowRight /></el-icon>
          <span style="color:#1E293B;font-weight:500;">{{ breadcrumbs.label }}</span>
        </div>
        <div style="display:flex;align-items:center;gap:12px;">
          <button style="background:none;border:none;cursor:pointer;color:#94A3B8;padding:7px;display:flex;border-radius:10px;position:relative;transition:background 0.15s;"
            @mouseenter="e => e.currentTarget.style.background = '#F0F2F8'"
            @mouseleave="e => e.currentTarget.style.background = 'none'"
          >
            <el-icon style="font-size:17px;"><Bell /></el-icon>
            <span style="position:absolute;top:5px;right:5px;width:6px;height:6px;border-radius:50%;background:#F43F5E;border:1.5px solid #fff;"></span>
          </button>
          <div style="position:relative;">
            <button @click="userMenuOpen = !userMenuOpen"
              style="display:flex;align-items:center;gap:8px;background:none;border:none;cursor:pointer;padding:5px 8px;border-radius:10px;transition:background 0.15s;"
              @mouseenter="e => e.currentTarget.style.background = '#F0F2F8'"
              @mouseleave="e => e.currentTarget.style.background = 'transparent'"
            >
              <div style="width:32px;height:32px;border-radius:10px;background:linear-gradient(135deg,#667EEA,#764BA2);display:flex;align-items:center;justify-content:center;color:#fff;font-size:13px;font-weight:700;flex-shrink:0;">{{ userInitial }}</div>
              <span style="font-size:13px;color:#1E293B;font-weight:600;">{{ auth.userInfo?.username || '-' }}</span>
              <el-icon :style="{fontSize:'12px',color:'#94A3B8',transition:'transform 0.2s',transform: userMenuOpen ? 'rotate(180deg)' : 'rotate(0)'}"><ArrowDown /></el-icon>
            </button>
            <div v-if="userMenuOpen" style="position:absolute;right:0;top:48px;background:#fff;border-radius:14px;box-shadow:0 8px 32px rgba(0,0,0,0.12);border:1px solid #F1F5F9;min-width:160px;z-index:200;overflow:hidden;">
              <div style="padding:14px 16px;border-bottom:1px solid #F8FAFC;background:linear-gradient(135deg,rgba(102,126,234,0.05),rgba(118,75,162,0.05));">
                <div style="display:flex;align-items:center;gap:10px;">
                  <div style="width:36px;height:36px;border-radius:10px;background:linear-gradient(135deg,#667EEA,#764BA2);display:flex;align-items:center;justify-content:center;color:#fff;font-size:14px;font-weight:700;">{{ userInitial }}</div>
                  <div>
                    <div style="font-size:13px;font-weight:600;color:#1E293B;">{{ auth.userInfo?.username || '-' }}</div>
                    <div style="font-size:11px;color:#94A3B8;margin-top:1px;">{{ auth.userInfo?.nickname || '' }}</div>
                  </div>
                </div>
              </div>
              <div style="padding:6px;">
                <button @click="userMenuOpen = false; handleLogout()"
                  style="width:100%;display:flex;align-items:center;gap:8px;padding:9px 12px;background:none;border:none;cursor:pointer;color:#F43F5E;font-size:13px;border-radius:8px;transition:background 0.15s;"
                  @mouseenter="e => e.currentTarget.style.background = '#FFF1F2'"
                  @mouseleave="e => e.currentTarget.style.background = 'transparent'"
                >
                  <el-icon style="font-size:13px;"><SwitchButton /></el-icon>退出登录
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Content -->
      <div style="flex:1;overflow:auto;background:#F0F2F8;">
        <router-view />
      </div>
    </div>
  </div>
</template>
