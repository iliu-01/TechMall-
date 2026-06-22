# TechMall 数码商城

基于 Spring Cloud Alibaba 的微服务电商系统——面向 3C 数码产品的垂直电商平台。

## 技术栈

| 层 | 技术 | 版本 |
|----|------|------|
| 运行时 | JDK | 17 |
| 构建 | Maven | 3.9+ |
| 框架 | Spring Boot | 3.3.4 |
| 微服务 | Spring Cloud | 2023.0.3 |
| | Spring Cloud Alibaba | 2023.0.3.2 |
| ORM | MyBatis | 3.0.4 |
| 数据库 | MySQL | 8.0 |
| 注册/配置中心 | Nacos | 2.5.1 |
| 流量控制 | Sentinel | 1.8.9 |
| 网关 | Spring Cloud Gateway | — |
| 鉴权 | JWT (jjwt) | 0.12.6 |
| 前端 | Vue3 + Vite | 5.x |
| UI 库 | Element Plus | 2.x |
| 设计风格 | Cyber-Industrial 暗色主题 | — |

## 项目结构

```
techmall/
├── pom.xml                          # 父 POM（版本管控）
├── techmall-common/                 # 公共模块（Result, JwtUtils, 异常处理, 分页DTO）
├── techmall-service-user/           # 用户服务 (:8081)
├── techmall-service-product/        # 商品服务 (:8082)
├── techmall-service-order/          # 订单服务 (:8083) + OpenFeign + Sentinel 熔断
├── techmall-gateway/                # 网关 (:8080) + JWT 鉴权 + 路由 + 角色校验
├── techmall-web/                    # Vue3 前端 (Vite 构建)
├── docs/sql/                        # 建库建表脚本 + 初始数据
├── docs/superpowers/specs/          # 设计规格书
└── docs/superpowers/plans/          # 实现计划
```

## 快速启动

### 1. 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Node.js 18+

### 2. 启动中间件

```powershell
# Nacos 注册+配置中心（需先安装 Nacos 2.5.x，解压到任意目录）
<NACOS_HOME>\bin\startup.cmd -m standalone
# 访问 http://localhost:8848/nacos (默认 nacos/nacos)

# Sentinel Dashboard（可选，从 GitHub Releases 下载 sentinel-dashboard-1.8.9.jar）
java -jar sentinel-dashboard-1.8.9.jar --server.port=8858
# 访问 http://localhost:8858 (默认 sentinel/sentinel)
```

### 3. 初始化数据库

> 请根据实际 MySQL 密码修改 `-p` 参数

```powershell
mysql -u root -p < docs/sql/01_techmall_user.sql
mysql -u root -p < docs/sql/02_techmall_product.sql
mysql -u root -p < docs/sql/03_techmall_order.sql
```

### 4. 编译 & 启动后端

```powershell
# 在项目根目录执行
mvn clean package -DskipTests

# 4 个终端分别启动：
java -jar techmall-gateway/target/techmall-gateway-1.0.0.jar
java -jar techmall-service-user/target/techmall-service-user-1.0.0.jar
java -jar techmall-service-product/target/techmall-service-product-1.0.0.jar
java -jar techmall-service-order/target/techmall-service-order-1.0.0.jar
```

### 5. 启动前端

```powershell
cd techmall-web
npm install
npm run dev
# 访问 http://localhost:5173
```

## 测试账号

| 用户名 | 密码 | 角色 | 余额 | 说明 |
|--------|------|------|:----:|------|
| admin | 123456 | 管理员 | — | 全站管理 |
| merchant1 | 123456 | 商家 | 50,000 | 数码旗舰店（手机/电脑/平板） |
| merchant2 | 123456 | 商家 | 50,000 | 耳机专营店（耳机/耳塞） |
| user1 | 123456 | 普通用户 | 10,000 | 普通买家 |

## 功能地图

### 首页

- 🔄 **4 页轮播广告** — 对应真实商品，渐变切换动画，底部圆点切换
- 🔭 **6 大品类** — 响应式网格布局，点击筛选
- ✨ **新品速递** — 8 个真实商品，从 API 动态加载
- 🔥 **限时抢购** — 2 个特价商品，点击跳详情
- 🗺️ **搜索店铺+商品** — 输入商家名/商品名，先展示匹配店铺再展示商品
- 🛒 **迷你购物车抽屉**（仅 USER 可见）
- 📍 **滚动位置记忆** — 离开主页后返回恢复滚动位置

### 普通用户 (USER)

| 功能 | 路径 | 说明 |
|------|------|------|
| 浏览商品 | `/home` `/products` `/product/:id` | 搜索、品类筛选、按商家浏览 |
| 商品详情 | `/product/:id` | 查看规格标签、商家店铺入口 |
| 购物车 & 下单 | `/cart` | 填写收货信息 → 提交订单 |
| 订单管理 | `/orders` `/orders/:id` | 查看列表、支付、取消、确认收货 |
| 余额充值 | `/account` | 固定金额(100~10000) + 自定义金额 |
| 个人信息 | `/account` | 修改昵称/手机/邮箱/密码 |
| 导航栏 | — | 角色标签 + 昵称 + 余额显示 |

### 商家 (MERCHANT)

