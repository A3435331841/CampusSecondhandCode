<template>
  <div class="product-manage">
    <el-card shadow="never">
      <!-- Tabs 过滤 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane label="待审核" name="auditing"></el-tab-pane>
        <el-tab-pane label="售卖中" name="onsale"></el-tab-pane>
        <el-tab-pane label="已下架/违规" name="offsale"></el-tab-pane>
      </el-tabs>

      <!-- 表格 -->
      <el-table :data="tableData" border style="width: 100%; margin-top: 15px" v-loading="loading">
        <el-table-column prop="id" label="商品ID" width="80" align="center" />
        <el-table-column label="商品主图" width="100" align="center">
          <template #default="{ row }">
            <el-image style="width: 60px; height: 60px" :src="row.image" fit="cover" :preview-src-list="[row.image]" preview-teleported />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="商品标题" show-overflow-tooltip min-width="200" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥ {{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="seller" label="发布人" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="150">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" link size="small" @click="handleAudit(row, 1)">通过</el-button>
              <el-button type="danger" link size="small" @click="handleAudit(row, 2)">驳回</el-button>
            </template>
            <template v-if="row.status === 1">
              <el-button type="warning" link size="small" @click="handleAudit(row, 2)">强制下架</el-button>
            </template>
            <el-button type="primary" link size="small">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination background layout="prev, pager, next" :total="50" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('all')
const loading = ref(false)

const tableData = ref([
  { id: 1, title: '大四学姐自用 iPhone 13 128G', price: '2800.00', image: 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=400&q=80', category: '数码产品', seller: '林学姐', status: 1 },
  { id: 2, title: '全新未拆封 蓝牙耳机', price: '99.00', image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=400&q=80', category: '数码产品', seller: '张三', status: 0 },
  { id: 3, title: '代写作业/论文包过 (违规测试)', price: '500.00', image: 'https://images.unsplash.com/photo-1455390582262-044cdead27d8?auto=format&fit=crop&w=400&q=80', category: '其他', seller: '黑产小哥', status: 2 },
])

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '售卖中', 2: '已下架' }
  return map[status] || '未知'
}

const handleTabClick = () => {
  loading.value = true
  setTimeout(() => { loading.value = false }, 300)
}

const handleAudit = (row, targetStatus) => {
  const actionText = targetStatus === 1 ? '审核通过' : '下架/驳回'
  ElMessageBox.confirm(`确定要 ${actionText} 商品 "${row.title}" 吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    row.status = targetStatus
    ElMessage.success(`操作成功`)
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.product-manage {
  .pagination-wrap {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
