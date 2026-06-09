<script setup>
import { ref, computed } from 'vue'
import { Search, RefreshRight, CopyDocument, Close } from '@element-plus/icons-vue'

const avatarGradients = [
  'linear-gradient(135deg,#667EEA,#764BA2)',
  'linear-gradient(135deg,#11998E,#38EF7D)',
  'linear-gradient(135deg,#F7971E,#FFD200)',
  'linear-gradient(135deg,#F953C6,#B91D73)',
  'linear-gradient(135deg,#A855F7,#6366F1)',
  'linear-gradient(135deg,#06B6D4,#3B82F6)',
  'linear-gradient(135deg,#F43F5E,#FB923C)',
]

const users = ref([
  { id: 1, nickname: '小明同学', openid: 'oHgJ75yKmNpQrStUvWxYz', gender: 1, budget: 3000, records: 156, registeredAt: '2026-01-15 10:30:00', status: true, totalExpense: 18920, totalIncome: 35000, bookCount: 3, lastActive: '2026-06-08 09:45:00' },
  { id: 2, nickname: '花花🌸', openid: 'oHgJ75aBcDeFgHiJkLmNo', gender: 2, budget: 5000, records: 289, registeredAt: '2026-02-01 14:20:00', status: true, totalExpense: 32100, totalIncome: 48000, bookCount: 5, lastActive: '2026-06-07 22:10:00' },
  { id: 3, nickname: '节俭达人', openid: 'oHgJ75pQrStUvWxYzAbCd', gender: 1, budget: 1500, records: 412, registeredAt: '2025-12-10 08:00:00', status: true, totalExpense: 8760, totalIncome: 25000, bookCount: 2, lastActive: '2026-06-08 11:30:00' },
  { id: 4, nickname: '月光族', openid: 'oHgJ75eFgHiJkLmNoPqRs', gender: 2, budget: 8000, records: 68, registeredAt: '2026-03-20 16:45:00', status: false, totalExpense: 62340, totalIncome: 60000, bookCount: 1, lastActive: '2026-05-15 18:00:00' },
  { id: 5, nickname: '小富即安', openid: 'oHgJ75tUvWxYzAbCdEfGh', gender: 1, budget: 4000, records: 197, registeredAt: '2026-01-30 11:15:00', status: true, totalExpense: 22890, totalIncome: 40000, bookCount: 4, lastActive: '2026-06-08 08:20:00' },
  { id: 6, nickname: '记账小能手', openid: 'oHgJ75iJkLmNoPqRsThUv', gender: 2, budget: 3500, records: 534, registeredAt: '2025-11-05 09:30:00', status: true, totalExpense: 15670, totalIncome: 30000, bookCount: 6, lastActive: '2026-06-08 10:55:00' },
  { id: 7, nickname: 'Alex大叔', openid: 'oHgJ75wXyZaBcDeFgHiJk', gender: 1, budget: 10000, records: 45, registeredAt: '2026-04-08 13:00:00', status: true, totalExpense: 78900, totalIncome: 120000, bookCount: 2, lastActive: '2026-06-06 19:30:00' },
  { id: 8, nickname: '匿名用户_8823', openid: 'oHgJ75xYzAbCdEfGhIjKl', gender: 0, budget: 2000, records: 23, registeredAt: '2026-05-18 17:30:00', status: true, totalExpense: 4320, totalIncome: 8000, bookCount: 1, lastActive: '2026-06-07 14:20:00' },
])

const recentRecords = [
  { icon: '🍜', category: '餐饮', amount: -45.5, date: '2026-06-08', note: '午饭·公司附近沙县小吃' },
  { icon: '🚇', category: '交通', amount: -5.0, date: '2026-06-08', note: '地铁卡充值' },
  { icon: '💼', category: '工资', amount: 12000, date: '2026-06-05', note: '6月工资' },
  { icon: '🛒', category: '购物', amount: -238.0, date: '2026-06-04', note: '超市购物' },
  { icon: '☕', category: '咖啡', amount: -28.0, date: '2026-06-03', note: '瑞幸咖啡' },
  { icon: '🏠', category: '房租', amount: -2800, date: '2026-06-01', note: '6月房租' },
  { icon: '📱', category: '数码', amount: -1299, date: '2026-05-30', note: '手机壳+贴膜' },
  { icon: '🎁', category: '奖金', amount: 3000, date: '2026-05-28', note: '季度绩效奖金' },
  { icon: '🍕', category: '餐饮', amount: -89.0, date: '2026-05-25', note: '朋友聚餐' },
  { icon: '🚗', category: '打车', amount: -32.0, date: '2026-05-24', note: '滴滴打车' },
]

