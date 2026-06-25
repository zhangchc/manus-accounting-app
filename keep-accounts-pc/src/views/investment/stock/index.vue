<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getPosition, savePosition, getStockPrice, getCostHistory } from '@/api/stock'

use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])

const router = useRouter()

const position = ref(null)
const error = ref('')
const saving = ref(false)
const priceLoading = ref(false)
const stockPrice = ref(null)
const costHistory = ref([])

const form = ref({
  stockName: '常山药业',
  stockCode: 'sz300255',
  costPrice: null,
  shares: null,
})

const formValid = computed(() => {
  return form.value.stockName && form.value.stockCode
    && form.value.costPrice && Number(form.value.costPrice) > 0
    && form.value.shares && Number(form.value.shares) > 0
})

async function loadPosition() {
  error.value = ''
  try {
    const data = await getPosition()
    if (data && data.id) {
      position.value = data
      form.value.stockName = data.stockName || '常山药业'
      form.value.stockCode = data.stockCode || 'sz300255'
      form.value.costPrice = data.costPrice
      form.value.shares = data.shares
      if (data.currentPrice) {
        stockPrice.value = {
          price: data.currentPrice,
          change: data.change,
          changePercent: data.changePercent,
        }
      }
    }
  } catch (e) {
    error.value = '加载持仓数据失败'
    ElMessage.error('加载持仓数据失败')
  }
}

async function handleSave() {
  if (!formValid.value) {
    ElMessage.warning('请填写完整的持仓信息')
    return
  }
  saving.value = true
  try {
    const data = await savePosition({
      stockName: form.value.stockName,
      stockCode: form.value.stockCode,
      costPrice: Number(form.value.costPrice),
      shares: Number(form.value.shares),
    })
    if (data && data.id) {
      position.value = data
      if (data.currentPrice) {
        stockPrice.value = {
          price: data.currentPrice,
          change: data.change,
          changePercent: data.changePercent,
        }
      }
    }
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleRefreshPrice() {
  if (!form.value.stockCode) return
  priceLoading.value = true
  loadCostHistory()
  try {
    const res = await getStockPrice(form.value.stockCode)
    if (res) {
      stockPrice.value = res
      if (res.price && position.value && position.value.id) {
        const price = Number(res.price)
        const cost = Number(position.value.costPrice)
        const shares = Number(position.value.shares)
        position.value = {
          ...position.value,
          currentPrice: price,
          currentValue: price * shares,
          profitLoss: (price - cost) * shares,
          profitLossPercent: cost > 0 ? ((price - cost) / cost * 100) : 0,
        }
      }
    }
  } catch (e) {
    ElMessage.error('获取股价失败')
  } finally {
    priceLoading.value = false
  }
}

async function loadCostHistory() {
  try {
    const data = await getCostHistory()
    costHistory.value = data || []
  } catch (e) {
    // 成本历史加载失败不影响主功能
  }
}

const chartOption = computed(() => {
  const list = costHistory.value
  if (!list || list.length === 0) return {}
  return {
    grid: { top: 30, right: 30, bottom: 30, left: 60 },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const p = params[0]
        const item = list[p.dataIndex]
        return '时间：' + item.createdAt + '<br/>'
          + '回本价：' + Number(item.costPrice).toFixed(4) + '<br/>'
          + '持股数：' + item.shares + ' 股<br/>'
          + '净投入：¥' + Number(item.netInvestment).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
      }
    },
    xAxis: {
      type: 'category',
      data: list.map(item => item.createdAt ? item.createdAt.substring(5, 16) : ''),
      axisLabel: { fontSize: 11, color: '#94A3B8' },
      axisLine: { lineStyle: { color: '#E2E8F0' } },
    },
    yAxis: {
      type: 'value',
      name: '回本价（元）',
      nameTextStyle: { fontSize: 11, color: '#94A3B8' },
      axisLabel: { fontSize: 11, color: '#94A3B8' },
      splitLine: { lineStyle: { color: '#F1F5F9' } },
    },
    series: [{
      data: list.map(item => item.costPrice),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#667EEA', width: 2 },
      itemStyle: { color: '#667EEA' },
      areaStyle: { color: 'rgba(102, 126, 234, 0.06)' },
    }]
  }
})

onMounted(() => {
  loadPosition()
  loadCostHistory()
})

