<template>
  <div>
    <AppHeader />
    <div class="page-container" style="margin-top: var(--space-xl)">
      <router-link to="/admin/users" class="back-link">← 返回用户管理</router-link>
      <h2 class="section-title">📋 {{ targetName }} 的订单</h2>
      <div class="table-wrap">
        <el-table :data="orders" v-loading="loading">
          <el-table-column prop="orderNo" label="订单号" width="220" />
          <el-table-column label="金额" width="120"><template #default="{row}"><span class="price-cell">¥{{ Number(row.totalAmount).toLocaleString() }}</span></template></el-table-column>
          <el-table-column prop="status" label="状态" width="110" align="center">
            <template #default="{row}"><el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="receiverName" label="收货人" width="100" />
          <el-table-column label="操作" align="center">
            <template #default="{row}">
              <el-button size="small" text @click="$router.push(`/orders/${row.id}`)">详情</el-button>
              <el-button v-if="row.status === 'PENDING'" size="small" text type="danger" @click="cancel(row.id)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'

const route = useRoute()
const userId = Number(route.query.userId)
const targetName = ref(route.query.name as string || '')

const orders = ref<any[]>([])
const loading = ref(false)

function statusType(s: string) { return s === 'PENDING' ? 'warning' : s === 'PAID' ? 'primary' : s === 'SHIPPED' ? 'info' : s === 'COMPLETED' ? 'success' : 'danger' }

async function fetch() {
  loading.value = true
  const res: any = await request.get('/order/list', { params: { userId, page: 1, size: 100 } })
  orders.value = res.data?.records || []
  loading.value = false
}

async function cancel(id: number) {
  await request.put(`/order/${id}/cancel`)
  ElMessage.success('订单已取消')
  fetch()
}

onMounted(fetch)
</script>
