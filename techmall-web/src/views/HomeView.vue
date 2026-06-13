<template>
  <div>
    <AppHeader @toggleCart="showCart = true" />

    <!-- Hero Banner -->
    <section class="page-container" style="margin-top: var(--space-xl)">
      <div class="hero-banner">
        <div class="hero-content">
          <div class="hero-tag">2026 新品首发</div>
          <h1>重构性能想象的<span class="highlight">次世代</span></h1>
          <p class="hero-desc">搭载最新旗舰芯片，重新定义移动计算体验。<br/>限时首发价，尊享 12 期免息分期。</p>
          <div class="hero-actions">
            <a href="/products" class="btn-primary">立即选购 →</a>
            <a href="/products" class="btn-ghost">了解更多</a>
          </div>
        </div>
        <div class="hero-visual">
          <div class="hero-showcase">
            <div class="product-icon">💻</div>
            <div class="spec-line">14.2″ · 3.2K · 120Hz</div>
            <div class="spec-line">M4 Ultra · 32GB · 1TB</div>
            <div class="price-tag">¥12,999</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Categories -->
    <section class="page-container" style="margin-top: var(--space-3xl)">
      <div class="section-header">
        <h2 class="section-title">🔭 按品类探索</h2>
        <router-link to="/products" class="section-link">全部分类 →</router-link>
      </div>
      <div class="category-scroll">
        <div v-for="c in categories" :key="c.name" class="cat-card" @click="$router.push({path:'/products', query:{categoryId: c.id}})">
          <span class="cat-icon">{{ c.icon }}</span>
          <span class="cat-label">{{ c.name }}</span>
        </div>
      </div>
    </section>

    <!-- New Arrivals -->
    <section class="page-container" style="margin-top: var(--space-3xl)">
      <div class="section-header">
        <h2 class="section-title">✨ 新品速递</h2>
        <router-link to="/products" class="section-link">查看全部 →</router-link>
      </div>
      <div class="product-grid" v-if="newProducts.length">
        <ProductCard v-for="(p, i) in newProducts" :key="p.id" :product="p" :tag="i === 0 ? 'new' : i === 1 ? 'hot' : ''" />
      </div>
      <el-skeleton v-else :rows="2" animated />
    </section>

    <!-- Hot Deals -->
    <section class="page-container" style="margin-top: var(--space-3xl)">
      <div class="section-header">
        <h2 class="section-title">🔥 限时抢购</h2>
        <router-link to="/products" class="section-link">更多超值 →</router-link>
      </div>
      <div class="deal-row" v-if="deals.length">
        <div class="deal-card" v-for="d in deals" :key="d.id" @click="$router.push(`/product/${d.id}`)">
          <div class="deal-visual">{{ d.icon }}</div>
          <div class="deal-info">
            <div class="deal-tag">{{ d.tag }}</div>
            <h3>{{ d.name }}</h3>
            <p class="deal-desc">{{ d.desc }}</p>
            <div class="deal-price">¥{{ d.price?.toLocaleString() }} <span class="old" v-if="d.oldPrice">¥{{ d.oldPrice?.toLocaleString() }}</span></div>
          </div>
        </div>
      </div>
      <el-skeleton v-else :rows="2" animated />
    </section>

    <CartDrawer :visible="showCart" @close="showCart = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import AppHeader from '@/components/AppHeader.vue'
import ProductCard from '@/components/ProductCard.vue'
import CartDrawer from '@/components/CartDrawer.vue'

const showCart = ref(false)

const categories = [
  { id: 1, name: '智能手机', icon: '📱' },
  { id: 2, name: '笔记本', icon: '💻' },
  { id: 3, name: '耳机音频', icon: '🎧' },
  { id: 4, name: '智能穿戴', icon: '⌚' },
  { id: 5, name: '平板电脑', icon: '📋' },
  { id: 6, name: '配件', icon: '🔌' },
]

const newProducts = ref<any[]>([])
const deals = ref<any[]>([])

async function loadData() {
  // 从 API 获取真实商品数据
  const res: any = await request.get('/product/list', { params: { page: 1, size: 8 } })
  const all: any[] = res.data?.records || []
  newProducts.value = all

  // 用 ID=8 (AirPods Pro 3) 和 ID=6 (Sony WH-1000XM7) 作为限时抢购
  const dealIds = [8, 6]
  deals.value = all
    .filter((p: any) => dealIds.includes(p.id))
    .map((p: any) => ({
      ...p,
      tag: p.id === 8 ? '⚡ 闪购' : '🎯 今日特价',
      oldPrice: p.id === 8 ? 1999 : p.id === 6 ? 3299 : undefined,
      icon: p.name.includes('耳机') ? '🎧' : '📦',
      desc: p.description?.split('+').slice(0, 2).join('·') || '',
    }))
}

onMounted(loadData)
</script>

