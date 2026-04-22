<template>
  <div class="user-manage">
    <el-card shadow="never">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="账号状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="正常" :value="1" />
              <el-option label="已封禁" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="loadData">搜索</el-button>
            <el-button icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar || defaultAvatar(row.id)" />
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="username" label="账号" width="130" />
        <el-table-column prop="creditScore" label="信用分" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.creditScore >= 80 ? 'success' : (row.creditScore >= 60 ? 'warning' : 'danger')">
              {{ row.creditScore }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small">
              {{ row.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus } from '../../api/user.js'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({ status: '' })

const defaultAvatar = (id) => `https://api.dicebear.com/7.x/avataaars/svg?seed=${id}`

const loadData = async () => {
  loading.value = true
  try {
    const params = { current: currentPage.value, size: pageSize.value }
    if (searchForm.status !== '') params.status = searchForm.status
    const res = await getUserList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.status = ''
  currentPage.value = 1
  loadData()
}

const handleStatusChange = async (row, val) => {
  const action = val === 1 ? '解封' : '封禁'
  try {
    await ElMessageBox.confirm(`确定要${action}用户「${row.nickname}」吗？`, '提示', {
      type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
    })
    await updateUserStatus(row.id, val)
    ElMessage.success(`已${action}用户：${row.nickname}`)
  } catch {
    // 取消操作时恢复原值
    row.status = val === 1 ? 0 : 1
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.user-manage {
  .search-bar { margin-bottom: 20px; }
  .pagination-wrap {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
