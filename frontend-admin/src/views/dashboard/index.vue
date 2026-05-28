<template>
  <div class="dashboard-container">
    <!-- 统计卡片区 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="12" :sm="6" v-for="(item, idx) in statCards" :key="idx">
        <div class="stat-card">
          <div class="stat-card__content">
            <div class="stat-card__meta">
              <span class="stat-card__title">{{ item.title }}</span>
              <span class="stat-card__value">{{ item.value }}</span>
            </div>
            <div class="stat-card__icon" :style="{ background: item.iconBg }">
              <el-icon :size="22" :color="item.iconColor">
                <component :is="item.icon" />
              </el-icon>
            </div>
          </div>
          <div class="stat-card__footer">
            <span class="stat-card__trend" :class="item.trendUp ? 'up' : 'down'">
              {{ item.trendUp ? '↑' : '↓' }} {{ item.trendText }}
            </span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-section">
      <el-col :xs="24" :sm="16">
        <div class="chart-card">
          <div class="chart-card__header">
            <span class="chart-card__title">用户增长趋势</span>
          </div>
          <div ref="userTrendRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="chart-card">
          <div class="chart-card__header">
            <span class="chart-card__title">商品分类占比</span>
          </div>
          <div ref="productTrendRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 数据表格区 -->
    <el-row :gutter="16" class="table-section">
      <el-col :xs="24" :sm="14">
        <div class="table-card">
          <div class="table-card__header">
            <span class="table-card__title">待审核商品</span>
            <el-badge :value="pendingCount" type="danger" v-if="pendingCount > 0" />
          </div>
          <el-table :data="pendingProducts" style="width: 100%" size="small" v-loading="tableLoading">
            <el-table-column prop="id" label="商品编号" width="80" align="center" />
            <el-table-column prop="title" label="商品名称" show-overflow-tooltip />
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">
                <span style="color: #f5222d; font-weight: 500">￥{{ Number(row.price).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="160" />
            <el-table-column fixed="right" label="操作" width="70">
              <template #default>
                <el-button link type="primary" size="small" @click="$router.push('/product')">去审核</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="view-more">
            <el-button link type="primary" @click="$router.push('/product')">查看更多 ></el-button>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="10">
        <div class="table-card">
          <div class="table-card__header">
            <span class="table-card__title">快捷操作</span>
          </div>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/product')">
              <div class="action-icon" style="background: #e6f7ff; color: #1890ff;">
                <el-icon :size="20"><Goods /></el-icon>
              </div>
              <span>商品审核</span>
            </div>
            <div class="action-item" @click="$router.push('/user')">
              <div class="action-icon" style="background: #f6ffed; color: #52c41a;">
                <el-icon :size="20"><User /></el-icon>
              </div>
              <span>用户管理</span>
            </div>
            <div class="action-item" @click="$router.push('/category')">
              <div class="action-icon" style="background: #fff7e6; color: #faad14;">
                <el-icon :size="20"><CollectionTag /></el-icon>
              </div>
              <span>分类管理</span>
            </div>
          </div>
          <el-divider />
          <div class="recent-title">最近发布</div>
          <el-timeline>
            <el-timeline-item
              v-for="(item, idx) in recentProducts"
              :key="idx"
              :timestamp="item.createTime"
              :type="idx === 0 ? 'primary' : 'success'">
              {{ item.title }} (￥{{ Number(item.price).toFixed(2) }})
            </el-timeline-item>
            <el-timeline-item v-if="recentProducts.length === 0" timestamp="-" type="info">
              暂无商品数据
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getAdminProductList } from '../../api/product.js'
import { getUserList } from '../../api/user.js'
import { getOrderStats } from '../../api/order.js'
import { getCategoryList } from '../../api/category.js'
import { User, Goods, CollectionTag, ShoppingCart } from '@element-plus/icons-vue'

const statCards = ref([
  { title: '总用户', value: '-', icon: 'User', iconBg: '#e6f7ff', iconColor: '#1890ff', trendUp: true, trendText: '加载中' },
  { title: '在售商品', value: '-', icon: 'Goods', iconBg: '#f6ffed', iconColor: '#52c41a', trendUp: true, trendText: '加载中' },
  { title: '总订单', value: '-', icon: 'ShoppingCart', iconBg: '#fff7e6', iconColor: '#faad14', trendUp: true, trendText: '加载中' },
  { title: '待审核', value: '-', icon: 'CollectionTag', iconBg: '#fff1f0', iconColor: '#f5222d', trendUp: false, trendText: '需处理' }
])

const pendingProducts = ref([])
const pendingCount = ref(0)
const recentProducts = ref([])
const tableLoading = ref(false)
const categoryMap = ref({})
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
      xAxis: { type: 'category', data: userSeries.dates, boundaryGap: false },
      yAxis: { type: 'value', minInterval: 1 },
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
      series: [{
        type: 'line',
        smooth: true,
        data: userCumulative,
        lineStyle: { color: '#1890ff', width: 2 },
        itemStyle: { color: '#1890ff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(24, 144, 255, 0.25)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.02)' }
          ])
        }
      }]
    })
  }

  if (productTrendRef.value) {
    if (productChart) productChart.dispose()
    productChart = echarts.init(productTrendRef.value)
    // Build category counts for pie chart
    const categoryCounter = {}
    productRecords.forEach((item) => {
      const cat = item.categoryId || '未分类'
      categoryCounter[cat] = (categoryCounter[cat] || 0) + 1
    })
    const pieData = Object.entries(categoryCounter).map(([cat, value]) => ({
      name: categoryMap.value[cat] || '未知分类',
      value
    }))

    productChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      color: ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2', '#eb2f96'],
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '55%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, fontSize: 12 },
        data: pieData.length > 0 ? pieData : [{ name: '暂无数据', value: 1 }]
      }]
    })
  }
}

