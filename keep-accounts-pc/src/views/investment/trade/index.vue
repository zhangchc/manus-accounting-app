<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import {
  getTradeStrategy, saveTradeStrategy, tradePrecheck, tradeSell, tradeBuy,
  getTradeRecords, getTradeSummary, resetTrade, getStockPrice
} from '@/api/stock'

const summary = ref(null)
const strategy = ref(null)
const strategyForm = ref({
  basePrice: 64, sellShares: 600, buyShares: 600,
  maxSellCount: 3, maxBuyCount: 3, totalHolding: 6800,
  alertWarningPrice: 25, alertCriticalPrice: 22,
})
const priceLoading = ref(false)
const records = ref([])
const recordsPage = ref(1)
const recordsTotal = ref(0)
const savingStrategy = ref(false)
const submitting = ref(false)

// Precheck dialog state
const dialogVisible = ref(false)
const dialogData = ref(null)   // { type: 'SELL'|'BUY', ...precheck }
const dialogForm = ref({ tradePrice: null, shares: null, reason: '' })

const cardStyle = { background:'#fff',borderRadius:'16px',padding:'24px',boxShadow:'0 2px 16px rgba(0,0,0,0.06)',marginBottom:'16px' }
const labelS = { fontSize:'13px',color:'#94A3B8',marginBottom:'6px',fontWeight:'500' }
const inputS = { height:'40px',padding:'0 12px',borderRadius:'10px',background:'#F8FAFC',border:'1px solid #E2E8F0',fontSize:'14px',color:'#334155',outline:'none',width:'100%',boxSizing:'border-box' }

function fmt(n) {
  if (n == null || isNaN(n)) return '-'
  return Number(n).toLocaleString('zh-CN',{minimumFractionDigits:2,maximumFractionDigits:2})
}

// Load all data
async function loadAll() {
  priceLoading.value = true
  try {
    const s = await getTradeSummary()
    summary.value = s
    if (s.strategy && s.strategy.id) {
      strategy.value = s.strategy
      strategyForm.value = {
        basePrice: s.strategy.basePrice,
        sellShares: s.strategy.sellShares,
        buyShares: s.strategy.buyShares,
        maxSellCount: s.strategy.maxSellCount,
        maxBuyCount: s.strategy.maxBuyCount,
        totalHolding: s.strategy.totalHolding,
        alertWarningPrice: s.strategy.alertWarningPrice,
        alertCriticalPrice: s.strategy.alertCriticalPrice,
      }
    }
    // Always fetch live price regardless of strategy
    if (!summary.value.currentPrice) {
      await refreshStockPrice()
    }
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    priceLoading.value = false
  }
  loadRecords()
}

async function refreshStockPrice() {
  if (!summary.value) summary.value = {}
  try {
    const stockCode = (summary.value.strategy && summary.value.strategy.stockCode) || 'sz300255'
    const res = await getStockPrice(stockCode)
    if (res && res.price != null) {
      summary.value.currentPrice = res.price
      summary.value.changeAmount = res.change
      summary.value.changePercent = res.changePercent
    }
  } catch (e) {
    // ignore silently; handleRefreshPrice will show error
  }
}

async function handleRefreshPrice() {
  priceLoading.value = true
  try {
    await refreshStockPrice()
    if (!summary.value || summary.value.currentPrice == null) {
      ElMessage.error('获取行情失败')
    }
  } catch (e) {
    ElMessage.error('获取行情失败')
  } finally {
    priceLoading.value = false
  }
}

async function loadRecords() {
  try {
    const data = await getTradeRecords({ page: recordsPage.value, pageSize: 10 })
    records.value = data.records || []
    recordsTotal.value = data.total || 0
  } catch (e) {
    // ignore
  }
}

async function handleSaveStrategy() {
  savingStrategy.value = true
  try {
    const data = await saveTradeStrategy(strategyForm.value)
    strategy.value = data
    ElMessage.success('策略保存成功')
    loadAll()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    savingStrategy.value = false
  }
}

async function handleResetCount() {
  try {
    await resetTrade()
    ElMessage.success('计数已重置')
    loadAll()
  } catch (e) {
    ElMessage.error('重置失败')
  }
}

