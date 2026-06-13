<template>
  <div>
    <AppHeader @toggleCart="showCart = true" />
    <div class="page-container" style="margin-top: var(--space-xl)">
      <router-link to="/home" class="back-link">← 返回首页</router-link>
      <el-input v-model="keyword" placeholder="搜索商品…" size="large" @keyup.enter="search" style="max-width:420px;margin-bottom:var(--space-lg);margin-top:var(--space-md)" />
      <div class="product-grid" v-if="products.length">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>
      <el-empty v-else description="暂无商品" />
    </div>
    <CartDrawer :visible="showCart" @close="showCart = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import AppHeader from '@/components/AppHeader.vue'
import ProductCard from '@/components/ProductCard.vue'
import CartDrawer from '@/components/CartDrawer.vue'

const route = useRoute()
const showCart = ref(false)
const keyword = ref((route.query.keyword as string) || '')
const categoryId = ref(route.query.categoryId ? Number(route.query.categoryId) : undefined)
const products = ref<any[]>([])

async function search() {
  const params: any = { page: 1, size: 20 }
  if (keyword.value) params.keyword = keyword.value
  if (categoryId.value) params.categoryId = categoryId.value
  const res: any = await request.get('/product/list', { params })
  products.value = res.data?.records || []
}

onMounted(() => search())
</script>

<style scoped>
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-lg); }
@media (max-width: 1024px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
