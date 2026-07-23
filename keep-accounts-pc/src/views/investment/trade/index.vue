<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, WarningFilled, TrendCharts, CaretTop, CaretBottom, Bell } from '@element-plus/icons-vue'
import { getPositionList } from '@/api/stock'
import { getConfigList, getConfigByStockCode, saveConfig, getOperationList, saveRecord, getRecordList, getNotifyConfig, saveNotifyConfig } from '@/api/trade'

// 从真实接口获取持仓股票列表（用于下拉选择）
const positionStocks = ref([])

const avatarGradients = [
  'linear-gradient(135deg,#667EEA,#764BA2)',
  'linear-gradient(135deg,#11998E,#38EF7D)',
  'linear-gradient(135deg,#F7971E,#FFD200)',
  'linear-gradient(135deg,#F953C6,#B91D73)',
  'linear-gradient(135deg,#A855F7,#6366F1)',
  'linear-gradient(135deg,#06B6D4,#3B82F6)',
  'linear-gradient(135deg,#F43F5E,#FB923C)',
]

const fieldStyle = {
  height: '36px', padding: '0 12px', borderRadius: '10px', background: '#F8FAFC',
  border: '1px solid #E2E8F0', fontSize: '13px', color: '#334155', outline: 'none',
  width: '100%', boxSizing: 'border-box',
}

const tStocks = ref([])
const trades = ref([])
const operations = ref([])
const selected = ref(null)

// Stock config modal
const showStockModal = ref(false)
const fCode = ref('')
const fName = ref('')
const fBase = ref('')
const fLevels = ref('5')
const fUpPct = ref('5')
const fDownPct = ref('5')
const fFixed = ref('')
const fActive = ref(true)

// Trade modal
const showTradeModal = ref(false)
const tOperationId = ref(null)
const tDir = ref(2)
const tPrice = ref('')
const tShares = ref('')
const tReason = ref('')
const tDate = ref('')

// Sell limit warning
const showWarn = ref(false)
const warnStock = ref(null)

// Notification settings
const showNotifyModal = ref(false)
const nSendKey = ref('')
const nEnable = ref(false)

// 卖出 / 买入档位（从 operations 中过滤）
const sellOps = computed(() => operations.value.filter(o => o.direction === 2))
const buyOps = computed(() => operations.value.filter(o => o.direction === 1))

// 已触发的卖出次数
const sellTriggeredCount = computed(() => sellOps.value.filter(o => o.triggered === 1).length)

// 总做T盈亏
const totalPnl = computed(() => tStocks.value.reduce((sum, s) => sum + (s.totalPnl || 0), 0))
// 做T总次数（所有股票的买卖次数汇总）
const totalTrades = computed(() => tStocks.value.reduce((sum, s) => sum + (s.sellCount || 0) + (s.buyCount || 0), 0))

// 新增弹窗：网格预览
const previewLevels = computed(() => {
  if (fBase.value && Number(fBase.value) > 0 && Number(fLevels.value) > 0) {
    return gridLevels(Number(fBase.value), Math.min(Number(fLevels.value), 8), Number(fUpPct.value) || 5, Number(fDownPct.value) || 5)
  }
  return null
})

// 网格档位价格计算（纯前端预览用）
function gridLevels(base, levels, upPct, downPct) {
  const sell = []
  const buy = []
  let sellPrice = base
  let buyPrice = base
  for (let i = 0; i < levels; i++) {
    sellPrice = +(sellPrice * (1 + upPct / 100)).toFixed(2)
    buyPrice  = +(buyPrice  * (1 - downPct / 100)).toFixed(2)
    sell.push(sellPrice)
    buy.push(buyPrice)
  }
  return { sell, buy }
}

