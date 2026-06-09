<script setup>
import { ref, computed } from 'vue'
import { Search, RefreshRight, Download, View, Delete, WarningFilled, ArrowLeft, ArrowRight, Close } from '@element-plus/icons-vue'

const thSt = { padding: '12px 20px', fontSize: '12px', color: '#94A3B8', fontWeight: '600', textAlign: 'left', whiteSpace: 'nowrap', background: '#F8FAFC', borderBottom: '1px solid #F1F5F9', letterSpacing: '0.03em' }
const tdSt = { padding: '14px 20px', fontSize: '13px', color: '#334155', borderBottom: '1px solid #F8FAFC', whiteSpace: 'nowrap' }
const fieldStyle = { height: '36px', padding: '0 12px', borderRadius: '10px', background: '#F8FAFC', border: '1px solid #E2E8F0', fontSize: '13px', color: '#334155', outline: 'none', width: '100%', boxSizing: 'border-box' }

const avatarGradients = [
  'linear-gradient(135deg,#667EEA,#764BA2)',
  'linear-gradient(135deg,#11998E,#38EF7D)',
  'linear-gradient(135deg,#F7971E,#FFD200)',
  'linear-gradient(135deg,#F953C6,#B91D73)',
  'linear-gradient(135deg,#A855F7,#6366F1)',
  'linear-gradient(135deg,#06B6D4,#3B82F6)',
]

const searchUser = ref('')
const typeFilter = ref('all')
const dateStart = ref('')
const dateEnd = ref('')
const searchCategory = ref('')
const minAmount = ref('')
const maxAmount = ref('')
const page = ref(1)
const detail = ref(null)
const deleteId = ref(null)

const deleteVisible = computed({
  get: () => deleteId.value !== null,
  set: (v) => { if (!v) deleteId.value = null }
})

const records = ref([
  { id: 1, user: '小明同学', category: '餐饮', categoryIcon: '🍜', type: 'expense', amount: 45.5, note: '午饭·公司附近沙县小吃', bookName: '日常账本', date: '2026-06-08' },
  { id: 2, user: '花花🌸', category: '工资', categoryIcon: '💼', type: 'income', amount: 12000, note: '6月工资发放', bookName: '工作账本', date: '2026-06-05' },
  { id: 3, user: '节俭达人', category: '交通', categoryIcon: '🚇', type: 'expense', amount: 5.0, note: '地铁充值', bookName: '日常账本', date: '2026-06-08' },
  { id: 4, user: '小富即安', category: '购物', categoryIcon: '🛒', type: 'expense', amount: 238.0, note: '超市购物，买了一些零食和日用品', bookName: '日常账本', date: '2026-06-04' },
  { id: 5, user: '花花🌸', category: '奖金', categoryIcon: '🎁', type: 'income', amount: 3000, note: '季度绩效奖金', bookName: '工作账本', date: '2026-05-28' },
  { id: 6, user: '记账小能手', category: '房租', categoryIcon: '🏠', type: 'expense', amount: 2800, note: '6月房租', bookName: '固定支出', date: '2026-06-01' },
  { id: 7, user: 'Alex大叔', category: '咖啡', categoryIcon: '☕', type: 'expense', amount: 28.0, note: '瑞幸拿铁', bookName: '日常账本', date: '2026-06-03' },
  { id: 8, user: '小明同学', category: '数码', categoryIcon: '📱', type: 'expense', amount: 1299, note: '手机壳+屏幕贴膜', bookName: '日常账本', date: '2026-05-30' },
  { id: 9, user: '月光族', category: '餐饮', categoryIcon: '🍕', type: 'expense', amount: 89.0, note: '朋友聚餐·外婆家餐厅', bookName: '日常账本', date: '2026-05-25' },
  { id: 10, user: '节俭达人', category: '理财', categoryIcon: '📈', type: 'income', amount: 560.0, note: '基金收益', bookName: '投资账本', date: '2026-06-07' },
  { id: 11, user: '小富即安', category: '打车', categoryIcon: '🚗', type: 'expense', amount: 32.0, note: '滴滴打车', bookName: '日常账本', date: '2026-05-24' },
  { id: 12, user: '记账小能手', category: '工资', categoryIcon: '💼', type: 'income', amount: 9500, note: '5月工资', bookName: '工作账本', date: '2026-05-05' },
])

