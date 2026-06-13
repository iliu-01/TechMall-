<template>
  <div>
    <AppHeader @toggleCart="showCart = true" />
    <div class="page-container" style="margin-top: var(--space-xl)" v-if="detail">
      <h2 class="section-title">📋 订单详情 — {{ detail.order.orderNo }}</h2>
      <el-card style="margin-bottom:var(--space-lg);background:var(--bg-surface);border-color:var(--border-subtle)">
        <p>状态: <el-tag :type="statusType(detail.order.status)">{{ detail.order.status }}</el-tag></p>
        <p>收货人: {{ detail.order.receiverName }} | {{ detail.order.receiverPhone }}</p>
        <p>地址: {{ detail.order.receiverAddr }}</p>
        <p>总额: <span style="color:var(--accent-amber);font-weight:700;font-size:1.2rem">¥{{ detail.order.totalAmount }}</span></p>
      </el-card>
      <h3>商品明细</h3>
      <el-table :data="detail.items" style="width:100%;background:var(--bg-surface);margin-top:var(--space-md)">
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="productPrice" label="单价"><template #default="{row}">¥{{ row.productPrice }}</template></el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="amount" label="小计"><template #default="{row}">¥{{ row.amount }}</template></el-table-column>
      </el-table>
    </div>
    <CartDrawer :visible="showCart" @close="showCart = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import AppHeader from '@/components/AppHeader.vue'
import CartDrawer from '@/components/CartDrawer.vue'

const route = useRoute()
const showCart = ref(false)
const detail = ref<any>(null)

function statusType(s: string) { return s === 'PENDING' ? 'warning' : s === 'PAID' ? 'primary' : s === 'SHIPPED' ? 'info' : s === 'COMPLETED' ? 'success' : 'danger' }

onMounted(async () => {
  const id = route.params.id
  const res: any = await request.get(`/order/${id}`)
  detail.value = res.data
})
</script>
