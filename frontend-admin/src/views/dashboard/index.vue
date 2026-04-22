<template>
  <div class="dashboard-container">
    <!-- 数据统计卡片 -->
    <el-row :gutter="20" class="data-cards">
      <el-col :span="6" v-for="item in statistics" :key="item.title">
        <el-card shadow="hover" class="card-item">
          <div class="card-header">
            <span>{{ item.title }}</span>
            <el-tag :type="item.tagType" size="small">{{ item.tagText }}</el-tag>
          </div>
          <div class="card-value">{{ item.value }}</div>
          <div class="card-footer">
            <span>{{ item.desc }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-section">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><div class="card-title">用户增长趋势（累计）</div></template>
          <div ref="userTrendRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><div class="card-title">每日商品发布量</div></template>
          <div ref="productTrendRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待审核商品 & 快捷入口 -->
    <el-row :gutter="20" class="action-section">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <div class="card-title">
              <span>待审核商品</span>
              <el-badge :value="pendingCount" type="danger" v-if="pendingCount > 0" />
            </div>
          </template>
          <el-table :data="pendingProducts" style="width: 100%" size="small" v-loading="tableLoading">
            <el-table-column prop="id" label="ID" width="60" align="center" />
            <el-table-column prop="title" label="商品名称" show-overflow-tooltip />
            <el-table-column prop="price" label="价格" width="90">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="160" />
            <el-table-column fixed="right" label="操作" width="70">
              <template #default>
                <el-button link type="primary" size="small" @click="$router.push('/product')">去审核</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header><div class="card-title">快捷操作</div></template>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/product')">
              <div class="action-icon" style="background: #e6f7ff;">📦</div>
              <span>商品审核</span>
            </div>
            <div class="action-item" @click="$router.push('/user')">
              <div class="action-icon" style="background: #f6ffed;">👥</div>
              <span>用户管理</span>
            </div>
          </div>
          <el-divider />
          <el-timeline>
            <el-timeline-item timestamp="2026/04/07" type="primary">
              第二周开发：前后端联调完成
            </el-timeline-item>
            <el-timeline-item timestamp="2026/04/02" type="success">
              第一周开发：MVP 核心框架搭建完成
            </el-timeline-item>
            <el-timeline-item timestamp="2026/03/25" type="warning">
              需求分析与系统设计文档评审通过
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getProductList } from '../../api/product.js'
import { getUserList } from '../../api/user.js'

const statistics = ref([
  { title: '总用户数', value: '-', tagText: '总计', tagType: '', desc: '加载中...' },
  { title: '待审核商品', value: '-', tagText: '待办', tagType: 'danger', desc: '需要处理' },
  { title: '在售商品', value: '-', tagText: '上架', tagType: 'success', desc: '当前在售' },
  { title: '已售出', value: '-', tagText: '完成', tagType: 'warning', desc: '历史成交' }
])

const pendingProducts = ref([])
const pendingCount = ref(0)
const tableLoading = ref(false)
const userTrendRef = ref(null)
const productTrendRef = ref(null)
let userChart = null
let productChart = null

const normalizeDate = (timeText) => {
  if (!timeText) return ''
  const text = String(timeText).replace('T', ' ')
  return text.substring(0, 10)
}

const buildDailySeries = (records = []) => {
  const counter = {}
  records.forEach((item) => {
    const key = normalizeDate(item.createTime)
    if (!key) return
    counter[key] = (counter[key] || 0) + 1
  })
  const dates = Object.keys(counter).sort()
  const values = dates.map(date => counter[date])
  return { dates, values }
}

const toCumulative = (values = []) => {
  let sum = 0
  return values.map((v) => {
    sum += v
    return sum
  })
}

const renderCharts = (userRecords, productRecords) => {
  const userSeries = buildDailySeries(userRecords)
  const productSeries = buildDailySeries(productRecords)
  const userCumulative = toCumulative(userSeries.values)

  if (userTrendRef.value) {
    if (userChart) userChart.dispose()
    userChart = echarts.init(userTrendRef.value)
    userChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: userSeries.dates },
      yAxis: { type: 'value', minInterval: 1 },
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
      series: [{ type: 'line', smooth: true, data: userCumulative, areaStyle: {} }]
    })
  }

  if (productTrendRef.value) {
    if (productChart) productChart.dispose()
    productChart = echarts.init(productTrendRef.value)
    productChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: productSeries.dates },
      yAxis: { type: 'value', minInterval: 1 },
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
      series: [{ type: 'bar', data: productSeries.values, barMaxWidth: 32 }]
    })
  }
}

const loadStats = async () => {
  tableLoading.value = true
  try {
    const [userRes, pendingRes, onsaleRes, soldRes, productAllRes] = await Promise.all([
      getUserList({ current: 1, size: 500 }),
      getProductList({ current: 1, size: 5, status: 0 }),
      getProductList({ current: 1, size: 1, status: 1 }),
      getProductList({ current: 1, size: 1, status: 3 }),
      getProductList({ current: 1, size: 500 })
    ])

    statistics.value[0].value = userRes.total || 0
    statistics.value[1].value = pendingRes.total || 0
    statistics.value[2].value = onsaleRes.total || 0
    statistics.value[3].value = soldRes.total || 0

    pendingProducts.value = pendingRes.records || []
    pendingCount.value = pendingRes.total || 0
    await nextTick()
    renderCharts(userRes.records || [], productAllRes.records || [])
  } finally {
    tableLoading.value = false
  }
}

onMounted(loadStats)

onBeforeUnmount(() => {
  if (userChart) userChart.dispose()
  if (productChart) productChart.dispose()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  .data-cards {
    margin-bottom: 20px;
    .card-item {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        color: #909399;
        font-size: 14px;
      }
      .card-value {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
        margin: 15px 0;
      }
      .card-footer {
        font-size: 13px;
        color: #909399;
      }
    }
  }

  .chart-section {
    margin-bottom: 20px;
  }

  .chart-box {
    height: 300px;
    width: 100%;
  }

  .action-section {
    .card-title {
      font-weight: bold;
      color: #333;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .quick-actions {
      display: flex;
      gap: 20px;
      margin-bottom: 10px;

      .action-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        cursor: pointer;
        padding: 12px 20px;
        border-radius: 8px;
        transition: background 0.2s;
        &:hover { background: #f5f5f5; }

        .action-icon {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          margin-bottom: 8px;
        }

        span {
          font-size: 13px;
          color: #555;
        }
      }
    }
  }
}
</style>
