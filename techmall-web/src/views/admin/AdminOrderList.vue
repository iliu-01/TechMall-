<template>
  <div class="page-root">
    <AppHeader />
    <div class="page-container" style="margin-top: var(--space-xl)">
      <h2 class="section-title">📊 全站订单</h2>
        <div class="table-wrap">
        <el-table :data="orders" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="totalAmount" label="金额" width="120"><template #default="{row}">¥{{ row.totalAmount }}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}"><el-tag :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="receiverName" label="收货人" />
        <el-table-column label="操作">
          <template #default="{row}">
            <el-button size="small" @click="$router.push(`/orders/${row.id}`)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
    </div>
  </div>
  <AppFooter />
</template>

<script setup lang="ts">
import AppFooter from '@/components/AppFooter.vue'
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import AppHeader from '@/components/AppHeader.vue'



const orders = ref<any[]>([])
const loading = ref(false)

function statusType(s: string) { return s === 'PENDING' ? 'warning' : s === 'PAID' ? 'primary' : s === 'SHIPPED' ? 'info' : s === 'COMPLETED' ? 'success' : 'danger' }

onMounted(async () => {
  loading.value = true
  const res: any = await request.get('/order/list', { params: { page: 1, size: 100 } })
  orders.value = res.data?.records || []
  loading.value = false
})
</script>
