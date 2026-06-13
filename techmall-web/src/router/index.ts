import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(to, from, savedPosition) {
    // 如果设置了 scrollToTop 标记，强制置顶
    if (to.query._top === '1') {
      return { top: 0 }
    }
    if (savedPosition) return savedPosition
    // 回到首页时恢复保存的滚动位置
    if (to.name === 'Home') {
      const pos = sessionStorage.getItem('homeScrollY')
      return pos ? { top: Number(pos) } : { top: 0 }
    }
    return { top: 0 }
  },
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue') },
    { path: '/home', name: 'Home', component: () => import('@/views/HomeView.vue') },
    { path: '/products', name: 'Products', component: () => import('@/views/ProductListView.vue') },
    { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetailView.vue') },
    { path: '/cart', name: 'Cart', component: () => import('@/views/CartView.vue'), meta: { roles: ['USER'] } },
    { path: '/orders', name: 'Orders', component: () => import('@/views/OrderListView.vue'), meta: { roles: ['USER'] } },
    { path: '/orders/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetailView.vue'), meta: { roles: ['USER', 'MERCHANT', 'ADMIN'] } },
    { path: '/merchant/products', name: 'MerchantProducts', component: () => import('@/views/merchant/MerchantProductList.vue'), meta: { roles: ['MERCHANT'] } },
    { path: '/merchant/product/add', name: 'MerchantProductAdd', component: () => import('@/views/merchant/MerchantProductAdd.vue'), meta: { roles: ['MERCHANT'] } },
    { path: '/merchant/product/:id', name: 'MerchantProductEdit', component: () => import('@/views/merchant/MerchantProductEdit.vue'), meta: { roles: ['MERCHANT'] } },
    { path: '/merchant/orders', name: 'MerchantOrders', component: () => import('@/views/merchant/MerchantOrderList.vue'), meta: { roles: ['MERCHANT'] } },
    { path: '/admin/users', name: 'AdminUsers', component: () => import('@/views/admin/AdminUserList.vue'), meta: { roles: ['ADMIN'] } },
    { path: '/admin/products', name: 'AdminProducts', component: () => import('@/views/admin/AdminProductList.vue'), meta: { roles: ['ADMIN'] } },
    { path: '/admin/categories', name: 'AdminCategories', component: () => import('@/views/admin/AdminCategoryList.vue'), meta: { roles: ['ADMIN'] } },
    { path: '/admin/orders', name: 'AdminOrders', component: () => import('@/views/admin/AdminOrderList.vue'), meta: { roles: ['ADMIN'] } },
    { path: '/', redirect: '/home' },
    { path: '/:pathMatch(.*)*', redirect: '/home' },
  ],
})

const WHITE_LIST = ['/login', '/home', '/products']

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (WHITE_LIST.includes(to.path) || to.path.startsWith('/product/')) {
    return next()
  }
  if (!userStore.isLoggedIn) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  const requiredRoles = to.meta?.roles as string[] | undefined
  if (requiredRoles && !requiredRoles.includes(userStore.role)) {
    return next('/home')
  }
  next()
})

export default router
