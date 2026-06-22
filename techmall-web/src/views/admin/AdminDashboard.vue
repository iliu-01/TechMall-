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
          <div class="chart-header">
            <h4>🏪 各商家销售额</h4>
            <span class="chart-toggle" @click="merchantChartType = merchantChartType==='bar'?'pie':'bar'">{{ merchantChartType==='bar' ? '📊饼图' : '📊柱图' }}</span>
          </div>
          <v-chart :option="merchantChartOption" style="height:300px" autoresize />
          <div v-if="selectedMerchant" class="merchant-detail">
            <span>已选: 商家#{{ selectedMerchant }} | 销售额 ¥{{ Number(selectedMerchantSales).toLocaleString() }}</span>
            <a @click="selectedMerchant=null" style="color:var(--accent-cyan);cursor:pointer;margin-left:8px">清除</a>
          </div>
        </div>
        <div class="chart-box">
          <h4>🧑‍💼 用户角色分布</h4>
          <v-chart :option="rolePieOption" style="height:300px" autoresize />
        </div>
      </div>

      <!-- 最近订单 -->
      <div class="table-wrap" style="margin-top:var(--space-lg)">
        <h4 style="padding:var(--space-md) var(--space-lg) 0;font-family:var(--font-display)">📋 最近订单</h4>
        <el-table :data="recentOrders" style="width:100%" @row-click="(row:any) => $router.push(`/orders/${row.id}`)">
          <el-table-column prop="orderNo" label="订单号" min-width="200" />
          <el-table-column label="金额" width="130"><template #default="{row}"><span class="price-cell">¥{{ Number(row.totalAmount).toLocaleString() }}</span></template></el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center"><template #default="{row}"><el-tag :type="sType(row.status)" size="small">{{ row.status }}</el-tag></template></el-table-column>
          <el-table-column prop="receiverName" label="收货人" width="100" />
          <el-table-column label="时间" min-width="160"><template #default="{row}">{{ row.createdAt?.slice(0,16)?.replace('T',' ') }}</template></el-table-column>
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

const orderStats = ref<any>({ byStatus: {}, recentOrders: [], byMerchant: {} })
const userCount = ref(0)
const productCount = ref(0)
const users = ref<any[]>([])
const products = ref<any[]>([])
const merchantChartType = ref('bar')
const selectedMerchant = ref<number | null>(null)
const selectedMerchantSales = ref(0)

function sType(s: string) { return s === 'PENDING' ? 'warning' : s === 'PAID' ? 'primary' : s === 'SHIPPED' ? 'info' : s === 'COMPLETED' ? 'success' : 'danger' }

const statusNames: Record<string,string> = { PENDING:'待付款', PAID:'已付款', SHIPPED:'已发货', COMPLETED:'已完成', CANCELLED:'已取消' }

const merchantNames: Record<number,string> = {}


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
  const map = orderStats.value.byMerchant || {}
  const labels = Object.keys(map).map(k => merchantNames[Number(k)] || `商家#${k}`)
  const values: number[] = Object.values(map).map((v:any) => Number(v))
  const isBar = merchantChartType.value === 'bar'
  const base = {
    tooltip: { trigger: isBar ? 'axis' : 'item', formatter: isBar ? undefined : '{b}: ¥{c} ({d}%)' },
    grid: isBar ? { left: 100, right: 40, top: 30, bottom: 30 } : undefined,
  }
  const series = [{
    type: isBar ? 'bar' : 'pie',
    data: isBar ? values : labels.map((n,i) => ({ name: n, value: values[i] })),
    itemStyle: isBar ? { color: '#f59e0b', borderRadius: [0,4,4,0] } : { borderRadius: 4, borderColor: '#0b111e', borderWidth: 2 },
    barWidth: isBar ? 24 : undefined,
    radius: isBar ? undefined : ['45%','72%'],
    center: isBar ? undefined : ['50%','50%'],
    label: { show: true, position: isBar?'right':undefined, formatter: isBar?undefined:'{d}%', color: '#94a3b8', fontSize: 11 },
    avoidLabelOverlap: false,
  }]
  if (isBar) {
    return {
      ...base,
      xAxis: { type: 'value', axisLabel: { color: '#94a3b8' } },
      yAxis: { type: 'category', data: labels, axisLabel: { color: '#94a3b8', fontSize: 11 } },
      series,
      color: ['#f59e0b','#00c6f2','#22c55e'],
    }
  }
  return { ...base, series, legend: { bottom: 0, textStyle: { color: '#94a3b8', fontSize: 11 } }, color: ['#f59e0b','#00c6f2','#22c55e'] }
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

  // 获取商家名称
  for (const u of users.value) {
    if (u.role === 'MERCHANT') {
      merchantNames[u.id] = u.nickname || u.username
    }
  }
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
.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-md); }
.chart-header h4 { margin-bottom: 0; }
.chart-toggle { font-size: 0.75rem; color: var(--accent-cyan); cursor: pointer; user-select: none; }
.chart-toggle:hover { opacity: 0.8; }
.merchant-detail { font-size: 0.8rem; color: var(--text-secondary); margin-top: var(--space-sm); }
@media (max-width: 768px) { .stat-cards { grid-template-columns: repeat(2,1fr); } .chart-row { grid-template-columns: 1fr; } }
</style>
