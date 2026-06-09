<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const showPwd = ref(false)
const loading = ref(false)
const errorMsg = ref('')
const uFocus = ref(false)
const pFocus = ref(false)

function fieldStyle(focused) {
  return {
    width: '100%', height: '46px', paddingLeft: '42px', paddingRight: '14px',
    background: focused ? '#fff' : '#F8FAFC',
    border: `1.5px solid ${focused ? '#667EEA' : '#E2E8F0'}`,
    borderRadius: '10px', outline: 'none', fontSize: '14px',
    boxSizing: 'border-box', color: '#1E293B', transition: 'all 0.2s',
    boxShadow: focused ? '0 0 0 3px rgba(102,126,234,0.12)' : 'none',
  }
}

async function handleLogin() {
  if (!username.value || !password.value) return
  errorMsg.value = ''
  loading.value = true
  try {
    await auth.login(username.value, password.value)
    router.push('/dashboard')
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '登录失败，请重试'
    errorMsg.value = msg
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="min-height:100vh;background:linear-gradient(135deg,#1F2542 0%,#252D52 50%,#2A2358 100%);display:flex;align-items:center;justify-content:center;padding:20px;position:relative;overflow:hidden;font-family:system-ui,-apple-system,sans-serif;">
    <!-- Background decorations -->
    <div style="position:absolute;inset:0;pointer-events:none;">
      <div style="position:absolute;top:-140px;left:-100px;width:520px;height:520px;border-radius:50%;background:radial-gradient(circle,rgba(102,126,234,0.22) 0%,transparent 65%);"></div>
      <div style="position:absolute;bottom:-120px;right:-80px;width:460px;height:460px;border-radius:50%;background:radial-gradient(circle,rgba(118,75,162,0.2) 0%,transparent 65%);"></div>
      <div style="position:absolute;top:38%;right:12%;width:220px;height:220px;border-radius:50%;background:radial-gradient(circle,rgba(167,139,250,0.1) 0%,transparent 70%);"></div>
      <div style="position:absolute;inset:0;background-image:radial-gradient(rgba(255,255,255,0.04) 1px,transparent 1px);background-size:28px 28px;"></div>
    </div>

    <!-- Card -->
    <div style="background:#fff;border-radius:20px;padding:48px 40px;width:100%;max-width:420px;box-shadow:0 8px 40px rgba(102,126,234,0.14),0 2px 12px rgba(0,0,0,0.06);position:relative;z-index:1;">
      <!-- Logo -->
      <div style="text-align:center;margin-bottom:36px;">
        <div style="width:64px;height:64px;background:linear-gradient(135deg,#667EEA,#764BA2);border-radius:18px;display:inline-flex;align-items:center;justify-content:center;margin-bottom:16px;font-size:32px;box-shadow:0 8px 24px rgba(102,126,234,0.4);">🐜</div>
        <div style="font-size:24px;font-weight:700;color:#1E293B;letter-spacing:-0.3px;">蚂蚁记账</div>
        <div style="font-size:13px;color:#94A3B8;margin-top:5px;letter-spacing:0.04em;">ADMIN CONSOLE</div>
      </div>

      <!-- Divider -->
      <div style="height:1px;background:linear-gradient(90deg,transparent,rgba(102,126,234,0.2),transparent);margin-bottom:28px;"></div>

      <form @submit.prevent="handleLogin">
        <!-- Username -->
        <div style="margin-bottom:14px;">
          <label style="display:block;font-size:13px;font-weight:500;color:#475569;margin-bottom:6px;">用户名</label>
          <div style="position:relative;">
            <span style="position:absolute;left:13px;top:50%;transform:translateY(-50%);color:#94A3B8;display:flex;z-index:1;transition:color 0.2s;" :style="{color: uFocus ? '#667EEA' : '#94A3B8'}">
              <el-icon size="15"><User /></el-icon>
            </span>
            <input
              v-model="username"
              type="text"
              placeholder="请输入用户名"
              :style="fieldStyle(uFocus)"
              @focus="uFocus = true"
              @blur="uFocus = false"
            />
          </div>
        </div>

        <!-- Password -->
        <div style="margin-bottom:28px;">
          <label style="display:block;font-size:13px;font-weight:500;color:#475569;margin-bottom:6px;">密码</label>
          <div style="position:relative;">
            <span style="position:absolute;left:13px;top:50%;transform:translateY(-50%);color:#94A3B8;display:flex;z-index:1;transition:color 0.2s;" :style="{color: pFocus ? '#667EEA' : '#94A3B8'}">
              <el-icon size="15"><Lock /></el-icon>
            </span>
            <input
              v-model="password"
              :type="showPwd ? 'text' : 'password'"
              placeholder="请输入密码"
              :style="{...fieldStyle(pFocus), paddingRight: '44px'}"
              @focus="pFocus = true"
              @blur="pFocus = false"
            />
            <button type="button" @click="showPwd = !showPwd"
              style="position:absolute;right:12px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;color:#94A3B8;padding:0;display:flex;transition:color 0.15s;"
              @mouseenter="e => e.currentTarget.style.color = '#667EEA'"
              @mouseleave="e => e.currentTarget.style.color = '#94A3B8'"
            >
              <el-icon v-if="showPwd" size="15"><Hide /></el-icon>
              <el-icon v-else size="15"><View /></el-icon>
            </button>
          </div>
        </div>

        <!-- Error message -->
        <div v-if="errorMsg" style="margin-bottom:16px;padding:10px 14px;background:#FFF1F2;border:1px solid #FECDD3;border-radius:8px;display:flex;align-items:center;gap:8px;">
          <el-icon style="color:#F43F5E;font-size:14px;flex-shrink:0;"><WarningFilled /></el-icon>
          <span style="color:#BE123C;font-size:13px;">{{ errorMsg }}</span>
        </div>

        <!-- Submit -->
        <button
          type="submit"
          :disabled="loading"
          :style="{
            width: '100%', height: '46px',
            background: loading ? 'rgba(102,126,234,0.5)' : 'linear-gradient(135deg,#667EEA,#764BA2)',
            color: '#fff', border: 'none', borderRadius: '10px',
            fontSize: '15px', fontWeight: 600, cursor: loading ? 'not-allowed' : 'pointer',
            letterSpacing: '3px', transition: 'all 0.2s',
            boxShadow: loading ? 'none' : '0 4px 16px rgba(102,126,234,0.45)',
          }"
          @mouseenter="e => { if (!loading) e.currentTarget.style.boxShadow = '0 6px 22px rgba(102,126,234,0.55)' }"
          @mouseleave="e => { if (!loading) e.currentTarget.style.boxShadow = '0 4px 16px rgba(102,126,234,0.45)' }"
        >
          {{ loading ? '登录中...' : '登　录' }}
        </button>
      </form>

      <div style="text-align:center;margin-top:28px;font-size:12px;color:#CBD5E1;">
        © 2026 蚂蚁记账 · All Rights Reserved
      </div>
    </div>
  </div>
</template>
