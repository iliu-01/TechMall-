<template>
  <div>
    <AppHeader @toggleCart="showCart = true" />
    <div class="page-container" style="margin-top: var(--space-xl)">
      <h2 class="section-title">📋 我的订单</h2>
      <el-table :data="orders" v-loading="loading" style="width:100%;background:var(--bg-surface)" :header-cell-style="{background:'var(--bg-elevated)',color:'var(--text-secondary)'}" :cell-style="{background:'var(--bg-surface)'}" stripe>
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="totalAmount" label="金额" width="120"><template #default="{row}">¥{{ row.totalAmount }}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="120"><template #default="{row}"><el-tag :type="statusType(row.status)">{{ row.status }}</el-tag></template></el-table-column>
        <el-table-column prop="receiverName" label="收货人" width="100" />
        <el-table-column label="操作"><template #default="{row}">
          <el-button size="small" @click="$router.push(`/orders/${row.id}`)">详情</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="danger" @click="cancelOrder(row.id)">取消</el-button>
        </template></el-table-column>
      </el-table>
      <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
    </div>
    <CartDrawer :visible="showCart" @close="showCart = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
import CartDrawer from '@/components/CartDrawer.vue'

const showCart = ref(false)
const orders = ref<any[]>([])
const loading = ref(false)

function statusType(s: string) { return s === 'PENDING' ? 'warning' : s === 'PAID' ? 'primary' : s === 'SHIPPED' ? 'info' : s === 'COMPLETED' ? 'success' : 'danger' }

async function fetchOrders() {
  loading.value = true
  const res: any = await request.get('/order/my', { params: { page: 1, size: 50 } })
  orders.value = res.data?.records || []
  loading.value = false
}

async function cancelOrder(id: number) {
  await request.put(`/order/${id}/cancel`)
  ElMessage.success('订单已取消')
  fetchOrders()
}

onMounted(fetchOrders)
</script>
