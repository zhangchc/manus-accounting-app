<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit, Delete, DataAnalysis, CaretTop, CaretBottom, WarningFilled, RefreshRight } from '@element-plus/icons-vue'
import { getPositionSummary, addPosition, getPositionList, savePosition } from '@/api/stock'

const avatarGradients = [
  'linear-gradient(135deg,#667EEA,#764BA2)',
  'linear-gradient(135deg,#11998E,#38EF7D)',
  'linear-gradient(135deg,#F7971E,#FFD200)',
  'linear-gradient(135deg,#F953C6,#B91D73)',
  'linear-gradient(135deg,#A855F7,#6366F1)',
  'linear-gradient(135deg,#06B6D4,#3B82F6)',
  'linear-gradient(135deg,#F43F5E,#FB923C)',
]

// 汇总指标
const summary = ref({
  totalCost: 0,
  totalMarketValue: 0,
  totalDailyPnl: 0,
  totalProfit: 0,
  totalProfitRate: 0,
  positionRate: 0,
})

// 列表数据
const records = ref([])
const total = ref(0)
const loading = ref(false)

const searchCode = ref('')
const searchName = ref('')
const page = ref(1)
const pageSize = 10

const showModal = ref(false)
const editStock = ref(null)
const deleteId = ref(null)
const fCode = ref('')
const fName = ref('')
const fShares = ref('')
const fCost = ref('')

const totalProfit = computed(() => summary.value.totalProfit)
const totalRate = computed(() => summary.value.totalProfitRate)

function pnl(s) {
  if (!s.currentPrice) {
    return { profit: 0, rate: 0, marketValue: 0 }
  }
  const cost = s.costPrice * s.shares
  const mv = s.currentPrice * s.shares
  return { profit: mv - cost, rate: ((mv - cost) / cost) * 100, marketValue: mv }
}

const statCards = computed(() => {
  const profit = totalProfit.value
  const rate = totalRate.value
  const dailyPnl = summary.value.totalDailyPnl || 0
  return [
    { label: '总市值', value: `¥${formatNumber(summary.value.totalMarketValue)}`, sub: '', bg: 'linear-gradient(135deg,#667EEA,#764BA2)', subColor: '#667EEA' },
    { label: '总成本', value: `¥${formatNumber(summary.value.totalCost)}`, sub: '', bg: 'linear-gradient(135deg,#11998E,#38EF7D)', subColor: '#11998E' },
    {
      label: '当日盈亏',
      value: `${dailyPnl >= 0 ? '+' : ''}¥${formatNumber(dailyPnl)}`,
      sub: '',
      bg: dailyPnl >= 0 ? 'linear-gradient(135deg,#F43F5E,#FB923C)' : 'linear-gradient(135deg,#11998E,#38EF7D)',
      subColor: dailyPnl >= 0 ? '#F43F5E' : '#11998E',
    },
    {
      label: '总盈亏',
      value: `${profit >= 0 ? '+' : ''}¥${formatNumber(profit)}`,
      sub: `收益率 ${rate >= 0 ? '+' : ''}${(rate || 0).toFixed(2)}%`,
      bg: profit >= 0 ? 'linear-gradient(135deg,#F43F5E,#FB923C)' : 'linear-gradient(135deg,#11998E,#38EF7D)',
      subColor: profit >= 0 ? '#F43F5E' : '#11998E',
    },
    { label: '总仓位', value: `${(summary.value.positionRate || 0).toFixed(2)}%`, sub: '', bg: 'linear-gradient(135deg,#F59E0B,#EF4444)', subColor: '#F59E0B' },
  ]
})

const thStyle = {
  padding: '12px 20px', fontSize: '12px', color: '#94A3B8', fontWeight: 600,
  textAlign: 'left', whiteSpace: 'nowrap', background: '#F8FAFC',
  borderBottom: '1px solid #F1F5F9', letterSpacing: '0.03em',
}
const tdStyle = {
  padding: '14px 20px', fontSize: '13px', color: '#334155',
  borderBottom: '1px solid #F8FAFC', whiteSpace: 'nowrap',
}
const fieldStyle = {
  height: '36px', padding: '0 12px', borderRadius: '10px', background: '#F8FAFC',
  border: '1px solid #E2E8F0', fontSize: '13px', color: '#334155', outline: 'none',
  width: '100%', boxSizing: 'border-box',
}

