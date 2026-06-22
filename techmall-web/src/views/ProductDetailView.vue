<template>
  <div>
    <AppHeader @toggleCart="showCart = true" />
    <div class="page-container" style="margin-top: var(--space-xl)" v-if="product">
      <router-link v-if="backSearch" :to="backSearch" class="back-link">← 返回搜索结果</router-link>
      <router-link to="/home" class="back-link" :style="{marginLeft: backSearch ? 'var(--space-md)' : '0'}">← 返回首页</router-link>
      <div class="detail-layout">
        <div class="detail-img"><span style="font-size:6rem">{{ icon }}</span></div>
        <div class="detail-info">
          <h1 class="detail-name">{{ product.name }}</h1>
          <p class="detail-desc">{{ product.description }}</p>
          <div class="detail-specs">
            <span v-for="s in specs" :key="s" class="spec-chip">{{ s }}</span>
          </div>
          <div class="detail-price">¥{{ product.price }}</div>
          <div class="merchant-row">
            <span>库存: {{ product.stock ?? 0 }} 件</span>
            <span v-if="merchantName" class="merchant-link" @click="$router.push({path:'/products', query:{merchantId: product.merchantId}})">
              🏪 {{ merchantName }} 的店铺 →
            </span>
          </div>
          <div class="detail-actions" v-if="userStore.role === 'USER'">
            <el-input-number v-model="qty" :min="1" :max="Math.max(product.stock ?? 0, 1)" :disabled="(product.stock ?? 0) === 0" size="large" />
            <el-button type="primary" size="large" @click="addToCart" style="border-radius:24px" :disabled="(product.stock ?? 0) === 0">
              {{ (product.stock ?? 0) === 0 ? '已售罄' : '加入购物车' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
    <CartDrawer :visible="showCart" @close="showCart = false" />
  </div>
  <AppFooter />
</template>

<script setup lang="ts">
import AppFooter from '@/components/AppFooter.vue'
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
import CartDrawer from '@/components/CartDrawer.vue'

const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const showCart = ref(false)
const product = ref<any>(null)
const merchantName = ref('')
const qty = ref(1)

// 从 sessionStorage 恢复上次搜索结果
const backSearch = computed(() => {
  try {
    const raw = sessionStorage.getItem('lastSearch')
    return raw ? { path: '/products', query: JSON.parse(raw) } : null
  } catch { return null }
})

const specs = computed(() => {
  if (!product.value?.tags) return []
  return product.value.tags.split(',').map((s: string) => s.trim()).filter(Boolean)
})

const icon = computed(() => {
  const n = product.value?.name || ''
  if (n.includes('手机')) return '📱'
  if (n.includes('笔记本')) return '💻'
  if (n.includes('耳机')) return '🎧'
  if (n.includes('手表')) return '⌚'
  if (n.includes('平板') || n.includes('iPad')) return '📋'
  return '📦'
})

async function fetch() {
  const id = route.params.id
  const res: any = await request.get(`/product/${id}`)
  product.value = res.data
  // 获取商家信息
  if (res.data?.merchantId) {
    try {
      const u: any = await request.get(`/user/${res.data.merchantId}`)
      merchantName.value = u.data?.nickname || u.data?.username || ''
    } catch { /* 忽略 */ }
  }
}

function addToCart() {
  if (!product.value) return
  cartStore.addItem({
    productId: product.value.id,
    name: product.value.name,
    price: product.value.price,
    imageUrl: product.value.imageUrl || '',
    quantity: qty.value,
  })
  ElMessage.success('已加入购物车')
  showCart.value = true
}

onMounted(fetch)
</script>

<style scoped>
.back-link {
  display: inline-block; color: var(--text-muted); font-size: 0.85rem;
  text-decoration: none; margin-bottom: var(--space-lg);
  transition: color var(--duration-fast);
}
.back-link:hover { color: var(--accent-cyan); }
.detail-layout { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-3xl); }
.detail-img {
  background: linear-gradient(145deg, #141e2e, #0f172a);
  border: 1px solid var(--border-subtle); border-radius: var(--radius-xl);
  aspect-ratio: 1; display: flex; align-items: center; justify-content: center;
}
.detail-info { display: flex; flex-direction: column; }
.detail-name { font-family: var(--font-display); font-size: 1.6rem; font-weight: 700; margin-bottom: var(--space-md); }
.detail-desc { color: var(--text-secondary); line-height: 1.7; margin-bottom: var(--space-lg); }
.detail-specs { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: var(--space-lg); }
.spec-chip {
  font-family: var(--font-display); font-size: 0.8rem; font-weight: 500;
  padding: 4px 12px; background: rgba(0,198,242,0.06);
  border: 1px solid rgba(0,198,242,0.12); border-radius: 10px;
  color: var(--accent-cyan-dim);
}
.detail-price {
  font-family: var(--font-display); font-weight: 700; font-size: 2rem;
  color: var(--accent-amber); margin-bottom: var(--space-md);
}
.detail-actions { display: flex; gap: var(--space-md); }
.merchant-row {
  display: flex; align-items: center; gap: var(--space-lg);
  color: var(--text-muted); margin-bottom: var(--space-xl);
}
.merchant-link {
  color: var(--accent-cyan); cursor: pointer; font-weight: 500;
  transition: opacity var(--duration-fast);
}
.merchant-link:hover { opacity: 0.8; }
@media (max-width: 768px) { .detail-layout { grid-template-columns: 1fr; } }
</style>
