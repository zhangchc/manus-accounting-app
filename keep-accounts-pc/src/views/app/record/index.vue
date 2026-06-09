<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshRight, Download, View, ArrowLeft, ArrowRight, Close } from '@element-plus/icons-vue'
import { getRecordList, getRecordCategories, exportRecords } from '@/api/record'

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
const searchCategoryId = ref('')
const minAmount = ref('')
const maxAmount = ref('')
const page = ref(1)

const detail = ref(null)
const records = ref([])
const total = ref(0)
const loading = ref(false)
const exporting = ref(false)
const categories = ref([])

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / 10)))

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

const totalExpense = computed(() =>
  records.value.filter(r => r.type === 1).reduce((s, r) => s + (r.amount || 0), 0)
)
const totalIncome = computed(() =>
  records.value.filter(r => r.type === 2).reduce((s, r) => s + (r.amount || 0), 0)
)

async function loadCategories() {
  try {
    const data = await getRecordCategories()
    categories.value = data || []
  } catch (e) {
    // silent fail, dropdown will be empty
  }
}

async function loadRecords() {
  loading.value = true
  try {
    const params = {
      userNickName: searchUser.value || undefined,
      type: typeFilter.value !== 'all' ? (typeFilter.value === 'expense' ? 1 : 2) : undefined,
      categoryId: searchCategoryId.value || undefined,
      startDate: dateStart.value || undefined,
      endDate: dateEnd.value || undefined,
      minAmount: minAmount.value || undefined,
      maxAmount: maxAmount.value || undefined,
      page: page.value,
      pageSize: 10,
    }
    const data = await getRecordList(params)
    records.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载记录列表失败')
  } finally {
    loading.value = false
  }
}

