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
# Nacos 注册+配置中心
D:\Nacos\nacos-server-2.5.1\bin\startup.cmd -m standalone
# 访问 http://localhost:8848/nacos (默认 nacos/nacos)

# Sentinel Dashboard（可选，监控限流熔断）
java -jar E:/sentinel/sentinel-dashboard-1.8.9.jar --server.port=8858
# 访问 http://localhost:8858 (默认 sentinel/sentinel)
```

### 3. 初始化数据库

```powershell
mysql -u root -p123456 < docs/sql/01_techmall_user.sql
mysql -u root -p123456 < docs/sql/02_techmall_product.sql
mysql -u root -p123456 < docs/sql/03_techmall_order.sql
```

### 4. 编译 & 启动后端

```powershell
cd E:\techmall
mvn clean package -DskipTests

# 4 个终端分别启动：
java -jar techmall-gateway\target\techmall-gateway-1.0.0.jar
java -jar techmall-service-user\target\techmall-service-user-1.0.0.jar
java -jar techmall-service-product\target\techmall-service-product-1.0.0.jar
java -jar techmall-service-order\target\techmall-service-order-1.0.0.jar
```

### 5. 启动前端

```powershell
cd E:\techmall\techmall-web
npm install
npm run dev
# 访问 http://localhost:5173
```

## 测试账号

| 用户名 | 密码 | 角色 | 余额 | 初始数据 |
|--------|------|------|:----:|----------|
| admin | 123456 | 管理员 | — | 全站管理权限 |
| merchant1 | 123456 | 商家 | 50,000 | 5 个数码商品 |
| merchant2 | 123456 | 商家 | 50,000 | 3 个耳机商品 |
| user1 | 123456 | 普通用户 | 10,000 | — |

## 功能地图

### 首页

- 🔄 4 页轮播广告，对应真实商品，点击跳转详情
- 🔭 6 大品类快速检索
- ✨ 新品速递（8 个真实商品，从数据库拉取）
- 🔥 限时抢购（2 个特价商品，点击跳详情）
- 🛒 迷你购物车抽屉
- 📍 滚动位置记忆——子页面返回时恢复位置

### 普通用户 (USER)

| 功能 | 路径 |
|------|------|
| 浏览商品、搜索、按品类筛选 | `/home` `/products` `/product/:id` |
| 加入购物车、提交订单 | `/cart` |
| 查看我的订单、取消订单 | `/orders` |
| 账户设置（修改昵称/手机/邮箱/密码） | `/account` |
| 查看余额 | 导航栏 `💳 ¥余额` |

### 商家 (MERCHANT)

| 功能 | 路径 |
|------|------|
| 商品管理（增删改、上架/下架） | `/merchant/products` |
| 本店订单（发货） | `/merchant/orders` |
| 账户设置（修改昵称/手机/邮箱/密码） | `/account` |
| 查看收益 | 导航栏 `💰 ¥余额` |

### 管理员 (ADMIN)

| 功能 | 路径 |
|------|------|
| 用户管理（启用/禁用、查看用户订单/商品） | `/admin/users` |
| 全站商品管理（上架/下架） | `/admin/products` |
| 分类管理（新增分类） | `/admin/categories` |
| 全站订单管理 | `/admin/orders` |
| 查看指定用户的订单 | `/admin/user-orders?userId=` |
| 管理指定商家的商品 | `/admin/user-products?merchantId=` |

## API 概览

| 服务 | 端口 | 路径前缀 | 主要功能 |
|------|:--:|----------|----------|
| Gateway | 8080 | `/api/**` | JWT 鉴权 + 路由转发 + 角色校验 + Sentinel 限流 |
| User | 8081 | `/api/user/**` | 注册/登录、个人信息 CRUD、用户管理、余额 |
| Product | 8082 | `/api/product/**` | 商品 CRUD、分类、图片上传、库存管理、商家批量操作 |
| Order | 8083 | `/api/order/**` | 下单（Feign+Sentinel）、订单管理、状态流转 |

### 关键 API

```
POST   /api/user/register             注册
POST   /api/user/login                登录（返回 JWT + 用户信息 + 余额）
GET    /api/user/me                   当前用户信息
PUT    /api/user/me                   修改个人信息
GET    /api/product/list              商品列表（支持 categoryId/keyword/merchantId）
POST   /api/order                     创建订单（OpenFeign 调用 User + Product）
PUT    /api/order/{id}/cancel         取消订单
PUT    /api/product/merchant/{id}/status  管理员批量操作商家商品
```

### 内部端点（仅 localhost 访问）

```
GET    /internal/user/{id}           用户服务内部查询
PUT    /internal/product/{id}/stock  原子库存扣减
PUT    /internal/product/merchant/{id}/status  批量更新商家商品状态
```

## 订单状态机

```
PENDING → PAID → SHIPPED → COMPLETED
   │        │
   └─取消──→ CANCELLED
   └─取消──→ CANCELLED

规则: 仅 PENDING 可取消 · 仅商家可发货 · 仅本人可确认完成
```

## 微服务调用链路（核心考点）

```
Browser → Gateway(:8080)
  ├─ JwtAuthFilter (解析 JWT → X-User-Id header)
  ├─ RoleAuthFilter (角色 + 路径校验)
  └─ 路由转发
       ├─ User(:8081)  — 注册/登录/用户管理
       ├─ Product(:8082) — 商品/分类/图片
       └─ Order(:8083)
            ├─ Feign → User (验证用户)
            ├─ Feign → Product (查价格 + 扣库存)
            └─ @SentinelResource (createOrder 降级保护)
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
| 首页预览 | `E:\testEnvs\javaSpring\techmall-preview.html` |
| Design System 规范 | `E:\testEnvs\javaSpring\techmall-design-system.html` |
| 设计规格书 | `docs/superpowers/specs/2026-06-12-techmall-design.md` |
| 实现计划 | `docs/superpowers/plans/2026-06-12-techmall-plan.md` |