const categories = ['餐饮', '交通', '购物', '房租', '数码', '咖啡', '打车', '工资', '奖金', '理财']

const filtered = computed(() => {
  return records.value.filter(r =>
    (!searchUser.value || r.user.includes(searchUser.value)) &&
    (!searchCategory.value || r.category.includes(searchCategory.value)) &&
    (typeFilter.value === 'all' || r.type === typeFilter.value) &&
    (!minAmount.value || r.amount >= Number(minAmount.value)) &&
    (!maxAmount.value || r.amount <= Number(maxAmount.value))
  )
})

const paged = computed(() => {
  const start = (page.value - 1) * 10
  return filtered.value.slice(start, start + 10)
})

const totalExpense = computed(() => filtered.value.filter(r => r.type === 'expense').reduce((s, r) => s + r.amount, 0))
const totalIncome = computed(() => filtered.value.filter(r => r.type === 'income').reduce((s, r) => s + r.amount, 0))
const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / 10)))

const pages = computed(() => {
  const tp = totalPages.value
  const p = page.value
  const result = []
  if (tp <= 7) {
    for (let i = 1; i <= tp; i++) result.push(i)
  } else {
    if (p <= 3) { result.push(1, 2, 3, 4, '...', tp) }
    else if (p >= tp - 2) { result.push(1, '...', tp - 3, tp - 2, tp - 1, tp) }
    else { result.push(1, '...', p - 1, p, p + 1, '...', tp) }
  }
  return result
})

function handleDelete(id) {
  deleteId.value = id
}

function confirmDelete() {
  if (deleteId.value !== null) {
    records.value = records.value.filter(r => r.id !== deleteId.value)
    if (detail.value?.id === deleteId.value) detail.value = null
  }
  deleteId.value = null
}

function handleSearch() { page.value = 1 }
function handleReset() {
  searchUser.value = ''
  typeFilter.value = 'all'
  searchCategory.value = ''
  minAmount.value = ''
  maxAmount.value = ''
  dateStart.value = ''
  dateEnd.value = ''
  page.value = 1
}
</script>

