<template>
  <footer class="site-footer">
    <div class="footer-inner">
      <div class="footer-grid">
        <div class="footer-brand">
          <span class="logo-text"><span class="mark">⚡</span>TechMall</span>
          <p>探索极致科技，尽在 TechMall。<br/>正品保障 · 极速物流 · 无忧售后。</p>
        </div>
        <div class="footer-col">
          <h4>购物指南</h4>
          <router-link to="/products">全部商品</router-link>
          <router-link to="/home">首页</router-link>
          <a href="#">常见问题</a>
        </div>
        <div class="footer-col">
          <h4>用户服务</h4>
          <router-link to="/account">账户设置</router-link>
          <router-link to="/orders">我的订单</router-link>
          <router-link to="/cart">购物车</router-link>
        </div>
        <div class="footer-col">
          <h4>关于我们</h4>
          <a href="#">关于 TechMall</a>
          <a href="#">隐私政策</a>
          <a href="#">服务条款</a>
        </div>
      </div>
      <div class="footer-bottom">
        <span>© 2026 TechMall. 基于 Spring Cloud Alibaba 微服务架构.</span>
        <span>Made with ❤️ for 《微服务应用开发》</span>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer { margin-top: auto; border-top: 1px solid var(--border-subtle); background: var(--bg-primary); }
.footer-inner { max-width: 1320px; margin: 0 auto; padding: var(--space-2xl) var(--space-lg) var(--space-lg); }
.footer-grid { display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; gap: var(--space-2xl); }
.logo-text { font-family: var(--font-display); font-weight: 900; font-size: 1.2rem; display: inline-flex; align-items: center; gap: 8px; margin-bottom: var(--space-md); }
.mark { width: 28px; height: 28px; background: linear-gradient(135deg, var(--accent-cyan), #0088cc); border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; font-size: 0.8rem; }
.footer-brand p { font-size: 0.82rem; color: var(--text-muted); line-height: 1.7; max-width: 300px; }
.footer-col h4 { font-family: var(--font-display); font-weight: 600; font-size: 0.82rem; letter-spacing: 0.04em; margin-bottom: var(--space-md); color: var(--text-primary); }
.footer-col a { display: block; font-size: 0.8rem; color: var(--text-muted); text-decoration: none; margin-bottom: var(--space-sm); transition: color var(--duration-fast); }
.footer-col a:hover { color: var(--accent-cyan); }
.footer-bottom { margin-top: var(--space-xl); padding-top: var(--space-lg); border-top: 1px solid var(--border-subtle); display: flex; justify-content: space-between; font-size: 0.75rem; color: var(--text-muted); }
@media (max-width: 768px) { .footer-grid { grid-template-columns: 1fr 1fr; } .footer-bottom { flex-direction: column; gap: 4px; } }
</style>
