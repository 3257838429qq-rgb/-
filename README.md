# 校园访客接待与招待所一体化管理系统

## 项目介绍

本系统是一套面向大学校园的访客接待与招待所一体化后台管理 Web 系统，支持访客预约、审核、招待所房间管理、入住登记、退房结算等完整业务流程。

## 技术栈

### 后端
- **Spring Boot 3.2.5** - Web 框架
- **MyBatis-Plus 3.5.6** - ORM 框架
- **MySQL 8.0** - 关系数据库
- **Druid** - 数据库连接池
- **JWT** - 身份认证
- **Knife4j** - API 文档

### 前端
- **Vue 3** - 渐进式前端框架
- **Vite 5** - 构建工具
- **Element Plus** - UI 组件库
- **Axios** - HTTP 客户端
- **Vue Router 4** - 路由管理
- **Pinia** - 状态管理
- **ECharts** - 数据可视化

## 系统角色

| 角色 | 角色编码 | 说明 |
|------|---------|------|
| 超级管理员 | SUPER_ADMIN | 系统最高权限，可管理所有模块 |
| 校园接待员 | RECEPTIONIST | 负责访客接待、来访预约、审核 |
| 招待所宿管 | DORM_MANAGER | 负责招待所房间、入住、退房结算 |

## 系统功能

### 1. 系统管理
- 用户管理：增删改查、密码重置、状态管理
- 角色管理：角色权限配置
- 部门管理：树形结构组织架构
- 操作日志：完整操作审计追踪

### 2. 访客管理
- 访客信息录入
- 线上来访预约
- 来访审核（通过/拒绝）
- 访客记录查询

### 3. 招待所管理
- 房型管理（房型、价格、配置）
- 房间信息管理（状态：空闲/入住/维护）
- 访客入住登记
- 退房结算
- 住宿记录查询

## 项目结构

```
hotel/
├── backend/                    # 后端 Spring Boot 项目
│   ├── src/main/java/com/neu/hotel/
│   │   ├── entity/           # 实体类
│   │   ├── mapper/           # 数据访问层
│   │   ├── service/          # 业务逻辑层
│   │   │   └── impl/         # 业务逻辑实现
│   │   ├── controller/        # 控制层（RESTful API）
│   │   ├── config/           # 配置类
│   │   └── common/           # 公共工具类
│   │       ├── result/       # 统一返回结果
│   │       ├── annotation/   # 自定义注解
│   │       ├── aspect/       # 切面（日志）
│   │       └── utils/        # 工具类
│   └── src/main/resources/
│       ├── sql/              # 数据库建表脚本
│       └── mapper/           # MyBatis XML 映射文件
│
└── frontend/                  # 前端 Vue3 项目
    └── src/
        ├── api/              # API 接口封装
        ├── assets/           # 静态资源
        ├── router/           # 路由配置
        ├── stores/           # 状态管理
        ├── utils/            # 工具函数
        └── views/            # 页面组件
            ├── dashboard/    # 首页仪表盘
            ├── login/         # 登录页
            ├── layout/       # 布局组件
            ├── system/        # 系统管理
            ├── visitor/       # 访客管理
            └── dorm/          # 招待所管理
```

## 快速启动

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+

### 1. 数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 执行建表脚本
source backend/src/main/resources/sql/schema.sql
```

### 2. 后端启动

```bash
cd backend

# 安装依赖（使用 Maven）
mvn clean install

# 启动项目
mvn spring-boot:run

# 或者打包后运行
java -jar target/hotel-1.0.0.jar
```

后端启动地址: http://localhost:8080/api

### 3. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 开发模式启动
npm run dev

# 生产构建
npm run build
```

前端启动地址: http://localhost:3000

### 4. 登录账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 超级管理员 |
| receptionist1 | 123456 | 校园接待员 |
| dorm_manager | 123456 | 招待所宿管 |

## 数据库配置

修改 `backend/src/main/resources/application.yml` 中的数据库连接配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hotel_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

## API 文档

启动后端后，访问 Knife4j API 文档：
- 开发环境: http://localhost:8080/api/doc.html

## 主要接口说明

### 认证接口
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/userinfo` - 获取用户信息
- `POST /api/auth/logout` - 退出登录

### 系统管理
- `GET /api/system/user/page` - 用户分页列表
- `POST /api/system/user` - 新增用户
- `PUT /api/system/user` - 修改用户
- `DELETE /api/system/user/{id}` - 删除用户
- `GET /api/system/role/list` - 角色列表
- `GET /api/system/dept/list` - 部门列表

### 访客管理
- `GET /api/visitor/page` - 访客分页列表
- `POST /api/visitor` - 录入访客
- `PUT /api/visitor/review/{id}` - 审核访客

### 招待所管理
- `GET /api/dorm/room/page` - 房间分页列表
- `GET /api/dorm/room/available` - 可用房间列表
- `GET /api/dorm/room/types` - 房型列表
- `POST /api/dorm/checkin` - 入住登记
- `PUT /api/dorm/checkin/checkout/{id}` - 退房结算
- `GET /api/dorm/checkin/statistics` - 统计数据

## License

MIT License
