<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import { Search, Plus, Edit, Delete, ArrowDown, ArrowRight, RefreshRight } from '@element-plus/icons-vue'

const roles = ref([
  { id: 1, name: '超级管理员', code: 'SUPER_ADMIN', desc: '拥有系统所有权限', sort: 1, status: true },
  { id: 2, name: '运营管理员', code: 'OPS_ADMIN', desc: '负责日常运营管理', sort: 2, status: true },
  { id: 3, name: '内容编辑', code: 'CONTENT_EDITOR', desc: '分类内容管理权限', sort: 3, status: true },
  { id: 4, name: '数据分析师', code: 'DATA_ANALYST', desc: '只读数据查看权限', sort: 4, status: false },
])

const permTree = [
  {
    id: 'system', label: '系统管理', children: [
      {
        id: 'admin', label: '管理员管理', children: [
          { id: 'admin:list', label: '查看列表' }, { id: 'admin:create', label: '新增管理员' },
          { id: 'admin:edit', label: '编辑管理员' }, { id: 'admin:delete', label: '删除管理员' },
        ]
      },
      {
        id: 'role', label: '角色管理', children: [
          { id: 'role:list', label: '查看列表' }, { id: 'role:create', label: '新增角色' },
          { id: 'role:edit', label: '编辑角色' }, { id: 'role:delete', label: '删除角色' },
          { id: 'role:assign', label: '分配权限' },
        ]
      },
      {
        id: 'menu', label: '菜单管理', children: [
          { id: 'menu:list', label: '查看列表' }, { id: 'menu:create', label: '新增菜单' },
          { id: 'menu:edit', label: '编辑菜单' }, { id: 'menu:delete', label: '删除菜单' },
        ]
      },
    ]
  },
  {
    id: 'app', label: '应用管理', children: [
      {
        id: 'user', label: '小程序用户', children: [
          { id: 'user:list', label: '查看列表' }, { id: 'user:detail', label: '查看详情' }, { id: 'user:ban', label: '封禁用户' },
        ]
      },
      {
        id: 'record', label: '记账记录', children: [
          { id: 'record:list', label: '查看列表' }, { id: 'record:delete', label: '删除记录' }, { id: 'record:export', label: '导出数据' },
        ]
      },
      {
        id: 'category', label: '分类管理', children: [
          { id: 'category:list', label: '查看列表' }, { id: 'category:create', label: '新增分类' },
          { id: 'category:edit', label: '编辑分类' }, { id: 'category:delete', label: '删除分类' },
        ]
      },
    ]
  },
  {
    id: 'log', label: '操作日志', children: [
      { id: 'log:list', label: '查看日志' }, { id: 'log:export', label: '导出日志' },
    ]
  },
]

const superAdminPerms = ['system','admin','admin:list','admin:create','admin:edit','admin:delete','role','role:list','role:create','role:edit','role:delete','role:assign','menu','menu:list','menu:create','menu:edit','menu:delete','app','user','user:list','user:detail','user:ban','record','record:list','record:delete','record:export','category','category:list','category:create','category:edit','category:delete','log','log:list','log:export']

const selectedRole = ref(null)
const perms = ref(new Set())
const expandedNodes = ref(new Set())
const searchName = ref('')

const showModal = ref(false)
const editRole = ref(null)
const formStatus = ref(true)
const deleteId = ref(null)
const form = reactive({ name: '', code: '', desc: '', sort: 1 })

// Initialize expanded nodes
permTree.forEach(n => expandedNodes.value.add(n.id))

const filtered = computed(() => {
  let list = roles.value
  if (searchName.value) {
    const kw = searchName.value
    list = list.filter(r => r.name.includes(kw) || r.code.includes(kw))
  }
  return list
})

function selectRole(r) {
  selectedRole.value = r
  if (r.id === 1) perms.value = new Set(superAdminPerms)
  else if (r.id === 2) perms.value = new Set(['app','user','user:list','user:detail','user:ban','record','record:list','record:delete','record:export','category','category:list','log','log:list'])
  else perms.value = new Set(['app','category','category:list','category:create','category:edit'])
}

function togglePerm(id) {
  const next = new Set(perms.value)
  if (next.has(id)) next.delete(id); else next.add(id)
  perms.value = next
}

function isChecked(id) { return perms.value.has(id) }

