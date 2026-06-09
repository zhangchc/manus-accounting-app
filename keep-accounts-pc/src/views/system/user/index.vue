<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit, Delete, Setting, RefreshRight, WarningFilled } from '@element-plus/icons-vue'
import { getUserList, createUser, updateUser, deleteUser, getUserRoles, assignUserRoles } from '@/api/user'
import { getRoleList } from '@/api/role'

const roleColorMap = [
  { color: '#667EEA', bg: '#EEF2FF' },
  { color: '#11998E', bg: '#F0FDF4' },
  { color: '#F7971E', bg: '#FFF7ED' },
  { color: '#F953C6', bg: '#FDF2F9' },
  { color: '#A855F7', bg: '#FDF4FF' },
  { color: '#06B6D4', bg: '#ECFEFF' },
  { color: '#F43F5E', bg: '#FFF1F2' },
]

const avatarGradients = [
  'linear-gradient(135deg,#667EEA,#764BA2)',
  'linear-gradient(135deg,#11998E,#38EF7D)',
  'linear-gradient(135deg,#F7971E,#FFD200)',
  'linear-gradient(135deg,#F953C6,#B91D73)',
  'linear-gradient(135deg,#A855F7,#6366F1)',
  'linear-gradient(135deg,#06B6D4,#3B82F6)',
  'linear-gradient(135deg,#F43F5E,#FB923C)',
]

const users = ref([])
const allRoles = ref([])

const searchUser = ref('')
const searchStatus = ref('')
const page = ref(1)
const pageSize = ref(10)

const showModal = ref(false)
const editUser = ref(null)
const showRoleModal = ref(false)
const currentRoleUser = ref(null)
const selectedRoleIds = ref([])
const formStatus = ref(true)
const deleteId = ref(null)
const saving = ref(false)
const form = reactive({ username: '', nickname: '', password: '', email: '', phone: '' })

const filtered = computed(() => {
  let list = users.value
  if (searchUser.value) {
    const kw = searchUser.value.toLowerCase()
    list = list.filter(u => u.username.toLowerCase().includes(kw) || u.nickname.toLowerCase().includes(kw))
  }
  if (searchStatus.value) {
    list = list.filter(u => u.status === (searchStatus.value === '1'))
  }
  return list
})

const paged = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filtered.value.slice(start, start + pageSize.value)
})

function getRoleColor(idx) {
  return roleColorMap[idx % roleColorMap.length]
}

async function loadUsers() {
  try {
    users.value = await getUserList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载用户列表失败')
  }
}

async function loadRoles() {
  try {
    allRoles.value = await getRoleList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载角色列表失败')
  }
}

onMounted(() => {
  loadUsers()
  loadRoles()
})

function openAdd() {
  editUser.value = null
  formStatus.value = true
  Object.assign(form, { username: '', nickname: '', password: '', email: '', phone: '' })
  showModal.value = true
}

function openEdit(u) {
  editUser.value = u
  formStatus.value = u.status
  Object.assign(form, { username: u.username, nickname: u.nickname, password: '', email: u.email || '', phone: u.phone || '' })
  showModal.value = true
}

async function openRoles(u) {
  currentRoleUser.value = u
  try {
    const ids = await getUserRoles(u.id)
    selectedRoleIds.value = [...ids]
  } catch (e) {
    selectedRoleIds.value = []
  }
  showRoleModal.value = true
}

function handleDelete(id) { deleteId.value = id }

async function confirmDelete() {
  if (deleteId.value !== null) {
    try {
      await deleteUser(deleteId.value)
      ElMessage.success('删除成功')
      deleteId.value = null
      await loadUsers()
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '删除失败')
    }
  }
}

