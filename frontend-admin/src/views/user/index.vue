<template>
  <div class="user-manage">
    <el-card shadow="never">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="用户昵称">
            <el-input v-model="searchForm.nickname" placeholder="请输入昵称" clearable />
          </el-form-item>
          <el-form-item label="账号状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="正常" :value="1" />
              <el-option label="已封禁" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
            <el-button icon="Refresh" @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="studentId" label="学号" width="150" />
        <el-table-column prop="creditScore" label="信用分" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.creditScore >= 80 ? 'success' : (row.creditScore >= 60 ? 'warning' : 'danger')">
              {{ row.creditScore }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="180" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button type="danger" link size="small" @click="resetCredit(row)">重置信用分</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="100"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  nickname: '',
  status: ''
})

// 模拟数据
const tableData = ref([
  { id: 1001, avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix', nickname: '计科小明', studentId: '20210001', creditScore: 100, registerTime: '2023-09-01 10:00:00', status: 1 },
  { id: 1002, avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Lily', nickname: '林学姐', studentId: '20200012', creditScore: 95, registerTime: '2023-09-02 14:20:00', status: 1 },
  { id: 1003, avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jack', nickname: '违规小号', studentId: '20230999', creditScore: 40, registerTime: '2023-10-15 08:30:00', status: 0 },
])

const handleSearch = () => {
  loading.value = true
  setTimeout(() => { loading.value = false }, 500)
}

const resetSearch = () => {
  searchForm.nickname = ''
  searchForm.status = ''
  handleSearch()
}

const handleStatusChange = (row, val) => {
  const action = val === 1 ? '解封' : '封禁'
  ElMessage.success(`已${action}用户：${row.nickname}`)
}

const viewDetail = (row) => {
  ElMessage.info(`查看用户详情：${row.nickname}`)
}

const resetCredit = (row) => {
  ElMessageBox.confirm(
    `确定要将用户 ${row.nickname} 的信用分重置为 100 吗？`,
    '警告',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    row.creditScore = 100
    ElMessage.success('重置成功')
  }).catch(() => {})
}

const handleSizeChange = (val) => { console.log(`${val} items per page`) }
const handleCurrentChange = (val) => { console.log(`current page: ${val}`) }
</script>

<style scoped lang="scss">
.user-manage {
  .search-bar {
    margin-bottom: 20px;
  }
  
  .pagination-wrap {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
