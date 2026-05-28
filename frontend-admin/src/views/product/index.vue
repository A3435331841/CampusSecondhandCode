<template>
  <div class="product-manage">
    <!-- 页面标题区 -->
    <div class="page-header">
      <h2 class="page-title">商品管理</h2>
      <p class="page-desc">审核和管理平台中的所有商品信息</p>
    </div>

    <!-- 筛选区 -->
    <div class="filter-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待审核" name="0" />
        <el-tab-pane label="在售" name="1" />
        <el-tab-pane label="已下架" name="2" />
        <el-tab-pane label="已驳回" name="4" />
      </el-tabs>
    </div>

    <!-- 表格区 -->
    <div class="table-card">
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="商品编号" width="70" align="center" />
        <el-table-column label="图片" width="100" align="center">
          <template #default="{ row }">
            <el-image
              style="width: 60px; height: 60px; border-radius: 6px"
              :src="getFirstImage(row.images)"
              fit="cover"
              :preview-src-list="[getFirstImage(row.images)]"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" show-overflow-tooltip min-width="220" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price-text">￥ {{ Number(row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="categoryId" label="分类" width="150">
          <template #default="{ row }">
            {{ categoryNameMap[row.categoryId] || '未知分类' }}
          </template>
        </el-table-column>
        <el-table-column prop="sellerId" label="卖家ID" width="100" align="center" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small" round>{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" fixed="right" min-width="150">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" link size="small" @click="handleAudit(row, 1)">通过</el-button>
              <el-button type="danger" link size="small" @click="handleAudit(row, 4)">驳回</el-button>
            </template>
            <template v-else-if="row.status === 1">
              <el-button type="warning" link size="small" @click="handleAudit(row, 2)">强制下架</el-button>
            </template>
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
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminProductList, auditProduct } from '../../api/product.js'
import { getAdminCategoryList } from '../../api/category.js'

const activeTab = ref('all')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const categories = ref([])
let latestProductListRequestId = 0

const categoryNameMap = computed(() =>
  Object.fromEntries((categories.value || []).map(item => [item.id, item.name]))
)

const getFirstImage = (images) => {
  if (!images) return 'https://via.placeholder.com/60x60?text=%E6%97%A0%E5%9B%BE'
  return images.split(',')[0].trim()
}

const statusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger', 3: 'info', 4: 'danger' }[status] || 'info')
const statusText = (status) => ({ 0: '待审核', 1: '在售', 2: '已下架', 3: '已售出', 4: '已驳回' }[status] || '未知')

const loadCategories = async () => {
  categories.value = await getAdminCategoryList()
}

const loadData = async (status = activeTab.value) => {
  const requestId = ++latestProductListRequestId
  loading.value = true
  try {
    const params = { current: currentPage.value, size: pageSize.value }
    if (status !== 'all') {
      // 直接传递字符串（如"0"），避免Number()转换后falsy值被axios忽略
      // 后端 @RequestParam Integer status 会自动将"0"转为整数0
      params.status = status
    }
    console.log('请求参数:', params)
    const res = await getAdminProductList(params)
    if (requestId !== latestProductListRequestId) return
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    if (requestId === latestProductListRequestId) {
      loading.value = false
    }
  }
}

const handleTabChange = (tabName) => {
  activeTab.value = tabName
  currentPage.value = 1
  loadData(tabName)
}

const handlePageChange = () => {
  loadData()
}

const handleAudit = (row, targetStatus) => {
  const actionMap = { 1: '通过', 2: '强制下架', 4: '驳回' }
  const actionText = actionMap[targetStatus] || '更新'
  ElMessageBox.confirm(
    `确定要${actionText}「${row.title}」吗？`,
    '操作确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  )
    .then(async () => {
      await auditProduct(row.id, targetStatus)
      ElMessage.success('操作成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(async () => {
  await loadCategories()
  await loadData()
})
</script>

<style scoped lang="scss">
.product-manage {
  .page-header {
    margin-bottom: 16px;

    .page-title {
      font-size: 20px;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
      margin-bottom: 4px;
    }

    .page-desc {
      font-size: 14px;
      color: rgba(0, 0, 0, 0.45);
    }
  }

  .filter-card {
    background: #fff;
    border-radius: 12px;
    padding: 16px 24px 0;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    border-left: 4px solid #1890ff;

    :deep(.el-tabs__nav-wrap::after) {
      height: 1px;
    }

    :deep(.el-tabs__item.is-active) {
      font-weight: 600;
    }
  }

  .table-card {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .price-text {
      color: #FF6600;
      font-weight: 600;
      font-variant-numeric: tabular-nums;
    }

    :deep(.el-table__header-wrapper th) {
      background: #fafbfc;
      color: rgba(0, 0, 0, 0.65);
      font-weight: 600;
      font-size: 13px;
    }

    :deep(.el-table__row) {
      transition: background 0.15s;

      &:hover td {
        background: #f0f7ff !important;
      }
    }

    :deep(.el-tag) {
      font-weight: 500;
    }
  }

  .pagination-wrap {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
