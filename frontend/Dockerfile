# ============================================================
# Vue 前端 Dockerfile（多阶段构建）
# 第一阶段：Node.js 编译打包
# 第二阶段：Nginx 托管静态文件
# 最终镜像只包含 dist + Nginx，非常精简（约10MB）
# ============================================================

# ============ 第一阶段：构建阶段 ============
# node:20-alpine 基于 Alpine Linux，只有 ~5MB，非常轻量
# AS builder 给这个阶段取名，后面可以从它复制文件
FROM node:20-alpine AS builder

WORKDIR /app

# 先单独复制 package.json 和 package-lock.json
# 这样如果依赖没变，Docker会复用缓存，不用重新 npm ci
COPY package*.json ./

# npm ci 比 npm install 更快更严格（严格按lock文件安装）
RUN npm ci

# 复制所有源代码（包括 src/ public/ vite.config.js 等）
COPY . .

# 执行 Vue 项目构建，产物在 dist/ 目录
RUN npm run build

# ============ 第二阶段：运行阶段 ============
# nginx:alpine 是官方的轻量Nginx镜像
FROM nginx:alpine

# --from=builder 表示从第一阶段的文件系统复制
# 把构建好的 dist/ 复制到 Nginx 默认的静态文件目录
COPY --from=builder /app/dist /usr/share/nginx/html

# 用我们自己的 nginx.conf 覆盖 Nginx 默认配置
# /etc/nginx/conf.d/default.conf 是 Nginx 自动加载的配置文件
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# daemon off：让 Nginx 在前台运行（Docker需要前台进程，否则容器会立即退出）
CMD ["nginx", "-g", "daemon off;"]
