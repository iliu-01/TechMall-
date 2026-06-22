<template>
  <div>
    <AppHeader />
    <div class="page-container" style="margin-top: var(--space-xl)">
      <h2 class="section-title">📊 数据概览</h2>

      <!-- 统计卡片 -->
      <div class="stat-cards">
        <div class="stat-card" v-for="c in statCards" :key="c.label">
          <div class="stat-icon">{{ c.icon }}</div>
          <div class="stat-body">
            <div class="stat-value">{{ c.value }}</div>
            <div class="stat-label">{{ c.label }}</div>
          </div>
        </div>
      </div>

      <!-- 图表行 -->
      <div class="chart-row">
        <div class="chart-box">
          <h4>📈 销售额分布（按订单状态）</h4>
          <v-chart :option="statusChartOption" style="height:320px" autoresize />
        </div>
        <div class="chart-box">
          <h4>📊 订单状态分布</h4>
          <v-chart :option="pieChartOption" style="height:320px" autoresize />
        </div>
      </div>

      <div class="chart-row">
        <div class="chart-box">
          <h4>🏪 各商家商品数量</h4>
          <v-chart :option="merchantChartOption" style="height:300px" autoresize />
        </div>
        <div class="chart-box">
          <h4>🧑‍💼 用户角色分布</h4>
          <v-chart :option="rolePieOption" style="height:300px" autoresize />
        </div>
      </div>

      <!-- 最近订单 -->
      <div class="table-wrap" style="margin-top:var(--space-lg)">
        <h4 style="padding:var(--space-md) var(--space-lg) 0;font-family:var(--font-display)">📋 最近订单</h4>
        <el-table :data="recentOrders" style="width:100%">
          <el-table-column prop="orderNo" label="订单号" width="200" />
          <el-table-column prop="totalAmount" label="金额" width="120"><template #default="{row}">¥{{ Number(row.totalAmount).toLocaleString() }}</template></el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center"><template #default="{row}"><el-tag :type="sType(row.status)" size="small">{{ row.status }}</el-tag></template></el-table-column>
          <el-table-column prop="createdAt" label="时间" width="180"><template #default="{row}">{{ row.createdAt?.slice(0,16)?.replace('T',' ') }}</template></el-table-column>
        </el-table>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'

use([BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer])

const orderStats = ref<any>({ byStatus: {}, recentOrders: [] })
const userCount = ref(0)
const productCount = ref(0)
const users = ref<any[]>([])
const products = ref<any[]>([])

function sType(s: string) { return s === 'PENDING' ? 'warning' : s === 'PAID' ? 'primary' : s === 'SHIPPED' ? 'info' : s === 'COMPLETED' ? 'success' : 'danger' }

const statusNames: Record<string,string> = { PENDING:'待付款', PAID:'已付款', SHIPPED:'已发货', COMPLETED:'已完成', CANCELLED:'已取消' }

const statCards = computed(() => [
  { icon: '👥', value: userCount.value, label: '用户总数' },
  { icon: '📦', value: productCount.value, label: '商品总数' },
  { icon: '📋', value: orderStats.value.totalOrders || 0, label: '订单总数' },
  { icon: '💰', value: '¥' + Number(orderStats.value.totalSales || 0).toLocaleString(), label: '总销售额' },
])

const recentOrders = computed(() => orderStats.value.recentOrders || [])

const statusChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 60, right: 20, top: 30, bottom: 30 },
  xAxis: { type: 'category', data: Object.keys(orderStats.value.byStatus || {}).map(k => statusNames[k]||k), axisLabel: { color: '#94a3b8', fontSize: 11 } },
  yAxis: { type: 'value', axisLabel: { color: '#94a3b8' } },
  series: [{
    type: 'bar', data: Object.values(orderStats.value.byStatus || {}),
    itemStyle: { color: '#00c6f2', borderRadius: [4,4,0,0] }, barWidth: 32,
    label: { show: true, position: 'top', color: '#94a3b8', fontSize: 11 },
  }],
}))

const pieChartOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c}单 ({d}%)' },
  legend: { bottom: 0, textStyle: { color: '#94a3b8', fontSize: 11 } },
  series: [{
    type: 'pie', radius: ['45%', '72%'], center: ['50%', '45%'], avoidLabelOverlap: false,
    label: { show: true, formatter: '{d}%', color: '#94a3b8', fontSize: 11 },
    itemStyle: { borderRadius: 4, borderColor: '#0b111e', borderWidth: 2 },
    data: Object.entries(orderStats.value.byStatus || {}).map(([k, v]) => ({ name: statusNames[k]||k, value: v })),
  }],
  color: ['#f59e0b', '#00c6f2', '#3b82f6', '#22c55e', '#64748b'],
}))

const merchantChartOption = computed(() => {
  const map: Record<string,number> = {}
  for (const p of products.value) {
    const mid = '商家#' + p.merchantId
    map[mid] = (map[mid]||0) + 1
  }
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 80, right: 40, top: 30, bottom: 30 },
    xAxis: { type: 'value', axisLabel: { color: '#94a3b8' } },
    yAxis: { type: 'category', data: Object.keys(map), axisLabel: { color: '#94a3b8', fontSize: 11 } },
    series: [{
      type: 'bar', data: Object.values(map), itemStyle: { color: '#f59e0b', borderRadius: [0,4,4,0] }, barWidth: 24,
      label: { show: true, position: 'right', color: '#94a3b8', fontSize: 11 },
    }],
  }
})

const rolePieOption = computed(() => {
  const roles: Record<string,number> = {}
  for (const u of users.value) { roles[u.role] = (roles[u.role]||0) + 1 }
  const labels: Record<string,string> = { USER:'普通用户', MERCHANT:'商家', ADMIN:'管理员' }
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#94a3b8', fontSize: 11 } },
    series: [{
      type: 'pie', radius: ['45%','72%'], center: ['50%','45%'], avoidLabelOverlap: false,
      label: { show: true, formatter: '{d}%', color: '#94a3b8', fontSize: 11 },
      itemStyle: { borderRadius: 4, borderColor: '#0b111e', borderWidth: 2 },
      data: Object.entries(roles).map(([k, v]) => ({ name: labels[k]||k, value: v })),
    }],
    color: ['#3b82f6', '#f59e0b', '#f43f5e'],
  }
})

onMounted(async () => {
  const [orderRes, userRes, productRes] = await Promise.all([
    request.get('/order/stats'),
    request.get('/user/list', { params: { page:1, size:1 } }),
    request.get('/product/list', { params: { page:1, size:1, includeOffShelf: true } }),
  ])
  orderStats.value = orderRes.data || {}
  userCount.value = userRes.data?.total || 0
  productCount.value = productRes.data?.total || 0

  // 获取全量数据用于图表
  const [allUsers, allProducts] = await Promise.all([
    request.get('/user/list', { params: { page:1, size:100 } }),
    request.get('/product/list', { params: { page:1, size:100, includeOffShelf: true } }),
  ])
  users.value = allUsers.data?.records || []
  products.value = allProducts.data?.records || []
})
</script>

<style scoped>
.stat-cards { display: grid; grid-template-columns: repeat(4,1fr); gap: var(--space-md); margin-bottom: var(--space-xl); }
.stat-card {
  background: var(--bg-surface); border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md); padding: var(--space-lg);
  display: flex; align-items: center; gap: var(--space-md);
}
.stat-icon { font-size: 2rem; }
.stat-value { font-family: var(--font-display); font-weight: 700; font-size: 1.3rem; }
.stat-label { font-size: 0.75rem; color: var(--text-muted); }
.chart-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-md); margin-bottom: var(--space-md); }
.chart-box { background: var(--bg-surface); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); padding: var(--space-lg); }
.chart-box h4 { font-family: var(--font-display); font-size: 0.85rem; color: var(--text-secondary); margin-bottom: var(--space-md); }
@media (max-width: 768px) { .stat-cards { grid-template-columns: repeat(2,1fr); } .chart-row { grid-template-columns: 1fr; } }
</style>
