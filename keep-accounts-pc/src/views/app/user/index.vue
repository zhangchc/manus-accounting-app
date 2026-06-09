<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshRight, CopyDocument, Close } from '@element-plus/icons-vue'
import { getAppUserList, getAppUserDetail } from '@/api/app-user'

const avatarGradients = [
  'linear-gradient(135deg,#667EEA,#764BA2)',
  'linear-gradient(135deg,#11998E,#38EF7D)',
  'linear-gradient(135deg,#F7971E,#FFD200)',
  'linear-gradient(135deg,#F953C6,#B91D73)',
  'linear-gradient(135deg,#A855F7,#6366F1)',
  'linear-gradient(135deg,#06B6D4,#3B82F6)',
  'linear-gradient(135deg,#F43F5E,#FB923C)',
]

const drawer = ref(null)
const detailData = ref(null)
const detailLoading = ref(false)

const users = ref([])
const total = ref(0)
const loading = ref(false)

const searchNickName = ref('')
const searchOpenId = ref('')
const dateStart = ref('')
const dateEnd = ref('')
const page = ref(1)
const pageSize = ref(10)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

async function loadUsers() {
  loading.value = true
  try {
    const data = await getAppUserList({
      nickName: searchNickName.value || undefined,
      openId: searchOpenId.value || undefined,
      startDate: dateStart.value || undefined,
      endDate: dateEnd.value || undefined,
      page: page.value,
      pageSize: pageSize.value,
    })
    users.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUsers()
})

function handleSearch() {
  page.value = 1
  loadUsers()
}

function handleReset() {
  searchNickName.value = ''
  searchOpenId.value = ''
  dateStart.value = ''
  dateEnd.value = ''
  page.value = 1
  loadUsers()
}

function handlePageChange(p) {
  page.value = p
  loadUsers()
}

async function openDrawer(user) {
  drawer.value = user
  detailData.value = null
  detailLoading.value = true
  try {
    const data = await getAppUserDetail(user.id)
    detailData.value = data
  } catch (e) {
    ElMessage.error('加载用户详情失败')
  } finally {
    detailLoading.value = false
  }
}

function copyOpenid(openid) {
  navigator.clipboard?.writeText(openid)
}

const thSt = {
  padding: '12px 20px', fontSize: '12px', color: '#94A3B8', fontWeight: '600',
  textAlign: 'left', whiteSpace: 'nowrap', background: '#F8FAFC',
  borderBottom: '1px solid #F1F5F9', letterSpacing: '0.03em',
}
const tdSt = {
  padding: '14px 20px', fontSize: '13px', color: '#334155',
  borderBottom: '1px solid #F8FAFC', whiteSpace: 'nowrap',
}
const fieldStyle = {
  height: '36px', padding: '0 12px', borderRadius: '10px', background: '#F8FAFC',
  border: '1px solid #E2E8F0', fontSize: '13px', color: '#334155',
  outline: 'none', boxSizing: 'border-box',
}
</script>

