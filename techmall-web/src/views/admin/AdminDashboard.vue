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

      <!-- 用户消费/商家销售列表 + 订单状态饼图 -->
      <div class="chart-row">
        <div class="chart-box" style="overflow:auto">
          <div class="chart-header">
            <h4>🧑‍💼 用户消费 / 商家销售</h4>
            <el-input v-model="userFilter" placeholder="搜索名称…" size="small" style="width:180px" clearable />
          </div>
          <div class="table-wrap" style="margin-top:var(--space-sm)">
          <el-table :data="filteredUsers" highlight-current-row @row-click="selectUser" max-height="260">
            <el-table-column label="名称"><template #default="{row}"><span class="user-name-cell">{{ row.nickname || row.username }}</span></template></el-table-column>
            <el-table-column label="角色" width="70"><template #default="{row}"><el-tag :type="row.role==='MERCHANT'?'warning':'info'" size="small">{{ row.role==='MERCHANT'?'商家':'用户' }}</el-tag></template></el-table-column>
            <el-table-column label="金额" width="120" align="right"><template #default="{row}"><span class="price-cell">¥{{ amountFor(row).toLocaleString() }}</span></template></el-table-column>
          </el-table>
          </div>
          <div v-if="pickedUser" class="user-detail">
            <div class="detail-bar">
              <span>{{ pickedUser.nickname || pickedUser.username }} 的{{ pickedUser.role==='MERCHANT' ? '商品销售' : '消费记录' }}</span>
              <a @click="pickedUser=null; pickedItems=[]" class="detail-close">✕ 关闭</a>
            </div>
            <div class="table-wrap" v-if="pickedItems.length">
              <el-table :data="pickedItems" size="small" max-height="200">
                <template v-if="pickedUser.role==='MERCHANT'">
                  <el-table-column prop="productName" label="商品" />
                  <el-table-column prop="quantity" label="销量" />
                  <el-table-column label="销售额"><template #default="{row}"><span class="price-cell">¥{{ Number(row.amount).toLocaleString() }}</span></template></el-table-column>
                </template>
                <template v-else>
                  <el-table-column prop="orderNo" label="订单号" />
                  <el-table-column label="金额"><template #default="{row}"><span class="price-cell">¥{{ Number(row.amount).toLocaleString() }}</span></template></el-table-column>
                  <el-table-column label="商品"><template #default="{row}">{{ row.items?.map((i:any)=>i.productName).join('、') }}</template></el-table-column>
                </template>
              </el-table>
            </div>
            <div v-else style="text-align:center;color:var(--text-muted);padding:var(--space-md)">暂无交易记录</div>
          </div>
        </div>
        <div class="chart-box">
          <h4>📊 订单状态分布</h4>
          <v-chart :option="pieChartOption" style="height:320px" autoresize />
        </div>
      </div>

      <div class="chart-row">
        <div class="chart-box">
          <h4>🔥 热销商品排行</h4>
          <v-chart :option="topProductsOption" style="height:300px" autoresize />
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

// 用户列表
const userFilter = ref('')
const pickedUser = ref<any>(null)
const pickedItems = ref<any[]>([])

function sType(s: string) { return s === 'PENDING' ? 'warning' : s === 'PAID' ? 'primary' : s === 'SHIPPED' ? 'info' : s === 'COMPLETED' ? 'success' : 'danger' }

const statusNames: Record<string,string> = { PENDING:'待付款', PAID:'已付款', SHIPPED:'已发货', COMPLETED:'已完成', CANCELLED:'已取消' }

const filteredUsers = computed(() => {
  const t = userFilter.value.toLowerCase()
  return users.value.filter((u:any)=> u.role !== 'ADMIN' && (u.nickname||u.username||'').toLowerCase().includes(t))
})

function amountFor(user: any) {
  const map = user.role === 'MERCHANT' ? orderStats.value.byMerchant : orderStats.value.byUser
  return Number((map || {})[String(user.id)] || 0)
}