// 加载T配置列表
async function loadConfigs() {
  try {
    tStocks.value = await getConfigList()
    // 刷新后重新关联 selected 到新数组中的对象
    if (selected.value) {
      const match = tStocks.value.find(s => s.id === selected.value.id)
      if (match) {
        selectStock(match)
      } else {
        selected.value = tStocks.value.length > 0 ? tStocks.value[0] : null
        if (selected.value) selectStock(selected.value)
      }
    } else if (tStocks.value.length > 0) {
      selectStock(tStocks.value[0])
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载T配置失败')
  }
}

// 选中股票 → 加载其档位和交易记录
async function selectStock(s) {
  selected.value = s
  // 先清空，避免加载失败时展示上一只股票的数据
  operations.value = []
  trades.value = []
  try {
    const [ops, records] = await Promise.all([
      getOperationList(s.id),
      getRecordList(s.id),
    ])
    operations.value = ops || []
    trades.value = records || []
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载数据失败')
  }
}

// 新增弹窗
function openAddStock() {
  fCode.value = ''; fName.value = ''; fBase.value = ''; fLevels.value = '5'
  fUpPct.value = '5'; fDownPct.value = '5'; fFixed.value = ''; fActive.value = true
  showStockModal.value = true
}

// 股票下拉选择变更 → 查询是否有已配置规则，自动回填
async function onStockSelect() {
  if (!fCode.value) {
    fName.value = ''
    fBase.value = ''; fLevels.value = '5'; fUpPct.value = '5'; fDownPct.value = '5'
    fFixed.value = ''; fActive.value = true
    return
  }
  // 填充股票名称
  const s = positionStocks.value.find(p => p.stockCode === fCode.value)
  if (s) fName.value = s.stockName

  // 查询是否已有T配置，有则回填
  try {
    const config = await getConfigByStockCode(fCode.value)
    if (config) {
      fName.value = config.stockName
      fBase.value = String(config.basePrice)
      fLevels.value = String(config.levels)
      fUpPct.value = String(config.upPct)
      fDownPct.value = String(config.downPct)
      fFixed.value = String(config.fixedShares)
      fActive.value = config.active === 1
    }
  } catch (e) {
    // 不存在则忽略，用户自行填写
  }
}

async function saveStock() {
  if (!fCode.value) {
    ElMessage.warning('请选择股票')
    return
  }
  if (!fBase.value) {
    ElMessage.warning('请输入基准价')
    return
  }
  if (!fFixed.value) {
    ElMessage.warning('请输入每档操作股数')
    return
  }
  try {
    await saveConfig({
      stockCode: fCode.value,
      stockName: fName.value,
      basePrice: Number(fBase.value),
      levels: Number(fLevels.value),
      upPct: Number(fUpPct.value),
      downPct: Number(fDownPct.value),
      fixedShares: Number(fFixed.value),
      active: fActive.value ? 1 : 0,
    })
    showStockModal.value = false
    ElMessage.success('保存成功')
    await loadConfigs()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  }
}

function openLevelTrade(direction, price, operationId) {
  if (!selected.value) return
  // 先预填表单字段（强制卖出时也需要这些默认值）
  tDir.value = direction
  tPrice.value = String(price)
  tShares.value = String(selected.value.fixedShares)
  tReason.value = ''
  tDate.value = new Date().toISOString().slice(0, 16)
  // 强制卖出时不关联档位
  tOperationId.value = direction === 2 && sellTriggeredCount.value >= selected.value.levels ? null : operationId

  if (direction === 2 && sellTriggeredCount.value >= selected.value.levels) {
    warnStock.value = selected.value
    showWarn.value = true
    return
  }
  showTradeModal.value = true
}

async function saveTrade() {
  if (!selected.value) return
  if (!tPrice.value) {
    ElMessage.warning('请输入成交价')
    return
  }
  if (!tShares.value) {
    ElMessage.warning('请输入股数')
    return
  }
  if (!tReason.value.trim()) {
    ElMessage.warning('请输入买卖理由')
    return
  }
  if (!tDate.value) {
    ElMessage.warning('请选择交易时间')
    return
  }
  try {
    // 格式化交易时间
    const timeStr = tDate.value.replace('T', ' ') + ':00'
    await saveRecord({
      configId: selected.value.id,
      operationId: tOperationId.value,
      direction: tDir.value,
      shares: Number(tShares.value),
      price: Number(tPrice.value),
      reason: tReason.value,
      tradeTime: timeStr,
    })
    showTradeModal.value = false
    ElMessage.success('交易记录保存成功')
    // 刷新配置列表（顶部统计数据和左侧股票列表的买卖次数、盈亏）
    await loadConfigs()
    // 刷新当前选中股票的档位和交易记录
    if (selected.value) {
      const [ops, records] = await Promise.all([
        getOperationList(selected.value.id),
        getRecordList(selected.value.id),
      ])
      operations.value = ops || []
      trades.value = records || []
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  }
}

async function loadPositionStocks() {
  try {
    const res = await getPositionList({ page: 1, pageSize: 200 })
    positionStocks.value = res.records || []
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载持仓列表失败')
  }
}

async function openNotifySettings() {
  try {
    const config = await getNotifyConfig()
    nSendKey.value = config.sendKey || ''
    nEnable.value = config.enable === 'true'
  } catch (e) {
    nSendKey.value = ''
    nEnable.value = false
  }
  showNotifyModal.value = true
}

async function saveNotifySettings() {
  try {
    await saveNotifyConfig({
      sendKey: nSendKey.value,
      enable: nEnable.value ? 'true' : 'false',
    })
    showNotifyModal.value = false
    ElMessage.success('通知设置保存成功')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  }
}

onMounted(async () => {
  await loadPositionStocks()
  await loadConfigs()
})
</script>

<template>
  <div style="padding: 24px; background: #F0F2F8; min-height: 100%; box-sizing: border-box;">

    <!-- Top stats -->
    <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; margin-bottom: 16px;">
      <div v-for="c in [
        { label: '总做T盈亏', value: `${totalPnl >= 0 ? '+' : ''}¥${totalPnl.toFixed(2)}`, sub: totalPnl >= 0 ? '累计盈利' : '累计亏损', bg: totalPnl >= 0 ? 'linear-gradient(135deg,#F43F5E,#FB923C)' : 'linear-gradient(135deg,#11998E,#38EF7D)', color: totalPnl >= 0 ? '#F43F5E' : '#11998E' },
        { label: '做T总次数', value: `${totalTrades} 次`, sub: '', bg: 'linear-gradient(135deg,#667EEA,#764BA2)', color: '#667EEA' },
      ]" :key="c.label" style="background: #fff; border-radius: 16px; padding: 20px 22px; box-shadow: 0 2px 16px rgba(0,0,0,0.06); display: flex; align-items: center; gap: 16px;">
        <div :style="{ width: '46px', height: '46px', borderRadius: '14px', background: c.bg, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }">
          <el-icon style="font-size: 20px; color: #fff;"><TrendCharts /></el-icon>
        </div>
        <div>
          <div style="font-size: 12px; color: #94A3B8; margin-bottom: 4px;">{{ c.label }}</div>
          <div style="font-size: 18px; font-weight: 700; color: #1E293B; line-height: 1.2;">{{ c.value }}</div>
          <div v-if="c.sub" :style="{ fontSize: '11px', color: c.color, marginTop: '2px', fontWeight: 500 }">{{ c.sub }}</div>
        </div>
      </div>
    </div>

    <div style="display: flex; gap: 20px; align-items: flex-start;">

      <!-- Left sidebar: stock list -->
      <div style="flex: 0 0 300px; min-width: 0; display: flex; flex-direction: column; gap: 14px;">
        <div style="background: #fff; border-radius: 16px; box-shadow: 0 2px 16px rgba(0,0,0,0.06); overflow: hidden; flex: 1;">
          <div style="padding: 14px 16px; border-bottom: 1px solid #F1F5F9; display: flex; align-items: center; justify-content: space-between;">
            <span style="font-size: 14px; font-weight: 600; color: #1E293B;">T管理股票</span>
            <button @click="openAddStock" style="height: 28px; padding: 0 10px; background: linear-gradient(135deg,#667EEA,#764BA2); color: #fff; border: none; border-radius: 8px; cursor: pointer; font-size: 12px; display: flex; align-items: center; gap: 4px; font-weight: 500; box-shadow: 0 2px 8px rgba(102,126,234,0.3);">
              <el-icon style="font-size: 12px;"><Plus /></el-icon>新增
            </button>
          </div>
          <div>
            <div v-for="s in tStocks" :key="s.id"
              @click="selectStock(s)"
              :style="{ padding: '12px 16px', cursor: 'pointer', borderBottom: '1px solid #F8FAFC', background: selected?.id === s.id ? 'linear-gradient(135deg,rgba(102,126,234,0.06),rgba(118,75,162,0.04))' : '#fff', transition: 'background 0.15s', position: 'relative' }"
              @mouseenter="e => { if (selected?.id !== s.id) e.currentTarget.style.background = '#FAFBFF' }"
              @mouseleave="e => { if (selected?.id !== s.id) e.currentTarget.style.background = '#fff' }"
            >
              <span v-if="selected?.id === s.id" style="position: absolute; left: 0; top: 50%; transform: translateY(-50%); width: 3px; height: 24px; border-radius: 0 2px 2px 0; background: linear-gradient(180deg,#667EEA,#764BA2);" />
              <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 7px;">
                <div style="display: flex; align-items: center; gap: 8px;">
                  <div :style="{ width: '30px', height: '30px', borderRadius: '8px', background: avatarGradients[s.id % avatarGradients.length], display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: '11px', fontWeight: 700, flexShrink: 0 }">
                    {{ s.stockName[0] }}
                  </div>
                  <div>
                    <div style="font-size: 13px; font-weight: 700; color: '#1E293B';">{{ s.stockName }}</div>
                    <div style="font-size: 12px; color: '#94A3B8'; font-family: monospace;">{{ s.stockCode }}</div>
                  </div>
                </div>
                <span v-if="s.sellCount >= s.levels" style="font-size: 12px; padding: 2px 6px; border-radius: 20px; background: '#FFF1F2'; color: '#F43F5E'; font-weight: 600; flex-shrink: 0;">⚠ 满档</span>
              </div>
              <div style="display: flex; gap: 5px; margin-bottom: 6px; flex-wrap: wrap;">
                <span style="font-size: 12px; padding: 2px 8px; border-radius: 20px; background: '#EEF2FF'; color: '#667EEA'; font-weight: 500;">¥{{ s.basePrice }}</span>
                <span style="font-size: 12px; padding: 2px 8px; border-radius: 20px; background: '#F8FAFC'; color: '#475569'; font-weight: 500;">{{ s.levels }}档</span>
                <span style="font-size: 12px; padding: 2px 8px; border-radius: 20px; background: '#FFF1F2'; color: '#F43F5E'; font-weight: 500;">涨{{ s.upPct }}%</span>
                <span style="font-size: 12px; padding: 2px 8px; border-radius: 20px; background: '#F0FDF4'; color: '#11998E'; font-weight: 500;">跌{{ s.downPct }}%</span>
              </div>
              <div style="display: flex; align-items: center; gap: 6px;">
                <span style="font-size: 12px; padding: 2px 8px; border-radius: 20px; background: '#FFF1F2'; color: '#F43F5E'; font-weight: 500;">卖{{ s.sellCount }}</span>
                <span style="font-size: 12px; padding: 2px 8px; border-radius: 20px; background: '#F0FDF4'; color: '#11998E'; font-weight: 500;">买{{ s.buyCount }}</span>
                <span style="margin-left: auto; font-size: 13px; font-weight: 700;" :style="{ color: (s.totalPnl || 0) >= 0 ? '#F43F5E' : '#11998E' }">{{ (s.totalPnl || 0) >= 0 ? '+' : '' }}¥{{ (s.totalPnl || 0).toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Notification settings button -->
        <button @click="openNotifySettings"
          style="height: 40px; padding: 0 16px; margin-top: 8px; background: #F8FAFC; color: #475569; border: 1px solid #E2E8F0; border-radius: 12px; cursor: pointer; font-size: 13px; font-weight: 500; display: flex; align-items: center; gap: 6px; transition: all 0.15s; width: 100%;"
          @mouseenter="e => { e.currentTarget.style.background = '#EEF2FF'; e.currentTarget.style.borderColor = '#667EEA'; e.currentTarget.style.color = '#667EEA'; }"
          @mouseleave="e => { e.currentTarget.style.background = '#F8FAFC'; e.currentTarget.style.borderColor = '#E2E8F0'; e.currentTarget.style.color = '#475569'; }">
          <el-icon style="font-size: 14px;"><Bell /></el-icon>通知设置
        </button>
      </div>

      <!-- Right main -->
      <div style="flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 14px;">
        <template v-if="!selected">
          <div style="background: #fff; border-radius: 16px; padding: 80px 20px; text-align: center; box-shadow: 0 2px 16px rgba(0,0,0,0.06);">
            <el-icon style="font-size: 40px; opacity: 0.2; margin: 0 auto 12px; display: block;"><TrendCharts /></el-icon>
            <div style="font-size: 14px; color: '#94A3B8';">请选择左侧股票查看T记录</div>
          </div>
        </template>
        <template v-if="selected">
          <!-- Grid visualization -->
          <div style="background: #fff; border-radius: 16px; padding: 18px 24px; box-shadow: 0 2px 16px rgba(0,0,0,0.06);">
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 16px; flex-wrap: wrap;">
              <el-icon style="font-size: 14px; color: #667EEA;"><TrendCharts /></el-icon>
              <span style="font-size: 13px; font-weight: 600; color: '#1E293B';">{{ selected.stockName }} · 网格档位</span>
              <span style="font-size: 12px; color: '#94A3B8'; font-weight: 500;">基准价</span>
              <span style="font-size: 13px; padding: 2px 10px; border-radius: 8px; background: linear-gradient(135deg,#667EEA,#764BA2); color: #fff; font-weight: 700; font-family: monospace;">¥{{ selected.basePrice }}</span>
              <span style="font-size: 12px; padding: 2px 10px; border-radius: 20px; background: '#F8FAFC'; color: '#475569'; font-weight: 500;">{{ selected.levels }} 档</span>
              <span style="font-size: 12px; padding: 2px 10px; border-radius: 20px; background: '#FFF1F2'; color: '#F43F5E'; font-weight: 500;">每档涨 {{ selected.upPct }}%</span>
              <span style="font-size: 12px; padding: 2px 10px; border-radius: 20px; background: '#F0FDF4'; color: '#11998E'; font-weight: 500;">每档跌 {{ selected.downPct }}%</span>
              <span v-if="sellTriggeredCount >= selected.levels" style="font-size: 12px; padding: 2px 10px; border-radius: 20px; background: '#FFF1F2'; color: '#F43F5E'; font-weight: 600; display: flex; align-items: center; gap: 4px;">
                <el-icon style="font-size: 11px;"><WarningFilled /></el-icon>卖出已满 {{ selected.levels }} 档
              </span>
            </div>

            <!-- Sell levels -->
            <div style="margin-bottom: 8px;">
              <div style="font-size: 12px; color: '#F43F5E'; font-weight: 600; margin-bottom: 6px; letter-spacing: 0.04em;">▲ 卖出档位（每档在上一档基础上涨 {{ selected.upPct }}%）</div>
              <div style="display: flex; gap: 6px;">
                <div v-for="(op, i) in sellOps" :key="'s'+i"
                  :style="{ flex: 1, padding: '10px 8px 8px', borderRadius: '10px', textAlign: 'center', background: op.triggered === 1 ? '#FEF2F2' : '#FFF8F8', border: `1.5px solid ${op.triggered === 1 ? '#F43F5E' : '#FFE4E4'}`, position: 'relative', display: 'flex', flexDirection: 'column', alignItems: 'center' }">
                  <div v-if="(i + 1) === selected.levels && op.triggered === 0" style="position: absolute; top: -7px; left: 50%; transform: translateX(-50%); font-size: 10px; background: '#F43F5E'; color: '#fff'; padding: 1px 6px; border-radius: 10px; font-weight: 700; white-space: nowrap;">满档警告</div>
                  <div style="font-size: 12px; color: '#F43F5E'; font-weight: 600; margin-bottom: 3px;">卖出 {{ i + 1 }}</div>
                  <div style="font-size: 13px; font-weight: 700; font-family: monospace;" :style="{ color: op.triggered === 1 ? '#F43F5E' : '#94A3B8' }">¥{{ op.levelPrice }}</div>
                  <div style="font-size: 11px; margin-top: 2px; margin-bottom: 6px;" :style="{ color: op.triggered === 1 ? '#F43F5E' : '#CBD5E1' }">
                    {{ op.triggered === 1 ? '✓ 已触发' : `+${(Math.pow(1 + selected.upPct / 100, i + 1) * 100 - 100).toFixed(1)}%` }}
                  </div>
                  <div v-if="op.triggered === 1" style="font-size: 11px; padding: 3px 8px; border-radius: 20px; background: '#F43F5E'; color: '#fff'; font-weight: 700; letter-spacing: 0.02em;">已卖</div>
                  <button v-else
                    @click="openLevelTrade(2, op.levelPrice, op.id)"
                    style="font-size: 12px; padding: 3px 10px; border-radius: 20px; background: transparent; border: 1.5px solid #F43F5E; color: #F43F5E; cursor: pointer; font-weight: 600; line-height: 1.4; transition: all 0.15s;"
                    @mouseenter="e => { e.currentTarget.style.background = '#F43F5E'; e.currentTarget.style.color = '#fff' }"
                    @mouseleave="e => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = '#F43F5E' }"
                  >卖出</button>
                </div>
              </div>
            </div>

            <!-- Buy levels -->
            <div>
              <div style="font-size: 12px; color: '#11998E'; font-weight: 600; margin-bottom: 6px; letter-spacing: 0.04em;">▼ 买入档位（每档在上一档基础上跌 {{ selected.downPct }}%）</div>
              <div style="display: flex; gap: 6px;">
                <div v-for="(op, i) in buyOps" :key="'b'+i"
                  :style="{ flex: 1, padding: '10px 8px 8px', borderRadius: '10px', textAlign: 'center', background: op.triggered === 1 ? '#F0FDF4' : '#F8FFFC', border: `1.5px solid ${op.triggered === 1 ? '#11998E' : '#BBF7D0'}`, display: 'flex', flexDirection: 'column', alignItems: 'center' }">
                  <div style="font-size: 12px; color: '#11998E'; font-weight: 600; margin-bottom: 3px;">买入 {{ i + 1 }}</div>
                  <div style="font-size: 13px; font-weight: 700; font-family: monospace;" :style="{ color: op.triggered === 1 ? '#11998E' : '#94A3B8' }">¥{{ op.levelPrice }}</div>
                  <div style="font-size: 11px; margin-top: 2px; margin-bottom: 6px;" :style="{ color: op.triggered === 1 ? '#11998E' : '#CBD5E1' }">
                    {{ op.triggered === 1 ? '✓ 已触发' : `-${(100 - Math.pow(1 - selected.downPct / 100, i + 1) * 100).toFixed(1)}%` }}
                  </div>
                  <div v-if="op.triggered === 1" style="font-size: 11px; padding: 3px 8px; border-radius: 20px; background: '#11998E'; color: '#fff'; font-weight: 700; letter-spacing: 0.02em;">已买</div>
                  <button v-else-if="sellOps[i] && sellOps[i].triggered === 1"
                    @click="openLevelTrade(1, op.levelPrice, op.id)"
                    style="font-size: 12px; padding: 3px 10px; border-radius: 20px; background: transparent; border: 1.5px solid #11998E; color: #11998E; cursor: pointer; font-weight: 600; line-height: 1.4; transition: all 0.15s;"
                    @mouseenter="e => { e.currentTarget.style.background = '#11998E'; e.currentTarget.style.color = '#fff' }"
                    @mouseleave="e => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = '#11998E' }"
                  >买入</button>
                  <div v-else style="font-size: 11px; padding: 3px 8px; border-radius: 20px; background: '#F1F5F9'; color: '#94A3B8'; font-weight: 500; line-height: 1.4;">待卖出</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Trade records table -->
          <div style="background: #fff; border-radius: 16px; box-shadow: 0 2px 16px rgba(0,0,0,0.06); overflow: hidden;">
            <div style="padding: 14px 20px; border-bottom: 1px solid #F1F5F9; display: flex; align-items: center; justify-content: space-between;">
              <span style="font-size: 14px; font-weight: 600; color: '#1E293B';">{{ selected.stockName }} · 交易记录</span>
            </div>
            <div v-if="trades.length === 0" style="padding: 48px 20px; text-align: center; color: '#94A3B8'; font-size: 14px;">暂无交易记录</div>
            <div v-else style="overflow-x: auto;">
              <table style="width: 100%; border-collapse: collapse; min-width: 520px;">
                <thead>
                  <tr>
                    <th v-for="h in ['方向', '成交价', '股数', '配对盈亏', '买卖理由', '时间']" :key="h"
                      style="padding: 10px 20px; font-size: 12px; color: '#94A3B8'; font-weight: 600; text-align: left; white-space: nowrap; background: '#F8FAFC'; border-bottom: 1px solid #F1F5F9; letter-spacing: 0.03em;">{{ h }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="t in trades" :key="t.id"
                    style="transition: background 0.15s;"
                    @mouseenter="e => (e.currentTarget.style.background = '#FAFBFF')"
                    @mouseleave="e => (e.currentTarget.style.background = '#fff')"
                  >
                    <td style="padding: 12px 20px; white-space: nowrap; border-bottom: 1px solid #F8FAFC;">
                      <span :style="{ display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '12px', padding: '3px 10px', borderRadius: '20px', background: t.direction === 2 ? '#FFF1F2' : '#F0FDF4', color: t.direction === 2 ? '#F43F5E' : '#11998E', fontWeight: 700 }">
                        <el-icon style="font-size: 11px;"><CaretTop v-if="t.direction === 2" /><CaretBottom v-else /></el-icon>
                        {{ t.direction === 2 ? '卖出' : '买入' }}
                      </span>
                    </td>
                    <td :style="{ padding: '12px 20px', fontSize: '13px', fontWeight: 700, color: t.direction === 2 ? '#F43F5E' : '#11998E', whiteSpace: 'nowrap', borderBottom: '1px solid #F8FAFC', fontFamily: 'monospace' }">¥{{ t.price.toFixed(2) }}</td>
                    <td :style="{ padding: '12px 20px', fontSize: '13px', color: '#475569', whiteSpace: 'nowrap', borderBottom: '1px solid #F8FAFC', fontWeight: 500 }">{{ t.shares }} 股</td>
                    <td style="padding: 12px 20px; white-space: nowrap; border-bottom: 1px solid #F8FAFC;">
                      <template v-if="t.pairProfit === null || t.pairProfit === undefined">
                        <span style="font-size: 12px; color: '#CBD5E1'; font-style: italic;">待匹配</span>
                      </template>
                      <template v-else>
                        <span :style="{ fontSize: '13px', fontWeight: 700, color: t.pairProfit >= 0 ? '#F43F5E' : '#11998E', fontFamily: 'monospace' }">
                          {{ t.pairProfit >= 0 ? '+' : '' }}¥{{ t.pairProfit.toFixed(2) }}
                        </span>
                      </template>
                    </td>
                    <td :style="{ padding: '12px 20px', fontSize: '13px', color: '#475569', borderBottom: '1px solid #F8FAFC', maxWidth: '200px' }">
                      <div style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" :title="t.reason">{{ t.reason }}</div>
                    </td>
                    <td :style="{ padding: '12px 20px', fontSize: '12px', color: '#94A3B8', whiteSpace: 'nowrap', borderBottom: '1px solid #F8FAFC' }">{{ t.tradeTime }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- Stock config modal -->
    <el-dialog v-model="showStockModal" title="新增T管理股票" width="580px" :close-on-click-modal="false">
      <div style="display: flex; flex-wrap: wrap; gap: 0 16px;">
        <div style="flex: 1 1 100%; min-width: 200px; margin-bottom: 14px;">
          <div style="font-size: 13px; color: '#475569'; margin-bottom: 6px; font-weight: 500;">选择股票 <span style="color: #F43F5E;">*</span></div>
          <select v-model="fCode" @change="onStockSelect"
            :style="{...fieldStyle, cursor: 'pointer'}">
            <option value="">请选择持仓股票</option>
            <option v-for="s in positionStocks" :key="s.stockCode" :value="s.stockCode">{{ s.stockName }}（{{ s.stockCode }}）</option>
          </select>
        </div>
        <div style="flex: 1 1 calc(50% - 8px); min-width: 200px; margin-bottom: 14px;">
          <div style="font-size: 13px; color: '#475569'; margin-bottom: 6px; font-weight: 500;">基准价（元） <span style="color: #F43F5E;">*</span></div>
          <input v-model="fBase" type="number" step="0.01" placeholder="网格基准价" :style="fieldStyle" />
        </div>
        <div style="flex: 1 1 calc(50% - 8px); min-width: 200px; margin-bottom: 14px;">
          <div style="font-size: 13px; color: '#475569'; margin-bottom: 6px; font-weight: 500;">档位数 <span style="color: #F43F5E;">*</span></div>
          <input v-model="fLevels" type="number" min="1" max="20" placeholder="如：5" :style="fieldStyle" />
        </div>
        <div style="flex: 1 1 calc(50% - 8px); min-width: 200px; margin-bottom: 14px;">
          <div style="font-size: 13px; color: '#475569'; margin-bottom: 6px; font-weight: 500;">卖出每档涨幅 % <span style="color: #F43F5E;">*</span></div>
          <input v-model="fUpPct" type="number" step="0.1" min="0.1" placeholder="如：5" :style="fieldStyle" />
        </div>
        <div style="flex: 1 1 calc(50% - 8px); min-width: 200px; margin-bottom: 14px;">
          <div style="font-size: 13px; color: '#475569'; margin-bottom: 6px; font-weight: 500;">买入每档跌幅 % <span style="color: #F43F5E;">*</span></div>
          <input v-model="fDownPct" type="number" step="0.1" min="0.1" placeholder="如：10" :style="fieldStyle" />
        </div>
        <div style="flex: 1 1 calc(50% - 8px); min-width: 200px; margin-bottom: 14px;">
          <div style="font-size: 13px; color: '#475569'; margin-bottom: 6px; font-weight: 500;">每次固定股数 <span style="color: #F43F5E;">*</span></div>
          <input v-model="fFixed" type="number" placeholder="每档买卖股数" :style="fieldStyle" />
        </div>
        <div style="flex: 1 1 calc(50% - 8px); min-width: 200px; margin-bottom: 14px;">
          <div style="font-size: 13px; color: '#475569'; margin-bottom: 6px; font-weight: 500;">状态</div>
          <div style="display: flex; align-items: center; gap: 8px; margin-top: 6px;">
            <el-switch v-model="fActive" size="small" />
            <span :style="{ fontSize: '13px', color: fActive ? '#11998E' : '#94A3B8', fontWeight: 500 }">{{ fActive ? '启用' : '停用' }}</span>
          </div>
        </div>
      </div>

      <!-- Grid preview -->
      <div v-if="previewLevels" style="background: '#F8FAFC'; border-radius: 10px; padding: 14px 16px; margin-bottom: 8px;">
        <div style="font-size: 12px; color: '#94A3B8'; font-weight: 500; margin-bottom: 10px;">
          网格价位预览（卖出每档涨 {{ fUpPct }}%，买入每档跌 {{ fDownPct }}%）
        </div>
        <div style="margin-bottom: 6px;">
          <div style="font-size: 12px; color: '#F43F5E'; font-weight: 600; margin-bottom: 4px;">▲ 卖出档位</div>
          <div style="display: flex; gap: 5px; flex-wrap: wrap;">
            <div v-for="(p, i) in previewLevels.sell" :key="'ps'+i" style="padding: 3px 10px; border-radius: 8px; background: '#FFF1F2'; font-size: 12px; color: '#F43F5E'; font-weight: 600; font-family: monospace;">
              卖{{ i + 1 }} ¥{{ p }}
            </div>
          </div>
        </div>

        <div>
          <div style="font-size: 12px; color: '#11998E'; font-weight: 600; margin-bottom: 4px;">▼ 买入档位</div>
          <div style="display: flex; gap: 5px; flex-wrap: wrap;">
            <div v-for="(p, i) in previewLevels.buy" :key="'pb'+i" style="padding: 3px 10px; border-radius: 8px; background: '#F0FDF4'; font-size: 12px; color: '#11998E'; font-weight: 600; font-family: monospace;">
              买{{ i + 1 }} ¥{{ p }}
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 10px;">
          <button @click="showStockModal = false" style="height: 38px; padding: 0 20px; border: 1px solid #E2E8F0; border-radius: 10px; background: #fff; color: #64748B; cursor: pointer; font-size: 14px;">取消</button>
          <button @click="saveStock" style="height: 38px; padding: 0 24px; border: none; border-radius: 10px; background: linear-gradient(135deg,#667EEA,#764BA2); color: #fff; cursor: pointer; font-size: 14px; font-weight: 600; box-shadow: 0 4px 12px rgba(102,126,234,0.4);">确定</button>
        </div>
      </template>
    </el-dialog>

    <!-- Trade record modal -->
    <el-dialog v-model="showTradeModal" title="记录交易" width="520px" :close-on-click-modal="false">
      <div style="margin-bottom: 14px;">
        <div style="font-size: 13px; color: '#475569'; margin-bottom: 6px; font-weight: 500;">交易方向 <span style="color: #F43F5E;">*</span></div>
        <div style="display: flex; border: 1px solid #E2E8F0; border-radius: 10px; overflow: hidden; height: 40px;">
          <button v-for="d in [{ v: 2, label: '卖出' }, { v: 1, label: '买入' }]" :key="d.v" type="button" @click="tDir = d.v"
            :style="{ flex: 1, border: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', background: tDir === d.v ? (d.v === 2 ? 'linear-gradient(135deg,#F43F5E,#E11D48)' : 'linear-gradient(135deg,#11998E,#38EF7D)') : '#F8FAFC', color: tDir === d.v ? '#fff' : '#64748B', cursor: 'pointer', fontSize: '14px', fontWeight: tDir === d.v ? 700 : 400, transition: 'all 0.15s' }">
            <el-icon style="font-size: 14px;"><CaretTop v-if="d.v === 2" /><CaretBottom v-else /></el-icon>
            {{ d.label }}
          </button>
        </div>
      </div>
      <div style="display: flex; gap: 16px;">
        <div style="flex: 1; margin-bottom: 14px;">
          <div style="font-size: 13px; color: '#475569'; margin-bottom: 6px; font-weight: 500;">成交价（元） <span style="color: #F43F5E;">*</span></div>
          <input v-model="tPrice" type="number" step="0.01" placeholder="请输入成交价" :style="fieldStyle" />
        </div>
        <div style="flex: 1; margin-bottom: 14px;">
          <div style="font-size: 13px; color: '#475569'; margin-bottom: 6px; font-weight: 500;">股数 <span style="color: #F43F5E;">*</span></div>
          <input v-model="tShares" type="number" placeholder="交易股数" :style="fieldStyle" />
        </div>
      </div>
      <div style="margin-bottom: 14px;">
        <div style="font-size: 13px; color: '#475569'; margin-bottom: 6px; font-weight: 500;">交易时间 <span style="color: #F43F5E;">*</span></div>
        <input v-model="tDate" type="datetime-local" :style="fieldStyle" />
      </div>
      <div style="margin-bottom: 14px;">
        <div style="font-size: 13px; color: '#475569'; margin-bottom: 6px; font-weight: 500;">买卖理由 <span style="color: #F43F5E;">*</span></div>
        <textarea v-model="tReason" placeholder="请描述本次买卖理由，如：触及二档止盈位，市场情绪偏热，获利了结..." rows="3"
          style="height: auto; resize: vertical; padding: 8px 12px; line-height: 1.6; border-radius: 10px; background: #F8FAFC; border: 1px solid #E2E8F0; font-size: 13px; color: #334155; outline: none; width: 100%; box-sizing: border-box;" />
      </div>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 10px;">
          <button @click="showTradeModal = false" style="height: 38px; padding: 0 20px; border: 1px solid #E2E8F0; border-radius: 10px; background: #fff; color: #64748B; cursor: pointer; font-size: 14px;">取消</button>
          <button @click="saveTrade" style="height: 38px; padding: 0 24px; border: none; border-radius: 10px; background: linear-gradient(135deg,#667EEA,#764BA2); color: #fff; cursor: pointer; font-size: 14px; font-weight: 600; box-shadow: 0 4px 12px rgba(102,126,234,0.4);">确定</button>
        </div>
      </template>
    </el-dialog>

    <!-- Sell limit warning -->
    <div v-if="showWarn && warnStock"
      style="position: fixed; inset: 0; background: rgba(15,23,42,0.55); display: flex; align-items: center; justify-content: center; z-index: 1000;"
      @click="showWarn = false">
      <div style="background: #fff; border-radius: 16px; width: 420px; max-width: 90vw; padding: 32px 28px; box-shadow: 0 16px 48px rgba(244,63,94,0.18); text-align: center;"
        @click.stop>
        <div style="width: 72px; height: 72px; border-radius: 50%; background: linear-gradient(135deg,#F43F5E,#E11D48); display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; box-shadow: 0 8px 24px rgba(244,63,94,0.35);">
          <el-icon style="font-size: 32px; color: #fff;"><WarningFilled /></el-icon>
        </div>
        <div style="font-size: 20px; font-weight: 800; color: '#1E293B'; margin-bottom: 8px;">⚠ 卖出已达上限</div>
        <div style="font-size: 14px; color: '#64748B'; line-height: 1.7; margin-bottom: 8px;">
          <span style="font-weight: 600; color: '#F43F5E';">{{ warnStock.stockName }}（{{ warnStock.stockCode }}）</span>
          已卖出 <span style="font-weight: 700; color: '#F43F5E';">{{ sellTriggeredCount }} 次</span>，已触达设定的 <span style="font-weight: 700; color: '#F43F5E';">{{ warnStock.levels }} 档</span>上限。
        </div>
        <div style="font-size: 13px; color: '#94A3B8'; line-height: 1.6; margin-bottom: 24px; background: '#FFF8F8'; border-radius: 10px; padding: 12px 16px;">
          继续卖出将超出网格策略范围，仓位可能过轻。建议先调整基准价或档位配置，再进行操作。
        </div>
        <div style="display: flex; gap: 10px; justify-content: center;">
          <button @click="showWarn = false" style="height: 40px; padding: 0 24px; border: 1px solid #E2E8F0; border-radius: 10px; background: #fff; color: #64748B; cursor: pointer; font-size: 14px; font-weight: 500;">取消操作</button>
          <button @click="showWarn = false; showTradeModal = true" style="height: 40px; padding: 0 24px; border: none; border-radius: 10px; background: linear-gradient(135deg,#F43F5E,#E11D48); color: #fff; cursor: pointer; font-size: 14px; font-weight: 600; box-shadow: 0 4px 12px rgba(244,63,94,0.35);">强制继续卖出</button>
        </div>
      </div>
    </div>

    <!-- Notification settings modal -->
    <el-dialog v-model="showNotifyModal" title="通知设置" width="420px" :close-on-click-modal="false">
      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;">
        <div>
          <div style="font-size: 14px; color: '#1E293B'; font-weight: 600;">通知开关</div>
          <div style="font-size: 12px; color: '#94A3B8'; margin-top: 2px;">开启后，股价到达网格档位时将推送微信通知</div>
        </div>
        <el-switch v-model="nEnable" size="large" />
      </div>
      <div style="margin-bottom: 8px;">
        <div style="font-size: 13px; color: '#475569'; margin-bottom: 6px; font-weight: 500;">SendKey</div>
        <input v-model="nSendKey" placeholder="请输入 Server酱 SendKey" :style="fieldStyle" />
      </div>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 10px;">
          <button @click="showNotifyModal = false" style="height: 38px; padding: 0 20px; border: 1px solid #E2E8F0; border-radius: 10px; background: #fff; color: #64748B; cursor: pointer; font-size: 14px;">取消</button>
          <button @click="saveNotifySettings" style="height: 38px; padding: 0 24px; border: none; border-radius: 10px; background: linear-gradient(135deg,#667EEA,#764BA2); color: #fff; cursor: pointer; font-size: 14px; font-weight: 600; box-shadow: 0 4px 12px rgba(102,126,234,0.4);">保存</button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>