async function handleExport() {
  exporting.value = true
  try {
    const params = {
      userNickName: searchUser.value || undefined,
      type: typeFilter.value !== 'all' ? (typeFilter.value === 'expense' ? 1 : 2) : undefined,
      categoryId: searchCategoryId.value || undefined,
      startDate: dateStart.value || undefined,
      endDate: dateEnd.value || undefined,
      minAmount: minAmount.value || undefined,
      maxAmount: maxAmount.value || undefined,
    }
    await exportRecords(params)
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

function handleSearch() {
  page.value = 1
  loadRecords()
}

function handleReset() {
  searchUser.value = ''
  typeFilter.value = 'all'
  searchCategoryId.value = ''
  minAmount.value = ''
  maxAmount.value = ''
  dateStart.value = ''
  dateEnd.value = ''
  page.value = 1
  loadRecords()
}

function handlePageChange(p) {
  page.value = p
  loadRecords()
}

onMounted(() => {
  loadCategories()
  loadRecords()
})
</script>

<template>
  <div style="padding:24px;background:#F0F2F8;min-height:100%;">
    <!-- Search card -->
    <div style="background:#fff;border-radius:16px;padding:16px 24px;margin-bottom:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);">
      <div style="display:flex;gap:10px;flex-wrap:wrap;margin-bottom:10px;">
        <div style="position:relative;">
          <el-icon style="position:absolute;left:11px;top:50%;transform:translateY(-50%);color:#94A3B8;font-size:13px;"><Search /></el-icon>
          <input v-model="searchUser" placeholder="用户昵称搜索" @keyup.enter="handleSearch"
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
        <select v-model="searchCategoryId" style="width:140px;height:36px;padding:0 10px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:13px;color:#334155;outline:none;cursor:pointer;">
          <option value="">全部分类</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.icon }} {{ c.name }}</option>
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
        <button @click="handleExport" :disabled="exporting" style="height:36px;padding:0 16px;background:#F0FDF4;color:#11998E;border:none;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;font-weight:500;">
          <el-icon style="font-size:13px;"><Download /></el-icon>{{ exporting ? '导出中...' : '导出Excel' }}
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
            <tr v-for="(r, i) in records" :key="r.id"
              @mouseenter="e => e.currentTarget.style.background = '#FAFBFF'"
              @mouseleave="e => e.currentTarget.style.background = '#fff'"
              style="transition:background 0.15s;">
              <td :style="{...tdSt, color:'#CBD5E1', width:'60px'}">{{ (page-1) * 10 + i + 1 }}</td>
              <td :style="tdSt">
                <div style="display:flex;align-items:center;gap:8px;">
                  <div v-if="r.userAvatarUrl" style="width:30px;height:30px;border-radius:8px;overflow:hidden;flex-shrink:0;">
                    <img :src="r.userAvatarUrl" style="width:100%;height:100%;object-fit:cover;" />
                  </div>
                  <div v-else :style="{width:'30px',height:'30px',borderRadius:'8px',background:avatarGradients[r.id % avatarGradients.length],display:'flex',alignItems:'center',justifyContent:'center',color:'#fff',fontSize:'11px',fontWeight:700,flexShrink:0}">
                    {{ (r.userNickName || '?')[0] }}
                  </div>
                  <span style="font-weight:500;">{{ r.userNickName || '-' }}</span>
                </div>
              </td>
              <td :style="tdSt">
                <div style="display:flex;align-items:center;gap:6px;">
                  <span>{{ r.categoryIcon }}</span>
                  <span>{{ r.categoryName }}</span>
                </div>
              </td>
              <td :style="tdSt">
                <span :style="{fontSize:'12px',padding:'2px 10px',borderRadius:'20px',background:r.type===1?'#FFF1F2':'#F0FDF4',color:r.type===1?'#F43F5E':'#11998E',fontWeight:500}">
                  {{ r.type === 1 ? '支出' : '收入' }}
                </span>
              </td>
              <td :style="{...tdSt, fontWeight:700, fontSize:'14px', color: r.type===1?'#F43F5E':'#11998E', textAlign:'right'}">
                {{ r.type === 1 ? '-' : '+' }}¥{{ (r.amount || 0).toFixed(2) }}
              </td>
              <td :style="{...tdSt, maxWidth:'150px', overflow:'hidden', textOverflow:'ellipsis', color:'#475569'}" :title="r.remark">{{ r.remark || '-' }}</td>
              <td :style="{...tdSt, color:'#94A3B8'}">{{ r.bookName || '-' }}</td>
              <td :style="{...tdSt, color:'#94A3B8', fontSize:'12px'}">{{ (r.recordTime || '').slice(0, 10) }}</td>
              <td :style="tdSt">
                <button @click="detail = r" style="height:28px;padding:0 10px;background:#EEF2FF;color:#667EEA;border:none;border-radius:7px;cursor:pointer;font-size:12px;display:flex;align-items:center;gap:3px;font-weight:500;">
                  <el-icon style="font-size:11px;"><View /></el-icon>查看
                </button>
              </td>
            </tr>
          </tbody>
          <tfoot>
            <tr style="background:#F8FAFC;">
              <td :colspan="4" :style="{...tdSt, fontWeight:600, color:'#1E293B', borderBottom:'none'}">汇总（当前页 {{ records.length }} 条，共 {{ total }} 条）</td>
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

      <!-- Empty state -->
      <div v-if="!loading && records.length === 0" style="padding:60px;text-align:center;color:#94A3B8;">
        <div style="font-size:14px;">暂无记录</div>
      </div>

      <!-- Pagination -->
      <div style="display:flex;justify-content:flex-end;padding:12px 16px;align-items:center;gap:4px;">
        <span style="font-size:12px;color:#94A3B8;margin-right:8px;">共 {{ total }} 条</span>
        <button :disabled="page <= 1" @click="handlePageChange(page - 1)"
          style="width:32px;height:32px;border:1px solid #E2E8F0;border-radius:8px;background:#fff;display:flex;align-items:center;justify-content:center;"
          :style="{opacity: page <= 1 ? 0.4 : 1, cursor: page <= 1 ? 'not-allowed' : 'pointer'}">
          <el-icon style="font-size:14px;color:#64748B;"><ArrowLeft /></el-icon>
        </button>
        <template v-for="(p, idx) in pages" :key="idx">
          <span v-if="p === '...'" style="width:32px;text-align:center;color:#94A3B8;font-size:13px;">…</span>
          <button v-else @click="handlePageChange(p)"
            :style="{width:'32px',height:'32px',border:'none',borderRadius:'8px',background: p===page ? 'linear-gradient(135deg,#667EEA,#764BA2)' : '#fff',color: p===page ? '#fff' : '#475569',cursor:'pointer',fontSize:'13px',fontWeight: p===page ? 600 : 400}">{{ p }}</button>
        </template>
        <button :disabled="page >= totalPages" @click="handlePageChange(page + 1)"
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
                  <div :style="{width:'24px',height:'24px',borderRadius:'6px',background:avatarGradients[detail.id % avatarGradients.length],display:'flex',alignItems:'center',justifyContent:'center',color:'#fff',fontSize:'10px',fontWeight:700}">{{ (detail.userNickName || '?')[0] }}</div>
                  {{ detail.userNickName || '-' }}
                </div>
              </span>
            </div>
            <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #F8FAFC;">
              <span style="width:80px;font-size:13px;color:#94A3B8;flex-shrink:0;">分类</span>
              <span style="font-size:14px;color:#1E293B;">{{ detail.categoryIcon }} {{ detail.categoryName }}</span>
            </div>
            <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #F8FAFC;">
              <span style="width:80px;font-size:13px;color:#94A3B8;flex-shrink:0;">类型</span>
              <span :style="{fontSize:'12px',padding:'2px 10px',borderRadius:'20px',background:detail.type===1?'#FFF1F2':'#F0FDF4',color:detail.type===1?'#F43F5E':'#11998E',fontWeight:500}">{{ detail.type === 1 ? '支出' : '收入' }}</span>
            </div>
            <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #F8FAFC;">
              <span style="width:80px;font-size:13px;color:#94A3B8;flex-shrink:0;">金额</span>
              <span :style="{fontSize:'24px',fontWeight:700,color:detail.type===1?'#F43F5E':'#11998E'}">{{ detail.type === 1 ? '-' : '+' }}¥{{ (detail.amount || 0).toFixed(2) }}</span>
            </div>
            <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #F8FAFC;">
              <span style="width:80px;font-size:13px;color:#94A3B8;flex-shrink:0;">备注</span>
              <span style="font-size:14px;color:#1E293B;">{{ detail.remark || '—' }}</span>
            </div>
            <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #F8FAFC;">
              <span style="width:80px;font-size:13px;color:#94A3B8;flex-shrink:0;">记账时间</span>
              <span style="font-size:14px;color:#1E293B;">{{ detail.recordTime || '-' }}</span>
            </div>
            <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #F8FAFC;">
              <span style="width:80px;font-size:13px;color:#94A3B8;flex-shrink:0;">所属账本</span>
              <span style="font-size:14px;color:#1E293B;">{{ detail.bookName || '-' }}</span>
            </div>
          </div>
          <div style="display:flex;justify-content:flex-end;padding-top:16px;border-top:1px solid #F1F5F9;margin-top:8px;">
            <button @click="detail = null" style="height:36px;padding:0 20px;background:#F8FAFC;color:#64748B;border:1px solid #E2E8F0;border-radius:10px;cursor:pointer;font-size:13px;">关闭</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
