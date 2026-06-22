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

      <!-- 用户消费表 + 订单状态饼图 -->
      <div class="chart-row">
        <div class="chart-box" style="overflow:auto">
          <div class="chart-header">
            <h4>🧑‍💼 用户消费 / 商家销售</h4>
            <el-input v-model="userFilter" placeholder="搜索用户名…" size="small" style="width:180px" clearable />
          </div>
          <el-table :data="filteredUsers" style="width:100%;min-width:400px" highlight-current-row @row-click="selectUser" max-height="260">
            <el-table-column prop="nickname" label="名称" min-width="120"><template #default="{row}"><span class="user-name-cell">{{ row.nickname || row.username }}</span></template></el-table-column>
            <el-table-column prop="role" label="角色" width="80" align="center"><template #default="{row}"><el-tag :type="row.role==='MERCHANT'?'warning':'info'" size="small">{{ row.role==='MERCHANT'?'商家':'用户' }}</el-tag></template></el-table-column>
            <el-table-column label="金额" width="140" align="right"><template #default="{row}"><span class="price-cell">¥{{ Number(row.role==='MERCHANT' ? (orderStats.value.byMerchant||{})[String(row.id)] : (orderStats.value.byUser||{})[String(row.id)] ||0).toLocaleString() }}</span></template></el-table-column>
          </el-table>
          <!-- 选中用户详情 -->
          <div v-if="selectedUserId" class="user-detail">
            <div class="detail-bar">
              <span>{{ selectedUserName }} 的{{ selectedUserRole==='MERCHANT' ? '商品销售' : '消费记录' }}</span>
              <a @click="selectedUserId=null" style="color:var(--accent-cyan);cursor:pointer">✕ 关闭</a>
            </div>
            <el-table :data="selectedUserItems" style="width:100%;min-width:350px" size="small" max-height="220">
              <template v-if="selectedUserRole==='MERCHANT'">
                <el-table-column prop="productName" label="商品" min-width="140" />
                <el-table-column prop="price" label="单价" width="90" align="right"><template #default="{row}">¥{{ Number(row.price).toLocaleString() }}</template></el-table-column>
                <el-table-column prop="quantity" label="数量" width="60" align="center" />
                <el-table-column prop="amount" label="小计" width="100" align="right"><template #default="{row}"><span class="price-cell">¥{{ Number(row.amount).toLocaleString() }}</span></template></el-table-column>
              </template>
              <template v-else>
                <el-table-column prop="orderNo" label="订单号" width="180" />
                <el-table-column prop="amount" label="金额" width="100" align="right"><template #default="{row}"><span class="price-cell">¥{{ Number(row.amount).toLocaleString() }}</span></template></el-table-column>
                <el-table-column label="包含商品" min-width="180"><template #default="{row}">{{ row.items?.map((i:any)=>i.productName).join('、') }}</template></el-table-column>
              </template>
            </el-table>
          </div>
        </div>
        <div class="chart-box">
          <h4>📊 订单状态分布</h4>
          <v-chart :option="pieChartOption" style="height:320px" autoresize />
        </div>
      </div>

      <div class="chart-row">
        <div class="chart-box">
          <div class="chart-header">
            <h4 v-if="selectedUserId">📈 {{ selectedUserName }} 的{{ selectedUserRole==='MERCHANT' ? '商品销售' : '消费' }}明细</h4>
            <h4 v-else>🏪 各商家销售额</h4>
            <span class="chart-toggle" @click="detailChartType = detailChartType==='bar'?'pie':'bar'">{{ detailChartType==='bar' ? '📊饼图' : '📊柱图' }}</span>
          </div>
          <v-chart v-if="selectedUserId" :option="userDetailChartOption" style="height:300px" autoresize />
          <v-chart v-else :option="merchantChartOption" style="height:300px" autoresize />
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

const orderStats = ref<any>({ byStatus: {}, recentOrders: [], byMerchant: {}, byUser: {}, userOrders: {} })
const userCount = ref(0)
const productCount = ref(0)
const users = ref<any[]>([])
const products = ref<any[]>([])
const detailChartType = ref('bar')

// 用户表逻辑
const userFilter = ref('')
const selectedUserId = ref<number | null>(null)
const selectedUserName = ref('')
const selectedUserRole = ref('')
const selectedUserItems = ref<any[]>([])

const filteredUsers = computed(() => {
  const term = userFilter.value.toLowerCase()
  return users.value.filter((u:any)=> {
    if (u.role === 'ADMIN') return false
    return (u.nickname||u.username||'').toLowerCase().includes(term)
  })
})