async function toggleStatus(u) {
  try {
    await updateUser({ id: u.id, username: u.username, nickname: u.nickname, email: u.email, phone: u.phone, status: !u.status })
    u.status = !u.status
    ElMessage.success(u.status ? '已启用' : '已禁用')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

async function handleSave() {
  saving.value = true
  try {
    const data = {
      username: form.username,
      nickname: form.nickname,
      email: form.email,
      phone: form.phone,
      status: formStatus.value,
    }
    if (form.password) {
      data.password = form.password
    }
    if (editUser.value) {
      data.id = editUser.value.id
      await updateUser(data)
      ElMessage.success('编辑成功')
    } else {
      await createUser(data)
      ElMessage.success('新增成功')
    }
    showModal.value = false
    await loadUsers()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function saveRoles() {
  try {
    await assignUserRoles(currentRoleUser.value.id, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    showRoleModal.value = false
    await loadUsers()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '角色分配失败')
  }
}

function handleSearch() { page.value = 1 }
function handleReset() { searchUser.value = ''; searchStatus.value = ''; page.value = 1 }

const thStyle = {
  padding: '12px 20px', fontSize: '12px', color: '#94A3B8',
  fontWeight: '600', textAlign: 'left', whiteSpace: 'nowrap',
  background: '#F8FAFC', borderBottom: '1px solid #F1F5F9',
  letterSpacing: '0.03em',
}
const tdStyle = {
  padding: '14px 20px', fontSize: '13px', color: '#334155',
  borderBottom: '1px solid #F8FAFC', whiteSpace: 'nowrap',
}
const fieldStyle = {
  height: '36px', padding: '0 12px', borderRadius: '10px',
  background: '#F8FAFC', border: '1px solid #E2E8F0',
  fontSize: '13px', color: '#334155', outline: 'none', width: '100%', boxSizing: 'border-box',
}
</script>

<template>
  <div style="padding:24px;background:#F0F2F8;min-height:100%;">
    <!-- Search card -->
    <div style="background:#fff;border-radius:16px;padding:16px 24px;margin-bottom:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);">
      <div style="display:flex;align-items:center;gap:10px;justify-content:space-between;">
        <div style="display:flex;gap:10px;flex-wrap:wrap;">
          <div style="position:relative;">
            <el-icon style="position:absolute;left:11px;top:50%;transform:translateY(-50%);color:#94A3B8;font-size:13px;"><Search /></el-icon>
            <input v-model="searchUser" placeholder="用户名/昵称"
              :style="{...fieldStyle, width:'240px', paddingLeft:'32px'}" />
          </div>
          <select v-model="searchStatus" :style="{...fieldStyle, width:'130px', cursor:'pointer'}">
            <option value="">全部状态</option>
            <option value="1">启用</option>
            <option value="0">禁用</option>
          </select>
          <button @click="handleSearch" style="height:36px;padding:0 16px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;border:none;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;font-weight:500;box-shadow:0 3px 10px rgba(102,126,234,0.35);">
            <el-icon style="font-size:13px;"><Search /></el-icon>搜索
          </button>
          <button @click="handleReset" style="height:36px;padding:0 16px;background:#F8FAFC;color:#64748B;border:1px solid #E2E8F0;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:5px;">
            <el-icon style="font-size:13px;"><RefreshRight /></el-icon>重置
          </button>
        </div>
        <button v-permission="'sys:admin:create'" @click="openAdd" style="height:36px;padding:0 18px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;border:none;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:6px;font-weight:500;box-shadow:0 3px 10px rgba(102,126,234,0.35);flex-shrink:0;">
          <el-icon style="font-size:15px;"><Plus /></el-icon>新增用户
        </button>
      </div>
    </div>

    <!-- Table card -->
    <div style="background:#fff;border-radius:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);overflow:hidden;">
      <div style="overflow-x:auto;">
        <table style="width:100%;border-collapse:collapse;min-width:900px;">
          <thead>
            <tr>
              <th :style="thStyle">序号</th>
              <th :style="thStyle">用户名</th>
              <th :style="thStyle">昵称</th>
              <th :style="thStyle">角色</th>
              <th :style="thStyle">状态</th>
              <th :style="thStyle">最后登录时间</th>
              <th :style="thStyle">创建时间</th>
              <th :style="thStyle">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(u, i) in paged" :key="u.id"
              style="transition:background 0.15s;"
              @mouseenter="e => e.currentTarget.style.background = '#FAFBFF'"
              @mouseleave="e => e.currentTarget.style.background = '#fff'"
            >
              <td :style="{...tdStyle, color:'#CBD5E1', width:'60px'}">{{ (page-1) * pageSize + i + 1 }}</td>
              <td :style="tdStyle">
                <div style="display:flex;align-items:center;gap:10px;">
                  <div :style="{width:'32px',height:'32px',borderRadius:'10px',background:avatarGradients[u.id % avatarGradients.length],display:'flex',alignItems:'center',justifyContent:'center',color:'#fff',fontSize:'12px',fontWeight:700,flexShrink:0}">
                    {{ u.nickname[0] }}
                  </div>
                  <span style="font-weight:600;color:#1E293B;">{{ u.username }}</span>
                </div>
              </td>
              <td :style="{...tdStyle, color:'#475569'}">{{ u.nickname }}</td>
              <td :style="tdStyle">
                <div style="display:flex;gap:4px;flex-wrap:wrap;">
                  <template v-if="u.roleNames && u.roleNames.length">
                    <span v-for="(rn, ri) in u.roleNames.slice(0, 2)" :key="ri"
                      :style="{fontSize:'12px',padding:'3px 10px',borderRadius:'20px',background:getRoleColor(ri).bg,color:getRoleColor(ri).color,fontWeight:600}">
                      {{ rn }}
                    </span>
                    <span v-if="u.roleNames.length > 2"
                      style="fontSize:'11px',color:'#94A3B8',padding:'3px 6px'">
                      +{{ u.roleNames.length - 2 }}
                    </span>
                  </template>
                  <span v-else style="fontSize:'12px',color:'#CBD5E1'">未分配</span>
                </div>
              </td>
              <td :style="tdStyle">
                <el-switch :model-value="u.status" size="small" @change="toggleStatus(u)" />
              </td>
              <td :style="{...tdStyle, color:'#94A3B8', fontSize:'12px'}">{{ u.lastLogin || '-' }}</td>
              <td :style="{...tdStyle, color:'#CBD5E1', fontSize:'12px'}">{{ u.createdAt || '-' }}</td>
              <td :style="tdStyle">
                <div style="display:flex;gap:6px;">
                  <button v-permission="'sys:admin:edit'" @click="openEdit(u)" style="height:28px;padding:0 10px;background:#EEF2FF;color:#667EEA;border:none;border-radius:7px;cursor:pointer;font-size:12px;display:flex;align-items:center;gap:3px;font-weight:500;">
                    <el-icon style="font-size:11px;"><Edit /></el-icon>编辑
                  </button>
                  <button v-permission="'sys:admin:assign-role'" @click="openRoles(u)" style="height:28px;padding:0 10px;background:#F0FDF4;color:#11998E;border:none;border-radius:7px;cursor:pointer;font-size:12px;display:flex;align-items:center;gap:3px;font-weight:500;">
                    <el-icon style="font-size:11px;"><Setting /></el-icon>分配角色
                  </button>
                  <button v-permission="'sys:admin:delete'" @click="handleDelete(u.id)" style="height:28px;padding:0 10px;background:#FFF1F2;color:#F43F5E;border:none;border-radius:7px;cursor:pointer;font-size:12px;display:flex;align-items:center;gap:3px;font-weight:500;">
                    <el-icon style="font-size:11px;"><Delete /></el-icon>删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <!-- Pagination -->
      <div style="display:flex;justify-content:flex-end;padding:12px 16px;align-items:center;gap:6px;">
        <span style="font-size:13px;color:#94A3B8;margin-right:8px;">共 {{ filtered.length }} 条</span>
        <button :disabled="page <= 1" @click="page--" style="width:32px;height:32px;display:flex;align-items:center;justify-content:center;border:1px solid #E2E8F0;border-radius:8px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;"
          :style="{opacity: page <= 1 ? 0.4 : 1, cursor: page <= 1 ? 'default' : 'pointer'}">‹</button>
        <button v-for="p in Math.min(Math.ceil(filtered.length / pageSize), 5)" :key="p" @click="page = p"
          :style="{width:'32px',height:'32px',display:'flex',alignItems:'center',justifyContent:'center',border:'none',borderRadius:'8px',cursor:'pointer',fontSize:'13px',fontWeight:500,
            background: page === p ? 'linear-gradient(135deg,#667EEA,#764BA2)' : 'transparent',
            color: page === p ? '#fff' : '#64748B'}">{{ p }}</button>
        <button :disabled="page >= Math.ceil(filtered.length / pageSize)" @click="page++" style="width:32px;height:32px;display:flex;align-items:center;justify-content:center;border:1px solid #E2E8F0;border-radius:8px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;"
          :style="{opacity: page >= Math.ceil(filtered.length / pageSize) ? 0.4 : 1, cursor: page >= Math.ceil(filtered.length / pageSize) ? 'default' : 'pointer'}">›</button>
      </div>
    </div>

    <!-- Add/Edit Modal -->
    <el-dialog v-model="showModal" :title="editUser ? '编辑用户' : '新增用户'" width="520px" :close-on-click-modal="false">
      <div style="display:flex;flex-wrap:wrap;gap:0 16px;">
        <div style="flex:1 1 calc(50% - 8px);min-width:200px;margin-bottom:14px;">
          <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">用户名 <span style="color:#F43F5E;">*</span></div>
          <input v-model="form.username" placeholder="请输入用户名" :style="fieldStyle" />
        </div>
        <div style="flex:1 1 calc(50% - 8px);min-width:200px;margin-bottom:14px;">
          <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">昵称 <span style="color:#F43F5E;">*</span></div>
          <input v-model="form.nickname" placeholder="请输入昵称" :style="fieldStyle" />
        </div>
        <div style="flex:1 1 calc(50% - 8px);min-width:200px;margin-bottom:14px;">
          <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">{{ editUser ? '密码（留空不修改）' : '密码' }} <span v-if="!editUser" style="color:#F43F5E;">*</span></div>
          <input v-model="form.password" type="password" :placeholder="editUser ? '留空则不修改' : '请设置密码'" :style="fieldStyle" />
        </div>
        <div style="flex:1 1 calc(50% - 8px);min-width:200px;margin-bottom:14px;">
          <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">邮箱</div>
          <input v-model="form.email" type="email" placeholder="请输入邮箱地址" :style="fieldStyle" />
        </div>
        <div style="flex:1 1 calc(50% - 8px);min-width:200px;margin-bottom:14px;">
          <div style="font-size:13px;color:#475569;margin-bottom:6px;font-weight:500;">手机号</div>
          <input v-model="form.phone" placeholder="请输入手机号" :style="fieldStyle" />
        </div>
        <div style="flex:1 1 calc(50% - 8px);min-width:200px;margin-bottom:14px;">
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
          <button @click="handleSave" :disabled="saving" style="height:38px;padding:0 24px;border:none;border-radius:10px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;cursor:pointer;font-size:14px;font-weight:600;box-shadow:0 4px 12px rgba(102,126,234,0.4);">{{ saving ? '保存中...' : '确定' }}</button>
        </div>
      </template>
    </el-dialog>

    <!-- Role Assignment Modal -->
    <el-dialog v-model="showRoleModal" title="分配角色" width="480px" :close-on-click-modal="false">
      <div style="margin-bottom:4px;">
        <label v-for="r in allRoles" :key="r.id"
          style="display:flex;align-items:center;gap:12px;padding:12px 0;cursor:pointer;border-bottom:1px solid #F8FAFC;">
          <input type="checkbox" :checked="selectedRoleIds.includes(r.id)"
            @change="e => { if (e.target.checked) selectedRoleIds.push(r.id); else selectedRoleIds = selectedRoleIds.filter(x => x !== r.id) }"
            style="accent-color:#667EEA;width:16px;height:16px;cursor:pointer;flex-shrink:0;" />
          <div>
            <div style="font-size:14px;color:#1E293B;font-weight:500;">{{ r.name }}</div>
            <div style="font-size:12px;color:#94A3B8;margin-top:2px;">{{ r.desc || r.code }}</div>
          </div>
        </label>
      </div>
      <template #footer>
        <div style="display:flex;justify-content:flex-end;gap:10px;padding-top:16px;border-top:1px solid #F1F5F9;margin-top:8px;">
          <button @click="showRoleModal = false" style="height:38px;padding:0 20px;border:1px solid #E2E8F0;border-radius:10px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;">取消</button>
          <button @click="saveRoles" style="height:38px;padding:0 24px;border:none;border-radius:10px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;cursor:pointer;font-size:14px;font-weight:600;box-shadow:0 4px 12px rgba(102,126,234,0.4);">确定</button>
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
          此操作不可逆，删除后该用户数据将无法恢复。<br />确定要继续吗？
        </div>
        <div style="display:flex;gap:10px;justify-content:center;">
          <button @click="deleteId = null" style="height:40px;padding:0 24px;border:1px solid #E2E8F0;border-radius:10px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;font-weight:500;">取消</button>
          <button @click="confirmDelete" style="height:40px;padding:0 28px;border:none;border-radius:10px;background:linear-gradient(135deg,#F43F5E,#E11D48);color:#fff;cursor:pointer;font-size:14px;font-weight:600;box-shadow:0 4px 12px rgba(244,63,94,0.35);">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>
