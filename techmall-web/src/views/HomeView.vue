<template>
  <div class="page-root">
    <AppHeader @toggleCart="showCart = true" />

    <!-- Hero Banner Carousel -->
    <section class="page-container" style="margin-top: var(--space-xl)">
      <div class="hero-banner" v-if="slides.length">
        <Transition name="slide-fade" mode="out-in">
          <div class="hero-content" :key="current">
            <div class="hero-tag">{{ slides[current].tag }}</div>
            <h1>{{ slides[current].title }}<span class="highlight">{{ slides[current].highlight }}</span></h1>
            <p class="hero-desc">{{ slides[current].desc }}</p>
            <div class="hero-actions">
              <router-link :to="`/product/${slides[current].productId}`" class="btn-primary">立即选购 →</router-link>
              <router-link to="/products" class="btn-ghost">了解更多</router-link>
            </div>
          </div>
        </Transition>
        <Transition name="slide-fade" mode="out-in">
          <div class="hero-visual" :key="'v'+current">
            <div class="hero-showcase" @click="$router.push(`/product/${slides[current].productId}`)">
              <div class="product-icon">{{ slides[current].icon }}</div>
              <div class="spec-line">{{ slides[current].spec1 }}</div>
              <div class="spec-line">{{ slides[current].spec2 }}</div>
              <div class="price-tag">¥{{ slides[current].price?.toLocaleString() }}</div>
            </div>
          </div>
        </Transition>
      </div>
      <div class="hero-dots">
        <span v-for="(_s, i) in slides" :key="i" :class="{ active: i === current }" @click="goToSlide(i)"></span>
      </div>
    </section>

    <!-- Categories -->
    <section class="page-container" style="margin-top: var(--space-3xl)">
      <div class="section-header">
        <h2 class="section-title">🔭 按品类探索</h2>
        <router-link to="/products" class="section-link">查看全部商品 →</router-link>
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
  <AppFooter />
</template>

<script setup lang="ts">
import AppFooter from '@/components/AppFooter.vue'
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
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

// 轮播数据（对应数据库真实产品 ID）
const slides = ref<any[]>([])
const current = ref(0)
let timer: any = null

// 4 张轮播图 — 产品 ID 对应数据库真实产品
const slideDefs = [
  { productId: 1, icon: '📱', tag: '2026 旗舰首发', title: '徕卡影像', highlight: '新纪元', desc: '200MP 徕卡四摄 · 骁龙 8 Gen5 · 120W 秒充', spec1: '6.73″ · 2K LTPO · 120Hz', spec2: '骁龙 8 Gen5 · 16+512GB' },
  { productId: 2, icon: '💻', tag: 'AI 超能本', title: '重构性能想象的', highlight: '次世代', desc: 'Ryzen AI 9 + RTX 5070 · 2.5K OLED · 仅 1.5kg', spec1: '16″ · 2.5K · 240Hz OLED', spec2: 'Ryzen AI 9 · RTX 5070 · 32GB' },
  { productId: 6, icon: '🎧', tag: '降噪新标杆', title: '静享纯粹', highlight: '声学之旅', desc: '集成降噪 V3 · 50h 超长续航 · LDAC 高清无线', spec1: '降噪芯片 V3 · 30mm 驱动', spec2: '50h 续航 · LDAC · 快充' },
  { productId: 8, icon: '🎧', tag: '今天不抢明天后悔', title: '限时特惠', highlight: '闪购价', desc: 'H3 芯片 · 自适应音频 · USB-C · 精确查找', spec1: 'H3 芯片 · 自适应均衡', spec2: 'USB-C · IPX4 · 精确查找' },
]

function goToSlide(i: number) {
  current.value = i
  resetTimer()
}

function startTimer() {
  timer = setInterval(() => {
    current.value = (current.value + 1) % slides.value.length
  }, 4000)
}

function resetTimer() {
  clearInterval(timer)
  startTimer()
}

const newProducts = ref<any[]>([])
const deals = ref<any[]>([])

