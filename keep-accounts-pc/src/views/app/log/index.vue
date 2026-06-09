<script setup>
import { ref, computed } from 'vue'
import { Search, RefreshRight, ArrowLeft, ArrowRight, ArrowDown } from '@element-plus/icons-vue'

const thSt = { padding: '12px 20px', fontSize: '12px', color: '#94A3B8', fontWeight: '600', textAlign: 'left', whiteSpace: 'nowrap', background: '#F8FAFC', borderBottom: '1px solid #F1F5F9', letterSpacing: '0.03em' }
const tdSt = { padding: '13px 20px', fontSize: '13px', color: '#334155', borderBottom: '1px solid #F8FAFC', whiteSpace: 'nowrap' }
const fieldStyle = { height: '36px', padding: '0 12px', borderRadius: '10px', background: '#F8FAFC', border: '1px solid #E2E8F0', fontSize: '13px', color: '#334155', outline: 'none', boxSizing: 'border-box' }

const moduleStyle = {
  '用户管理':  { bg: '#EEF2FF', color: '#667EEA' },
  '角色管理':  { bg: '#F5F3FF', color: '#A855F7' },
  '菜单管理':  { bg: '#FFF7ED', color: '#F7971E' },
  '小程序用户':{ bg: '#F0FDF4', color: '#11998E' },
  '记账记录':  { bg: '#EFF6FF', color: '#3B82F6' },
  '分类管理':  { bg: '#FFF1F2', color: '#F43F5E' },
}