function toggleExpand(id) {
  const next = new Set(expandedNodes.value)
  if (next.has(id)) next.delete(id); else next.add(id)
  expandedNodes.value = next
}

// Flatten tree for rendering with visibility
const flatPermNodes = computed(() => {
  const result = []
  function walk(nodes, depth) {
    for (const n of nodes) {
      result.push({ node: n, depth, visible: true })
      if (n.children?.length && expandedNodes.value.has(n.id)) {
        walk(n.children, depth + 1)
      }
    }
  }
  walk(permTree, 0)
  return result
})

function openAdd() { editRole.value = null; formStatus.value = true; Object.assign(form, { name: '', code: '', desc: '', sort: 1 }); showModal.value = true }
function openEdit(r) { editRole.value = r; formStatus.value = r.status; Object.assign(form, { name: r.name, code: r.code, desc: r.desc, sort: r.sort }); showModal.value = true }
function handleDelete(id) { deleteId.value = id }

function confirmDelete() {
  if (deleteId.value !== null) {
    roles.value = roles.value.filter(r => r.id !== deleteId.value)
    if (selectedRole.value?.id === deleteId.value) selectedRole.value = null
  }
  deleteId.value = null
}

function handleSave() {
  if (editRole.value) {
    const idx = roles.value.findIndex(r => r.id === editRole.value.id)
    if (idx >= 0) Object.assign(roles.value[idx], { ...form, status: formStatus.value })
  } else {
    roles.value.push({ id: Date.now(), ...form, status: formStatus.value })
  }
  showModal.value = false
}

function handleSearch() {}
function handleReset() { searchName.value = '' }

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
  outline: 'none', width: '100%', boxSizing: 'border-box',
}
</script>

