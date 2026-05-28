<template>
  <div class="category-manage">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="openCreate">新增分类</el-button>
      </div>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="sortNo" label="排序" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(value) => handleStatusChange(row, value)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="420px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="ID" v-if="editingId">
          <el-input v-model="form.id" disabled />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortNo" :min="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminCategoryList, saveCategory, updateCategoryStatus } from '../../api/category.js'

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const tableData = ref([])
const form = reactive({
  id: null,
  name: '',
  sortNo: 1,
  status: 1
})

const resetForm = () => {
  form.id = null
  form.name = ''
  form.sortNo = 1
  form.status = 1
  editingId.value = null
}

const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await getAdminCategoryList()
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  form.id = row.id
  form.name = row.name
  form.sortNo = row.sortNo
  form.status = row.status
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.name.trim()) {
    ElMessage.error('请输入分类名称')
    return
  }
  await saveCategory({
    id: form.id,
    name: form.name.trim(),
    sortNo: form.sortNo,
    status: form.status
  })
  ElMessage.success('已保存')
  dialogVisible.value = false
  loadData()
}

const handleStatusChange = async (row, value) => {
  await updateCategoryStatus(row.id, value)
  ElMessage.success('状态已更新')
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.toolbar {
  margin-bottom: 16px;
}
</style>
