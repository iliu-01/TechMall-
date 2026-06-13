# TechMall 数码商城

基于 Spring Cloud Alibaba 的微服务电商系统。

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

## 项目结构

```
techmall/
├── pom.xml                      # 父 POM（版本管控）
├── techmall-common/             # 公共模块（Result, JwtUtils, 异常处理）
├── techmall-service-user/       # 用户服务 (:8081)
├── techmall-service-product/    # 商品服务 (:8082)
├── techmall-service-order/      # 订单服务 (:8083) + OpenFeign + Sentinel
├── techmall-gateway/            # 网关 (:8080) + JWT + 路由 + CORS
├── techmall-web/                # Vue3 前端
└── docs/sql/                    # 建库建表脚本 + 初始数据
```

## 快速启动

### 1. 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Node.js 18+

### 2. 启动中间件

```bash
# MySQL（确保已启动）
# Nacos（D:\Nacos\nacos-server-2.5.1\bin\startup.cmd -m standalone）
# 访问 http://localhost:8848/nacos 确认启动

# Sentinel Dashboard（可选）
# java -jar E:/sentinel/sentinel-dashboard-1.8.9.jar --server.port=8858
```

### 3. 初始化数据库

```bash
mysql -u root -p < docs/sql/01_techmall_user.sql
mysql -u root -p < docs/sql/02_techmall_product.sql
mysql -u root -p < docs/sql/03_techmall_order.sql
```

### 4. 编译 & 启动后端

```bash
# 编译全部模块
cd E:/techmall
mvn clean package -DskipTests

# 启动服务（4 个终端窗口）
java -jar techmall-gateway/target/techmall-gateway-1.0.0.jar
java -jar techmall-service-user/target/techmall-service-user-1.0.0.jar
java -jar techmall-service-product/target/techmall-service-product-1.0.0.jar
java -jar techmall-service-order/target/techmall-service-order-1.0.0.jar
```

### 5. 启动前端

```bash
cd E:/techmall/techmall-web
npm install
npm run dev
# 访问 http://localhost:5173
```

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| merchant1 | 123456 | 商家 |
| user1 | 123456 | 普通用户 |

## API 概览

| 服务 | 端口 | 路径前缀 | 主要接口 |
|------|:---:|----------|----------|
| Gateway | 8080 | /api/** | JWT 鉴权 + 路由转发 |
| User | 8081 | /api/user/** | 注册、登录、用户管理 |
| Product | 8082 | /api/product/** | 商品 CRUD、分类、图片上传 |
| Order | 8083 | /api/order/** | 下单、订单管理、状态流转 |

## 核心考点覆盖

| 考点 | 实现位置 |
|------|----------|
| Maven 多模块 | 父 POM + 5 子模块 |
| Spring Boot 3.3.4 | 全部服务模块 |
| MyBatis CRUD | 各 Mapper.xml + 动态 SQL |
| JWT 鉴权 | Gateway JwtAuthFilter |
| Nacos 注册 | @EnableDiscoveryClient |
| Nacos 配置中心 | application.yml → Nacos |
| OpenFeign 远程调用 | Order → User + Product |
| Sentinel 限流熔断 | @SentinelResource + Fallback |
| Spring Cloud Gateway | 路由 + Filter 链 + CORS |
| Git 版本管理 | 完整提交历史 |