function selectUser(row: any) {
  pickedUser.value = row
  const uid = String(row.id)
  if (row.role === 'MERCHANT') {
    // 遍历所有用户订单，筛选该商家商品
    const all = orderStats.value.userOrders || {}
    const agg: Record<number,any> = {}
    for (const key of Object.keys(all)) {
      for (const o of (all[key] || [])) {
        for (const item of (o.items || [])) {
          if (Number(item.merchantId) === Number(row.id)) {
            const k = item.productId
            if (!agg[k]) agg[k] = { productName: item.productName, quantity: 0, amount: 0 }
            agg[k].quantity += Number(item.quantity || 0)
            agg[k].amount += Number(item.amount || 0)
          }
        }
      }
    }
    pickedItems.value = Object.values(agg)
  } else {
    const orders = (orderStats.value.userOrders || {})[uid] || []
    pickedItems.value = orders
  }
}


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

const topProductsOption = computed(() => {
  const all = orderStats.value.userOrders || {}
  const agg: Record<string,{name:string,qty:number;amount:number}> = {}
  for (const key of Object.keys(all)) {
    for (const o of (all[key] || [])) {
      for (const item of (o.items || [])) {
        const name = item.productName || ''
        if (!agg[name]) agg[name] = { name, qty: 0, amount: 0 }
        agg[name].qty += Number(item.quantity || 0)
        agg[name].amount += Number(item.amount || 0)
      }
    }
  }
  const sorted = Object.values(agg).sort((a,b) => b.amount - a.amount).slice(0, 8).reverse()
  return {
    tooltip: {
      trigger: 'axis',
      formatter: (p:any) => {
        const d = sorted.find((s:any) => (s.name.length > 12 ? s.name.slice(0,12)+'…' : s.name) === p[0].name)
        const full = d || sorted[p[0].dataIndex]
        return `<b>${full?.name || p[0].name}</b><br/>销量: ${full?.qty || 0} 件 | 销售额: ¥${(full?.amount || 0).toLocaleString()}`
      }
    },
    grid: { left: 130, right: 60, top: 10, bottom: 20 },
    xAxis: { type: 'value', axisLabel: { color: '#94a3b8', fontSize: 10 } },
    yAxis: { type: 'category', data: sorted.map(s => s.name.length > 12 ? s.name.slice(0,12)+'…' : s.name), axisLabel: { color: '#94a3b8', fontSize: 10 } },
    series: [{ type: 'bar', data: sorted.map(s => s.amount), itemStyle: { color: '#00c6f2', borderRadius: [0,4,4,0] }, barWidth: 18, label: { show: true, position: 'right', color: '#94a3b8', fontSize: 10, formatter: (p:any) => '¥'+Number(p.value).toLocaleString() } }],
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
  // 先加载用户和产品（填充 merchantNames）
  const [userRes, productRes, allUsersRes, allProductsRes] = await Promise.all([
    request.get('/user/list', { params: { page:1, size:1 } }),
    request.get('/product/list', { params: { page:1, size:1, includeOffShelf: true } }),
    request.get('/user/list', { params: { page:1, size:100 } }),
    request.get('/product/list', { params: { page:1, size:100, includeOffShelf: true } }),
  ])
  userCount.value = userRes.data?.total || 0
  productCount.value = productRes.data?.total || 0
  users.value = allUsersRes.data?.records || []
  products.value = allProductsRes.data?.records || []

  // 加载订单统计
  try {
    const orderRes: any = await request.get('/order/stats')
    orderStats.value = orderRes.data || {}
  } catch {}
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
.detail-close { font-size: 0.8rem; color: var(--accent-cyan); cursor: pointer; }
.detail-close:hover { opacity: 0.8; }
.user-name-cell { cursor: pointer; }
.table-wrap :deep(.el-table__body tr) { cursor: pointer; }
.table-wrap :deep(.el-table__body tr.current-row td) { background: rgba(0,198,242,0.08) !important; }
.user-detail { margin-top: var(--space-md); padding-top: var(--space-md); border-top: 1px solid var(--border-subtle); }
.detail-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-sm); font-size: 0.85rem; font-weight: 600; }
@media (max-width: 768px) { .stat-cards { grid-template-columns: repeat(2,1fr); } .chart-row { grid-template-columns: 1fr; } }
</style>
