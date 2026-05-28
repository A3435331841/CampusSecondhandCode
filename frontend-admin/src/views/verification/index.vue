<template>
  <div class="verification-manage">
    <el-card shadow="never">
      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="id" label="用户ID" width="90" />
        <el-table-column prop="nickname" label="昵称" width="160" />
        <el-table-column prop="realName" label="真实姓名" width="160" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="college" label="学院" min-width="180" />
        <el-table-column prop="major" label="专业" min-width="180" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column label="认证状态" width="140">
          <template #default="{ row }">
            <el-tag :type="statusType(row.verifyStatus)">{{ statusText(row.verifyStatus) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getUserList } from '../../api/user.js'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const statusType = (status) => {
  if (status === 'VERIFIED') return 'success'
  if (status === 'PENDING') return 'warning'
  if (status === 'REJECTED') return 'danger'
  return 'info'
}

const statusText = (status) => {
  if (status === 'VERIFIED') return '已认证'
  if (status === 'PENDING') return '待审核'
  if (status === 'REJECTED') return '已驳回'
  return '未认证'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({ current: currentPage.value, size: pageSize.value })
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
