<template>
  <div>
    <AppHeader @toggleCart="showCart = true" />
    <div class="page-container" style="margin-top: var(--space-xl)">
      <h2 class="section-title">🛒 购物车</h2>

      <div v-if="cartStore.totalCount === 0" style="text-align:center;padding:var(--space-3xl)">购物车是空的</div>

      <template v-else>
        <div v-for="item in cartStore.items" :key="item.productId" class="cart-item-row">
          <span style="font-size:2rem">{{ itemIcon(item.name) }}</span>
          <span class="ci-name">{{ item.name }}</span>
          <el-input-number v-model="item.quantity" :min="1" size="small" @change="cartStore.updateQuantity(item.productId, item.quantity)" />
          <span class="ci-price">¥{{ (item.price * item.quantity).toLocaleString() }}</span>
          <el-button type="danger" size="small" circle @click="cartStore.removeItem(item.productId)">✕</el-button>
        </div>

        <el-card style="margin-top:var(--space-xl);background:var(--bg-surface);border-color:var(--border-subtle)">
          <h3 style="margin-bottom:var(--space-lg)">收货信息</h3>
          <el-form :model="orderForm" label-width="80px">
            <el-form-item label="收货人"><el-input v-model="orderForm.receiverName" /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="orderForm.receiverPhone" /></el-form-item>
            <el-form-item label="地址"><el-input v-model="orderForm.receiverAddr" /></el-form-item>
          </el-form>
          <div style="text-align:right;margin-top:var(--space-lg)">
            <span style="font-size:1.2rem;margin-right:var(--space-lg)">
              合计: <span style="color:var(--accent-amber);font-family:var(--font-display);font-weight:700;font-size:1.5rem">¥{{ cartStore.totalPrice.toLocaleString() }}</span>
            </span>
            <el-button type="primary" size="large" @click="submitOrder" :loading="submitting" :disabled="!userStore.isLoggedIn" style="border-radius:24px">
              {{ userStore.isLoggedIn ? '提交订单' : '请先登录' }}
            </el-button>
          </div>
        </el-card>
      </template>
    </div>
    <CartDrawer :visible="showCart" @close="showCart = false" />
  </div>
  <AppFooter />
</template>

<script setup lang="ts">
import AppFooter from '@/components/AppFooter.vue'
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
import CartDrawer from '@/components/CartDrawer.vue'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const showCart = ref(false)
const submitting = ref(false)

const orderForm = reactive({
  receiverName: '',
  receiverPhone: '',
  receiverAddr: '',
})

function itemIcon(name: string) {
  if (name.includes('手机')) return '📱'
  if (name.includes('笔记本')) return '💻'
  if (name.includes('耳机')) return '🎧'
  return '📦'
}

async function submitOrder() {
  if (!orderForm.receiverName || !orderForm.receiverPhone || !orderForm.receiverAddr) {
    ElMessage.warning('请填写收货信息')
    return
  }
  submitting.value = true
  try {
    await request.post('/order', {
      items: cartStore.items.map(i => ({ productId: i.productId, quantity: i.quantity })),
      receiverName: orderForm.receiverName,
      receiverPhone: orderForm.receiverPhone,
      receiverAddr: orderForm.receiverAddr,
    })
    ElMessage.success('下单成功')
    cartStore.clear()
    router.push('/orders')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '下单失败')
  } finally { submitting.value = false }
}
</script>

<style scoped>
.cart-item-row {
  display: flex; align-items: center; gap: var(--space-lg);
  padding: var(--space-md); background: var(--bg-surface);
  border: 1px solid var(--border-subtle); border-radius: var(--radius-md);
  margin-bottom: var(--space-sm);
}
.ci-name { flex: 1; font-weight: 500; }
.ci-price { font-family: var(--font-display); font-weight: 700; color: var(--accent-amber); }
</style>