<template>
  <div style="padding:24px;background:#F0F2F8;min-height:100%;">
    <!-- Search card -->
    <div style="background:#fff;border-radius:16px;padding:16px 24px;margin-bottom:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);">
      <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap;justify-content:space-between;">
        <div style="display:flex;gap:10px;flex-wrap:wrap;">
          <div style="position:relative;">
            <el-icon style="position:absolute;left:11px;top:50%;transform:translateY(-50%);color:#94A3B8;font-size:13px;"><Search /></el-icon>
            <input v-model="searchNickName" placeholder="昵称" @keyup.enter="handleSearch"
              :style="{...fieldStyle, width:'180px', paddingLeft:'32px'}" />
          </div>
          <div style="position:relative;">
            <el-icon style="position:absolute;left:11px;top:50%;transform:translateY(-50%);color:#94A3B8;font-size:13px;"><Search /></el-icon>
            <input v-model="searchOpenId" placeholder="OpenID" @keyup.enter="handleSearch"
              :style="{...fieldStyle, width:'220px', paddingLeft:'32px'}" />
          </div>
          <input v-model="dateStart" type="date" :style="{...fieldStyle, width:'140px'}" />
          <span style="line-height:36px;color:#94A3B8;font-size:13px;">~</span>
          <input v-model="dateEnd" type="date" :style="{...fieldStyle, width:'140px'}" />
          <button @click="handleSearch" style="height:36px;padding:0 16px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;border:none;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;font-weight:500;box-shadow:0 3px 10px rgba(102,126,234,0.35);">
            <el-icon style="font-size:13px;"><Search /></el-icon>搜索
          </button>
          <button @click="handleReset" style="height:36px;padding:0 16px;background:#F8FAFC;color:#64748B;border:1px solid #E2E8F0;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;">
            <el-icon style="font-size:13px;"><RefreshRight /></el-icon>重置
          </button>
        </div>
      </div>
    </div>

    <!-- Table card -->
    <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);overflow:hidden;">
      <div style="overflow-x:auto;">
        <table style="width:100%;border-collapse:collapse;min-width:700px;">
          <thead>
            <tr>
              <th :style="thSt">序号</th>
              <th :style="thSt">用户</th>
              <th :style="thSt">OpenID</th>
              <th :style="thSt">性别</th>
              <th :style="thSt">月预算</th>
              <th :style="thSt">注册时间</th>
              <th :style="thSt">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(u, i) in users" :key="u.id"
              style="transition:background 0.15s;"
              @mouseenter="e => e.currentTarget.style.background = '#FAFBFF'"
              @mouseleave="e => e.currentTarget.style.background = '#fff'"
            >
              <td :style="{...tdSt, color:'#CBD5E1', width:'60px'}">{{ (page-1) * pageSize + i + 1 }}</td>
              <td :style="tdSt">
                <div style="display:flex;align-items:center;gap:10px;">
                  <div v-if="u.avatarUrl" style="width:32px;height:32px;border-radius:10px;overflow:hidden;flex-shrink:0;">
                    <img :src="u.avatarUrl" style="width:100%;height:100%;object-fit:cover;" />
                  </div>
                  <div v-else :style="{width:'32px',height:'32px',borderRadius:'10px',background:avatarGradients[u.id % avatarGradients.length],display:'flex',alignItems:'center',justifyContent:'center',color:'#fff',fontSize:'12px',fontWeight:700,flexShrink:0}">
                    {{ (u.nickName || '?')[0] }}
                  </div>
                  <span style="font-weight:600;color:#1E293B;">{{ u.nickName || '-' }}</span>
                </div>
              </td>
              <td :style="tdSt">
                <div style="display:flex;align-items:center;gap:6px;">
                  <span style="font-family:monospace;font-size:12px;color:#475569;">{{ (u.openId || '').slice(0, 16) }}{{ (u.openId || '').length > 16 ? '...' : '' }}</span>
                  <button @click="copyOpenid(u.openId)" style="background:none;border:none;color:#94A3B8;cursor:pointer;padding:0;display:flex;">
                    <el-icon size="12"><CopyDocument /></el-icon>
                  </button>
                </div>
              </td>
              <td :style="tdSt">
                <span v-if="u.gender === 1" style="color:#3B82F6;font-size:13px;font-weight:600;">♂ 男</span>
                <span v-else-if="u.gender === 2" style="color:#F953C6;font-size:13px;font-weight:600;">♀ 女</span>
                <span v-else style="color:#94A3B8;font-size:13px;">未知</span>
              </td>
              <td :style="{...tdSt, fontWeight:600, color:'#1E293B'}">¥{{ (u.monthlyBudget || 0).toLocaleString() }}</td>
              <td :style="{...tdSt, color:'#94A3B8', fontSize:'12px'}">{{ u.createTime ? u.createTime.slice(0, 10) : '-' }}</td>
              <td :style="tdSt">
                <button @click="openDrawer(u)" style="height:28px;padding:0 12px;background:#EEF2FF;color:#667EEA;border:none;border-radius:7px;cursor:pointer;font-size:12px;font-weight:500;">详情</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <!-- Empty state -->
      <div v-if="!loading && users.length === 0" style="padding:60px;text-align:center;color:#94A3B8;">
        <div style="font-size:14px;">暂无数据</div>
      </div>
      <!-- Pagination -->
      <div style="display:flex;justify-content:flex-end;padding:12px 16px;align-items:center;gap:6px;">
        <span style="font-size:13px;color:#94A3B8;margin-right:8px;">共 {{ total }} 条</span>
        <button :disabled="page <= 1" @click="handlePageChange(page - 1)" style="width:32px;height:32px;display:flex;align-items:center;justify-content:center;border:1px solid #E2E8F0;border-radius:8px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;"
          :style="{opacity: page <= 1 ? 0.4 : 1, cursor: page <= 1 ? 'default' : 'pointer'}">‹</button>
        <button v-for="p in Math.min(totalPages, 5)" :key="p" @click="handlePageChange(p)"
          :style="{width:'32px',height:'32px',display:'flex',alignItems:'center',justifyContent:'center',border:'none',borderRadius:'8px',cursor:'pointer',fontSize:'13px',fontWeight:500,
            background: page === p ? 'linear-gradient(135deg,#667EEA,#764BA2)' : 'transparent',
            color: page === p ? '#fff' : '#64748B'}">{{ p }}</button>
        <button :disabled="page >= totalPages" @click="handlePageChange(page + 1)" style="width:32px;height:32px;display:flex;align-items:center;justify-content:center;border:1px solid #E2E8F0;border-radius:8px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;"
          :style="{opacity: page >= totalPages ? 0.4 : 1, cursor: page >= totalPages ? 'default' : 'pointer'}">›</button>
      </div>
    </div>

    <!-- Drawer -->
    <template v-if="drawer">
      <div style="position:fixed;inset:0;background:rgba(0,0,0,0.3);z-index:900;" @click="drawer = null" />
      <div style="position:fixed;right:0;top:0;bottom:0;width:480px;background:#fff;z-index:1000;display:flex;flex-direction:column;box-shadow:-4px 0 32px rgba(0,0,0,0.14);">
        <!-- Drawer header -->
        <div style="padding:20px 24px;border-bottom:1px solid #F1F5F9;display:flex;align-items:center;justify-content:space-between;">
          <span style="font-size:16px;font-weight:600;color:#1E293B;">用户详情</span>
          <button @click="drawer = null" style="background:none;border:none;cursor:pointer;color:#94A3B8;display:flex;">
            <el-icon size="20"><Close /></el-icon>
          </button>
        </div>
        <!-- Drawer body -->
        <div style="flex:1;overflow-y:auto;padding:24px;">
          <!-- Avatar header -->
          <div style="display:flex;flex-direction:column;align-items:center;margin-bottom:24px;padding:24px 20px;background:#F8FAFC;border-radius:16px;">
            <div v-if="drawer.avatarUrl" style="width:72px;height:72px;border-radius:16px;overflow:hidden;margin-bottom:12px;">
              <img :src="drawer.avatarUrl" style="width:100%;height:100%;object-fit:cover;" />
            </div>
            <div v-else :style="{width:'72px',height:'72px',borderRadius:'16px',background:avatarGradients[drawer.id % avatarGradients.length],display:'flex',alignItems:'center',justifyContent:'center',color:'#fff',fontSize:'28px',fontWeight:700,marginBottom:'12px'}">
              {{ (drawer.nickName || '?')[0] }}
            </div>
            <div style="font-size:18px;font-weight:700;color:#1E293B;">{{ drawer.nickName || '-' }}</div>
            <div style="font-size:12px;font-family:monospace;color:#94A3B8;margin-top:4px;">{{ drawer.openId }}</div>
          </div>

          <!-- Basic info -->
          <div style="background:#fff;border:1px solid #F1F5F9;border-radius:12px;margin-bottom:16px;overflow:hidden;">
            <div style="padding:12px 16px;border-bottom:1px solid #F8FAFC;font-size:13px;font-weight:600;color:#1E293B;background:#F8FAFC;">基本信息</div>
            <div style="padding:10px 16px;display:flex;justify-content:space-between;border-bottom:1px solid #F8FAFC;">
              <span style="font-size:13px;color:#94A3B8;">注册时间</span>
              <span style="font-size:13px;color:#334155;">{{ drawer.createTime || '-' }}</span>
            </div>
            <div style="padding:10px 16px;display:flex;justify-content:space-between;border-bottom:1px solid #F8FAFC;">
              <span style="font-size:13px;color:#94A3B8;">性别</span>
              <span style="font-size:13px;color:#334155;">{{ drawer.gender === 1 ? '♂ 男' : drawer.gender === 2 ? '♀ 女' : '⊘ 未知' }}</span>
            </div>
            <div style="padding:10px 16px;display:flex;justify-content:space-between;border-bottom:1px solid #F8FAFC;">
              <span style="font-size:13px;color:#94A3B8;">月预算</span>
              <span style="font-size:13px;color:#334155;">¥{{ (drawer.monthlyBudget || 0).toLocaleString() }}</span>
            </div>
            <div style="padding:10px 16px;display:flex;justify-content:space-between;">
              <span style="font-size:13px;color:#94A3B8;">UnionID</span>
              <span style="font-size:12px;font-family:monospace;color:#94A3B8;">{{ drawer.unionId || '-' }}</span>
            </div>
          </div>

          <!-- Stats grid -->
          <div style="display:grid;grid-template-columns:repeat(2,1fr);gap:10px;margin-bottom:16px;">
            <div style="background:#EEF2FF;border-radius:12px;padding:16px;text-align:center;">
              <div style="font-size:18px;font-weight:700;color:#667EEA;">{{ detailData ? detailData.totalRecords : '-' }}</div>
              <div style="font-size:12px;color:#94A3B8;margin-top:4px;">总记账笔数</div>
            </div>
            <div style="background:#F5F3FF;border-radius:12px;padding:16px;text-align:center;">
              <div style="font-size:18px;font-weight:700;color:#A855F7;">{{ detailData ? detailData.bookCount : '-' }}</div>
              <div style="font-size:12px;color:#94A3B8;margin-top:4px;">账本数量</div>
            </div>
            <div style="background:#FFF1F2;border-radius:12px;padding:16px;text-align:center;">
              <div style="font-size:18px;font-weight:700;color:#F43F5E;">{{ detailData ? '¥' + detailData.totalExpense : '-' }}</div>
              <div style="font-size:12px;color:#94A3B8;margin-top:4px;">总支出</div>
            </div>
            <div style="background:#F0FDF4;border-radius:12px;padding:16px;text-align:center;">
              <div style="font-size:18px;font-weight:700;color:#11998E;">{{ detailData ? '¥' + detailData.totalIncome : '-' }}</div>
              <div style="font-size:12px;color:#94A3B8;margin-top:4px;">总收入</div>
            </div>
          </div>

          <!-- Recent records -->
          <div style="background:#fff;border:1px solid #F1F5F9;border-radius:12px;overflow:hidden;">
            <div style="padding:12px 16px;border-bottom:1px solid #F8FAFC;font-size:13px;font-weight:600;color:#1E293B;background:#F8FAFC;">最近记录</div>
            <div v-if="!detailData" style="padding:40px 16px;text-align:center;color:#94A3B8;font-size:13px;">
              {{ detailLoading ? '加载中...' : '点击详情加载数据' }}
            </div>
            <div v-else-if="detailData.recentRecords.length === 0" style="padding:40px 16px;text-align:center;color:#94A3B8;font-size:13px;">
              暂无记录
            </div>
            <div v-for="(r, i) in detailData.recentRecords" :key="r.id"
              style="padding:10px 16px;display:flex;align-items:center;gap:10px;"
              :style="{borderBottom: i < detailData.recentRecords.length - 1 ? '1px solid #F8FAFC' : 'none'}"
            >
              <span style="font-size:18px;">{{ r.categoryIcon || '📋' }}</span>
              <div style="flex:1;min-width:0;">
                <div style="font-size:13px;color:#1E293B;font-weight:500;">{{ r.categoryName || '未知分类' }}</div>
                <div style="font-size:11px;color:#94A3B8;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{{ r.remark || '-' }}</div>
              </div>
              <div style="text-align:right;flex-shrink:0;">
                <div :style="{fontSize:'14px',fontWeight:600,color: r.type === 1 ? '#F43F5E' : '#11998E'}">
                  {{ r.type === 2 ? '+' : '' }}{{ (r.amount || 0).toFixed(2) }}
                </div>
                <div style="font-size:11px;color:#94A3B8;">{{ (r.recordTime || '').slice(0, 10) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