async function loadSummary() {
  try {
    summary.value = await getPositionSummary()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载汇总数据失败')
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await getPositionList({
      stockCode: searchCode.value || undefined,
      stockName: searchName.value || undefined,
      page: page.value,
      pageSize,
    })
    records.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载持仓列表失败')
  } finally {
    loading.value = false
  }
}

async function loadData() {
  await Promise.all([loadSummary(), loadList()])
}

onMounted(loadData)

function openAdd() {
  editStock.value = null
  fCode.value = ''; fName.value = ''; fShares.value = ''; fCost.value = ''
  showModal.value = true
}

function openEdit(s) {
  editStock.value = s
  fCode.value = s.stockCode; fName.value = s.stockName
  fShares.value = String(s.shares); fCost.value = String(s.costPrice)
  showModal.value = true
}

async function handleSave() {
  try {
    if (editStock.value) {
      await savePosition({
        id: editStock.value.id,
        stockName: fName.value,
        shares: Number(fShares.value),
        costPrice: Number(fCost.value),
        deleted: 0,
      })
      ElMessage.success('修改成功')
    } else {
      await addPosition({
        stockCode: fCode.value,
        stockName: fName.value,
        shares: Number(fShares.value),
        costPrice: Number(fCost.value),
      })
      ElMessage.success('新增成功')
    }
    showModal.value = false
    await loadData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  }
}

async function confirmDelete() {
  if (deleteId.value !== null) {
    try {
      await savePosition({ id: deleteId.value, deleted: 1 })
      ElMessage.success('删除成功')
      deleteId.value = null
      await loadData()
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '删除失败')
    }
  }
}

function handleSearch() { page.value = 1; loadData() }
function handleReset() { searchCode.value = ''; searchName.value = ''; page.value = 1; loadData() }
async function refreshPrices() {
  if (loading.value) return
  await loadData()
  ElMessage.success('价格已刷新')
}

function formatNumber(n) {
  if (!n) return '0'
  return Number(n).toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}
</script>

