<template>
  <div class="product-manage">
    <el-card shadow="never">
      <!-- Tabs 过滤 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待审核" name="0" />
        <el-tab-pane label="售卖中" name="1" />
        <el-tab-pane label="已下架" name="2" />
      </el-tabs>

      <!-- 表格 -->
      <el-table :data="tableData" border style="width: 100%; margin-top: 15px" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="商品主图" width="100" align="center">
          <template #default="{ row }">
            <el-image
              style="width: 60px; height: 60px; border-radius: 4px;"
              :src="getFirstImage(row.images)"
              fit="cover"
              :preview-src-list="[getFirstImage(row.images)]"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="商品标题" show-overflow-tooltip min-width="200" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥ {{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="categoryId" label="分类" width="90">
          <template #default="{ row }">{{ categoryMap[row.categoryId] || '未知' }}</template>
        </el-table-column>
        <el-table-column prop="sellerId" label="卖家ID" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" fixed="right" min-width="150">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" link size="small" @click="handleAudit(row, 1)">通过</el-button>
              <el-button type="danger" link size="small" @click="handleAudit(row, 2)">驳回</el-button>
            </template>
            <template v-if="row.status === 1">
              <el-button type="warning" link size="small" @click="handleAudit(row, 2)">强制下架</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList, auditProduct } from '../../api/product.js'

const activeTab = ref('all')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const categoryMap = {
  1: '数码产品', 2: '书籍教材', 3: '衣物鞋帽',
  4: '代步工具', 5: '生活日用', 6: '其他闲置'
}

const getFirstImage = (images) => {
  if (!images) return 'https://via.placeholder.com/60x60?text=无图'
  return images.split(',')[0].trim()
}

const getStatusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[status] || 'info')
const getStatusText = (status) => ({ 0: '待审核', 1: '售卖中', 2: '已下架', 3: '已售出' }[status] || '未知')

const loadData = async () => {
  loading.value = true
  try {
    const params = { current: currentPage.value, size: pageSize.value }
    if (activeTab.value !== 'all') params.status = Number(activeTab.value)
    const res = await getProductList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleTabClick = () => {
  currentPage.value = 1
  loadData()
}

const handleAudit = (row, targetStatus) => {
  const actionText = targetStatus === 1 ? '审核通过' : '下架/驳回'
  ElMessageBox.confirm(`确定要「${actionText}」商品「${row.title}」吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    await auditProduct(row.id, targetStatus)
    ElMessage.success('操作成功')
    loadData()
  }).catch(() => {})
}

onMounted(loadData)
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