<template>
  <div style="padding:24px;background:#F0F2F8;min-height:100%;">
    <!-- Search card -->
    <div style="background:#fff;border-radius:16px;padding:16px 24px;margin-bottom:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);">
      <div style="display:flex;gap:10px;flex-wrap:wrap;margin-bottom:10px;">
        <div style="position:relative;">
          <el-icon style="position:absolute;left:11px;top:50%;transform:translateY(-50%);color:#94A3B8;font-size:13px;"><Search /></el-icon>
          <input v-model="searchUser" placeholder="用户昵称搜索"
            style="width:200px;height:36px;padding:0 12px 0 32px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:13px;color:#334155;outline:none;box-sizing:border-box;" />
        </div>
        <div style="display:flex;border:1px solid #E2E8F0;border-radius:10px;overflow:hidden;height:36px;">
          <button v-for="opt in [['all','全部'],['expense','支出'],['income','收入']]" :key="opt[0]"
            @click="typeFilter = opt[0]"
            :style="{padding:'0 16px',border:'none',cursor:'pointer',fontSize:'13px',fontWeight: typeFilter===opt[0]?600:400,transition:'all 0.15s',
              background: typeFilter===opt[0] ? 'linear-gradient(135deg,#667EEA,#764BA2)' : '#F8FAFC',
              color: typeFilter===opt[0] ? '#fff' : '#64748B'}">{{ opt[1] }}</button>
        </div>
        <input v-model="dateStart" type="date" style="width:136px;height:36px;padding:0 10px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:13px;color:#334155;outline:none;box-sizing:border-box;" />
        <span style="line-height:36px;color:#94A3B8;font-size:13px;">~</span>
        <input v-model="dateEnd" type="date" style="width:136px;height:36px;padding:0 10px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:13px;color:#334155;outline:none;box-sizing:border-box;" />
      </div>
      <div style="display:flex;gap:10px;flex-wrap:wrap;align-items:center;">
        <select v-model="searchCategory" style="width:140px;height:36px;padding:0 10px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:13px;color:#334155;outline:none;cursor:pointer;">
          <option value="">全部分类</option>
          <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
        </select>
        <input v-model="minAmount" type="number" placeholder="最小金额" style="width:110px;height:36px;padding:0 10px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:13px;color:#334155;outline:none;box-sizing:border-box;" />
        <span style="color:#94A3B8;font-size:13px;">~</span>
        <input v-model="maxAmount" type="number" placeholder="最大金额" style="width:110px;height:36px;padding:0 10px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:13px;color:#334155;outline:none;box-sizing:border-box;" />
        <button @click="handleSearch" style="height:36px;padding:0 16px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;border:none;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;font-weight:500;box-shadow:0 3px 10px rgba(102,126,234,0.35);">
          <el-icon style="font-size:13px;"><Search /></el-icon>搜索
        </button>
        <button @click="handleReset" style="height:36px;padding:0 16px;background:#F8FAFC;color:#64748B;border:1px solid #E2E8F0;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;">
          <el-icon style="font-size:13px;"><RefreshRight /></el-icon>重置
        </button>
        <button style="height:36px;padding:0 16px;background:#F0FDF4;color:#11998E;border:none;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;font-weight:500;">
          <el-icon style="font-size:13px;"><Download /></el-icon>导出Excel
        </button>
      </div>
    </div>

    <!-- Table card -->
    <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);overflow:hidden;">
      <div style="overflow-x:auto;">
        <table style="width:100%;border-collapse:collapse;min-width:900px;">
          <thead>
            <tr>
              <th :style="thSt">序号</th>
              <th :style="thSt">用户</th>
              <th :style="thSt">分类</th>
              <th :style="thSt">类型</th>
              <th :style="thSt">金额</th>
              <th :style="thSt">备注</th>
              <th :style="thSt">账本</th>
              <th :style="thSt">记账日期</th>
              <th :style="thSt">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(r, i) in paged" :key="r.id"
              @mouseenter="e => e.currentTarget.style.background = '#FAFBFF'"
              @mouseleave="e => e.currentTarget.style.background = '#fff'"
              style="transition:background 0.15s;">
              <td :style="{...tdSt, color:'#CBD5E1', width:'60px'}">{{ (page-1) * 10 + i + 1 }}</td>
              <td :style="tdSt">
                <div style="display:flex;align-items:center;gap:8px;">
                  <div :style="{width:'30px',height:'30px',borderRadius:'8px',background:avatarGradients[r.id % avatarGradients.length],display:'flex',alignItems:'center',justifyContent:'center',color:'#fff',fontSize:'11px',fontWeight:700,flexShrink:0}">
                    {{ r.user[0] }}
                  </div>
                  <span style="font-weight:500;">{{ r.user }}</span>
                </div>
              </td>
              <td :style="tdSt">
                <div style="display:flex;align-items:center;gap:6px;">
                  <span>{{ r.categoryIcon }}</span>
                  <span>{{ r.category }}</span>
                </div>
              </td>
              <td :style="tdSt">
                <span :style="{fontSize:'12px',padding:'2px 10px',borderRadius:'20px',background:r.type==='expense'?'#FFF1F2':'#F0FDF4',color:r.type==='expense'?'#F43F5E':'#11998E',fontWeight:500}">
                  {{ r.type === 'expense' ? '支出' : '收入' }}
                </span>
              </td>
              <td :style="{...tdSt, fontWeight:700, fontSize:'14px', color: r.type==='expense'?'#F43F5E':'#11998E', textAlign:'right'}">
                {{ r.type === 'expense' ? '-' : '+' }}¥{{ r.amount.toFixed(2) }}
              </td>
              <td :style="{...tdSt, maxWidth:'150px', overflow:'hidden', textOverflow:'ellipsis', color:'#475569'}" :title="r.note">{{ r.note }}</td>
              <td :style="{...tdSt, color:'#94A3B8'}">{{ r.bookName }}</td>
              <td :style="{...tdSt, color:'#94A3B8', fontSize:'12px'}">{{ r.date }}</td>
              <td :style="tdSt">
                <div style="display:flex;gap:6px;">
                  <button @click="detail = r" style="height:28px;padding:0 10px;background:#EEF2FF;color:#667EEA;border:none;border-radius:7px;cursor:pointer;font-size:12px;display:flex;align-items:center;gap:3px;font-weight:500;">
                    <el-icon style="font-size:11px;"><View /></el-icon>查看
                  </button>
                  <button @click="handleDelete(r.id)" style="height:28px;padding:0 10px;background:#FFF1F2;color:#F43F5E;border:none;border-radius:7px;cursor:pointer;font-size:12px;display:flex;align-items:center;gap:3px;font-weight:500;">
                    <el-icon style="font-size:11px;"><Delete /></el-icon>删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
          <tfoot>
            <tr style="background:#F8FAFC;">
              <td :colspan="4" :style="{...tdSt, fontWeight:600, color:'#1E293B', borderBottom:'none'}">汇总（当前筛选 {{ filtered.length }} 条）</td>
              <td :style="{...tdSt, textAlign:'right', borderBottom:'none'}">
                <div style="font-size:12px;color:#F43F5E;font-weight:600;">支出 ¥{{ totalExpense.toFixed(2) }}</div>
                <div style="font-size:12px;color:#11998E;font-weight:600;">收入 ¥{{ totalIncome.toFixed(2) }}</div>
                <div style="font-size:13px;color:#1E293B;font-weight:700;margin-top:2px;">净额 {{ (totalIncome - totalExpense) >= 0 ? '+' : '' }}¥{{ (totalIncome - totalExpense).toFixed(2) }}</div>
              </td>
              <td :colspan="4" style="border-bottom:none;" />
            </tr>
          </tfoot>
        </table>
      </div>

      <!-- Custom pagination -->
      <div style="display:flex;justify-content:flex-end;padding:12px 16px;align-items:center;gap:4px;">
        <span style="font-size:12px;color:#94A3B8;margin-right:8px;">共 {{ filtered.length }} 条</span>
        <button :disabled="page <= 1" @click="page--"
          style="width:32px;height:32px;border:1px solid #E2E8F0;border-radius:8px;background:#fff;display:flex;align-items:center;justify-content:center;cursor:pointer;opacity:1;"
          :style="{opacity: page <= 1 ? 0.4 : 1, cursor: page <= 1 ? 'not-allowed' : 'pointer'}">
          <el-icon style="font-size:14px;color:#64748B;"><ArrowLeft /></el-icon>
        </button>
        <template v-for="(p, idx) in pages" :key="idx">
          <span v-if="p === '...'" style="width:32px;text-align:center;color:#94A3B8;font-size:13px;">…</span>
          <button v-else @click="page = p"
            :style="{width:'32px',height:'32px',border:'none',borderRadius:'8px',background: p===page ? 'linear-gradient(135deg,#667EEA,#764BA2)' : '#fff',color: p===page ? '#fff' : '#475569',cursor:'pointer',fontSize:'13px',fontWeight: p===page ? 600 : 400}">{{ p }}</button>
        </template>
        <button :disabled="page >= totalPages" @click="page++"
          style="width:32px;height:32px;border:1px solid #E2E8F0;border-radius:8px;background:#fff;display:flex;align-items:center;justify-content:center;"
          :style="{opacity: page >= totalPages ? 0.4 : 1, cursor: page >= totalPages ? 'not-allowed' : 'pointer'}">
          <el-icon style="font-size:14px;color:#64748B;"><ArrowRight /></el-icon>
        </button>
      </div>
    </div>

    <!-- Detail Modal -->
    <div v-if="detail" style="position:fixed;inset:0;background:rgba(15,23,42,0.5);display:flex;align-items:center;justify-content:center;z-index:1000;" @click="detail = null">
      <div style="background:#fff;border-radius:16px;width:420px;max-width:90vw;max-height:90vh;overflow:auto;box-shadow:0 16px 48px rgba(0,0,0,0.14),0 4px 16px rgba(0,0,0,0.08);" @click.stop>
        <div style="display:flex;justify-content:space-between;align-items:center;padding:20px 24px;border-bottom:1px solid #F1F5F9;">
          <span style="font-size:16px;font-weight:700;color:#1E293B;">记录详情</span>
          <el-icon style="font-size:18px;color:#94A3B8;cursor:pointer;" @click="detail = null"><Close /></el-icon>
        </div>
        <div style="padding:20px 24px;">
          <div style="display:flex;flex-direction:column;">
            <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #F8FAFC;">
              <span style="width:80px;font-size:13px;color:#94A3B8;flex-shrink:0;">用户</span>
              <span style="font-size:14px;color:#1E293B;">
                <div style="display:flex;align-items:center;gap:8px;">
                  <div :style="{width:'24px',height:'24px',borderRadius:'6px',background:'linear-gradient(135deg,#667EEA,#764BA2)',display:'flex',alignItems:'center',justifyContent:'center',color:'#fff',fontSize:'10px',fontWeight:700}">{{ detail.user[0] }}</div>
                  {{ detail.user }}
                </div>
              </span>
            </div>
            <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #F8FAFC;">
              <span style="width:80px;font-size:13px;color:#94A3B8;flex-shrink:0;">分类</span>
              <span style="font-size:14px;color:#1E293B;">{{ detail.categoryIcon }} {{ detail.category }}</span>
            </div>
            <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #F8FAFC;">
              <span style="width:80px;font-size:13px;color:#94A3B8;flex-shrink:0;">类型</span>
              <span :style="{fontSize:'12px',padding:'2px 10px',borderRadius:'20px',background:detail.type==='expense'?'#FFF1F2':'#F0FDF4',color:detail.type==='expense'?'#F43F5E':'#11998E',fontWeight:500}">{{ detail.type === 'expense' ? '支出' : '收入' }}</span>
            </div>
            <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #F8FAFC;">
              <span style="width:80px;font-size:13px;color:#94A3B8;flex-shrink:0;">金额</span>
              <span :style="{fontSize:'24px',fontWeight:700,color:detail.type==='expense'?'#F43F5E':'#11998E'}">{{ detail.type === 'expense' ? '-' : '+' }}¥{{ detail.amount.toFixed(2) }}</span>
            </div>
            <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #F8FAFC;">
              <span style="width:80px;font-size:13px;color:#94A3B8;flex-shrink:0;">备注</span>
              <span style="font-size:14px;color:#1E293B;">{{ detail.note || '—' }}</span>
            </div>
            <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #F8FAFC;">
              <span style="width:80px;font-size:13px;color:#94A3B8;flex-shrink:0;">记账日期</span>
              <span style="font-size:14px;color:#1E293B;">{{ detail.date }}</span>
            </div>
            <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #F8FAFC;">
              <span style="width:80px;font-size:13px;color:#94A3B8;flex-shrink:0;">所属账本</span>
              <span style="font-size:14px;color:#1E293B;">{{ detail.bookName }}</span>
            </div>
          </div>
          <div style="display:flex;justify-content:space-between;align-items:center;padding-top:16px;border-top:1px solid #F1F5F9;margin-top:8px;">
            <button @click="handleDelete(detail.id); detail = null"
              style="background:none;border:none;color:#F43F5E;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:4px;">
              <el-icon style="font-size:13px;"><Delete /></el-icon>删除此记录
            </button>
            <button @click="detail = null" style="height:36px;padding:0 20px;background:#F8FAFC;color:#64748B;border:1px solid #E2E8F0;border-radius:10px;cursor:pointer;font-size:13px;">关闭</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Confirm overlay -->
    <div v-if="deleteVisible" style="position:fixed;inset:0;background:rgba(15,23,42,0.5);display:flex;align-items:center;justify-content:center;z-index:1000;" @click="deleteId = null">
      <div style="background:#fff;border-radius:16px;width:380px;max-width:90vw;padding:32px 28px;box-shadow:0 16px 48px rgba(244,63,94,0.14),0 4px 16px rgba(0,0,0,0.08);text-align:center;" @click.stop>
        <div style="width:64px;height:64px;border-radius:50%;background:#FFF1F2;display:flex;align-items:center;justify-content:center;margin:0 auto 16px;">
          <el-icon style="font-size:28px;color:#F43F5E;"><WarningFilled /></el-icon>
        </div>
        <div style="font-size:17px;font-weight:700;color:#1E293B;margin-bottom:8px;">确认删除</div>
        <div style="font-size:14px;color:#64748B;line-height:1.6;margin-bottom:24px;">
          此操作不可逆，删除后该记录数据将无法恢复。<br />确定要继续吗？
        </div>
        <div style="display:flex;gap:10px;justify-content:center;">
          <button @click="deleteId = null" style="height:40px;padding:0 24px;border:1px solid #E2E8F0;border-radius:10px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;font-weight:500;">取消</button>
          <button @click="confirmDelete" style="height:40px;padding:0 28px;border:none;border-radius:10px;background:linear-gradient(135deg,#F43F5E,#E11D48);color:#fff;cursor:pointer;font-size:14px;font-weight:600;box-shadow:0 4px 12px rgba(244,63,94,0.35);">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>