function selectUser(row: any) {
  selectedUserId.value = row.id
  selectedUserName.value = row.nickname || row.username
  selectedUserRole.value = row.role
  const uid = String(row.id)

  if (row.role === 'MERCHANT') {
    // 遍历所有用户的订单，筛选该商家的商品
    const allOrders: Record<string,any[]> = orderStats.value.userOrders || {}
    const map: Record<number,any> = {}
    for (const uidKey of Object.keys(allOrders)) {
      for (const o of (allOrders[uidKey] || [])) {
        for (const item of (o.items || [])) {
          if (Number(item.merchantId) === Number(row.id)) {
            const key = item.productId
            if (!map[key]) map[key] = { productName: item.productName, price: Number(item.price||0), quantity: 0, amount: 0 }
            map[key].quantity += Number(item.quantity || 0)
            map[key].amount += Number(item.amount || 0)
          }
        }
      }
    }
    if (Object.keys(map).length > 0) {
      selectedUserItems.value = Object.values(map)
    } else {
      // 无销售记录，展示该商家商品列表
      selectedUserItems.value = products.value.filter((p: any) => p.merchantId === row.id).map((p: any) => ({
        productName: p.name, price: p.price, quantity: 0, amount: 0,
      }))
    }
  } else {
    selectedUserItems.value = (orderStats.value.userOrders?.[uid] || []) as any[]
  }
}

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
  const isBar = detailChartType.value === 'bar'
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
    return { ...base, xAxis: { type: 'value', axisLabel: { color: '#94a3b8' } }, yAxis: { type: 'category', data: labels, axisLabel: { color: '#94a3b8', fontSize: 11 } }, series, color: ['#f59e0b','#00c6f2','#22c55e'] }
  }
  return { ...base, series, legend: { bottom: 0, textStyle: { color: '#94a3b8', fontSize: 11 } }, color: ['#f59e0b','#00c6f2','#22c55e'] }
})

const userDetailChartOption = computed(() => {
  const items = selectedUserItems.value
  const labels = items.map((i:any) => i.productName || i.orderNo || '—')
  const values = items.map((i:any) => Number(i.amount || 0))
  const isBar = detailChartType.value === 'bar'
  if (isBar) {
    return {
      tooltip: { trigger: 'axis' },
      grid: { left: 120, right: 40, top: 20, bottom: 30 },
      xAxis: { type: 'value', axisLabel: { color: '#94a3b8' } },
      yAxis: { type: 'category', data: labels, axisLabel: { color: '#94a3b8', fontSize: 11 } },
      series: [{ type: 'bar', data: values, itemStyle: { color: '#00c6f2', borderRadius: [0,4,4,0] }, barWidth: 20, label: { show: true, position: 'right', color: '#94a3b8', fontSize: 10 } }],
    }
  }
  return {
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#94a3b8', fontSize: 10 } },
    series: [{ type: 'pie', radius: ['45%','72%'], center: ['50%','50%'], avoidLabelOverlap: false, label: { show: true, formatter: '{d}%', color: '#94a3b8', fontSize: 10 }, itemStyle: { borderRadius: 4, borderColor: '#0b111e', borderWidth: 2 }, data: labels.map((n,i) => ({ name: n, value: values[i] })) }],
    color: ['#f59e0b','#00c6f2','#3b82f6','#22c55e','#a855f7','#f43f5e'],
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
  // 独立请求，避免一个失败影响全部
  try { const r: any = await request.get('/order/stats'); orderStats.value = r.data || {} } catch {}
  try { const r: any = await request.get('/user/list', { params: { page:1, size:1 } }); userCount.value = r.data?.total || 0 } catch {}
  try { const r: any = await request.get('/product/list', { params: { page:1, size:1, includeOffShelf:true } }); productCount.value = r.data?.total || 0 } catch {}
  try { const r: any = await request.get('/user/list', { params: { page:1, size:100 } }); users.value = r.data?.records || [] } catch {}
  try { const r: any = await request.get('/product/list', { params: { page:1, size:100, includeOffShelf:true } }); products.value = r.data?.records || [] } catch {}

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
.user-name-cell { font-weight: 600; cursor: pointer; }
.user-detail { margin-top: var(--space-md); padding-top: var(--space-md); border-top: 1px solid var(--border-subtle); }
.detail-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-sm); font-size: 0.85rem; font-weight: 600; }
@media (max-width: 768px) { .stat-cards { grid-template-columns: repeat(2,1fr); } .chart-row { grid-template-columns: 1fr; } }
</style>
