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
            <span>较昨日</span>
            <span :class="item.trend > 0 ? 'up' : 'down'">
              {{ Math.abs(item.trend) }}%
              <el-icon><Top v-if="item.trend > 0" /><Bottom v-else /></el-icon>
            </span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办事项与快捷操作 -->
    <el-row :gutter="20" class="action-section">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-title">待办事项 (需审核)</div>
          </template>
          <el-table :data="todoList" style="width: 100%" size="small">
            <el-table-column prop="date" label="发布时间" width="150" />
            <el-table-column prop="title" label="商品名称" show-overflow-tooltip />
            <el-table-column prop="user" label="发布人" width="100" />
            <el-table-column fixed="right" label="操作" width="80">
              <template #default>
                <el-button link type="primary" size="small" @click="$router.push('/product')">去审核</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-title">平台公告</div>
          </template>
          <el-timeline>
            <el-timeline-item timestamp="2023/11/01" type="primary">
              新增“毕业季清仓”专区功能上线
            </el-timeline-item>
            <el-timeline-item timestamp="2023/10/28" type="success">
              完成 V1.0 版本服务器部署与压力测试
            </el-timeline-item>
            <el-timeline-item timestamp="2023/10/15" type="warning">
              系统设计说明书与数据库模型评审通过
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const statistics = ref([
  { title: '总用户数', value: '1,284', tagText: '总计', tagType: '', trend: 5.2 },
  { title: '今日新增商品', value: '86', tagText: '今日', tagType: 'success', trend: 12.5 },
  { title: '待审核商品', value: '12', tagText: '待办', tagType: 'danger', trend: -2.4 },
  { title: '今日交易额', value: '¥ 3,420', tagText: '营收', tagType: 'warning', trend: 8.9 }
])

const todoList = ref([
  { date: '2023-11-01 10:23', title: '九成新 iPhone 13 128G 冰川蓝', user: '林学姐' },
  { date: '2023-11-01 09:45', title: '《计算机网络》第8版', user: '计科王同学' },
  { date: '2023-11-01 08:30', title: '捷安特自行车，毕业大甩卖', user: '张三' },
  { date: '2023-10-31 22:10', title: '考研复习神座，人体工学椅', user: '李四' }
])
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
        display: flex;
        align-items: center;
        
        .up { color: #f56c6c; margin-left: 8px; display: flex; align-items: center; }
        .down { color: #67c23a; margin-left: 8px; display: flex; align-items: center; }
      }
    }
  }
  
  .action-section {
    .card-title {
      font-weight: bold;
      color: #333;
    }
  }
}
</style>
