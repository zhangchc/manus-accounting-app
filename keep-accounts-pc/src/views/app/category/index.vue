<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Plus, Edit, Delete, WarningFilled, Close, CollectionTag, Search } from '@element-plus/icons-vue'
import { getAppCategoryList } from '@/api/app-category'
import { ElMessage } from 'element-plus'

const fieldStyle = { height: '36px', padding: '0 12px', borderRadius: '10px', background: '#F8FAFC', border: '1px solid #E2E8F0', fontSize: '13px', color: '#334155', outline: 'none', width: '100%', boxSizing: 'border-box' }

const tab = ref('expense')
const searchName = ref('')
const loading = ref(false)

const cats = ref({
  expense: [],
  income: []
})

function mapType(type) {
  return type === 1 ? 'expense' : 'income'
}

async function loadCategories() {
  loading.value = true
  try {
    const typeParam = tab.value === 'expense' ? 1 : 2
    const data = await getAppCategoryList({
      name: searchName.value || undefined,
      type: typeParam,
      page: 1,
      pageSize: 100
    })
    const list = (data.records || []).map(item => ({
      id: item.id,
      icon: item.icon,
      name: item.name,
      sort: item.sortOrder,
      type: mapType(item.type),
      isSystem: item.userId === 0
    }))
    cats.value[tab.value] = list
  } catch (e) {
    ElMessage.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadCategories()
}

watch(tab, () => {
  searchName.value = ''
  loadCategories()
})

onMounted(() => {
  loadCategories()
})

const iconOptions = ['🍜','🚇','🛒','🏠','💊','📚','🎮','👗','☕','🚗','✈️','🐾','💄','🏋️','🎁','💼','📈','🏡','💰','🎉','📦','💎','🍕','🍔','🍦','🎵','🎬','📱','💻','🔑','🧾']

const current = computed(() => cats.value[tab.value])

const showModal = ref(false)
const editCat = ref(null)
const hoveredId = ref(null)
const formIcon = ref('🍜')
const formType = ref('expense')
const formName = ref('')
const formSort = ref(1)
const deleteId = ref(null)

const deleteVisible = computed({
  get: () => deleteId.value !== null,
  set: (v) => { if (!v) deleteId.value = null }
})

function openAdd() {
  editCat.value = null
  formIcon.value = '🍜'
  formType.value = tab.value
  formName.value = ''
  formSort.value = current.value.length + 1
  showModal.value = true
}

function openEdit(c) {
  editCat.value = c
  formIcon.value = c.icon
  formType.value = c.type
  formName.value = c.name
  formSort.value = c.sort
  showModal.value = true
}

function handleDelete(id) {
  deleteId.value = id
}

function confirmDelete() {
  if (deleteId.value !== null) {
    cats.value[tab.value] = cats.value[tab.value].filter(c => c.id !== deleteId.value)
  }
  deleteId.value = null
}

function handleSave() {
  if (editCat.value) {
    const idx = cats.value[tab.value].findIndex(c => c.id === editCat.value.id)
    if (idx >= 0) {
      cats.value[tab.value][idx] = { ...cats.value[tab.value][idx], icon: formIcon.value, name: formName.value, sort: formSort.value }
    }
  } else {
    cats.value[tab.value].push({
      id: Date.now(),
      icon: formIcon.value,
      name: formName.value,
      sort: formSort.value,
      type: formType.value,
      isSystem: false
    })
  }
  showModal.value = false
}
</script>

<template>
  <div style="padding:24px;background:#F0F2F8;min-height:100%;">
    <!-- Tabs card -->
    <div style="background:#fff;border-radius:16px;padding:16px 24px;margin-bottom:16px;box-shadow:0 2px 16px rgba(0,0,0,0.06);">
      <div style="display:flex;align-items:center;justify-content:space-between;">
        <div style="display:flex;gap:4px;background:#F0F2F8;border-radius:10px;padding:4px;">
          <button v-for="opt in [['expense','支出分类'],['income','收入分类']]" :key="opt[0]"
            @click="tab = opt[0]"
            :style="{
              padding:'8px 24px',border:'none',borderRadius:'8px',cursor:'pointer',fontSize:'13px',fontWeight:500,transition:'all 0.2s',
              background: tab===opt[0] ? '#fff' : 'transparent',
              color: tab===opt[0] ? '#1E293B' : '#94A3B8',
              boxShadow: tab===opt[0] ? '0 2px 8px rgba(0,0,0,0.08)' : 'none',
            }">
            {{ opt[1] }}
            <span :style="{marginLeft:'6px',background: tab===opt[0] ? 'linear-gradient(135deg,#667EEA,#764BA2)' : '#E2E8F0',color:'#fff',borderRadius:'20px',padding:'1px 8px',fontSize:'11px',fontWeight:600}">
              {{ tab === opt[0] ? current.length : (opt[0] === 'expense' ? cats.expense.length : cats.income.length) }}
            </span>
          </button>
        </div>
        <div style="display:flex;align-items:center;gap:10px;">
          <div style="position:relative;">
            <input v-model="searchName" placeholder="搜索分类名称" :style="{...fieldStyle, width:'200px',paddingLeft:'36px'}" @keyup.enter="handleSearch" />
            <el-icon style="position:absolute;left:10px;top:50%;transform:'translateY(-50%)';font-size:14px;color:#94A3B8;"><Search /></el-icon>
          </div>
          <button @click="handleSearch" style="height:36px;padding:0 16px;background:#F0F2F8;color:#64748B;border:1px solid #E2E8F0;border-radius:10px;cursor:pointer;font-size:13px;">搜索</button>
          <button @click="openAdd" style="height:36px;padding:0 18px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;border:none;border-radius:10px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:6px;font-weight:500;box-shadow:0 3px 10px rgba(102,126,234,0.35);">
            <el-icon style="font-size:15px;"><Plus /></el-icon>新增分类
          </button>
        </div>
      </div>
    </div>

    <!-- Category grid or empty state -->
    <div v-if="current.length === 0" style="background:#fff;border-radius:16px;padding:80px 20px;text-align:center;box-shadow:0 2px 16px rgba(0,0,0,0.06);">
      <el-icon style="font-size:48px;opacity:0.2;margin-bottom:12px;display:block;margin:0 auto 12px;"><CollectionTag /></el-icon>
      <div style="font-size:14px;color:#94A3B8;">暂无分类，点击上方按钮新增</div>
    </div>
    <div v-else style="display:grid;grid-template-columns:repeat(auto-fill,minmax(160px,1fr));gap:14px;">
      <div v-for="cat in current" :key="cat.id"
        @mouseenter="hoveredId = cat.id"
        @mouseleave="hoveredId = null"
        :style="{
          background:'#fff',borderRadius:'16px',padding:'20px 16px',
          boxShadow: hoveredId===cat.id ? '0 8px 28px rgba(102,126,234,0.18)' : '0 2px 16px rgba(0,0,0,0.06)',
          transform: hoveredId===cat.id ? 'translateY(-3px)' : 'none',
          transition:'box-shadow 0.2s, transform 0.2s',cursor:'default',position:'relative',
          display:'flex',flexDirection:'column',alignItems:'center',gap:'8px'
        }">
        <div v-if="cat.isSystem" style="position:absolute;top:10px;right:10px;">
          <span style="font-size:10px;padding:2px 7px;border-radius:20px;background:#EEF2FF;color:#667EEA;font-weight:500;">系统</span>
        </div>
        <div style="width:52px;height:52px;border-radius:14px;background:#F0F2F8;display:flex;align-items:center;justify-content:center;font-size:24px;">
          {{ cat.icon }}
        </div>
        <div style="font-size:14px;font-weight:600;color:#1E293B;text-align:center;">{{ cat.name }}</div>
        <div style="font-size:11px;color:#94A3B8;">排序: {{ cat.sort }}</div>
        <div style="display:flex;gap:8px;margin-top:4px;opacity:0;pointer-events:none;transition:opacity 0.2s;"
          :style="hoveredId === cat.id ? { opacity: 1, pointerEvents: 'auto' } : {}">
          <button @click="openEdit(cat)" style="height:28px;padding:0 10px;background:#EEF2FF;color:#667EEA;border:none;border-radius:7px;cursor:pointer;display:flex;align-items:center;gap:3px;font-size:12px;font-weight:500;">
            <el-icon style="font-size:11px;"><Edit /></el-icon>编辑
          </button>
          <button @click="handleDelete(cat.id)" style="height:28px;padding:0 10px;background:#FFF1F2;color:#F43F5E;border:none;border-radius:7px;cursor:pointer;display:flex;align-items:center;gap:3px;font-size:12px;font-weight:500;">
            <el-icon style="font-size:11px;"><Delete /></el-icon>删除
          </button>
        </div>
      </div>
    </div>

    <!-- Delete Confirm overlay -->
    <div v-if="deleteVisible" style="position:fixed;inset:0;background:rgba(15,23,42,0.5);display:flex;align-items:center;justify-content:center;z-index:1000;" @click="deleteId = null">
      <div style="background:#fff;border-radius:16px;width:380px;max-width:90vw;padding:32px 28px;box-shadow:0 16px 48px rgba(244,63,94,0.14),0 4px 16px rgba(0,0,0,0.08);text-align:center;" @click.stop>
        <div style="width:64px;height:64px;border-radius:50%;background:#FFF1F2;display:flex;align-items:center;justify-content:center;margin:0 auto 16px;">
          <el-icon style="font-size:28px;color:#F43F5E;"><WarningFilled /></el-icon>
        </div>
        <div style="font-size:17px;font-weight:700;color:#1E293B;margin-bottom:8px;">确认删除</div>
        <div style="font-size:14px;color:#64748B;line-height:1.6;margin-bottom:24px;">
          此操作不可逆，删除后该分类数据将无法恢复。<br />确定要继续吗？
        </div>
        <div style="display:flex;gap:10px;justify-content:center;">
          <button @click="deleteId = null" style="height:40px;padding:0 24px;border:1px solid #E2E8F0;border-radius:10px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;font-weight:500;">取消</button>
          <button @click="confirmDelete" style="height:40px;padding:0 28px;border:none;border-radius:10px;background:linear-gradient(135deg,#F43F5E,#E11D48);color:#fff;cursor:pointer;font-size:14px;font-weight:600;box-shadow:0 4px 12px rgba(244,63,94,0.35);">确认删除</button>
        </div>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" style="position:fixed;inset:0;background:rgba(15,23,42,0.5);display:flex;align-items:center;justify-content:center;z-index:1000;" @click="showModal = false">
      <div style="background:#fff;border-radius:16px;width:420px;max-width:90vw;max-height:90vh;overflow:auto;box-shadow:0 16px 48px rgba(0,0,0,0.14),0 4px 16px rgba(0,0,0,0.08);" @click.stop>
        <div style="display:flex;justify-content:space-between;align-items:center;padding:20px 24px;border-bottom:1px solid #F1F5F9;">
          <span style="font-size:16px;font-weight:700;color:#1E293B;">{{ editCat ? '编辑分类' : '新增分类' }}</span>
          <el-icon style="font-size:18px;color:#94A3B8;cursor:pointer;" @click="showModal = false"><Close /></el-icon>
        </div>
        <div style="padding:20px 24px;">
          <!-- Icon selector -->
          <div style="margin-bottom:16px;">
            <div style="display:flex;align-items:center;margin-bottom:6px;">
              <span style="font-size:13px;font-weight:500;color:#1E293B;">选择图标</span>
              <span style="color:#F43F5E;margin-left:4px;font-size:13px;">*</span>
            </div>
            <div style="display:flex;align-items:center;gap:12px;margin-bottom:12px;">
              <div style="width:52px;height:52px;border-radius:12px;background:#F0F2F8;display:flex;align-items:center;justify-content:center;font-size:24px;flex-shrink:0;">{{ formIcon }}</div>
              <span style="font-size:13px;color:#94A3B8;">点击下方图标选择</span>
            </div>
            <div style="display:flex;flex-wrap:wrap;gap:6px;padding:10px;background:#F8FAFC;border-radius:10px;">
              <button v-for="ic in iconOptions" :key="ic" @click="formIcon = ic"
                :style="{width:'36px',height:'36px',borderRadius:'8px',border: formIcon===ic ? '2px solid #667EEA' : '2px solid transparent',background: formIcon===ic ? '#EEF2FF' : 'transparent',cursor:'pointer',fontSize:'18px',transition:'all 0.15s'}">
                {{ ic }}
              </button>
            </div>
          </div>

          <!-- Category name -->
          <div style="margin-bottom:16px;">
            <div style="display:flex;align-items:center;margin-bottom:6px;">
              <span style="font-size:13px;font-weight:500;color:#1E293B;">分类名称</span>
              <span style="color:#F43F5E;margin-left:4px;font-size:13px;">*</span>
            </div>
            <input v-model="formName" placeholder="请输入分类名称" :style="fieldStyle" />
          </div>

          <!-- Type + Sort side by side -->
          <div style="display:flex;gap:16px;">
            <div style="flex:1;margin-bottom:16px;">
              <div style="display:flex;align-items:center;margin-bottom:6px;">
                <span style="font-size:13px;font-weight:500;color:#1E293B;">类型</span>
                <span style="color:#F43F5E;margin-left:4px;font-size:13px;">*</span>
              </div>
              <select v-model="formType" :disabled="!!editCat"
                :style="{...fieldStyle, cursor: editCat ? 'not-allowed' : 'pointer', opacity: editCat ? 0.6 : 1}">
                <option value="expense">支出</option>
                <option value="income">收入</option>
              </select>
            </div>
            <div style="flex:1;margin-bottom:16px;">
              <div style="display:flex;align-items:center;margin-bottom:6px;">
                <span style="font-size:13px;font-weight:500;color:#1E293B;">排序</span>
              </div>
              <input v-model.number="formSort" type="number" :style="{...fieldStyle, width:'100%'}" />
            </div>
          </div>

          <!-- Footer -->
          <div style="display:flex;justify-content:flex-end;gap:10px;padding-top:16px;border-top:1px solid #F1F5F9;margin-top:8px;">
            <button @click="showModal = false" style="height:38px;padding:0 20px;border:1px solid #E2E8F0;border-radius:10px;background:#fff;color:#64748B;cursor:pointer;font-size:14px;">取消</button>
            <button @click="handleSave" style="height:38px;padding:0 24px;border:none;border-radius:10px;background:linear-gradient(135deg,#667EEA,#764BA2);color:#fff;cursor:pointer;font-size:14px;font-weight:600;box-shadow:0 4px 12px rgba(102,126,234,0.4);">确定</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
