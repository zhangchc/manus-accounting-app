<script setup>
import { ref, computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent])

const dauRange = ref('7')
const newUserRange = ref('7')

// Chart colors from original
const chartBlue = '#667EEA'
const chartPurple = '#764BA2'
const chartGreen = '#11998E'
const chartGreenEnd = '#38EF7D'
const chartPink = '#F953C6'

const dauData7 = [
  { date: '06-02', dau: 1820 }, { date: '06-03', dau: 2140 }, { date: '06-04', dau: 1960 },
  { date: '06-05', dau: 2380 }, { date: '06-06', dau: 2210 }, { date: '06-07', dau: 2650 }, { date: '06-08', dau: 2430 },
]
const dauData30 = Array.from({ length: 30 }, (_, i) => ({
  date: `05-${String(i + 9).padStart(2, '0')}`,
  dau: Math.floor(1600 + Math.random() * 1200 + Math.sin(i / 3) * 400),
}))
const newData7 = [
  { date: '06-02', n: 28 }, { date: '06-03', n: 42 }, { date: '06-04', n: 31 },
  { date: '06-05', n: 55 }, { date: '06-06', n: 38 }, { date: '06-07', n: 61 }, { date: '06-08', n: 47 },
]
const newData30 = Array.from({ length: 30 }, (_, i) => ({
  date: `05-${String(i + 9).padStart(2, '0')}`, n: Math.floor(20 + Math.random() * 60),
}))

const categoryDist = [
  { name: '餐饮', value: 3420, color: '#7B9EF5' },
  { name: '购物', value: 2180, color: '#5CC9A7' },
  { name: '交通', value: 1560, color: '#F5C07C' },
  { name: '住房', value: 980, color: '#F5707A' },
  { name: '娱乐', value: 760, color: '#B8A0F5' },
  { name: '其他', value: 640, color: '#6CC2F5' },
]

const hourlyData = Array.from({ length: 24 }, (_, h) => ({
  hour: String(h).padStart(2, '0'),
  count: h < 7 ? Math.floor(Math.random() * 30 + 5) : h < 9 ? Math.floor(Math.random() * 120 + 80) : h < 12 ? Math.floor(Math.random() * 200 + 150) : h < 14 ? Math.floor(Math.random() * 280 + 200) : h < 18 ? Math.floor(Math.random() * 160 + 100) : h < 22 ? Math.floor(Math.random() * 300 + 220) : Math.floor(Math.random() * 100 + 50),
}))

const topUsers = [
  { name: '记账小能手', records: 534, delta: '+12%', up: true },
  { name: '节俭达人', records: 412, delta: '+8%', up: true },
  { name: '花花🌸', records: 289, delta: '+5%', up: true },
  { name: '小富即安', records: 197, delta: '-3%', up: false },
  { name: '小明同学', records: 156, delta: '+1%', up: true },
]

const anomalies = [
  { text: '用户「月光族」单日记账 98 笔，超出阈值', time: '10:32', level: 'warn' },
  { text: '记录 ID#4521 金额异常：¥99,999.00', time: '09:15', level: 'error' },
  { text: '用户「测试账号」30分钟内登录 12 次', time: '08:50', level: 'warn' },
]

const adminLogs = [
  { op: 'admin', action: '新增管理员 zhangsan', time: '10:23' },
  { op: 'zhangsan', action: '修改分类「餐饮」图标', time: '10:15' },
  { op: 'admin', action: '为角色「运营管理员」分配权限', time: '09:58' },
  { op: 'wangwu', action: '禁用用户「月光族」', time: '09:45' },
]