| 功能 | 路径 | 说明 |
|------|------|------|
| 商品管理 | `/merchant/products` | 增删改、上架/下架、规格标签编辑 |
| 本店订单 | `/merchant/orders` | 查看本店订单、发货 |
| 账户设置 | `/account` | 修改信息 + 查看收益 |
| 导航栏 | — | 角色标签 + 昵称 + 余额（💰收益） |
| 购物车 | — | 不可见（商家不购物） |
| 商品详情 | — | 只能查看，无加购按钮 |

### 管理员 (ADMIN)

| 功能 | 路径 | 说明 |
|------|------|------|
| 📊 数据概览 | `/admin/dashboard` | 统计卡片 + 用户消费表 + 热销排行 + 图表（ECharts）|
| 用户管理 | `/admin/users` | 启用/禁用、查看用户订单/商家商品 |
| 全站商品 | `/admin/products` | 上架/下架 |
| 全站订单 | `/admin/orders` | 查看所有订单 |
| 分类管理 | `/admin/categories` | 新增分类 |
| 用户订单 | `/admin/user-orders?userId=` | 查看+取消指定用户订单 |
| 商家商品 | `/admin/user-products?merchantId=` | 管理指定商家商品 |
| 禁用商家 | — | 禁用商家时自动下架其全部商品 |

## 订单全流程

```
用户下单(PENDING) → 💳模拟支付 → 扣用户余额、加商家收入 → PAID
  → 📦商家发货 → SHIPPED → ✅确认收货 → COMPLETED
  → ❌用户取消 → CANCELLED（仅 PENDING 状态）
```

**余额流转**: 用户支付 → 用户扣款 + 商家收款（按订单项分账给各商家）

## API 概览

| 服务 | 端口 | 路径前缀 | 主要功能 |
|------|:--:|----------|----------|
| Gateway | 8080 | `/api/**` | JWT 鉴权 + 路由转发 + 角色校验 + Sentinel 限流 |
| User | 8081 | `/api/user/**` | 注册/登录、个人信息 CRUD、用户管理、余额、充值 |
| Product | 8082 | `/api/product/**` | 商品 CRUD、分类、标签、库存管理、商家批量操作 |
| Order | 8083 | `/api/order/**` | 下单（Feign+Sentinel）、支付、发货、确认收货、状态流转 |

### 关键 API

```
POST   /api/user/register                 注册
POST   /api/user/login                    登录（返回 JWT + 用户信息 + 余额）
GET    /api/user/me                       当前用户信息
PUT    /api/user/me                       修改个人信息
PUT    /api/user/recharge                 充值余额
GET    /api/product/list                  商品列表（支持 categoryId/keyword/merchantId/includeOffShelf）
POST   /api/order                         创建订单
PUT    /api/order/{id}/pay                支付（扣用户余额 + 加商家收入）
PUT    /api/order/{id}/status             状态流转（发货/确认收货）
PUT    /api/order/{id}/cancel             取消订单
PUT    /api/product/merchant/{id}/status  管理员批量操作商家商品
```

### 内部端点（Feign 服务间调用）

```
GET    /internal/user/{id}                用户查询
PUT    /internal/user/{id}/deduct-balance 扣减余额
PUT    /internal/user/{id}/add-balance    增加余额（商家收款）
GET    /internal/product/{id}             商品查询
PUT    /internal/product/{id}/stock       原子库存扣减
```

## 订单状态机

```
PENDING ──支付──→ PAID ──发货──→ SHIPPED ──确认──→ COMPLETED
   │                │
   └──取消──→ CANCELLED  ←──取消──┘

规则: 仅 PENDING/PAID 可取消 · 商家/管理员可发货 · 仅买家可确认完成
```

## 微服务调用链路

```
Browser → Gateway(:8080)
  ├─ JwtAuthFilter (解析 JWT → X-User-Id/X-User-Role header)
  ├─ RoleAuthGatewayFilterFactory (角色 + 路径校验)
  └─ 路由转发
       ├─ User(:8081)     — 注册/登录/余额/用户管理
       ├─ Product(:8082)  — 商品/分类/标签/库存
       └─ Order(:8083)    — 下单/支付/发货/确认
            ├─ Feign → User (验证用户 + 扣余额 + 加商家收入)
            ├─ Feign → Product (查价格 + 扣库存)
            └─ @SentinelResource (createOrder 熔断降级)
```

## 核心考点覆盖

| 考点 | 实现位置 | 分值 |
|------|----------|:--:|
| Maven 多模块 | 父 POM + 5 子模块 | 基础20/进阶10 |
| Spring Boot 3.3.4 | 全部服务模块 | 基础20 |
| MyBatis CRUD | 各 Mapper.xml + 动态 SQL | 基础30/进阶20 |
| JWT 鉴权 | Gateway JwtAuthFilter | 基础15/进阶15 |
| Nacos 注册中心 | @EnableDiscoveryClient | 进阶10 |
| Nacos 配置中心 | spring.config.import | 进阶5 |
| OpenFeign 远程调用 | Order → User + Product | 进阶10 |
| Sentinel 限流熔断 | @SentinelResource + FallbackFactory | 基础15/进阶5 |
| Spring Cloud Gateway | 路由 + GlobalFilter + GatewayFilterFactory | 进阶10 |
| Git 版本管理 | 完整提交历史 | 进阶10 |

## 设计资源

| 资源 | 路径 |
|------|------|
| 首页预览 | `techmall-preview.html` |
| Design System 规范 | `techmall-design-system.html` |
| 设计规格书 | `docs/superpowers/specs/2026-06-12-techmall-design.md` |
| 实现计划 | `docs/superpowers/plans/2026-06-12-techmall-plan.md` |