async function loadData() {
  // 从 API 获取真实商品，与轮播数据合并价格
  const res: any = await request.get('/product/list', { params: { page: 1, size: 8 } })
  const all: any[] = res.data?.records || []
  newProducts.value = all

  // 轮播数据绑定真实价格
  slides.value = slideDefs.map(def => {
    const dbProduct = all.find((p: any) => p.id === def.productId)
    return { ...def, price: dbProduct?.price || 0 }
  })

  // 限时抢购
  const dealIds = [8, 6]
  deals.value = all
    .filter((p: any) => dealIds.includes(p.id))
    .map((p: any) => ({
      ...p,
      tag: p.id === 8 ? '⚡ 闪购' : '🎯 今日特价',
      oldPrice: p.id === 8 ? 1999 : p.id === 6 ? 3299 : undefined,
      icon: p.name.includes('耳机') ? '🎧' : '📦',
      desc: p.tags?.split(',').slice(0, 2).join(' · ') || '',
    }))

  startTimer()

  // 数据加载后恢复滚动位置
  const saved = sessionStorage.getItem('homeScrollY')
  if (saved) {
    await nextTick()
    window.scrollTo(0, Number(saved))
  }
}

onMounted(() => {
  loadData()
  sessionStorage.removeItem('lastSearch')
})
onUnmounted(() => clearInterval(timer))

// 离开首页时保存滚动位置
onBeforeRouteLeave(() => {
  sessionStorage.setItem('homeScrollY', String(window.scrollY))
})
</script>

<style scoped>
.hero-banner {
  background: var(--bg-surface); border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl); overflow: hidden;
  display: grid; grid-template-columns: 1fr 1fr; min-height: 400px;
  position: relative;
}
.hero-content {
  padding: var(--space-3xl) var(--space-2xl); display: flex;
  flex-direction: column; justify-content: center;
}
.hero-visual { display: flex; align-items: center; justify-content: center; }
/* Vue Transition */
.slide-fade-enter-active { transition: all 0.6s ease-out; }
.slide-fade-leave-active { transition: all 0.3s ease-in; }
.slide-fade-enter-from  { opacity: 0; transform: translateY(12px); }
.slide-fade-leave-to    { opacity: 0; transform: translateY(-12px); }

/* Hero dots */
.hero-dots { display: flex; justify-content: center; gap: 8px; margin-top: var(--space-lg); }
.hero-dots span {
  width: 8px; height: 8px; border-radius: 50%;
  background: var(--bg-elevated); cursor: pointer;
  transition: all var(--duration-fast);
}
.hero-dots span.active { background: var(--accent-cyan); box-shadow: 0 0 8px rgba(0,198,242,0.5); width: 24px; border-radius: 4px; }
.hero-dots span:hover:not(.active) { background: var(--text-muted); }
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
  cursor: pointer;
}
.btn-primary { background: var(--accent-cyan); color: #060b14; }
.btn-primary:hover { box-shadow: 0 0 24px rgba(0,198,242,0.35); }
.btn-ghost { border: 1px solid var(--border-subtle); color: var(--text-primary); }
.btn-ghost:hover { border-color: var(--text-secondary); background: rgba(255,255,255,0.03); }
.hero-showcase {
  width: 280px; height: 280px;
  background: linear-gradient(145deg, #1a2332, #0f172a);
  border: 1px solid var(--border-subtle); border-radius: var(--radius-xl);
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px;
  transition: transform var(--duration-normal) var(--ease-smooth);
  cursor: pointer;
}
.hero-visual:hover .hero-showcase { transform: scale(1.03); }
.product-icon { font-size: 4rem; }
.spec-line { font-family: var(--font-display); font-size: 0.75rem; color: var(--text-muted); }
.price-tag { font-family: var(--font-display); font-weight: 700; font-size: 1.5rem; color: var(--accent-amber); }

.section-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: var(--space-lg); }
.section-link { color: var(--accent-cyan); font-size: 0.85rem; text-decoration: none; }

.category-scroll {
  display: grid; grid-template-columns: repeat(6, 1fr);
  gap: var(--space-md); overflow-y: visible;
}
.cat-card {
  background: var(--bg-surface); border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg); padding: var(--space-lg) var(--space-sm);
  text-align: center; cursor: pointer; transition: all var(--duration-fast);
}
@media (max-width: 768px) { .category-scroll { grid-template-columns: repeat(3, 1fr); } }
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