<style scoped>
.hero-banner {
  background: var(--bg-surface); border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl); overflow: hidden;
  display: grid; grid-template-columns: 1fr 1fr; min-height: 400px;
}
.hero-content { padding: var(--space-3xl) var(--space-2xl); display: flex; flex-direction: column; justify-content: center; }
.hero-tag {
  font-family: var(--font-display); font-weight: 600; font-size: 0.75rem;
  letter-spacing: 0.15em; color: var(--accent-cyan);
  margin-bottom: var(--space-md); display: flex; align-items: center; gap: 8px;
}
.hero-tag::before { content: ''; width: 24px; height: 1px; background: var(--accent-cyan); }
.hero-content h1 {
  font-family: var(--font-display); font-weight: 900; font-size: 2.6rem;
  line-height: 1.15; margin-bottom: var(--space-md);
}
.highlight { color: var(--accent-cyan); }
.hero-desc { font-size: 1rem; color: var(--text-secondary); margin-bottom: var(--space-xl); line-height: 1.7; }
.hero-actions { display: flex; gap: var(--space-md); }
.btn-primary, .btn-ghost {
  font-family: var(--font-display); font-weight: 600; font-size: 0.9rem;
  padding: 11px 26px; border-radius: 24px; text-decoration: none;
  display: inline-flex; align-items: center; gap: 6px;
  transition: all var(--duration-fast);
}
.btn-primary { background: var(--accent-cyan); color: #060b14; }
.btn-primary:hover { box-shadow: 0 0 24px rgba(0,198,242,0.35); }
.btn-ghost { border: 1px solid var(--border-subtle); color: var(--text-primary); }
.btn-ghost:hover { border-color: var(--text-secondary); background: rgba(255,255,255,0.03); }
.hero-visual { display: flex; align-items: center; justify-content: center; }
.hero-showcase {
  width: 280px; height: 280px;
  background: linear-gradient(145deg, #1a2332, #0f172a);
  border: 1px solid var(--border-subtle); border-radius: var(--radius-xl);
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px;
}
.product-icon { font-size: 4rem; }
.spec-line { font-family: var(--font-display); font-size: 0.75rem; color: var(--text-muted); }
.price-tag { font-family: var(--font-display); font-weight: 700; font-size: 1.5rem; color: var(--accent-amber); }

.section-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: var(--space-lg); }
.section-link { color: var(--accent-cyan); font-size: 0.85rem; text-decoration: none; }

.category-scroll { display: flex; gap: var(--space-md); overflow-x: auto; overflow-y: visible; scrollbar-width: none; padding: 6px 2px 6px; margin: -6px -2px; }
.category-scroll::-webkit-scrollbar { display: none; }
.cat-card {
  flex-shrink: 0; width: 120px; background: var(--bg-surface);
  border: 1px solid var(--border-subtle); border-radius: var(--radius-lg);
  padding: var(--space-lg) var(--space-md); text-align: center; cursor: pointer;
  transition: all var(--duration-fast);
}
.cat-card:hover { border-color: var(--border-active); box-shadow: var(--shadow-glow); transform: translateY(-2px); }
.cat-icon { font-size: 2rem; display: block; margin-bottom: var(--space-sm); }
.cat-label { font-family: var(--font-display); font-weight: 600; font-size: 0.85rem; }

.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-lg); }
@media (max-width: 1024px) { .product-grid { grid-template-columns: repeat(3, 1fr); } .hero-banner { grid-template-columns: 1fr; } .hero-visual { display: none; } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, 1fr); } .hero-content h1 { font-size: 2rem; } }

.deal-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-lg); }
.deal-card {
  background: var(--bg-surface); border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl); padding: var(--space-xl);
  display: flex; gap: var(--space-xl); align-items: center; cursor: pointer;
  transition: all var(--duration-normal);
}
.deal-card:hover { border-color: var(--border-active); box-shadow: var(--shadow-glow); }
.deal-visual { font-size: 3rem; width: 120px; height: 120px; background: linear-gradient(145deg, #1a2332, #0f172a); border: 1px solid var(--border-subtle); border-radius: var(--radius-lg); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.deal-info { flex: 1; }
.deal-tag { font-family: var(--font-display); font-size: 0.7rem; font-weight: 700; color: var(--accent-rose); margin-bottom: 8px; }
.deal-info h3 { font-family: var(--font-display); font-weight: 700; font-size: 1.15rem; margin-bottom: 4px; }
.deal-desc { font-size: 0.85rem; color: var(--text-secondary); margin-bottom: var(--space-md); }
.deal-price { font-family: var(--font-display); font-weight: 700; font-size: 1.3rem; color: var(--accent-amber); }
.old { font-size: 0.75rem; color: var(--text-muted); text-decoration: line-through; margin-left: 8px; font-weight: 400; }
@media (max-width: 768px) { .deal-row { grid-template-columns: 1fr; } }
</style>