const loadStats = async () => {
  tableLoading.value = true
  try {
    // 加载分类列表构建映射
    try {
      const categoryList = await getCategoryList()
      const list = Array.isArray(categoryList) ? categoryList : (categoryList.records || categoryList.data || [])
      list.forEach(item => {
        categoryMap.value[item.id] = item.name
      })
    } catch (e) {
      console.warn('加载分类列表失败', e)
    }

    const [userRes, pendingRes, onsaleRes, soldRes, productAllRes, orderStats] = await Promise.all([
      getUserList({ current: 1, size: 500 }),
      getAdminProductList({ current: 1, size: 5, status: 0 }),
      getAdminProductList({ current: 1, size: 1, status: 1 }),
      getAdminProductList({ current: 1, size: 1, status: 3 }),
      getAdminProductList({ current: 1, size: 500 }),
      getOrderStats().catch(() => ({ totalOrders: 0, completedOrders: 0 }))
    ])

    statCards.value[0].value = userRes.total || 0
    statCards.value[0].trendText = `共 ${userRes.total || 0} 人`
    statCards.value[1].value = onsaleRes.total || 0
    statCards.value[1].trendText = `在架 ${onsaleRes.total || 0} 件`
    statCards.value[2].value = orderStats.totalOrders || 0
    statCards.value[2].trendText = `已完成 ${orderStats.completedOrders || 0}`
    statCards.value[3].value = pendingRes.total || 0
    statCards.value[3].trendText = `待处理 ${pendingRes.total || 0} 件`

    pendingProducts.value = pendingRes.records || []
    pendingCount.value = pendingRes.total || 0
    recentProducts.value = (productAllRes.records || []).slice(0, 5)
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
  .stat-cards {
    margin-bottom: 16px;

    .stat-card {
      background: #fff;
      border-radius: 12px;
      padding: 0;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      transition: transform 0.2s ease, box-shadow 0.2s ease;
      overflow: hidden;
      cursor: default;
      padding: 20px 24px;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      /* Colored top bar per card index */
      &:nth-child(1) { border-top: 4px solid #1890ff; }
      &:nth-child(2) { border-top: 4px solid #52c41a; }
      &:nth-child(3) { border-top: 4px solid #faad14; }
      &:nth-child(4) { border-top: 4px solid #f5222d; }

      &__content {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
      }

      &__meta {
        display: flex;
        flex-direction: column;
      }

      &__title {
        font-size: 13px;
        color: rgba(0, 0, 0, 0.45);
        margin-bottom: 10px;
        font-weight: 500;
        letter-spacing: 0.3px;
      }

      &__value {
        font-size: 32px;
        font-weight: 700;
        color: rgba(0, 0, 0, 0.85);
        line-height: 1.1;
        font-variant-numeric: tabular-nums;
      }

      &__icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      &__footer {
        margin-top: 14px;
        padding-top: 12px;
        border-top: 1px solid #f5f5f5;
      }

      &__trend {
        font-size: 13px;
        &.up { color: #52c41a; }
        &.down { color: #f5222d; }
      }
    }
  }

  .chart-section {
    margin-bottom: 16px;
  }

  .chart-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    height: 100%;

    &__header {
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid #f5f5f5;
    }

    &__title {
      font-size: 15px;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
      position: relative;
      padding-left: 12px;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 14px;
        background: #1890ff;
        border-radius: 2px;
      }
    }
  }

  .chart-box {
    height: 300px;
    width: 100%;
  }

  .table-section {
    .table-card {
      background: #fff;
      border-radius: 12px;
      padding: 20px 24px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      height: 100%;

      &__header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #f5f5f5;
      }

      &__title {
        font-size: 15px;
        font-weight: 600;
        color: rgba(0, 0, 0, 0.85);
        position: relative;
        padding-left: 12px;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 14px;
          background: #1890ff;
          border-radius: 2px;
        }
      }
    }
  }

  .quick-actions {
    display: flex;
    gap: 12px;
    margin-bottom: 8px;

    .action-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      cursor: pointer;
      padding: 14px 8px;
      border-radius: 12px;
      transition: background 0.18s, transform 0.18s;
      flex: 1;
      border: 1px solid transparent;

      &:hover {
        background: #f0f7ff;
        border-color: rgba(24, 144, 255, 0.15);
        transform: translateY(-2px);
      }

      .action-icon {
        width: 44px;
        height: 44px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 8px;
        transition: transform 0.18s;
      }

      &:hover .action-icon {
        transform: scale(1.08);
      }

      span {
        font-size: 12px;
        color: rgba(0, 0, 0, 0.65);
        font-weight: 500;
      }
    }
  }

  .recent-title {
    font-size: 14px;
    font-weight: 500;
    color: rgba(0, 0, 0, 0.65);
    margin-bottom: 12px;
  }

  .view-more {
    text-align: center;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
  }
}
</style>