<template>
  <div style="padding: 24px; background: #F0F2F8; min-height: 100%; box-sizing: border-box;">

    <!-- Stat cards -->
    <div style="display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; margin-bottom: 16px;">
      <div v-for="c in statCards" :key="c.label" style="background: #fff; border-radius: 16px; padding: 20px 22px; box-shadow: 0 2px 16px rgba(0,0,0,0.06); display: flex; align-items: center; gap: 16px;">
        <div :style="{ width: '46px', height: '46px', borderRadius: '14px', background: c.bg, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }">
          <el-icon style="font-size: 20px; color: #fff;"><DataAnalysis /></el-icon>
        </div>
        <div>
          <div style="font-size: 12px; color: #94A3B8; margin-bottom: 4px;">{{ c.label }}</div>
          <div style="font-size: 18px; font-weight: 700; color: #1E293B; line-height: 1.2;">{{ c.value }}</div>
          <div v-if="c.sub" :style="{ fontSize: '11px', color: c.subColor, marginTop: '2px', fontWeight: 500 }">{{ c.sub }}</div>
        </div>
      </div>
    </div>

    <!-- Search card -->
    <div style="background: #fff; border-radius: 16px; padding: 16px 24px; margin-bottom: 16px; box-shadow: 0 2px 16px rgba(0,0,0,0.06);">
      <div style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center;">
        <div style="position: relative;">
          <el-icon style="position: absolute; left: 11px; top: 50%; transform: translateY(-50%); color: #94A3B8; font-size: 13px;"><Search /></el-icon>
          <input v-model="searchCode" placeholder="股票代码" :style="{ ...fieldStyle, width: '160px', paddingLeft: '32px' }" />
        </div>
        <div style="position: relative;">
          <el-icon style="position: absolute; left: 11px; top: 50%; transform: translateY(-50%); color: #94A3B8; font-size: 13px;"><Search /></el-icon>
          <input v-model="searchName" placeholder="股票名称" :style="{ ...fieldStyle, width: '160px', paddingLeft: '32px' }" />
        </div>
        <button @click="handleSearch" style="height: 36px; padding: 0 16px; background: linear-gradient(135deg,#667EEA,#764BA2); color: #fff; border: none; border-radius: 10px; cursor: pointer; font-size: 13px; display: flex; align-items: center; gap: 5px; font-weight: 500; box-shadow: 0 3px 10px rgba(102,126,234,0.35);">
          <el-icon style="font-size: 13px;"><Search /></el-icon>搜索
        </button>
        <button @click="handleReset" style="height: 36px; padding: 0 16px; background: #F8FAFC; color: #64748B; border: 1px solid #E2E8F0; border-radius: 10px; cursor: pointer; font-size: 13px; display: flex; align-items: center; gap: 5px;">
          <el-icon style="font-size: 13px;"><RefreshRight /></el-icon>重置
        </button>
        <button @click="refreshPrices" :disabled="loading" style="height: 36px; padding: 0 16px; background: #F0FDF4; color: #11998E; border: 1px solid #BBF7D0; border-radius: 10px; cursor: pointer; font-size: 13px; display: flex; align-items: center; gap: 5px; font-weight: 500;">
          <el-icon :style="{ fontSize: '13px', animation: loading ? 'spin 1s linear infinite' : 'none' }"><RefreshRight /></el-icon>刷新价格
        </button>
        <button @click="openAdd" style="height: 36px; padding: 0 18px; background: linear-gradient(135deg,#667EEA,#764BA2); color: #fff; border: none; border-radius: 10px; cursor: pointer; font-size: 13px; display: flex; align-items: center; gap: 6px; font-weight: 500; box-shadow: 0 3px 10px rgba(102,126,234,0.35); margin-left: auto;">
          <el-icon style="font-size: 15px;"><Plus /></el-icon>新增持仓
        </button>
      </div>
    </div>

    <!-- Table card -->
    <div style="background: #fff; border-radius: 16px; box-shadow: 0 2px 16px rgba(0,0,0,0.06); overflow: hidden;" v-loading="loading">
      <div style="overflow-x: auto;">
        <table style="width: 100%; border-collapse: collapse; min-width: 960px;">
          <thead>
            <tr>
              <th :style="thStyle">序号</th>
              <th :style="thStyle">股票代码</th>
              <th :style="thStyle">股票名称</th>
              <th :style="thStyle">持仓股数</th>
              <th :style="{...thStyle, textAlign: 'right'}">成本价</th>
              <th :style="{...thStyle, textAlign: 'right'}">现价</th>
              <th :style="{...thStyle, textAlign: 'right'}">市值</th>
              <th :style="{...thStyle, textAlign: 'right'}">盈亏金额</th>
              <th :style="{...thStyle, textAlign: 'right'}">盈亏比例</th>
              <th :style="thStyle">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(s, i) in records" :key="s.id"
              style="transition: background 0.15s;"
              @mouseenter="e => e.currentTarget.style.background = '#FAFBFF'"
              @mouseleave="e => e.currentTarget.style.background = '#fff'"
            >
              <td :style="{...tdStyle, color: '#CBD5E1', width: '60px'}">{{ (page - 1) * pageSize + i + 1 }}</td>
              <td :style="tdStyle">
                <span style="font-family: monospace; font-weight: 600; color: #1E293B; background: #F1F5F9; padding: 2px 8px; border-radius: 6px; font-size: 13px;">{{ s.stockCode }}</span>
              </td>
              <td :style="tdStyle">
                <div style="display: flex; align-items: center; gap: 8px;">
                  <div :style="{ width: '30px', height: '30px', borderRadius: '8px', background: avatarGradients[i % avatarGradients.length], display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: '11px', fontWeight: 700, flexShrink: 0 }">
                    {{ s.stockName[0] }}
                  </div>
                  <span style="font-weight: 600; color: #1E293B;">{{ s.stockName }}</span>
                </div>
              </td>
              <td :style="{...tdStyle, color: '#475569', fontWeight: 500}">{{ s.shares.toLocaleString() }} 股</td>
              <td :style="{...tdStyle, textAlign: 'right', color: '#475569'}">¥{{ s.costPrice.toFixed(2) }}</td>
              <td :style="{...tdStyle, textAlign: 'right', fontWeight: 600, color: pnl(s).profit >= 0 ? '#F43F5E' : '#11998E'}">
                <div v-if="s.currentPrice" style="display: flex; align-items: center; justify-content: flex-end; gap: 3px;">
                  <el-icon v-if="pnl(s).profit >= 0" style="font-size: 12px;"><CaretTop /></el-icon>
                  <el-icon v-else style="font-size: 12px;"><CaretBottom /></el-icon>
                  ¥{{ s.currentPrice.toFixed(2) }}
                </div>
                <span v-else style="color: #CBD5E1;">--</span>
              </td>
              <td :style="{...tdStyle, textAlign: 'right', fontWeight: 600, color: '#1E293B'}">¥{{ formatNumber(pnl(s).marketValue) }}</td>
              <td :style="{...tdStyle, textAlign: 'right', fontWeight: 700, color: pnl(s).profit >= 0 ? '#F43F5E' : '#11998E'}">
                {{ pnl(s).profit >= 0 ? '+' : '' }}¥{{ formatNumber(pnl(s).profit) }}
              </td>
              <td :style="{...tdStyle, textAlign: 'right'}">
                <span :style="{ fontSize: '12px', padding: '3px 10px', borderRadius: '20px', background: pnl(s).profit >= 0 ? '#FFF1F2' : '#F0FDF4', color: pnl(s).profit >= 0 ? '#F43F5E' : '#11998E', fontWeight: 700 }">
                  {{ pnl(s).profit >= 0 ? '▲' : '▼' }} {{ Math.abs(pnl(s).rate).toFixed(2) }}%
                </span>
              </td>
              <td :style="tdStyle">
                <div style="display: flex; gap: 6px;">
                  <button @click="openEdit(s)" style="height: 28px; padding: 0 10px; background: #EEF2FF; color: #667EEA; border: none; border-radius: 7px; cursor: pointer; font-size: 12px; display: flex; align-items: center; gap: 3px; font-weight: 500;">
                    <el-icon style="font-size: 11px;"><Edit /></el-icon>编辑
                  </button>
                  <button @click="deleteId = s.id" style="height: 28px; padding: 0 10px; background: #FFF1F2; color: #F43F5E; border: none; border-radius: 7px; cursor: pointer; font-size: 12px; display: flex; align-items: center; gap: 3px; font-weight: 500;">
                    <el-icon style="font-size: 11px;"><Delete /></el-icon>删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div style="display: flex; justify-content: flex-end; padding: 12px 16px; align-items: center; gap: 6px;">
        <span style="font-size: 13px; color: #94A3B8; margin-right: 8px;">共 {{ total }} 条</span>
        <button :disabled="page <= 1" @click="page--; loadList()"
          :style="{ width: '32px', height: '32px', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '1px solid #E2E8F0', borderRadius: '8px', background: '#fff', color: '#64748B', cursor: page <= 1 ? 'default' : 'pointer', fontSize: '14px', opacity: page <= 1 ? 0.4 : 1 }">‹</button>
        <button v-for="p in Math.min(Math.ceil(total / pageSize), 5)" :key="p" @click="page = p; loadList()"
          :style="{ width: '32px', height: '32px', display: 'flex', alignItems: 'center', justifyContent: 'center', border: 'none', borderRadius: '8px', cursor: 'pointer', fontSize: '13px', fontWeight: 500,
            background: page === p ? 'linear-gradient(135deg,#667EEA,#764BA2)' : 'transparent',
            color: page === p ? '#fff' : '#64748B' }">{{ p }}</button>
        <button :disabled="page >= Math.ceil(total / pageSize)" @click="page++; loadList()"
          :style="{ width: '32px', height: '32px', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '1px solid #E2E8F0', borderRadius: '8px', background: '#fff', color: '#64748B', cursor: page >= Math.ceil(total / pageSize) ? 'default' : 'pointer', fontSize: '14px', opacity: page >= Math.ceil(total / pageSize) ? 0.4 : 1 }">›</button>
      </div>
    </div>

    <!-- Add/Edit Modal -->
    <el-dialog v-model="showModal" :title="editStock ? '编辑持仓' : '新增持仓'" width="520px" :close-on-click-modal="false">
      <div style="display: flex; flex-wrap: wrap; gap: 0 16px;">
        <div style="flex: 1 1 calc(50% - 8px); min-width: 200px; margin-bottom: 14px;">
          <div style="font-size: 13px; color: #475569; margin-bottom: 6px; font-weight: 500;">股票代码 <span style="color: #F43F5E;">*</span></div>
          <input v-model="fCode" placeholder="如：600519" :disabled="!!editStock" :style="{...fieldStyle, fontFamily: 'monospace', opacity: editStock ? 0.5 : 1}" />
        </div>
        <div style="flex: 1 1 calc(50% - 8px); min-width: 200px; margin-bottom: 14px;">
          <div style="font-size: 13px; color: #475569; margin-bottom: 6px; font-weight: 500;">股票名称 <span style="color: #F43F5E;">*</span></div>
          <input v-model="fName" placeholder="请输入股票名称" :style="fieldStyle" />
        </div>
        <div style="flex: 1 1 calc(50% - 8px); min-width: 200px; margin-bottom: 14px;">
          <div style="font-size: 13px; color: #475569; margin-bottom: 6px; font-weight: 500;">持仓股数 <span style="color: #F43F5E;">*</span></div>
          <input v-model="fShares" type="number" placeholder="请输入股数" :style="fieldStyle" />
        </div>
        <div style="flex: 1 1 calc(50% - 8px); min-width: 200px; margin-bottom: 14px;">
          <div style="font-size: 13px; color: #475569; margin-bottom: 6px; font-weight: 500;">成本价（元） <span style="color: #F43F5E;">*</span></div>
          <input v-model="fCost" type="number" step="0.01" placeholder="请输入成本价" :style="fieldStyle" />
        </div>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 10px; padding-top: 16px; border-top: 1px solid #F1F5F9; margin-top: 8px;">
          <button @click="showModal = false" style="height: 38px; padding: 0 20px; border: 1px solid #E2E8F0; border-radius: 10px; background: #fff; color: #64748B; cursor: pointer; font-size: 14px;">取消</button>
          <button @click="handleSave" style="height: 38px; padding: 0 24px; border: none; border-radius: 10px; background: linear-gradient(135deg,#667EEA,#764BA2); color: #fff; cursor: pointer; font-size: 14px; font-weight: 600; box-shadow: 0 4px 12px rgba(102,126,234,0.4);">确定</button>
        </div>
      </template>
    </el-dialog>

    <!-- Delete Confirm -->
    <div v-if="deleteId !== null"
      style="position: fixed; inset: 0; background: rgba(15,23,42,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000;"
      @click="deleteId = null">
      <div style="background: #fff; border-radius: 16px; width: 380px; max-width: 90vw; padding: 32px 28px; box-shadow: 0 16px 48px rgba(244,63,94,0.14), 0 4px 16px rgba(0,0,0,0.08); text-align: center;"
        @click.stop>
        <div style="width: 64px; height: 64px; border-radius: 50%; background: #FFF1F2; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px;">
          <el-icon style="font-size: 28px; color: #F43F5E;"><WarningFilled /></el-icon>
        </div>
        <div style="font-size: 17px; font-weight: 700; color: #1E293B; margin-bottom: 8px;">确认删除</div>
        <div style="font-size: 14px; color: #64748B; line-height: 1.6; margin-bottom: 24px;">
          此操作不可逆，删除后该持仓记录将无法恢复。<br />确定要继续吗？
        </div>
        <div style="display: flex; gap: 10px; justify-content: center;">
          <button @click="deleteId = null" style="height: 40px; padding: 0 24px; border: 1px solid #E2E8F0; border-radius: 10px; background: #fff; color: #64748B; cursor: pointer; font-size: 14px; font-weight: 500;">取消</button>
          <button @click="confirmDelete" style="height: 40px; padding: 0 28px; border: none; border-radius: 10px; background: linear-gradient(135deg,#F43F5E,#E11D48); color: #fff; cursor: pointer; font-size: 14px; font-weight: 600; box-shadow: 0 4px 12px rgba(244,63,94,0.35);">确认删除</button>
        </div>
      </div>
    </div>

  </div>
</template>

<style>
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