const logs = ref([
  { id: 1, operator: 'admin', module: '用户管理', opType: '新增', target: '创建管理员 zhangsan', ip: '192.168.1.101', duration: 45, status: 'success', time: '2026-06-08 10:23:15', method: 'POST', url: '/api/admin/users', ua: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', params: '{"username":"zhangsan","nickname":"张三","role":"ops_admin"}' },
  { id: 2, operator: 'zhangsan', module: '分类管理', opType: '编辑', target: '修改分类 餐饮', ip: '10.0.0.52', duration: 32, status: 'success', time: '2026-06-08 10:15:44', method: 'PUT', url: '/api/categories/1', ua: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', params: '{"name":"餐饮","icon":"🍜","sort":1}' },
  { id: 3, operator: 'admin', module: '角色管理', opType: '分配权限', target: '为角色 运营管理员 分配权限', ip: '192.168.1.101', duration: 128, status: 'success', time: '2026-06-08 09:58:30', method: 'POST', url: '/api/roles/2/permissions', ua: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', params: '{"permissions":["app:read","record:read","category:write"]}' },
  { id: 4, operator: 'lisi', module: '记账记录', opType: '删除', target: '删除记账记录 ID:1024', ip: '172.16.0.23', duration: 18, status: 'fail', time: '2026-06-08 09:45:12', method: 'DELETE', url: '/api/records/1024', ua: 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0)', params: '{}' },
  { id: 5, operator: 'admin', module: '菜单管理', opType: '新增', target: '新增菜单 数据统计', ip: '192.168.1.101', duration: 56, status: 'success', time: '2026-06-08 09:30:00', method: 'POST', url: '/api/menus', ua: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', params: '{"name":"数据统计","type":"menu","path":"/stats","sort":10}' },
  { id: 6, operator: 'wangwu', module: '小程序用户', opType: '禁用', target: '禁用用户 月光族 (ID:4)', ip: '10.0.0.33', duration: 22, status: 'success', time: '2026-06-07 18:20:10', method: 'PUT', url: '/api/miniapp/users/4/status', ua: 'Mozilla/5.0 (Windows NT 10.0)', params: '{"status":false}' },
  { id: 7, operator: 'admin', module: '用户管理', opType: '删除', target: '删除管理员 testuser', ip: '192.168.1.101', duration: 38, status: 'success', time: '2026-06-07 17:00:00', method: 'DELETE', url: '/api/admin/users/8', ua: 'Mozilla/5.0 (Macintosh)', params: '{}' },
  { id: 8, operator: 'zhaoliu', module: '记账记录', opType: '导出', target: '导出6月记账记录', ip: '10.0.0.44', duration: 2340, status: 'success', time: '2026-06-07 16:45:22', method: 'GET', url: '/api/records/export?month=2026-06', ua: 'Mozilla/5.0 (Windows NT 10.0)', params: '{"month":"2026-06","type":"all"}' },
  { id: 9, operator: 'admin', module: '角色管理', opType: '新增', target: '创建角色 财务专员', ip: '192.168.1.101', duration: 41, status: 'success', time: '2026-06-07 14:30:00', method: 'POST', url: '/api/roles', ua: 'Mozilla/5.0 (Macintosh)', params: '{"name":"财务专员","code":"FINANCE_STAFF","sort":5}' },
  { id: 10, operator: 'zhangsan', module: '分类管理', opType: '新增', target: '新增分类 健身', ip: '10.0.0.52', duration: 29, status: 'fail', time: '2026-06-07 11:20:08', method: 'POST', url: '/api/categories', ua: 'Mozilla/5.0 (Windows NT 10.0)', params: '{"name":"健身","icon":"🏋️","type":"expense"}' },
  { id: 11, operator: 'admin', module: '菜单管理', opType: '编辑', target: '修改菜单排序', ip: '192.168.1.101', duration: 33, status: 'success', time: '2026-06-06 16:00:00', method: 'PUT', url: '/api/menus/batch-sort', ua: 'Mozilla/5.0 (Macintosh)', params: '{"orders":[{"id":1,"sort":1},{"id":2,"sort":2}]}' },
  { id: 12, operator: 'wangwu', module: '小程序用户', opType: '查看', target: '查看用户详情 小明同学', ip: '10.0.0.33', duration: 15, status: 'success', time: '2026-06-06 14:22:00', method: 'GET', url: '/api/miniapp/users/1', ua: 'Mozilla/5.0 (Windows NT 10.0)', params: '{}' },
])

const modules = ['用户管理', '角色管理', '菜单管理', '小程序用户', '记账记录', '分类管理']
const opTypes = ['新增', '编辑', '删除', '查看', '导出', '禁用', '分配权限']

const page = ref(1)
const expanded = ref(new Set())
const searchOp = ref('')
const moduleFilter = ref('')
const opTypeFilter = ref('')
const statusFilter = ref('')

const filtered = computed(() => {
  return logs.value.filter(l =>
    (!searchOp.value || l.operator.includes(searchOp.value)) &&
    (!moduleFilter.value || l.module === moduleFilter.value) &&
    (!opTypeFilter.value || l.opType === opTypeFilter.value) &&
    (!statusFilter.value || l.status === statusFilter.value)
  )
})

const paged = computed(() => {
  const start = (page.value - 1) * 10
  return filtered.value.slice(start, start + 10)
})

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

function toggleExpand(id) {
  const next = new Set(expanded.value)
  if (next.has(id)) next.delete(id); else next.add(id)
  expanded.value = next
}

function formatJson(str) {
  try { return JSON.stringify(JSON.parse(str || '{}'), null, 2) } catch { return str }
}

function handleSearch() { page.value = 1 }
function handleReset() {
  searchOp.value = ''
  moduleFilter.value = ''
  opTypeFilter.value = ''
  statusFilter.value = ''
  page.value = 1
}
</script>

<template>
  <div style="padding:24px;background:#F0F2F8;min-height:100%;">
    <!-- Search card -->
    <div style="background:#fff;border-radius:16px;padding:16px 24px;margin-bottom:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);">
      <div style="display:flex;gap:10px;flex-wrap:wrap;align-items:center;">
        <div style="position:relative;">
          <el-icon style="position:absolute;left:11px;top:50%;transform:translateY(-50%);color:#94A3B8;font-size:13px;"><Search /></el-icon>
          <input v-model="searchOp" placeholder="操作人"
            style="width:140px;height:36px;padding:0 12px 0 32px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:13px;color:#334155;outline:none;box-sizing:border-box;" />
        </div>
        <select v-model="moduleFilter" style="width:130px;height:36px;padding:0 10px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:13px;color:#334155;outline:none;cursor:pointer;">
          <option value="">全部模块</option>
          <option v-for="m in modules" :key="m" :value="m">{{ m }}</option>
        </select>
        <select v-model="opTypeFilter" style="width:120px;height:36px;padding:0 10px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:13px;color:#334155;outline:none;cursor:pointer;">
          <option value="">操作类型</option>
          <option v-for="t in opTypes" :key="t" :value="t">{{ t }}</option>
        </select>
        <select v-model="statusFilter" style="width:110px;height:36px;padding:0 10px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:13px;color:#334155;outline:none;cursor:pointer;">
          <option value="">全部状态</option>
          <option value="success">成功</option>
          <option value="fail">失败</option>
        </select>
        <input type="date" style="width:136px;height:36px;padding:0 10px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:13px;color:#334155;outline:none;box-sizing:border-box;" />
        <span style="color:#94A3B8;font-size:13px;">~</span>
        <input type="date" style="width:136px;height:36px;padding:0 10px;border-radius:10px;background:#F8FAFC;border:1px solid #E2E8F0;font-size:13px;color:#334155;outline:none;box-sizing:border-box;" />
        <button @click="handleSearch" style="height:36px;padding:0 16px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;border:none;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;font-weight:500;box-shadow:0 3px 10px rgba(102,126,234,0.35);">
          <el-icon style="font-size:13px;"><Search /></el-icon>搜索
        </button>
        <button @click="handleReset" style="height:36px;padding:0 16px;background:#F8FAFC;color:#64748B;border:1px solid #E2E8F0;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;">
          <el-icon style="font-size:13px;"><RefreshRight /></el-icon>重置
        </button>
      </div>
    </div>

    <!-- Table card -->
    <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);overflow:hidden;">
      <div style="overflow-x:auto;">
        <table style="width:100%;border-collapse:collapse;min-width:900px;">
          <thead>
            <tr>
              <th :style="{...thSt, width:'28px', padding:'12px 8px 12px 16px'}" />
              <th :style="thSt">序号</th>
              <th :style="thSt">操作人</th>
              <th :style="thSt">模块</th>
              <th :style="thSt">操作类型</th>
              <th :style="thSt">操作对象</th>
              <th :style="thSt">IP地址</th>
              <th :style="thSt">耗时</th>
              <th :style="thSt">状态</th>
              <th :style="thSt">操作时间</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="(log, i) in paged" :key="log.id">
              <tr
                @click="toggleExpand(log.id)"
                @mouseenter="e => e.currentTarget.style.background = '#FAFBFF'"
                @mouseleave="e => e.currentTarget.style.background = expanded.has(log.id) ? '#F8FAFC' : '#fff'"
                :style="{cursor:'pointer',transition:'background 0.15s',background: expanded.has(log.id) ? '#F8FAFC' : '#fff'}">
                <td :style="{...tdSt, textAlign:'center', width:'28px', padding:'13px 4px 13px 16px', color:'#94A3B8'}">
                  <el-icon v-if="expanded.has(log.id)" style="font-size:13px;"><ArrowDown /></el-icon>
                  <el-icon v-else style="font-size:13px;"><ArrowRight /></el-icon>
                </td>
                <td :style="{...tdSt, color:'#CBD5E1', width:'50px'}">{{ (page-1) * 10 + i + 1 }}</td>
                <td :style="{...tdSt, fontWeight:600, color:'#1E293B'}">{{ log.operator }}</td>
                <td :style="tdSt">
                  <span v-if="moduleStyle[log.module]" :style="{fontSize:'12px',padding:'2px 10px',borderRadius:'20px',background:moduleStyle[log.module].bg,color:moduleStyle[log.module].color,fontWeight:500}">{{ log.module }}</span>
                  <span v-else style="color:#94A3B8;">{{ log.module }}</span>
                </td>
                <td :style="{...tdSt, color:'#475569'}">{{ log.opType }}</td>
                <td :style="{...tdSt, maxWidth:'180px', overflow:'hidden', textOverflow:'ellipsis', color:'#475569'}" :title="log.target">{{ log.target }}</td>
                <td :style="{...tdSt, fontFamily:'monospace', fontSize:'12px', color:'#94A3B8'}">{{ log.ip }}</td>
                <td :style="{...tdSt, textAlign:'right', color: log.duration > 1000 ? '#F7971E' : '#94A3B8', fontWeight: log.duration > 1000 ? 600 : 400}">{{ log.duration }}ms</td>
                <td :style="tdSt">
                  <div style="display:flex;align-items:center;gap:5px;">
                    <span :style="{width:'6px',height:'6px',borderRadius:'50%',background:log.status==='success'?'#11998E':'#F43F5E',display:'inline-block',flexShrink:0}" />
                    <span :style="{fontSize:'13px',color:log.status==='success'?'#11998E':'#F43F5E',fontWeight:500}">{{ log.status === 'success' ? '成功' : '失败' }}</span>
                  </div>
                </td>
                <td :style="{...tdSt, color:'#94A3B8', fontSize:'12px'}">{{ log.time }}</td>
              </tr>
              <tr v-if="expanded.has(log.id)" :key="`${log.id}-exp`" style="background:#F8FAFC;">
                <td :colspan="10" style="padding:0 20px 16px 48px;border-bottom:1px solid #F1F5F9;">
                  <div style="background:#fff;border-radius:12px;padding:16px 20px;border:1px solid #F1F5F9;">
                    <div style="display:flex;gap:24px;margin-bottom:12px;flex-wrap:wrap;">
                      <div style="display:flex;align-items:center;gap:8px;">
                        <span style="font-size:12px;color:#94A3B8;">请求方法</span>
                        <span style="font-size:11px;padding:2px 8px;border-radius:6px;background:#1E293B;color:#fff;font-weight:700;font-family:monospace;">{{ log.method }}</span>
                      </div>
                      <div style="display:flex;align-items:center;gap:8px;">
                        <span style="font-size:12px;color:#94A3B8;">请求URL</span>
                        <code style="font-size:12px;color:#667EEA;font-family:monospace;background:#EEF2FF;padding:2px 8px;border-radius:6px;">{{ log.url }}</code>
                      </div>
                      <div style="display:flex;align-items:center;gap:8px;flex:1;min-width:200px;">
                        <span style="font-size:12px;color:#94A3B8;flex-shrink:0;">User-Agent</span>
                        <span style="font-size:11px;color:#94A3B8;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{{ log.ua }}</span>
                      </div>
                    </div>
                    <div style="font-size:12px;color:#94A3B8;margin-bottom:6px;font-weight:500;">请求参数</div>
                    <pre style="background:#1E293B;color:#94A3B8;border-radius:10px;padding:14px 16px;font-size:12px;font-family:monospace;margin:0;overflow-x:auto;line-height:1.7;">{{ formatJson(log.params) }}</pre>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <!-- Custom pagination -->
      <div style="display:flex;justify-content:flex-end;padding:12px 16px;align-items:center;gap:4px;">
        <span style="font-size:12px;color:#94A3B8;margin-right:8px;">共 {{ filtered.length }} 条</span>
        <button :disabled="page <= 1" @click="page--"
          :style="{width:'32px',height:'32px',border:'1px solid #E2E8F0',borderRadius:'8px',background:'#fff',display:'flex',alignItems:'center',justifyContent:'center',opacity: page <= 1 ? 0.4 : 1, cursor: page <= 1 ? 'not-allowed' : 'pointer'}">
          <el-icon style="font-size:14px;color:#64748B;"><ArrowLeft /></el-icon>
        </button>
        <template v-for="(p, idx) in pages" :key="idx">
          <span v-if="p === '...'" style="width:32px;text-align:center;color:#94A3B8;font-size:13px;">…</span>
          <button v-else @click="page = p"
            :style="{width:'32px',height:'32px',border:'none',borderRadius:'8px',background: p===page ? 'linear-gradient(135deg,#667EEA,#764BA2)' : '#fff',color: p===page ? '#fff' : '#475569',cursor:'pointer',fontSize:'13px',fontWeight: p===page ? 600 : 400}">{{ p }}</button>
        </template>
        <button :disabled="page >= totalPages" @click="page++"
          :style="{width:'32px',height:'32px',border:'1px solid #E2E8F0',borderRadius:'8px',background:'#fff',display:'flex',alignItems:'center',justifyContent:'center',opacity: page >= totalPages ? 0.4 : 1, cursor: page >= totalPages ? 'not-allowed' : 'pointer'}">
          <el-icon style="font-size:14px;color:#64748B;"><ArrowRight /></el-icon>
        </button>
      </div>
    </div>
  </div>
</template>