// Stat cards - exact match with original
const statsCards = [
  { label: '今日活跃用户 (DAU)', value: '2,430', delta: '-8.3%', up: false, sub: '昨日 2,650', gradient: 'linear-gradient(135deg, #667EEA 0%, #764BA2 100%)', shadow: 'rgba(102,126,234,0.45)', iconBg: 'rgba(255,255,255,0.18)' },
  { label: '今日记账笔数', value: '1,247', delta: '+21.6%', up: true, sub: '支出 843 / 收入 404', gradient: 'linear-gradient(135deg, #11998E 0%, #38EF7D 100%)', shadow: 'rgba(17,153,142,0.4)', iconBg: 'rgba(255,255,255,0.18)' },
  { label: '本月记账总额', value: '¥1,284,560', delta: '+17.1%', up: true, sub: '较上月 ¥1,097,320', gradient: 'linear-gradient(135deg, #F7971E 0%, #FFD200 100%)', shadow: 'rgba(247,151,30,0.4)', iconBg: 'rgba(255,255,255,0.22)' },
  { label: '本周新增用户', value: '302', delta: '+14.4%', up: true, sub: '本月累计 1,024', gradient: 'linear-gradient(135deg, #F953C6 0%, #B91D73 100%)', shadow: 'rgba(249,83,198,0.4)', iconBg: 'rgba(255,255,255,0.18)' },
]

const dauData = computed(() => dauRange.value === '7' ? dauData7 : dauData30)
const newData = computed(() => newUserRange.value === '7' ? newData7 : newData30)

const dauInterval = computed(() => dauRange.value === '30' ? 4 : 0)
const newInterval = computed(() => newUserRange.value === '30' ? 4 : 0)

// ECharts options
const areaOption = (data, color, gradientEnd, label) => ({
  grid: { top: 8, right: 12, bottom: 0, left: 48 },
  xAxis: { type: 'category', data: data.map(d => d.date), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { fontSize: 11, color: '#94A3B8' } },
  yAxis: { type: 'value', axisLine: { show: false }, axisTick: { show: false }, splitLine: { lineStyle: { color: '#F1F5F9', type: 'dashed' } }, axisLabel: { fontSize: 11, color: '#94A3B8', formatter: v => v >= 1000 ? (v / 1000).toFixed(1) + 'k' : v } },
  tooltip: { trigger: 'axis', backgroundColor: '#1E293B', borderColor: 'transparent', textStyle: { color: '#fff', fontSize: 12 }, valueFormatter: v => v?.toLocaleString() + (label || '') },
  series: [{ type: 'line', data: data.map(d => d[Object.keys(d).find(k => k !== 'date')]), smooth: true, lineStyle: { color, width: 3 }, itemStyle: { color }, areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: color + '4D' }, { offset: 1, color: gradientEnd + '00' }] } }, symbol: 'none' }],
})

const dauOption = computed(() => areaOption(dauData.value, chartBlue, chartPurple, ' 人'))
const newOption = computed(() => areaOption(newData.value, chartGreen, chartGreenEnd, ' 人'))

const pieOption = {
  tooltip: { trigger: 'item', backgroundColor: '#1E293B', borderColor: 'transparent', textStyle: { color: '#fff', fontSize: 12 }, formatter: p => `${p.value.toLocaleString()} 笔` },
  series: [{
    type: 'pie', radius: ['54%', '82%'], center: ['50%', '50%'], avoidLabelOverlap: false,
    itemStyle: { borderRadius: 2, borderColor: '#F0F2F8', borderWidth: 2 },
    label: { show: false },
    data: categoryDist.map(c => ({ name: c.name, value: c.value, itemStyle: { color: c.color } })),
  }],
}

const barOption = {
  grid: { top: 4, right: 8, bottom: 0, left: 40 },
  xAxis: { type: 'category', data: hourlyData.map(d => d.hour), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { fontSize: 9, color: '#94A3B8', interval: 3 } },
  yAxis: { type: 'value', axisLine: { show: false }, axisTick: { show: false }, splitLine: { lineStyle: { color: '#F1F5F9', type: 'dashed' } }, axisLabel: { fontSize: 9, color: '#94A3B8' } },
  tooltip: { trigger: 'axis', backgroundColor: '#1E293B', borderColor: 'transparent', textStyle: { color: '#fff', fontSize: 11 }, valueFormatter: v => `${v} 笔` },
  series: [{
    name: '记账笔数', type: 'bar', data: hourlyData.map(d => ({ value: d.count, itemStyle: { color: d.count > 250 ? '#667EEA' : d.count > 150 ? '#A5B4FC' : '#E0E7FF', borderRadius: [3, 3, 0, 0] } })), barWidth: 9,
  }],
}