// Open operation dialog
async function openDialog(type) {
  if (!strategy.value || !strategy.value.id) {
    ElMessage.warning('请先保存策略配置')
    return
  }
  try {
    const currentPrice = summary.value && summary.value.currentPrice ? summary.value.currentPrice : null
    const precheck = await tradePrecheck(type, currentPrice)
    dialogData.value = { type, ...precheck }
    dialogForm.value = {
      tradePrice: type === 'SELL'
        ? (precheck.nextSellNo <= 3
          ? (strategy.value.basePrice * Math.pow(1.05, precheck.nextSellNo)).toFixed(2)
          : '')
        : '',
      shares: type === 'SELL' ? strategy.value.sellShares : strategy.value.buyShares,
      reason: '',
    }
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error('操作检查失败')
  }
}

async function confirmDialog() {
  if (!dialogForm.value.tradePrice || dialogForm.value.tradePrice <= 0) {
    ElMessage.warning('请输入成交价')
    return
  }
  if (dialogData.value.reasonRequired && !dialogForm.value.reason) {
    ElMessage.warning('该操作为' + (dialogData.value.opLevel === 'BOUNDARY' ? '边界' : '超限') + '操作，必须填写理由')
    return
  }
  submitting.value = true
  try {
    const payload = {
      tradePrice: Number(dialogForm.value.tradePrice),
      shares: Number(dialogForm.value.shares),
      reason: dialogForm.value.reason || '',
    }
    if (dialogData.value.type === 'SELL') {
      await tradeSell(payload)
    } else {
      await tradeBuy(payload)
    }
    dialogVisible.value = false
    ElMessage.success(dialogData.value.type === 'SELL' ? '卖出记录成功' : '买入记录成功')
    loadAll()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function prevPage() { if (recordsPage.value > 1) { recordsPage.value--; loadRecords() } }
function nextPage() {
  const totalPages = Math.ceil(recordsTotal.value / 10)
  if (recordsPage.value < totalPages) { recordsPage.value++; loadRecords() }
}

// Helpers
function getAlertBg(level) {
  if (level === 'CRITICAL') return '#FFF1F2'
  if (level === 'WARNING') return '#FFF7ED'
  return '#EFF6FF'
}
function getAlertBorder(level) {
  if (level === 'CRITICAL') return '#F43F5E'
  if (level === 'WARNING') return '#F7971E'
  return '#3B82F6'
}
function getAlertIcon(level) {
  if (level === 'CRITICAL') return '🔴'
  if (level === 'WARNING') return '🟡'
  return '🔵'
}

onMounted(() => loadAll())
</script>

<template>
  <div style="padding:24px;background:#F0F2F8;min-height:100%;">
    <!-- Alert zone -->
    <div v-if="summary && summary.alerts && summary.alerts.length" style="margin-bottom:16px;">
      <div v-for="(a, i) in summary.alerts" :key="i"
        :style="{padding:'10px 16px',borderRadius:'10px',fontSize:'13px',fontWeight:500,
          background:getAlertBg(a.level),color:a.level==='CRITICAL'?'#BE123C':a.level==='WARNING'?'#C2410C':'#1E40AF',
          border:'1px solid '+getAlertBorder(a.level),marginBottom:'6px'}">
        {{ getAlertIcon(a.level) }} {{ a.msg }}
      </div>
    </div>

    <!-- Row 1: 实时行情 | 策略配置 -->
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;">

      <div :style="cardStyle">
        <div style="font-size:15px;font-weight:600;color:#1E293B;margin-bottom:16px;">📊 实时行情</div>
        <div v-if="summary && summary.currentPrice != null" style="display:flex;align-items:baseline;justify-content:space-between;">
          <div>
            <div :style="labelS">最新价</div>
            <div style="font-size:32px;font-weight:700;color:#1E293B;">{{ summary.currentPrice }}</div>
          </div>
          <div style="text-align:right;">
            <div :style="labelS">涨跌</div>
            <div :style="{fontSize:'18px',fontWeight:600,color:Number(summary.changeAmount||0)>=0?'#F43F5E':'#11998E'}">
              {{ Number(summary.changeAmount||0)>=0?'+':'' }}{{ summary.changeAmount != null ? summary.changeAmount : '-' }}
            </div>
            <div :style="{fontSize:'14px',color:parseFloat(summary.changePercent||0)>=0?'#F43F5E':'#11998E'}">
              {{ parseFloat(summary.changePercent||0)>=0?'+':'' }}{{ summary.changePercent != null ? summary.changePercent : '-' }}%
            </div>
          </div>
        </div>
        <div v-else-if="summary" style="padding:16px 0;text-align:center;color:#94A3B8;font-size:13px;">
          暂无行情数据，请点击下方按钮刷新
        </div>
        <button @click="handleRefreshPrice" :disabled="priceLoading"
          style="margin-top:12px;height:34px;padding:0 14px;background:#F8FAFC;color:#64748B;border:1px solid #E2E8F0;border-radius:8px;cursor:pointer;font-size:12px;display:flex;align-items:center;gap:5px;">
          <el-icon style="font-size:12px;"><RefreshRight /></el-icon>{{ priceLoading?'获取中...':'刷新行情' }}
        </button>
      </div>

      <div :style="cardStyle">
        <div style="font-size:15px;font-weight:600;color:#1E293B;margin-bottom:16px;">⚙️ 策略配置</div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;">
          <div><div :style="labelS">基准价</div><input v-model.number="strategyForm.basePrice" :style="inputS" type="number" step="0.01" /></div>
          <div><div :style="labelS">卖出股数</div><input v-model.number="strategyForm.sellShares" :style="inputS" type="number" /></div>
          <div><div :style="labelS">买入股数</div><input v-model.number="strategyForm.buyShares" :style="inputS" type="number" /></div>
          <div><div :style="labelS">最多卖出次数</div><input v-model.number="strategyForm.maxSellCount" :style="inputS" type="number" /></div>
          <div><div :style="labelS">最多买入次数</div><input v-model.number="strategyForm.maxBuyCount" :style="inputS" type="number" /></div>
          <div><div :style="labelS">总持仓</div><input v-model.number="strategyForm.totalHolding" :style="inputS" type="number" /></div>
          <div><div :style="labelS">预警价</div><input v-model.number="strategyForm.alertWarningPrice" :style="inputS" type="number" step="0.01" /></div>
          <div><div :style="labelS">紧急价</div><input v-model.number="strategyForm.alertCriticalPrice" :style="inputS" type="number" step="0.01" /></div>
        </div>
        <div style="display:flex;gap:10px;margin-top:12px;">
          <button @click="handleSaveStrategy" :disabled="savingStrategy"
            :style="{flex:1,height:'38px',border:'none',borderRadius:'10px',cursor:'pointer',fontSize:'13px',fontWeight:600,color:'#fff',
              background:'linear-gradient(135deg,#667EEA,#764BA2)',boxShadow:'0 4px 12px rgba(102,126,234,0.4)'}">
            {{ savingStrategy ? '保存中...' : '保存策略' }}
          </button>
          <button @click="handleResetCount"
            style="height:38px;padding:0 16px;background:#FFF1F2;color:#F43F5E;border:1px solid #FECDD3;border-radius:10px;cursor:pointer;font-size:13px;font-weight:500;">
            重置计数
          </button>
        </div>
      </div>
    </div>

    <!-- Row 2: 卖出阶梯 + 待回补 -->
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;">
      <div>
        <div :style="cardStyle">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
            <div style="font-size:15px;font-weight:600;color:#1E293B;">📈 卖出阶梯（卖三）</div>
            <button @click="openDialog('SELL')"
              style="height:34px;padding:0 18px;background:linear-gradient(135deg,#F43F5E,#E11D48);color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:13px;font-weight:600;box-shadow:0 3px 10px rgba(244,63,94,0.3);">
              卖出
            </button>
          </div>
          <div v-if="strategy" style="margin-bottom:8px;font-size:13px;color:#64748B;">
            基准价: <b>{{ strategy.basePrice }}</b> &nbsp; 卖出进度: <b>{{ summary.unmatchedSellCount != null ? summary.unmatchedSellCount : strategy.sellCount }}/{{ strategy.maxSellCount }}</b>
          </div>
          <div v-if="summary && summary.sellLevels">
            <div v-for="lv in summary.sellLevels" :key="lv.level"
              style="display:flex;justify-content:space-between;align-items:center;padding:10px 12px;background:#F8FAFC;border-radius:8px;margin-bottom:6px;border:1px solid #E2E8F0;">
              <div>
                <div style="font-size:12px;color:#94A3B8;">卖{{ lv.level === 1 ? '一' : lv.level === 2 ? '二' : '三' }}</div>
                <div style="font-size:14px;font-weight:600;color:#1E293B;">{{ lv.price }}</div>
              </div>
              <div style="text-align:right;">
                <div :style="{fontSize:'12px',color:lv.done?'#11998E':'#CBD5E1'}">
                  {{ lv.done ? '✅已卖' : '⬜待触发' }}
                </div>
                <div style="font-size:11px;color:#94A3B8;">↓{{ lv.backBuyPrice }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div :style="cardStyle">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
          <div style="font-size:15px;font-weight:600;color:#1E293B;">📉 待回补（买三）</div>
            <button @click="openDialog('BUY')"
              style="height:34px;padding:0 18px;background:linear-gradient(135deg,#11998E,#0D9488);color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:13px;font-weight:600;box-shadow:0 3px 10px rgba(17,153,142,0.3);">
              买入
            </button>
          </div>
          <div v-if="strategy" style="margin-bottom:8px;font-size:13px;color:#64748B;">
            买入进度: <b>{{ summary.unmatchedBuyCount != null ? summary.unmatchedBuyCount : strategy.buyCount }}/{{ strategy.maxBuyCount }}</b>
          </div>
          <div v-if="summary && summary.buyLevels && summary.buyLevels.length">
            <div v-for="bl in summary.buyLevels" :key="bl.sellId"
              style="display:flex;justify-content:space-between;align-items:center;padding:10px 12px;background:#F8FAFC;border-radius:8px;margin-bottom:6px;border:1px solid #E2E8F0;">
              <div>
                <div style="font-size:12px;color:#94A3B8;">卖出价 #{{ bl.sellNo }}</div>
                <div style="font-size:14px;font-weight:600;color:#1E293B;">{{ bl.sellPrice }}</div>
              </div>
              <div style="font-size:18px;color:#CBD5E1;">→</div>
              <div style="text-align:right;">
                <div style="font-size:12px;color:#94A3B8;">回补价</div>
                <div style="font-size:14px;font-weight:600;color:#11998E;">{{ bl.buyPrice }}</div>
              </div>
            </div>
          </div>
          <div v-else style="text-align:center;padding:20px;color:#94A3B8;font-size:13px;">
            暂无待回补的卖出记录
          </div>
      </div>
    </div>

    <!-- Records table -->
    <div :style="cardStyle">
      <div style="font-size:15px;font-weight:600;color:#1E293B;margin-bottom:16px;">📋 操作记录</div>
      <div style="overflow-x:auto;">
        <table style="width:100%;border-collapse:collapse;min-width:1050px;font-size:13px;">
          <thead>
            <tr style="background:#F8FAFC;">
              <th style="padding:10px 12px;text-align:left;color:#94A3B8;font-weight:600;font-size:12px;">时间</th>
              <th style="padding:10px 12px;text-align:center;color:#94A3B8;font-weight:600;font-size:12px;">类型</th>
              <th style="padding:10px 12px;text-align:right;color:#94A3B8;font-weight:600;font-size:12px;">成交价</th>
              <th style="padding:10px 12px;text-align:right;color:#94A3B8;font-weight:600;font-size:12px;">股数</th>
              <th style="padding:10px 12px;text-align:right;color:#94A3B8;font-weight:600;font-size:12px;">获利</th>
              <th style="padding:10px 12px;text-align:center;color:#94A3B8;font-weight:600;font-size:12px;">卖计</th>
              <th style="padding:10px 12px;text-align:center;color:#94A3B8;font-weight:600;font-size:12px;">买计</th>
              <th style="padding:10px 12px;text-align:right;color:#94A3B8;font-weight:600;font-size:12px;">持仓</th>
              <th style="padding:10px 12px;text-align:left;color:#94A3B8;font-weight:600;font-size:12px;">场景</th>
              <th style="padding:10px 12px;text-align:left;color:#94A3B8;font-weight:600;font-size:12px;">理由</th>
              <th style="padding:10px 12px;text-align:center;color:#94A3B8;font-weight:600;font-size:12px;">级别</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in records" :key="r.id" style="border-bottom:1px solid #F8FAFC;">
              <td style="padding:10px 12px;color:#94A3B8;font-size:12px;">{{ (r.createdAt||'').slice(0,16) }}</td>
              <td style="padding:10px 12px;text-align:center;">
                <span :style="{fontSize:'12px',padding:'2px 10px',borderRadius:'20px',fontWeight:500,
                  background:r.tradeType==='SELL'?'#FFF1F2':'#F0FDF4',color:r.tradeType==='SELL'?'#F43F5E':'#11998E'}">
                  {{ r.tradeType === 'SELL' ? '卖出' : '买入' }}
                </span>
              </td>
              <td style="padding:10px 12px;text-align:right;font-weight:600;color:#1E293B;">{{ r.tradePrice }}</td>
              <td style="padding:10px 12px;text-align:right;color:#334155;">{{ r.shares }}</td>
              <td style="padding:10px 12px;text-align:right;font-weight:600;color:r.profit && Number(r.profit)>0?'#F43F5E':'#94A3B8';">
                {{ r.profit != null ? (Number(r.profit)>=0?'+':'')+fmt(r.profit) : '-' }}
              </td>
              <td style="padding:10px 12px;text-align:center;color:#334155;">{{ r.sellNo || '-' }}</td>
              <td style="padding:10px 12px;text-align:center;color:#334155;">{{ r.buyNo || '-' }}</td>
              <td style="padding:10px 12px;text-align:right;font-weight:600;color:#1E293B;">{{ r.currentHolding || '-' }}</td>
              <td style="padding:10px 12px;max-width:120px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:#64748B;font-size:12px;" :title="r.scenario">{{ r.scenario || '-' }}</td>
              <td style="padding:10px 12px;max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:#475569;" :title="r.reason">{{ r.reason || '-' }}</td>
              <td style="padding:10px 12px;text-align:center;">
                <span v-if="r.opLevel === 'NORMAL'" style="font-size:11px;color:#11998E;">正常</span>
                <span v-else-if="r.opLevel === 'BOUNDARY'" style="font-size:11px;color:#F7971E;font-weight:600;">⚠边界</span>
                <span v-else-if="r.opLevel === 'OVERLIMIT'" style="font-size:11px;color:#F43F5E;font-weight:600;">🔴超限</span>
                <span v-else style="font-size:11px;color:#94A3B8;">-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="records.length===0" style="text-align:center;padding:40px;color:#94A3B8;font-size:13px;">暂无操作记录</div>
      <div v-else style="display:flex;justify-content:flex-end;padding:12px 0 0;align-items:center;gap:6px;">
        <span style="font-size:12px;color:#94A3B8;">共 {{ recordsTotal }} 条</span>
        <button @click="prevPage" :disabled="recordsPage<=1"
          style="width:30px;height:30px;border:1px solid #E2E8F0;border-radius:6px;background:#fff;cursor:pointer;display:flex;align-items:center;justify-content:center;"
          :style="{opacity:recordsPage<=1?0.4:1}">◀</button>
        <span style="font-size:13px;color:#334155;">{{ recordsPage }} / {{ Math.max(1,Math.ceil(recordsTotal/10)) }}</span>
        <button @click="nextPage" :disabled="recordsPage>=Math.ceil(recordsTotal/10)"
          style="width:30px;height:30px;border:1px solid #E2E8F0;border-radius:6px;background:#fff;cursor:pointer;display:flex;align-items:center;justify-content:center;"
          :style="{opacity:recordsPage>=Math.ceil(recordsTotal/10)?0.4:1}">▶</button>
      </div>
    </div>

    <!-- Stats -->
    <div :style="cardStyle" v-if="summary">
      <div style="font-size:15px;font-weight:600;color:#1E293B;margin-bottom:16px;">💰 统计</div>
      <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:16px;">
        <div style="padding:14px;background:#F8FAFC;border-radius:10px;text-align:center;">
          <div style="font-size:12px;color:#94A3B8;">累计做T获利</div>
          <div :style="{fontSize:'20px',fontWeight:700,color:Number(summary.totalProfit||0)>=0?'#F43F5E':'#11998E'}">
            {{ Number(summary.totalProfit||0)>=0?'+':'' }}¥{{ fmt(summary.totalProfit) }}
          </div>
        </div>
        <div style="padding:14px;background:#F8FAFC;border-radius:10px;text-align:center;">
          <div style="font-size:12px;color:#94A3B8;">累计卖出</div>
          <div style="font-size:20px;font-weight:700;color:#1E293B;">{{ summary.totalSellCount }} 次</div>
          <div style="font-size:11px;color:#94A3B8;">{{ summary.totalSellShares }} 股</div>
        </div>
        <div style="padding:14px;background:#F8FAFC;border-radius:10px;text-align:center;">
          <div style="font-size:12px;color:#94A3B8;">累计买入</div>
          <div style="font-size:20px;font-weight:700;color:#1E293B;">{{ summary.totalBuyCount }} 次</div>
          <div style="font-size:11px;color:#94A3B8;">{{ summary.totalBuyShares }} 股</div>
        </div>
        <div style="padding:14px;background:#F8FAFC;border-radius:10px;text-align:center;">
          <div style="font-size:12px;color:#94A3B8;">当前持仓</div>
          <div style="font-size:20px;font-weight:700;color:#1E293B;">{{ summary.currentHolding }} 股</div>
          <div style="font-size:11px;color:#94A3B8;">市值 ¥{{ fmt(summary.currentMarketValue) }}</div>
        </div>
      </div>
    </div>

    <!-- Confirmation Dialog -->
    <div v-if="dialogVisible" style="position:fixed;inset:0;background:rgba(15,23,42,0.5);display:flex;align-items:center;justify-content:center;z-index:1000;" @click="dialogVisible = false">
      <div style="background:#fff;border-radius:16px;width:440px;max-width:90vw;box-shadow:0 16px 48px rgba(0,0,0,0.14);" @click.stop>
        <!-- Dialog header -->
        <div :style="{padding:'20px 24px',borderBottom:'1px solid #F1F5F9',
          background:dialogData&&dialogData.opLevel==='OVERLIMIT'?'linear-gradient(135deg,#FFF1F2,#FFF5F6)':
            dialogData&&dialogData.opLevel==='BOUNDARY'?'linear-gradient(135deg,#FFF7ED,#FFFBEB)':
            'linear-gradient(135deg,#F0FDF4,#F5FDF8)'}">
          <div :style="{fontSize:'16px',fontWeight:700,color:dialogData&&dialogData.opLevel==='OVERLIMIT'?'#BE123C':
            dialogData&&dialogData.opLevel==='BOUNDARY'?'#C2410C':'#1E293B'}">
            {{ dialogData && dialogData.opLevel === 'OVERLIMIT' ? '🔴 超限操作' :
               dialogData && dialogData.opLevel === 'BOUNDARY' ? '⚠️ 边界操作' : '确认' + (dialogData&&dialogData.type==='SELL'?'卖出':'买入') }}
          </div>
        </div>
        <div style="padding:20px 24px;">
          <!-- Warning -->
          <div v-if="dialogData && dialogData.warning"
            :style="{padding:'12px',borderRadius:'8px',fontSize:'13px',lineHeight:'1.6',marginBottom:'12px',
              background:dialogData.opLevel==='OVERLIMIT'?'#FFF1F2':dialogData.opLevel==='BOUNDARY'?'#FFF7ED':'#EFF6FF',
              color:dialogData.opLevel==='OVERLIMIT'?'#BE123C':dialogData.opLevel==='BOUNDARY'?'#C2410C':'#1E40AF',
              border:'1px solid '+(dialogData.opLevel==='OVERLIMIT'?'#FECDD3':dialogData.opLevel==='BOUNDARY'?'#FED7AA':'#BFDBFE')}">
            {{ dialogData.warning }}
          </div>

          <!-- Scenario display -->
          <div v-if="dialogData && dialogData.scenario"
            :style="{padding:'8px 12px',borderRadius:'8px',fontSize:'12px',marginBottom:'12px',
              background:'#F8FAFC',border:'1px solid #E2E8F0',color:'#64748B'}">
            📍 操作场景：<b style="color:#334155;">{{ dialogData.scenario }}</b>
          </div>

          <!-- Suggested reasons -->
          <div v-if="dialogData && dialogData.suggestedReasons && dialogData.suggestedReasons.length"
            style="margin-bottom:12px;">
            <div style="font-size:12px;color:#94A3B8;margin-bottom:6px;">💡 建议理由（点击快速填充）：</div>
            <div style="display:flex;flex-wrap:wrap;gap:6px;">
              <span v-for="(sr, i) in dialogData.suggestedReasons" :key="i"
                @click="dialogForm.reason = sr"
                :style="{padding:'4px 10px',background:'#EFF6FF',color:'#3B82F6',border:'1px solid #BFDBFE',borderRadius:'16px',fontSize:'12px',cursor:'pointer',display:'inline-block',maxWidth:'100%',overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}">
                {{ sr }}
              </span>
            </div>
          </div>

          <div style="display:flex;flex-direction:column;gap:12px;">
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;">
              <div><div :style="labelS">成交价</div><input v-model.number="dialogForm.tradePrice" :style="inputS" type="number" step="0.01" /></div>
              <div><div :style="labelS">股数</div><input v-model.number="dialogForm.shares" :style="inputS" type="number" /></div>
            </div>

            <div v-if="dialogData">
              <div :style="labelS">操作后持仓: {{ dialogData.holdingAfterOp }} 股</div>
            </div>

            <div>
              <div :style="labelS">
                操作理由
                <span v-if="dialogData && dialogData.reasonRequired" style="color:#F43F5E;">（必填）</span>
                <span v-else style="color:#94A3B8;">（选填，建议填写）</span>
              </div>
              <textarea v-model="dialogForm.reason"
                :style="{...inputS,height:'60px',padding:'8px 12px',resize:'vertical'}"
                :placeholder="dialogData && dialogData.reasonRequired
                  ? '该操作为'+(dialogData.opLevel==='BOUNDARY'?'边界':'超限')+'操作，必须填写理由'
                  : '建议填写操作理由，方便后续复盘'"></textarea>
            </div>
          </div>

          <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:20px;">
            <button @click="dialogVisible = false"
              style="height:38px;padding:0 20px;background:#F8FAFC;color:#64748B;border:1px solid #E2E8F0;border-radius:10px;cursor:pointer;font-size:13px;">取消</button>
            <button @click="confirmDialog" :disabled="submitting"
              :style="{height:'38px',padding:'0 24px',border:'none',borderRadius:'10px',cursor:'pointer',fontSize:'13px',fontWeight:600,color:'#fff',
                background:dialogData&&dialogData.opLevel==='OVERLIMIT'?'linear-gradient(135deg,#F43F5E,#E11D48)':
                  dialogData&&dialogData.type==='SELL'?'linear-gradient(135deg,#F43F5E,#E11D48)':
                  'linear-gradient(135deg,#11998E,#0D9488)',
                boxShadow:dialogData&&dialogData.opLevel==='OVERLIMIT'?'0 4px 12px rgba(244,63,94,0.4)':
                  dialogData&&dialogData.type==='SELL'?'0 4px 12px rgba(244,63,94,0.3)':
                  '0 4px 12px rgba(17,153,142,0.3)'}">
              {{ submitting ? '提交中...' : dialogData && dialogData.opLevel === 'OVERLIMIT' ? '强制'+ (dialogData.type==='SELL'?'卖出':'买入') : '确认'+ (dialogData&&dialogData.type==='SELL'?'卖出':'买入') }}
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>
