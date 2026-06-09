<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/menu'

const typeConfig = {
  dir:  { label: '目录', bg: '#EEF2FF', color: '#667EEA' },
  menu: { label: '菜单', bg: '#F0FDF4', color: '#11998E' },
  btn:  { label: '按钮', bg: '#FFF7ED', color: '#F7971E' },
}

const mockMenus = ref([])

onMounted(async () => {
  try {
    const data = await getMenuTree()
    if (data && data.length) mockMenus.value = data
  } catch (e) {
    ElMessage.error('加载菜单数据失败')
  }
})

const expanded = ref(new Set([1, 2, 11, 12]))
const showModal = ref(false)
const editItem = ref(null)
const menuType = ref('menu')
const formStatus = ref(true)
const deleteId = ref(null)
const lockType = ref(false)
const lockParent = ref(false)
const form = reactive({ parentId: '', name: '', icon: '', path: '', component: '', permission: '', sort: 1 })

const parentOptions = computed(() => {
  const result = []
  function collect(items) {
    for (const item of items) {
      if (item.type !== 'btn') result.push({ id: item.id, name: item.name })
      if (item.children) collect(item.children)
    }
  }
  collect(mockMenus.value)
  return result
})

// Flat list of visible rows
const flatRows = computed(() => {
  const result = []
  function walk(items, depth) {
    for (const item of items) {
      result.push({ item, depth })
      if (item.children?.length && expanded.value.has(item.id)) {
        walk(item.children, depth + 1)
      }
    }
  }
  walk(mockMenus.value, 0)
  return result
})

function toggle(id) {
  const next = new Set(expanded.value)
  if (next.has(id)) next.delete(id); else next.add(id)
  expanded.value = next
}

function expandAll() {
  const ids = new Set()
  const collect = (items) => items.forEach(i => { ids.add(i.id); if (i.children) collect(i.children) })
  collect(mockMenus.value)
  expanded.value = ids
}

function collapseAll() { expanded.value = new Set() }

function openAdd() {
  editItem.value = null; lockType.value = false; lockParent.value = false
  menuType.value = 'dir'; formStatus.value = true
  Object.assign(form, { parentId: '', name: '', icon: '', path: '', component: '', permission: '', sort: 1 })
  showModal.value = true
}

function openAddChild(parent) {
  editItem.value = null; lockType.value = true; lockParent.value = true
  menuType.value = parent.type === 'dir' ? 'menu' : 'btn'
  formStatus.value = true
  Object.assign(form, { parentId: parent.id, name: '', icon: '', path: '', component: '', permission: '', sort: 1 })
  showModal.value = true
}

function openEdit(item) {
  editItem.value = item; lockType.value = true; lockParent.value = true
  menuType.value = item.type; formStatus.value = item.status
  Object.assign(form, { parentId: item.parentId || '', name: item.name, icon: item.icon, path: item.path, component: item.component, permission: item.permission, sort: item.sort })
  showModal.value = true
}

function handleDelete(id) { deleteId.value = id }

async function refreshTree(extraExpandId) {
  const saved = new Set(expanded.value)
  if (extraExpandId) saved.add(extraExpandId)
  const data = await getMenuTree()
  if (data && data.length) {
    mockMenus.value = data
    const newIds = new Set()
    const collect = (children) => children.forEach(i => { newIds.add(i.id); if (i.children) collect(i.children) })
    collect(data)
    const restored = new Set()
    for (const id of saved) { if (newIds.has(id)) restored.add(id) }
    expanded.value = restored
  }
}

async function handleStatusChange(item, newStatus) {
  const oldStatus = item.status
  item.status = newStatus
  try {
    await updateMenu({
      id: item.id,
      parentId: item.parentId || 0,
      name: item.name,
      icon: item.icon,
      type: item.type,
      path: item.path,
      component: item.component,
      permission: item.permission,
      sort: item.sort,
      status: newStatus,
    })
  } catch (e) {
    item.status = oldStatus
    ElMessage.error('状态更新失败')
  }
}

async function confirmDelete() {
  if (deleteId.value === null) return
  try {
    await deleteMenu(deleteId.value)
    deleteId.value = null
    await refreshTree()
  } catch (e) {
    ElMessage.error(e?.data?.message || '删除失败')
  }
}

async function handleSave() {
  try {
    if (editItem.value) {
      await updateMenu({
        id: editItem.value.id,
        parentId: form.parentId || 0,
        name: form.name,
        icon: form.icon,
        type: menuType.value,
        path: form.path,
        component: form.component,
        permission: form.permission,
        sort: form.sort,
        status: formStatus.value,
      })
    } else {
      await createMenu({
        parentId: form.parentId || 0,
        name: form.name,
        icon: form.icon,
        type: menuType.value,
        path: form.path,
        component: form.component,
        permission: form.permission,
        sort: form.sort,
        status: formStatus.value,
      })
    }
    showModal.value = false
    await refreshTree(form.parentId || 0)
  } catch (e) {
    ElMessage.error(e?.data?.message || '保存失败')
  }
}

