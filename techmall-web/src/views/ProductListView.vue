<template>
  <div>
    <AppHeader @toggleCart="showCart = true" />
    <div class="page-container" style="margin-top: var(--space-xl)">
      <div class="toolbar">
        <router-link to="/home" class="back-link">← 返回首页</router-link>
        <el-input v-model="keyword" placeholder="搜索商品、店铺…" size="large" @keyup.enter="doSearch" class="toolbar-search" />
      </div>

      <!-- 明确按商家筛选时显示店铺标题 -->
      <h2 v-if="shopName && merchantId" class="shop-title">🏪 {{ shopName }}</h2>

      <!-- 匹配的商家卡片 -->
      <div v-if="matchedMerchants.length && !merchantId" class="merchant-section">
        <h3 class="section-label">🔍 相关店铺</h3>
        <div class="merchant-list">
          <div v-for="m in matchedMerchants" :key="m.id" class="shop-card" @click="$router.push({path:'/products', query:{merchantId: m.id}})">
            <div class="shop-avatar">🏪</div>
            <div class="shop-info">
              <div class="shop-name">{{ m.nickname || m.username }}</div>
              <div class="shop-hint">点击查看全部商品 →</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品列表 -->
      <h3 v-if="products.length && matchedMerchants.length && !merchantId" class="section-label">📦 相关商品</h3>
      <div class="product-grid" v-if="products.length">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>

      <el-empty v-if="!products.length && !matchedMerchants.length && searched" description="未找到匹配的商品或店铺" />
    </div>
    <CartDrawer :visible="showCart" @close="showCart = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import AppHeader from '@/components/AppHeader.vue'
import ProductCard from '@/components/ProductCard.vue'
import CartDrawer from '@/components/CartDrawer.vue'

const route = useRoute()
const showCart = ref(false)
const keyword = ref((route.query.keyword as string) || '')
const categoryId = ref(route.query.categoryId ? Number(route.query.categoryId) : undefined)
const merchantId = ref(route.query.merchantId ? Number(route.query.merchantId) : undefined)
const products = ref<any[]>([])
const matchedMerchants = ref<any[]>([])
const shopName = ref('')
const searched = ref(false)

async function fetchMerchants() {
  try {
    const r: any = await request.get('/user/merchants')
    const all: any[] = r.data || []
    const term = keyword.value
    if (term && !merchantId.value) {
      matchedMerchants.value = all.filter((m: any) =>
        (m.nickname || m.username || '').includes(term)
      )
    } else {
      matchedMerchants.value = []
    }
  } catch { /* 忽略 */ }
}

async function fetchShopName() {
  if (!merchantId.value) { shopName.value = ''; return }
  try {
    const r: any = await request.get(`/user/${merchantId.value}`)
    shopName.value = r.data?.nickname || r.data?.username || ''
  } catch { /* 忽略 */ }
}

async function doSearch() {
  searched.value = true
  const params: any = { page: 1, size: 20 }
  if (keyword.value) params.keyword = keyword.value
  if (categoryId.value) params.categoryId = categoryId.value
  if (merchantId.value) params.merchantId = merchantId.value
  const res: any = await request.get('/product/list', { params })
  products.value = res.data?.records || []
  await fetchMerchants()
}

onMounted(async () => {
  await doSearch()
  await fetchShopName()
})

// 已经在 /products 页面时，query 变化也触发搜索
watch(() => route.query, async (q) => {
  keyword.value = (q.keyword as string) || ''
  categoryId.value = q.categoryId ? Number(q.categoryId) : undefined
  merchantId.value = q.merchantId ? Number(q.merchantId) : undefined
  await doSearch()
  await fetchShopName()
})
</script>

<style scoped>
.section-label {
  font-family: var(--font-display); font-size: 1rem; font-weight: 600;
  color: var(--text-secondary); margin-bottom: var(--space-md);
}
.shop-title { font-family: var(--font-display); font-size: 1.2rem; margin-bottom: var(--space-lg); color: var(--accent-cyan); }
.toolbar {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: var(--space-xl); gap: var(--space-lg);
}
.toolbar-search { max-width: 400px; }
.back-link { margin-bottom: 0; }

.merchant-section { margin-bottom: var(--space-xl); }
.merchant-list { display: flex; gap: var(--space-md); flex-wrap: wrap; }
.shop-card {
  display: flex; align-items: center; gap: var(--space-md);
  background: var(--bg-surface); border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md); padding: var(--space-md) var(--space-lg);
  cursor: pointer; transition: all var(--duration-fast); min-width: 220px;
}
.shop-card:hover { border-color: var(--border-active); box-shadow: var(--shadow-glow); }
.shop-avatar { font-size: 1.6rem; }
.shop-name { font-weight: 600; color: var(--text-primary); }
.shop-hint { font-size: 0.75rem; color: var(--accent-cyan-dim); margin-top: 2px; }

.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-lg); }
@media (max-width: 1024px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
