<template>
  <div class="page-root">
    <AppHeader @toggleCart="showCart = true" />
    <div class="page-container" style="margin-top: var(--space-xl)" v-if="detail">
      <h2 class="section-title">📋 订单详情 — {{ detail.order.orderNo }}</h2>

      <el-card style="margin-bottom:var(--space-lg);background:var(--bg-surface);border-color:var(--border-subtle)">
        <p>状态: <el-tag :type="statusType(detail.order.status)">{{ statusLabel(detail.order.status) }}</el-tag></p>
        <p>收货人: {{ detail.order.receiverName }} | {{ detail.order.receiverPhone }}</p>
        <p>地址: {{ detail.order.receiverAddr }}</p>
        <p>总额: <span class="price-big">¥{{ Number(detail.order.totalAmount).toLocaleString() }}</span></p>
      </el-card>

      <!-- 操作按钮 -->
      <div class="order-actions" v-if="detail.order.status !== 'COMPLETED' && detail.order.status !== 'CANCELLED'">
        <!-- 用户：支付 -->
        <el-button v-if="detail.order.status === 'PENDING' && userStore.role === 'USER'" type="primary" size="large" @click="payOrder" :loading="acting" style="border-radius:20px">💳 模拟支付</el-button>
        <!-- 用户：取消 -->
        <el-button v-if="detail.order.status === 'PENDING' && userStore.role === 'USER'" type="danger" size="large" @click="cancelOrder" :loading="acting" style="border-radius:20px">取消订单</el-button>
        <!-- 商家：发货 -->
        <el-button v-if="detail.order.status === 'PAID' && userStore.role === 'MERCHANT'" type="primary" size="large" @click="shipOrder" :loading="acting" style="border-radius:20px">📦 确认发货</el-button>
        <!-- 用户：确认收货 -->
        <el-button v-if="detail.order.status === 'SHIPPED' && userStore.role === 'USER'" type="success" size="large" @click="confirmOrder" :loading="acting" style="border-radius:20px">✅ 确认收货</el-button>
      </div>

      <h3 style="margin-top:var(--space-xl)">商品明细</h3>
      <div class="table-wrap">
        <el-table :data="detail.items" style="margin-top:var(--space-md)">
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="productPrice" label="单价"><template #default="{row}">¥{{ Number(row.productPrice).toLocaleString() }}</template></el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="amount" label="小计"><template #default="{row}">¥{{ Number(row.amount).toLocaleString() }}</template></el-table-column>
        </el-table>
      </div>
    </div>
    <CartDrawer :visible="showCart" @close="showCart = false" />
  </div>
  <AppFooter />
</template>

<script setup lang="ts">
import AppFooter from '@/components/AppFooter.vue'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
import CartDrawer from '@/components/CartDrawer.vue'

const route = useRoute()
const userStore = useUserStore()
const showCart = ref(false)
const detail = ref<any>(null)
const acting = ref(false)

function statusType(s: string) { return s === 'PENDING' ? 'warning' : s === 'PAID' ? 'primary' : s === 'SHIPPED' ? 'info' : s === 'COMPLETED' ? 'success' : 'danger' }
function statusLabel(s: string) {
  const map: Record<string, string> = { PENDING: '待付款', PAID: '已付款', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[s] || s
}

async function fetch() {
  const id = route.params.id
  const res: any = await request.get(`/order/${id}`)
  detail.value = res.data
}

async function payOrder() {
  acting.value = true
  try {
    const res: any = await request.put(`/order/${detail.value.order.id}/pay`)
    if (res.code !== 200) {
      ElMessage.error(res.message || '支付失败')
      return
    }
    ElMessage.success('支付成功')
    // 刷新余额
    const me: any = await request.get('/user/me')
    if (me.data && userStore.userInfo) {
      userStore.userInfo.balance = me.data.balance
      localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
    }
    fetch()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '支付失败')
  } finally { acting.value = false }
}

async function cancelOrder() {
  try {
    await ElMessageBox.confirm('确定取消该订单？', '确认', { type: 'warning' })
    acting.value = true
    await request.put(`/order/${detail.value.order.id}/cancel`)
    ElMessage.success('订单已取消')
    fetch()
  } catch { /* 取消操作 */ } finally { acting.value = false }
}

async function shipOrder() {
  acting.value = true
  try {
    const res: any = await request.put(`/order/${detail.value.order.id}/status`, { status: 'SHIPPED' })
    if (res.code !== 200) { ElMessage.error(res.message || '操作失败'); return }
    ElMessage.success('已发货')
    fetch()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally { acting.value = false }
}

async function confirmOrder() {
  acting.value = true
  try {
    const res: any = await request.put(`/order/${detail.value.order.id}/status`, { status: 'COMPLETED' })
    if (res.code !== 200) { ElMessage.error(res.message || '操作失败'); return }
    ElMessage.success('已确认收货')
    fetch()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally { acting.value = false }
}

onMounted(fetch)
</script>

<style scoped>
.price-big { color: var(--accent-amber); font-weight:700; font-size:1.2rem; }
.order-actions { display: flex; gap: var(--space-md); margin-bottom: var(--space-lg); }
</style>