const totalCat = categoryDist.reduce((s, c) => s + c.value, 0)

function deltaStyle(up) {
  return {
    display: 'flex', alignItems: 'center', gap: '3px', fontSize: '12px', fontWeight: 600, color: '#fff',
    background: 'rgba(255,255,255,0.2)', padding: '3px 9px', borderRadius: '20px',
  }
}
</script>

<template>
  <div style="padding:24px 28px;background:#F0F2F8;min-height:100%;">
    <!-- ── Row 1: Gradient stat cards ── -->
    <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:18px;margin-bottom:20px;">
      <div v-for="(s, i) in statsCards" :key="i"
        :style="{
          background: s.gradient, borderRadius: '20px', padding: '24px',
          boxShadow: `0 8px 32px ${s.shadow}`, position: 'relative', overflow: 'hidden',
        }"
      >
        <div style="position:absolute;top:-20px;right:-20px;width:100px;height:100px;border-radius:50%;background:rgba(255,255,255,0.1);"></div>
        <div style="position:absolute;bottom:-30px;right:20px;width:70px;height:70px;border-radius:50%;background:rgba(255,255,255,0.07);"></div>
        <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px;position:relative;">
          <div :style="{width:'42px',height:'42px',borderRadius:'12px',background:s.iconBg,display:'flex',alignItems:'center',justifyContent:'center',color:'#fff',backdropFilter:'blur(4px)'}">
            <el-icon size="20"><component :is="['User','Notebook','Money','UserFilled'][i]" /></el-icon>
          </div>
          <div :style="deltaStyle(s.up)">
            <span style="font-size:11px;">{{ s.up ? '▲' : '▼' }}</span>{{ s.delta }}
          </div>
        </div>
        <div style="font-size:13px;color:rgba(255,255,255,0.8);margin-bottom:6px;position:relative;">{{ s.label }}</div>
        <div style="font-size:30px;font-weight:800;color:#fff;letter-spacing:'-0.5px';line-height:1.1;margin-bottom:8px;position:relative;">{{ s.value }}</div>
        <div style="font-size:12px;color:rgba(255,255,255,0.65);position:relative;">{{ s.sub }}</div>
      </div>
    </div>

    <!-- ── Row 2: DAU trend ── -->
    <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);padding:24px;margin-bottom:20px;">
      <div style="display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:24px;">
        <div>
          <div style="font-size:16px;font-weight:700;color:#1E293B;margin-bottom:4px;">每日登录用户趋势</div>
          <div style="font-size:12px;color:#94A3B8;">统计每日登录小程序的去重用户数</div>
        </div>
        <div style="display:flex;background:#F3F4F8;border-radius:8px;padding:3px;gap:2px;">
          <button v-for="v in ['7','30']" :key="v" @click="dauRange = v"
            :style="{padding:'5px 14px',border:'none',borderRadius:'6px',cursor:'pointer',fontSize:'12px',fontWeight:500,transition:'all 0.15s',
              background: dauRange === v ? '#667EEA' : 'transparent', color: dauRange === v ? '#fff' : '#6B7280',
              boxShadow: dauRange === v ? '0 2px 6px #667EEA55' : 'none'}">近{{ v }}天</button>
        </div>
      </div>
      <v-chart :option="dauOption" autoresize style="height:220px;" />
    </div>

    <!-- ── Row 3: New users trend ── -->
    <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);padding:24px;margin-bottom:20px;">
      <div style="display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:24px;">
        <div>
          <div style="font-size:16px;font-weight:700;color:#1E293B;margin-bottom:4px;">每日新增用户趋势</div>
          <div style="font-size:12px;color:#94A3B8;">统计每日首次注册小程序的新增用户数</div>
        </div>
        <div style="display:flex;background:#F3F4F8;border-radius:8px;padding:3px;gap:2px;">
          <button v-for="v in ['7','30']" :key="v" @click="newUserRange = v"
            :style="{padding:'5px 14px',border:'none',borderRadius:'6px',cursor:'pointer',fontSize:'12px',fontWeight:500,transition:'all 0.15s',
              background: newUserRange === v ? '#11998E' : 'transparent', color: newUserRange === v ? '#fff' : '#6B7280',
              boxShadow: newUserRange === v ? '0 2px 6px #11998E55' : 'none'}">近{{ v }}天</button>
        </div>
      </div>
      <v-chart :option="newOption" autoresize style="height:220px;" />
    </div>

    <!-- ── Row 4: Category + Hourly ── -->
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px;">
      <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);padding:24px;">
        <div style="font-size:16px;font-weight:700;color:#1E293B;margin-bottom:4px;">记账分类分布</div>
        <div style="font-size:12px;color:#94A3B8;margin-bottom:20px;">本月各分类记账笔数 Top 6</div>
        <div style="display:flex;gap:20px;align-items:center;">
          <v-chart :option="pieOption" autoresize style="width:148px;height:148px;flex-shrink:0;" />
          <div style="flex:1;display:flex;flex-direction:column;gap:10px;">
            <div v-for="(c, idx) in categoryDist" :key="idx">
              <div style="display:flex;justify-content:space-between;margin-bottom:4px;">
                <div style="display:flex;align-items:center;gap:6px;">
                  <span :style="{width:'8px',height:'8px',borderRadius:'50%',background:c.color,display:'inline-block'}"></span>
                  <span style="font-size:12px;color:#475569;">{{ c.name }}</span>
                </div>
                <span style="font-size:12px;font-weight:600;color:#1E293B;">{{ Math.round(c.value / totalCat * 100) }}%</span>
              </div>
              <div style="height:5px;background:#F1F5F9;border-radius:3px;overflow:hidden;">
                <div :style="{width: (c.value / totalCat * 100) + '%', height:'100%', background:c.color, borderRadius:'3px'}"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);padding:24px;">
        <div style="font-size:16px;font-weight:700;color:#1E293B;margin-bottom:4px;">每小时记账高峰</div>
        <div style="font-size:12px;color:#94A3B8;margin-bottom:20px;">今日 0–23 时记账活跃分布</div>
        <v-chart :option="barOption" autoresize style="height:185px;" />
        <div style="display:flex;justify-content:center;gap:20px;margin-top:10px;">
          <div v-for="[c,l] in [['#667EEA','高峰 >250'],['#A5B4FC','活跃 >150'],['#E0E7FF','低峰']]" :key="l"
            style="display:flex;align-items:center;gap:5px;font-size:11px;color:#94A3B8;">
            <span :style="{width:'10px',height:'10px',borderRadius:'2px',background:c,display:'inline-block'}"></span>{{ l }}
          </div>
        </div>
      </div>
    </div>

    <!-- ── Row 5: Quick insights ── -->
    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:20px;">
      <!-- Top 5 users -->
      <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);padding:24px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;">
          <div>
            <div style="font-size:15px;font-weight:700;color:#1E293B;margin-bottom:2px;">Top 5 活跃用户</div>
            <div style="font-size:12px;color:#94A3B8;">本周记账笔数排名</div>
          </div>
          <span style="font-size:11px;color:#667EEA;cursor:pointer;display:flex;align-items:center;gap:2px;font-weight:500;">查看全部 <span>›</span></span>
        </div>
        <div v-for="(u, idx) in topUsers" :key="idx"
          style="display:flex;align-items:center;gap:12px;padding:8px 0;"
          :style="{borderBottom: idx < 4 ? '1px solid #F8FAFC' : 'none'}"
        >
          <div :style="{width:'22px',height:'22px',borderRadius:'50%',display:'flex',alignItems:'center',justifyContent:'center',fontSize:'10px',fontWeight:800,flexShrink:0,
            background: idx === 0 ? '#F7971E' : idx === 1 ? '#94A3B8' : idx === 2 ? '#CD7F32' : '#E2E8F0',
            color: idx < 3 ? '#fff' : '#94A3B8'}">{{ idx + 1 }}</div>
          <div style="width:30px;height:30px;border-radius:50%;background:var(--gradient-primary);display:flex;align-items:center;justify-content:center;color:#fff;font-size:12px;font-weight:700;flex-shrink:0;">{{ u.name[0] }}</div>
          <span style="flex:1;font-size:13px;color:#334155;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-weight:500;">{{ u.name }}</span>
          <span style="font-size:13px;font-weight:700;color:#1E293B;">{{ u.records }}</span>
          <span :style="{fontSize:'11px',fontWeight:600,color:u.up?'#16A34A':'#DC2626',minWidth:'38px',textAlign:'right'}">{{ u.delta }}</span>
        </div>
      </div>

      <!-- Anomalies -->
      <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);padding:24px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;">
          <div>
            <div style="display:flex;align-items:center;gap:7px;font-size:15px;font-weight:700;color:#1E293B;margin-bottom:2px;">
              <div style="width:28px;height:28px;border-radius:8px;background:var(--gradient-orange);display:flex;align-items:center;justify-content:center;">
                <span style="color:#fff;font-size:14px;">⚠</span>
              </div>
              异常提醒
            </div>
            <div style="font-size:12px;color:#94A3B8;">今日触发 {{ anomalies.length }} 条规则</div>
          </div>
          <div style="background:#FEF3C7;color:#D97706;font-size:12px;font-weight:700;padding:3px 10px;border-radius:20px;">{{ anomalies.length }} 条</div>
        </div>
        <div v-for="(a, idx) in anomalies" :key="idx"
          :style="{padding:'12px',marginBottom: idx < anomalies.length - 1 ? '8px' : 0,
            background: a.level === 'error' ? '#FFF1F2' : '#FFFBEB', borderRadius:'10px',
            borderLeft: `3px solid ${a.level === 'error' ? '#F43F5E' : '#F59E0B'}`}"
        >
          <div style="display:flex;justify-content:space-between;margin-bottom:5px;">
            <span :style="{fontSize:'11px',fontWeight:700,color:a.level==='error'?'#E11D48':'#D97706',letterSpacing:'0.04em'}">
              {{ a.level === 'error' ? '● 严重' : '● 警告' }}</span>
            <span style="font-size:11px;color:#94A3B8;">{{ a.time }}</span>
          </div>
          <div style="font-size:12px;color:#334155;line-height:1.6;">{{ a.text }}</div>
        </div>
      </div>

      <!-- Admin logs -->
      <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);padding:24px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;">
          <div>
            <div style="display:flex;align-items:center;gap:7px;font-size:15px;font-weight:700;color:#1E293B;margin-bottom:2px;">
              <div style="width:28px;height:28px;border-radius:8px;background:var(--gradient-primary);display:flex;align-items:center;justify-content:center;">
                <span style="color:#fff;font-size:14px;">🛡</span>
              </div>
              操作日志摘要
            </div>
            <div style="font-size:12px;color:#94A3B8;">今日管理员操作记录</div>
          </div>
        </div>
        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:8px;margin-bottom:16px;">
          <div v-for="item in [['16','操作次数','#EEF2FF','#667EEA'],['4','涉及模块','#F0FDF4','#16A34A'],['3','操作人数','#FDF4FF','#A855F7']]" :key="item[1]"
            :style="{background:item[2],borderRadius:'10px',padding:'10px',textAlign:'center'}">
            <div :style="{fontSize:'22px',fontWeight:800,color:item[3],letterSpacing:'-0.5px'}">{{ item[0] }}</div>
            <div :style="{fontSize:'10px',color:'#94A3B8',marginTop:'2px'}">{{ item[1] }}</div>
          </div>
        </div>
        <div v-for="(log, idx) in adminLogs" :key="idx"
          style="display:flex;align-items:center;gap:10px;padding:8px 0;"
          :style="{borderBottom: idx < adminLogs.length - 1 ? '1px solid #F8FAFC' : 'none'}"
        >
          <div style="width:24px;height:24px;border-radius:50%;background:var(--gradient-primary);display:flex;align-items:center;justify-content:center;color:#fff;font-size:9px;font-weight:700;flex-shrink:0;">{{ log.op[0].toUpperCase() }}</div>
          <span style="font-size:11px;color:#94A3B8;flex-shrink:0;font-weight:500;">{{ log.op }}</span>
          <span style="flex:1;font-size:12px;color:#334155;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{{ log.action }}</span>
          <span style="font-size:11px;color:#CBD5E1;flex-shrink:0;">{{ log.time }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