<template>
  <div style="padding:24px;background:#F0F2F8;min-height:100%;">
    <!-- Search card -->
    <div style="background:#fff;border-radius:16px;padding:16px 24px;margin-bottom:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);">
      <div style="display:flex;align-items:center;gap:10px;justify-content:space-between;">
        <div style="display:flex;gap:10px;">
          <div style="position:relative;">
            <el-icon style="position:absolute;left:11px;top:50%;transform:translateY(-50%);color:#94A3B8;font-size:13px;"><Search /></el-icon>
            <input v-model="searchName" placeholder="角色名称/编码"
              :style="{...fieldStyle, width:'220px', paddingLeft:'32px'}" />
          </div>
          <button @click="handleSearch" style="height:36px;padding:0 16px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;border:none;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;font-weight:500;box-shadow:0 3px 10px rgba(102,126,234,0.35);">
            <el-icon style="font-size:13px;"><Search /></el-icon>搜索
          </button>
          <button @click="handleReset" style="height:36px;padding:0 16px;background:#F8FAFC;color:#64748B;border:1px solid #E2E8F0;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;">
            <el-icon style="font-size:13px;"><RefreshRight /></el-icon>重置
          </button>
        </div>
        <button @click="openAdd" style="height:36px;padding:0 18px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;border:none;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:6px;font-weight:500;box-shadow:0 3px 10px rgba(102,126,234,0.35);flex-shrink:0;">
          <el-icon style="font-size:15px;"><Plus /></el-icon>新增角色
        </button>
      </div>
    </div>

    <div style="display:flex;gap:16px;align-items:flex-start;">
      <!-- Role table - fixed width -->
      <div style="flex:0 0 520px;min-width:0;overflow:hidden;">
        <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);overflow:hidden;">
          <div style="overflow-x:auto;">
            <table style="width:100%;border-collapse:collapse;min-width:520px;">
              <thead>
                <tr>
                  <th :style="thSt">角色名称</th>
                  <th :style="thSt">角色编码</th>
                  <th :style="thSt">描述</th>
                  <th :style="thSt">排序</th>
                  <th :style="thSt">状态</th>
                  <th :style="thSt">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in filtered" :key="r.id"
                  @click="selectRole(r)"
                  style="cursor:pointer;transition:background 0.15s;"
                  :style="{background: selectedRole?.id === r.id ? 'rgba(102,126,234,0.06)' : '#fff'}"
                  @mouseenter="e => { if (selectedRole?.id !== r.id) e.currentTarget.style.background = '#FAFBFF' }"
                  @mouseleave="e => { if (selectedRole?.id !== r.id) e.currentTarget.style.background = '#fff' }"
                >
                  <td :style="tdSt">
                    <div style="display:flex;align-items:center;gap:8px;">
                      <div style="width:30px;height:30px;border-radius:8px;background:linear-gradient(135deg,#667EEA,#764BA2);display:flex;align-items:center;justify-content:center;color:#fff;flex-shrink:0;">
                        <el-icon size="13"><Avatar /></el-icon>
                      </div>
                      <span style="font-weight:600;color:#1E293B;">{{ r.name }}</span>
                    </div>
                  </td>
                  <td :style="{...tdSt, fontFamily:'monospace', color:'#667EEA', fontSize:'12px'}">
                    <span style="background:#EEF2FF;padding:2px 8px;border-radius:6px;">{{ r.code }}</span>
                  </td>
                  <td :style="{...tdSt, color:'#475569', maxWidth:'160px', overflow:'hidden', textOverflow:'ellipsis'}">{{ r.desc }}</td>
                  <td :style="{...tdSt, color:'#94A3B8'}">{{ r.sort }}</td>
                  <td :style="tdSt">
                    <el-switch :model-value="r.status" size="small" @change="roles = roles.map(x => x.id === r.id ? {...x, status: !x.status} : x)" />
                  </td>
                  <td :style="tdSt" @click.stop>
                    <div style="display:flex;gap:6px;">
                      <button @click="openEdit(r)" style="height:28px;padding:0 10px;background:#EEF2FF;color:#667EEA;border:none;border-radius:7px;cursor:pointer;font-size:12px;display:flex;align-items:center;gap:3px;font-weight:500;">
                        <el-icon style="font-size:11px;"><Edit /></el-icon>编辑
                      </button>
                      <button @click="handleDelete(r.id)" style="height:28px;padding:0 10px;background:#FFF1F2;color:#F43F5E;border:none;border-radius:7px;cursor:pointer;font-size:12px;display:flex;align-items:center;gap:3px;font-weight:500;">
                        <el-icon style="font-size:11px;"><Delete /></el-icon>删除
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Permission tree - takes remaining space -->
      <div style="flex:1;min-width:300px;background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);overflow:hidden;display:flex;flex-direction:column;">
        <div style="padding:14px 20px;border-bottom:1px solid #F1F5F9;display:flex;align-items:center;justify-content:space-between;">
          <span style="font-size:14px;font-weight:600;color:#1E293B;">
            权限分配{{ selectedRole ? ` — ${selectedRole.name}` : '' }}
          </span>
          <span v-if="selectedRole" style="font-size:12px;padding:2px 10px;border-radius:20px;background:#EEF2FF;color:#667EEA;font-weight:500;">{{ perms.size }} 个权限</span>
        </div>
        <div style="flex:1;padding:12px 20px;overflow-y:auto;">
          <template v-if="!selectedRole">
            <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;height:200px;color:#94A3B8;gap:12px;">
              <el-icon size="40" style="opacity:0.3;"><Avatar /></el-icon>
              <span style="font-size:14px;">请选择左侧角色</span>
            </div>
          </template>
          <template v-else>
            <div v-for="row in flatPermNodes" :key="row.node.id"
              style="display:flex;align-items:center;gap:6px;padding:6px 0;"
              :style="{paddingLeft: `${row.depth * 20}px`}">
              <input type="checkbox"
                :checked="isChecked(row.node.id)"
                @change="togglePerm(row.node.id)"
                :ref="el => { if (el) { const someChild = row.node.children?.some(c => isChecked(c.id) || c.children?.some(cc => isChecked(cc.id))); el.indeterminate = !isChecked(row.node.id) && !!someChild } }"
                style="accent-color:#667EEA;width:14px;height:14px;cursor:pointer;flex-shrink:0;" />
              <button v-if="row.node.children?.length"
                @click="toggleExpand(row.node.id)"
                style="background:none;border:none;cursor:pointer;color:#94A3B8;padding:0;display:flex;flex-shrink:0;">
                <el-icon size="12">
                  <component :is="expandedNodes.has(row.node.id) ? 'ArrowDown' : 'ArrowRight'" />
                </el-icon>
              </button>
              <span v-else style="width:12px;display:inline-block;" />
              <span :style="{fontSize:'13px',color:row.depth===0?'#1E293B':'#475569',fontWeight:row.depth===0?600:400}">{{ row.node.label }}</span>
              <span v-if="!row.node.children?.length" style="font-size:11px;padding:1px 8px;border-radius:20px;background:#EEF2FF;color:#667EEA;font-weight:500;">按钮</span>
            </div>
          </template>
        </div>
        <div v-if="selectedRole" style="padding:14px 20px;border-top:1px solid #F1F5F9;">
          <button @click="() => {}" style="height:36px;padding:0 20px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;border:none;border-radius:10px;cursor:pointer;font-size:13px;font-weight:600;box-shadow:0 3px 10px rgba(102,126,234,0.35);">保存权限</button>
        </div>
      </div>
    </div>

    <!-- Add/Edit Modal -->
    <el-dialog v-model="showModal" :title="editRole ? '编辑角色' : '新增角色'" width="480px" :close-on-click-modal="false">
      <div style="margin-bottom:14px;">
        <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">角色名称 <span style="color:#F43F5E;">*</span></div>
        <input v-model="form.name" placeholder="请输入角色名称" :style="fieldStyle" />
      </div>
      <div style="margin-bottom:14px;">
        <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">角色编码 <span style="color:#F43F5E;">*</span></div>
        <input v-model="form.code" placeholder="如：OPS_ADMIN（大写字母+下划线）" :style="{...fieldStyle, fontFamily:'monospace'}" />
      </div>
      <div style="margin-bottom:14px;">
        <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">描述</div>
        <textarea v-model="form.desc" placeholder="角色描述" rows="3"
          :style="{...fieldStyle, height:'auto', resize:'vertical', padding:'8px 12px', lineHeight:'1.5'}" />
      </div>
      <div style="display:flex;gap:16px;">
        <div style="flex:1;margin-bottom:14px;">
          <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">排序</div>
          <input v-model.number="form.sort" type="number" :style="fieldStyle" />
        </div>
        <div style="flex:1;margin-bottom:14px;">
          <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">状态</div>
          <div style="display:flex;align-items:center;gap:8px;margin-top:6px;">
            <el-switch v-model="formStatus" size="small" />
            <span :style="{fontSize:'13px',color:formStatus?'#11998E':'#94A3B8',fontWeight:500}">{{ formStatus ? '启用' : '禁用' }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div style="display:flex;justify-content:flex-end;gap:10px;padding-top:16px;border-top:1px solid #F1F5F9;margin-top:8px;">
          <button @click="showModal = false" style="height:38px;padding:0 20px;border:1px solid #E2E8F0;border-radius:10px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;">取消</button>
          <button @click="handleSave" style="height:38px;padding:0 24px;border:none;border-radius:10px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;cursor:pointer;font-size:14px;font-weight:600;box-shadow:0 4px 12px rgba(102,126,234,0.4);">确定</button>
        </div>
      </template>
    </el-dialog>

    <!-- Delete Confirm — custom overlay -->
    <div v-if="deleteId !== null"
      style="position:fixed;inset:0;background:rgba(15,23,42,0.5);display:flex;align-items:center;justify-content:center;z-index:1000;"
      @click="deleteId = null">
      <div style="background:#fff;border-radius:16px;width:380px;max-width:90vw;padding:32px 28px;box-shadow:0 16px 48px rgba(244,63,94,0.14),0 4px 16px rgba(0,0,0,0.08);text-align:center;"
        @click.stop>
        <div style="width:64px;height:64px;border-radius:50%;background:#FFF1F2;display:flex;align-items:center;justify-content:center;margin:0 auto 16px;">
          <el-icon style="font-size:28px;color:#F43F5E;"><WarningFilled /></el-icon>
        </div>
        <div style="font-size:17px;font-weight:700;color:#1E293B;margin-bottom:8px;">确认删除</div>
        <div style="font-size:14px;color:#64748B;line-height:1.6;margin-bottom:24px;">
          此操作不可逆，删除后该角色数据将无法恢复。<br />确定要继续吗？
        </div>
        <div style="display:flex;gap:10px;justify-content:center;">
          <button @click="deleteId = null" style="height:40px;padding:0 24px;border:1px solid #E2E8F0;border-radius:10px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;font-weight:500;">取消</button>
          <button @click="confirmDelete" style="height:40px;padding:0 28px;border:none;border-radius:10px;background:linear-gradient(135deg,#F43F5E,#E11D48);color:#fff;cursor:pointer;font-size:14px;font-weight:600;box-shadow:0 4px 12px rgba(244,63,94,0.35);">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>