const searchVal = ref('')
const statusFilter = ref('')
const dateStart = ref('')
const dateEnd = ref('')
const page = ref(1)
const pageSize = ref(10)
const drawer = ref(null)

const filtered = computed(() => {
  let list = users.value
  if (searchVal.value) {
    const kw = searchVal.value
    list = list.filter(u => u.nickname.includes(kw) || u.openid.includes(kw))
  }
  if (statusFilter.value) {
    list = list.filter(u => u.status === (statusFilter.value === '1'))
  }
  return list
})

const paged = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filtered.value.slice(start, start + pageSize.value)
})

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
  <div style="padding:24px;background:#F0F2F8;min-height:100%;position:relative;">
    <!-- Search card -->
    <div style="background:#fff;border-radius:16px;padding:16px 24px;margin-bottom:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);">
      <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap;justify-content:space-between;">
        <div style="display:flex;gap:10px;flex-wrap:wrap;">
          <div style="position:relative;">
            <el-icon style="position:absolute;left:11px;top:50%;transform:translateY(-50%);color:#94A3B8;font-size:13px;"><Search /></el-icon>
            <input v-model="searchVal" placeholder="昵称 / OpenID"
              :style="{...fieldStyle, width:'240px', paddingLeft:'32px'}" />
          </div>
          <input v-model="dateStart" type="date" :style="{...fieldStyle, width:'140px'}" />
          <span style="line-height:36px;color:#94A3B8;font-size:13px;">~</span>
          <input v-model="dateEnd" type="date" :style="{...fieldStyle, width:'140px'}" />
          <select v-model="statusFilter" :style="{...fieldStyle, width:'120px', cursor:'pointer'}">
            <option value="">全部状态</option>
            <option value="1">正常</option>
            <option value="0">已禁用</option>
          </select>
          <button @click="page = 1" style="height:36px;padding:0 16px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;border:none;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;font-weight:500;box-shadow:0 3px 10px rgba(102,126,234,0.35);">
            <el-icon style="font-size:13px;"><Search /></el-icon>搜索
          </button>
          <button @click="searchVal='';statusFilter='';dateStart='';dateEnd=''" style="height:36px;padding:0 16px;background:#F8FAFC;color:#64748B;border:1px solid #E2E8F0;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;">
            <el-icon style="font-size:13px;"><RefreshRight /></el-icon>重置
          </button>
        </div>
      </div>
    </div>

    <!-- Table card -->
    <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);overflow:hidden;">
      <div style="overflow-x:auto;">
        <table style="width:100%;border-collapse:collapse;min-width:860px;">
          <thead>
            <tr>
              <th :style="thSt">序号</th>
              <th :style="thSt">用户</th>
              <th :style="thSt">OpenID</th>
              <th :style="thSt">性别</th>
              <th :style="thSt">月预算</th>
              <th :style="thSt">记账笔数</th>
              <th :style="thSt">注册时间</th>
              <th :style="thSt">状态</th>
              <th :style="thSt">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(u, i) in paged" :key="u.id"
              style="transition:background 0.15s;"
              @mouseenter="e => e.currentTarget.style.background = '#FAFBFF'"
              @mouseleave="e => e.currentTarget.style.background = '#fff'"
            >
              <td :style="{...tdSt, color:'#CBD5E1', width:'60px'}">{{ (page-1) * pageSize + i + 1 }}</td>
              <td :style="tdSt">
                <div style="display:flex;align-items:center;gap:10px;">
                  <div :style="{width:'32px',height:'32px',borderRadius:'10px',background:avatarGradients[u.id % avatarGradients.length],display:'flex',alignItems:'center',justifyContent:'center',color:'#fff',fontSize:'12px',fontWeight:700,flexShrink:0}">
                    {{ u.nickname[0] }}
                  </div>
                  <span style="font-weight:600;color:#1E293B;">{{ u.nickname }}</span>
                </div>
              </td>
              <td :style="tdSt">
                <div style="display:flex;align-items:center;gap:6px;">
                  <span style="font-family:monospace;font-size:12px;color:#475569;">{{ u.openid.slice(0, 16) }}...</span>
                  <button @click="copyOpenid(u.openid)" style="background:none;border:none;color:#94A3B8;cursor:pointer;padding:0;display:flex;">
                    <el-icon size="12"><CopyDocument /></el-icon>
                  </button>
                </div>
              </td>
              <td :style="tdSt">
                <span v-if="u.gender === 1" style="color:#3B82F6;font-size:13px;font-weight:600;">♂ <span style="font-size:12px;">男</span></span>
                <span v-else-if="u.gender === 2" style="color:#F953C6;font-size:13px;font-weight:600;">♀ <span style="font-size:12px;">女</span></span>
                <span v-else style="color:#94A3B8;font-size:13px;display:inline-flex;align-items:center;gap:4px;">
                  <span style="width:14px;height:14px;border-radius:50%;border:1.5px solid #CBD5E1;display:inline-flex;align-items:center;justify-content:center;font-size:9px;">?</span>未知
                </span>
              </td>
              <td :style="{...tdSt, fontWeight:600, color:'#1E293B'}">¥{{ u.budget.toLocaleString() }}</td>
              <td :style="{...tdSt, textAlign:'center'}">
                <span style="font-size:13px;padding:2px 10px;border-radius:20px;background:#EEF2FF;color:#667EEA;font-weight:600;">{{ u.records }}</span>
              </td>
              <td :style="{...tdSt, color:'#94A3B8', fontSize:'12px'}">{{ u.registeredAt.slice(0, 10) }}</td>
              <td :style="tdSt">
                <el-switch :model-value="u.status" size="small" @change="users = users.map(x => x.id === u.id ? {...x, status: !x.status} : x)" />
              </td>
              <td :style="tdSt">
                <button @click="drawer = u" style="height:28px;padding:0 12px;background:#EEF2FF;color:#667EEA;border:none;border-radius:7px;cursor:pointer;font-size:12px;font-weight:500;">详情</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <!-- Empty state -->
      <div v-if="filtered.length === 0" style="padding:60px;text-align:center;color:#94A3B8;">
        <el-icon size="48" style="opacity:0.2;margin-bottom:12px;display:block;margin:0 auto 12px;"><User /></el-icon>
        <div>暂无数据</div>
      </div>
      <!-- Pagination -->
      <div style="display:flex;justify-content:flex-end;padding:12px 16px;align-items:center;gap:6px;">
        <span style="font-size:13px;color:#94A3B8;margin-right:8px;">共 {{ filtered.length }} 条</span>
        <button :disabled="page <= 1" @click="page--" style="width:32px;height:32px;display:flex;align-items:center;justify-content:center;border:1px solid #E2E8F0;border-radius:8px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;"
          :style="{opacity: page <= 1 ? 0.4 : 1, cursor: page <= 1 ? 'default' : 'pointer'}">‹</button>
        <button v-for="p in Math.min(Math.ceil(filtered.length / pageSize), 5)" :key="p" @click="page = p"
          :style="{width:'32px',height:'32px',display:'flex',alignItems:'center',justifyContent:'center',border:'none',borderRadius:'8px',cursor:'pointer',fontSize:'13px',fontWeight:500,
            background: page === p ? 'linear-gradient(135deg,#667EEA,#764BA2)' : 'transparent',
            color: page === p ? '#fff' : '#64748B'}">{{ p }}</button>
        <button :disabled="page >= Math.ceil(filtered.length / pageSize)" @click="page++" style="width:32px;height:32px;display:flex;align-items:center;justify-content:center;border:1px solid #E2E8F0;border-radius:8px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;"
          :style="{opacity: page >= Math.ceil(filtered.length / pageSize) ? 0.4 : 1, cursor: page >= Math.ceil(filtered.length / pageSize) ? 'default' : 'pointer'}">›</button>
      </div>
    </div>

    <!-- Custom Drawer -->
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
            <div :style="{width:'72px',height:'72px',borderRadius:'16px',background:avatarGradients[drawer.id % avatarGradients.length],display:'flex',alignItems:'center',justifyContent:'center',color:'#fff',fontSize:'28px',fontWeight:700,marginBottom:'12px'}">
              {{ drawer.nickname[0] }}
            </div>
            <div style="font-size:18px;font-weight:700;color:#1E293B;">{{ drawer.nickname }}</div>
            <div style="font-size:12px;font-family:monospace;color:#94A3B8;margin-top:4px;">{{ drawer.openid }}</div>
          </div>

          <!-- Basic info -->
          <div style="background:#fff;border:1px solid #F1F5F9;border-radius:12px;margin-bottom:16px;overflow:hidden;">
            <div style="padding:12px 16px;border-bottom:1px solid #F8FAFC;font-size:13px;font-weight:600;color:#1E293B;background:#F8FAFC;">基本信息</div>
            <div v-for="[k,v] in [
              ['注册时间', drawer.registeredAt],
              ['最后活跃', drawer.lastActive],
              ['性别', drawer.gender === 1 ? '♂ 男' : drawer.gender === 2 ? '♀ 女' : '⊘ 未知'],
              ['月预算', '¥' + drawer.budget.toLocaleString()],
              ['账户状态', drawer.status ? '✅ 正常' : '🚫 已禁用'],
            ]" :key="k" style="padding:10px 16px;display:flex;justify-content:space-between;border-bottom:1px solid #F8FAFC;">
              <span style="font-size:13px;color:#94A3B8;">{{ k }}</span>
              <span style="font-size:13px;color:#334155;">{{ v }}</span>
            </div>
          </div>

          <!-- Stats grid -->
          <div style="display:grid;grid-template-columns:repeat(2,1fr);gap:10px;margin-bottom:16px;">
            <div style="background:#EEF2FF;border-radius:12px;padding:16px;text-align:center;">
              <div style="font-size:18px;font-weight:700;color:#667EEA;">{{ drawer.records }}</div>
              <div style="font-size:12px;color:#94A3B8;margin-top:4px;">总记账笔数</div>
            </div>
            <div style="background:#F5F3FF;border-radius:12px;padding:16px;text-align:center;">
              <div style="font-size:18px;font-weight:700;color:#A855F7;">{{ drawer.bookCount }}</div>
              <div style="font-size:12px;color:#94A3B8;margin-top:4px;">账本数量</div>
            </div>
            <div style="background:#FFF1F2;border-radius:12px;padding:16px;text-align:center;">
              <div style="font-size:18px;font-weight:700;color:#F43F5E;">¥{{ drawer.totalExpense.toLocaleString() }}</div>
              <div style="font-size:12px;color:#94A3B8;margin-top:4px;">总支出</div>
            </div>
            <div style="background:#F0FDF4;border-radius:12px;padding:16px;text-align:center;">
              <div style="font-size:18px;font-weight:700;color:#11998E;">¥{{ drawer.totalIncome.toLocaleString() }}</div>
              <div style="font-size:12px;color:#94A3B8;margin-top:4px;">总收入</div>
            </div>
          </div>

          <!-- Recent records -->
          <div style="background:#fff;border:1px solid #F1F5F9;border-radius:12px;overflow:hidden;">
            <div style="padding:12px 16px;border-bottom:1px solid #F8FAFC;font-size:13px;font-weight:600;color:#1E293B;background:#F8FAFC;">最近 10 条记录</div>
            <div v-for="(r, i) in recentRecords" :key="i"
              style="padding:10px 16px;display:flex;align-items:center;gap:10px;"
              :style="{borderBottom: i < 9 ? '1px solid #F8FAFC' : 'none'}"
            >
              <span style="font-size:18px;">{{ r.icon }}</span>
              <div style="flex:1;min-width:0;">
                <div style="font-size:13px;color:#1E293B;font-weight:500;">{{ r.category }}</div>
                <div style="font-size:11px;color:#94A3B8;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{{ r.note }}</div>
              </div>
              <div style="text-align:right;flex-shrink:0;">
                <div :style="{fontSize:'14px',fontWeight:600,color: r.amount < 0 ? '#F43F5E' : '#11998E'}">
                  {{ r.amount < 0 ? '' : '+' }}{{ r.amount.toFixed(2) }}
                </div>
                <div style="font-size:11px;color:#94A3B8;">{{ r.date }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