function fmt(n) {
  if (n == null || isNaN(n)) return '-'
  return Number(n).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getDailyPlBg() {
  const p = position.value
  if (!p || p.dailyProfitLoss == null) return '#F8FAFC'
  return Number(p.dailyProfitLoss) >= 0 ? '#FFF1F2' : '#F0FDF4'
}

function getDailyPlColor() {
  const p = position.value
  if (!p || p.dailyProfitLoss == null) return '#1E293B'
  return Number(p.dailyProfitLoss) >= 0 ? '#F43F5E' : '#11998E'
}

function getDailyPlSign() {
  const p = position.value
  if (!p || p.dailyProfitLoss == null) return ''
  return Number(p.dailyProfitLoss) >= 0 ? '+' : ''
}

function getProfitBg() {
  const p = position.value
  if (!p || p.profitLoss == null) return '#F8FAFC'
  return Number(p.profitLoss) >= 0 ? '#FFF1F2' : '#F0FDF4'
}

function getProfitColor() {
  const p = position.value
  if (!p || p.profitLoss == null) return '#1E293B'
  return Number(p.profitLoss) >= 0 ? '#F43F5E' : '#11998E'
}

function getPlSign() {
  const p = position.value
  if (!p || p.profitLoss == null) return ''
  return Number(p.profitLoss) >= 0 ? '+' : ''
}
</script>

<template>
  <div style="padding:24px;background:#F0F2F8;min-height:100%;">



    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;">

      <!-- Left: Position form -->
      <div style="background:#fff;border-radius:16px;padding:24px;box-shadow:0 2px 16px rgba(0,0,0,0.06);">
        <div style="font-size:15px;font-weight:600;color:#1E293B;margin-bottom:20px;">📋 持仓信息</div>
        <div style="display:flex;flex-direction:column;gap:16px;">
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
            <div>
              <div style="font-size:13px;color:#94A3B8;margin-bottom:6px;font-weight:500;">股票名称</div>
              <input v-model="form.stockName" style="height:40px;padding:0 12px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:14px;color:#334155;outline:none;width:100%;box-sizing:border-box;" />
            </div>
            <div>
              <div style="font-size:13px;color:#94A3B8;margin-bottom:6px;font-weight:500;">股票代码</div>
              <input v-model="form.stockCode" style="height:40px;padding:0 12px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:14px;color:#334155;outline:none;width:100%;box-sizing:border-box;" />
            </div>
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
            <div>
              <div style="font-size:13px;color:#94A3B8;margin-bottom:6px;font-weight:500;">成本价（元/股）</div>
              <input v-model.number="form.costPrice" type="number" step="0.01" style="height:40px;padding:0 12px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:14px;color:#334155;outline:none;width:100%;box-sizing:border-box;" />
            </div>
            <div>
              <div style="font-size:13px;color:#94A3B8;margin-bottom:6px;font-weight:500;">持有股数</div>
              <input v-model.number="form.shares" type="number" style="height:40px;padding:0 12px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:14px;color:#334155;outline:none;width:100%;box-sizing:border-box;" />
            </div>
          </div>
          <button @click="handleSave" :disabled="saving || !formValid"
            :style="{height:'42px',border:'none',borderRadius:'10px',cursor:formValid?'pointer':'not-allowed',fontSize:'14px',fontWeight:600,color:'#fff',background:formValid?'linear-gradient(135deg,#667EEA,#764BA2)':'#CBD5E1',boxShadow:formValid?'0 4px 12px rgba(102,126,234,0.4)':'none'}">
            {{ saving ? '保存中...' : '保存持仓' }}
          </button>
        </div>
      </div>

      <!-- Right: Market price -->
      <div style="background:#fff;border-radius:16px;padding:24px;box-shadow:0 2px 16px rgba(0,0,0,0.06);">
        <div style="font-size:15px;font-weight:600;color:#1E293B;margin-bottom:20px;">📈 实时行情</div>

        <div v-if="stockPrice" style="display:flex;flex-direction:column;gap:16px;">
          <div style="display:flex;align-items:baseline;justify-content:space-between;">
            <div>
              <div style="font-size:13px;color:#94A3B8;margin-bottom:6px;font-weight:500;">最新价</div>
              <div style="font-size:32px;font-weight:700;color:#1E293B;">{{ stockPrice.price }}</div>
            </div>
            <div style="text-align:right;">
              <div style="font-size:13px;color:#94A3B8;margin-bottom:6px;font-weight:500;">涨跌</div>
              <div :style="{fontSize:'18px',fontWeight:600,color:Number(stockPrice.change||0)>=0?'#F43F5E':'#11998E'}">
                {{ Number(stockPrice.change||0)>=0?'+':'' }}{{ stockPrice.change }}
              </div>
              <div :style="{fontSize:'14px',fontWeight:500,color:parseFloat(stockPrice.changePercent||0)>=0?'#F43F5E':'#11998E'}">
                {{ parseFloat(stockPrice.changePercent||0)>=0?'+':'' }}{{ stockPrice.changePercent }}%
              </div>
            </div>
          </div>
          <button @click="handleRefreshPrice" :disabled="priceLoading"
            style="height:36px;padding:0 16px;background:#F8FAFC;color:#64748B;border:1px solid #E2E8F0;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:6px;justify-content:center;">
            <el-icon style="font-size:13px;"><RefreshRight /></el-icon>
            {{ priceLoading ? '获取中...' : '刷新行情' }}
          </button>
        </div>

        <div v-else style="display:flex;flex-direction:column;align-items:center;padding:24px 0;gap:10px;">
          <div style="font-size:40px;opacity:0.3;">📊</div>
          <div style="font-size:13px;color:#94A3B8;">保存持仓后自动获取行情</div>
          <button @click="handleRefreshPrice" :disabled="priceLoading || !form.stockCode"
            style="height:36px;padding:0 16px;background:#F8FAFC;color:#64748B;border:1px solid #E2E8F0;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:6px;">
            <el-icon style="font-size:13px;"><RefreshRight /></el-icon>手动刷新
          </button>
        </div>
      </div>

    </div>

    <!-- Profit/Loss -->
    <div style="background:#fff;border-radius:16px;padding:24px;box-shadow:0 2px 16px rgba(0,0,0,0.06);margin-top:16px;">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px;">
        <div style="font-size:15px;font-weight:600;color:#1E293B;">💰 盈亏概览</div>
        <button @click="router.push('/trade')"
          style="height:32px;padding:0 14px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:12px;font-weight:500;box-shadow:0 2px 8px rgba(102,126,234,0.3);">
          做T管理 →
        </button>
      </div>

      <div v-if="position && position.id" style="display:flex;flex-direction:column;gap:0;">
        <div style="display:grid;grid-template-columns:repeat(5, 1fr);gap:20px;">
          <div style="padding:16px;background:#F8FAFC;border-radius:12px;">
            <div style="font-size:13px;color:#94A3B8;margin-bottom:6px;font-weight:500;">持仓成本</div>
            <div style="font-size:22px;font-weight:700;color:#1E293B;">¥{{ fmt(position.totalCost) }}</div>
            <div style="font-size:12px;color:#94A3B8;margin-top:4px;">{{ position.costPrice }} × {{ position.shares }} 股</div>
          </div>
          <div style="padding:16px;background:#F8FAFC;border-radius:12px;">
            <div style="font-size:13px;color:#94A3B8;margin-bottom:6px;font-weight:500;">当前市值</div>
            <div style="font-size:22px;font-weight:700;color:#1E293B;">¥{{ fmt(position.currentValue) }}</div>
            <div style="font-size:12px;color:#94A3B8;margin-top:4px;">现价 {{ position.currentPrice || '-' }}</div>
          </div>
          <div :style="{padding:'16px',borderRadius:'12px',background:getDailyPlBg()}">
            <div style="font-size:13px;color:#94A3B8;margin-bottom:6px;font-weight:500;">当日盈亏</div>
            <div :style="{fontSize:'22px',fontWeight:700,color:getDailyPlColor()}">
              {{ getDailyPlSign() }}¥{{ fmt(position.dailyProfitLoss) }}
            </div>
            <div style="font-size:12px;color:#94A3B8;margin-top:4px;">今日盘中浮动</div>
          </div>
          <div :style="{padding:'16px',borderRadius:'12px',background:getProfitBg()}">
            <div style="font-size:13px;color:#94A3B8;margin-bottom:6px;font-weight:500;">浮动盈亏</div>
            <div :style="{fontSize:'22px',fontWeight:700,color:getProfitColor()}">
              {{ getPlSign() }}¥{{ fmt(position.profitLoss) }}
            </div>
            <div style="font-size:12px;color:#94A3B8;margin-top:4px;">
              每股盈亏 {{ ((position.currentPrice || 0) - (position.costPrice || 0)).toFixed(2) }}
            </div>
          </div>
          <div :style="{padding:'16px',borderRadius:'12px',background:getProfitBg()}">
            <div style="font-size:13px;color:#94A3B8;margin-bottom:6px;font-weight:500;">盈亏比例</div>
            <div :style="{fontSize:'22px',fontWeight:700,color:getProfitColor()}">
              {{ (position.profitLossPercent || 0) >= 0 ? '+' : '' }}{{ (position.profitLossPercent || 0).toFixed(2) }}%
            </div>
            <div style="font-size:12px;color:#94A3B8;margin-top:4px;">相对成本价</div>
          </div>
        </div>
        <div style="margin-top:16px;font-size:12px;color:#94A3B8;text-align:right;">
          数据更新时间：{{ position.updatedAt || '-' }}
        </div>
      </div>

      <div v-else style="display:flex;flex-direction:column;align-items:center;padding:32px 0;gap:10px;">
        <div style="font-size:40px;opacity:0.3;">📭</div>
        <div style="font-size:14px;color:#94A3B8;">暂无持仓数据，请先填写左侧持仓信息并保存</div>
      </div>
    </div>

    <!-- Cost Trend Chart -->
    <div v-if="costHistory && costHistory.length > 0"
      style="background:#fff;border-radius:16px;padding:24px;box-shadow:0 2px 16px rgba(0,0,0,0.06);margin-top:16px;">
      <div style="font-size:15px;font-weight:600;color:#1E293B;margin-bottom:16px;">📉 成本趋势</div>
      <v-chart :option="chartOption" style="height:320px;" autoresize />
    </div>

  </div>
</template>