const thSt = {
  padding: '12px 20px', fontSize: '12px', color: '#94A3B8', fontWeight: '600',
  textAlign: 'left', whiteSpace: 'nowrap', background: '#F8FAFC',
  borderBottom: '1px solid #F1F5F9', letterSpacing: '0.03em',
}
const tdSt = {
  padding: '13px 20px', fontSize: '13px', color: '#334155',
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
    <!-- Toolbar -->
    <div style="background:#fff;border-radius:16px;padding:14px 24px;margin-bottom:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);display:flex;align-items:center;justify-content:space-between;">
      <div style="display:flex;gap:8px;">
        <button @click="expandAll" style="height:32px;padding:0 14px;background:#EEF2FF;color:#667EEA;border:none;border-radius:8px;cursor:pointer;font-size:13px;font-weight:500;">展开全部</button>
        <button @click="collapseAll" style="height:32px;padding:0 14px;background:#F8FAFC;color:#64748B;border:1px solid #E2E8F0;border-radius:8px;cursor:pointer;font-size:13px;">折叠全部</button>
      </div>
      <button @click="openAdd" style="height:36px;padding:0 18px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;border:none;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:6px;font-weight:500;box-shadow:0 3px 10px rgba(102,126,234,0.35);">
        <el-icon style="font-size:15px;"><Plus /></el-icon>新增菜单
      </button>
    </div>

    <!-- Table card -->
    <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);overflow:hidden;">
      <div style="overflow-x:auto;">
        <table style="width:100%;border-collapse:collapse;min-width:860px;">
          <thead>
            <tr>
              <th :style="thSt">菜单名称</th>
              <th :style="thSt">图标</th>
              <th :style="thSt">类型</th>
              <th :style="thSt">路由路径</th>
              <th :style="thSt">权限标识</th>
              <th :style="thSt">排序</th>
              <th :style="thSt">状态</th>
              <th :style="thSt">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="{ item, depth } in flatRows" :key="item.id"
              style="transition:background 0.15s;"
              @mouseenter="e => e.currentTarget.style.background = '#FAFBFF'"
              @mouseleave="e => e.currentTarget.style.background = '#fff'"
            >
              <!-- Menu name with depth indent -->
              <td :style="{...tdSt, paddingLeft: `${20 + depth * 24}px`}">
                <div style="display:flex;align-items:center;gap:6px;">
                  <button v-if="item.children?.length"
                    @click="toggle(item.id)"
                    style="background:none;border:none;cursor:pointer;color:#94A3B8;padding:0;display:flex;flex-shrink:0;">
                    <el-icon size="14">
                      <component :is="expanded.has(item.id) ? 'ArrowDown' : 'ArrowRight'" />
                    </el-icon>
                  </button>
                  <span v-else style="width:14px;display:inline-block;flex-shrink:0;" />
                  <el-icon size="13" style="color:#94A3B8;flex-shrink:0;"><Operation /></el-icon>
                  <span :style="{fontWeight:depth===0?600:400,color:'#1E293B'}">{{ item.name }}</span>
                </div>
              </td>
              <!-- Icon -->
              <td :style="{...tdSt, color:'#94A3B8', fontSize:'12px'}">{{ item.icon || '—' }}</td>
              <!-- Type -->
              <td :style="tdSt">
                <span :style="{fontSize:'12px',padding:'2px 10px',borderRadius:'20px',background:typeConfig[item.type].bg,color:typeConfig[item.type].color,fontWeight:500}">
                  {{ typeConfig[item.type].label }}
                </span>
              </td>
              <!-- Path -->
              <td :style="{...tdSt, fontFamily:'monospace', fontSize:'12px', color:'#667EEA'}">
                <span v-if="item.path" style="background:#EEF2FF;padding:2px 8px;border-radius:6px;">{{ item.path }}</span>
                <span v-else style="color:#CBD5E1;">—</span>
              </td>
              <!-- Permission -->
              <td :style="{...tdSt, fontFamily:'monospace', fontSize:'12px', color:'#475569'}">
                {{ item.permission || '—' }}
              </td>
              <!-- Sort -->
              <td :style="{...tdSt, color:'#94A3B8'}">{{ item.sort }}</td>
              <!-- Status -->
              <td :style="tdSt">
                <el-switch :model-value="item.status" size="small" @change="val => handleStatusChange(item, val)" />
              </td>
              <!-- Actions -->
              <td :style="tdSt">
                <div style="display:flex;gap:6px;">
                  <button v-if="item.type !== 'btn'" @click="openAddChild(item)" style="height:28px;padding:0 10px;background:#F0FDF4;color:#11998E;border:none;border-radius:7px;cursor:pointer;font-size:12px;font-weight:500;">+子级</button>
                  <button @click="openEdit(item)" style="height:28px;padding:0 10px;background:#EEF2FF;color:#667EEA;border:none;border-radius:7px;cursor:pointer;font-size:12px;display:flex;align-items:center;gap:3px;font-weight:500;">
                    <el-icon style="font-size:11px;"><Edit /></el-icon>编辑
                  </button>
                  <button @click="handleDelete(item.id)" style="height:28px;padding:0 10px;background:#FFF1F2;color:#F43F5E;border:none;border-radius:7px;cursor:pointer;font-size:12px;display:flex;align-items:center;gap:3px;font-weight:500;">
                    <el-icon style="font-size:11px;"><Delete /></el-icon>删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Add/Edit Modal -->
    <el-dialog v-model="showModal" :title="editItem ? '编辑菜单' : '新增菜单'" width="560px" :close-on-click-modal="false">
      <!-- Parent menu -->
      <div style="margin-bottom:14px;">
        <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">上级菜单</div>
        <select v-model="form.parentId" :disabled="lockParent"
          :style="{...fieldStyle, cursor: lockParent ? 'not-allowed' : 'pointer', opacity: lockParent ? 0.6 : 1}">
          <option value="">顶级菜单（无上级）</option>
          <option v-for="p in parentOptions" :key="p.id" :value="p.id">{{ p.name }}</option>
        </select>
      </div>
      <!-- Name + Type row -->
      <div style="display:flex;gap:16px;margin-bottom:4px;">
        <div style="flex:1;margin-bottom:14px;">
          <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">菜单名称 <span style="color:#F43F5E;">*</span></div>
          <input v-model="form.name" placeholder="请输入菜单名称" :style="fieldStyle" />
        </div>
        <div style="flex:1;margin-bottom:14px;">
          <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">菜单类型 <span style="color:#F43F5E;">*</span></div>
          <div style="display:flex;border:1px solid #E2E8F0;border-radius:10px;overflow:hidden;height:36px;"
            :style="{opacity: lockType ? 0.5 : 1}">
            <button v-for="t in ['dir','menu','btn']" :key="t"
              :disabled="lockType"
              @click="menuType = t"
              :style="{flex:1,border:'none',background:menuType===t?'linear-gradient(135deg,#667EEA,#764BA2)':'#F8FAFC',color:menuType===t?'#fff':'#64748B',cursor:lockType?'not-allowed':'pointer',fontSize:'13px',transition:'all 0.15s',fontWeight:menuType===t?600:400}">
              {{ typeConfig[t].label }}
            </button>
          </div>
        </div>
      </div>
      <!-- Path + Icon (for dir/menu) -->
      <div v-if="menuType !== 'btn'" style="display:flex;gap:16px;">
        <div style="flex:1;margin-bottom:14px;">
          <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">路由路径</div>
          <input v-model="form.path" placeholder="/system/xxx" :style="fieldStyle" />
        </div>
        <div style="flex:1;margin-bottom:14px;">
          <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">图标</div>
          <select v-model="form.icon" :style="{...fieldStyle, cursor:'pointer'}">
            <option value="">选择图标</option>
            <option v-for="ic in ['Settings','Users','Shield','Menu','Smartphone','BookOpen','Tag','FileText']" :key="ic" :value="ic">{{ ic }}</option>
          </select>
        </div>
      </div>
      <!-- Component path (for menu only) -->
      <div v-if="menuType === 'menu'" style="margin-bottom:14px;">
        <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">组件路径</div>
        <input v-model="form.component" placeholder="views/xxx/index" :style="{...fieldStyle, fontFamily:'monospace', fontSize:'12px'}" />
      </div>
      <!-- Permission (for btn only) -->
      <div v-if="menuType === 'btn'" style="margin-bottom:14px;">
        <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">权限标识</div>
        <input v-model="form.permission" placeholder="sys:user:create" :style="{...fieldStyle, fontFamily:'monospace', fontSize:'12px'}" />
      </div>
      <!-- Sort + Status row -->
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

    <!-- Delete Confirm -->
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
          此操作不可逆，删除后该菜单数据将无法恢复。<br />确定要继续吗？
        </div>
        <div style="display:flex;gap:10px;justify-content:center;">
          <button @click="deleteId = null" style="height:40px;padding:0 24px;border:1px solid #E2E8F0;border-radius:10px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;font-weight:500;">取消</button>
          <button @click="confirmDelete" style="height:40px;padding:0 28px;border:none;border-radius:10px;background:linear-gradient(135deg,#F43F5E,#E11D48);color:#fff;cursor:pointer;font-size:14px;font-weight:600;box-shadow:0 4px 12px rgba(244,63,94,0.35);">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>
