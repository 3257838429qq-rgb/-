# ============================================================
# Spring Boot 后端 Dockerfile
# 构建命令：mvn package -DskipTests  →  docker compose build backend
# ============================================================

# 基础镜像：Eclipse Temurin 是 OpenJDK 的发行版
# 17 = Java 17版本，jre = 只需要运行环境，不需要编译工具（比jdk小很多）
FROM eclipse-temurin:17-jre

# 设置容器内的工作目录（不存在的目录会自动创建）
WORKDIR /app

# 将Maven打包好的jar文件复制到容器内
# target/*.jar 是 mvn package 的产物，如 hotel-management-1.0.0.jar
# 容器内重命名为 app.jar，方便启动命令引用
COPY target/*.jar app.jar

# 声明容器运行时监听的端口（仅文档作用，实际不影响网络）
# 真正的端口映射在 docker-compose.yml 中配置
EXPOSE 8080

# 容器启动时执行的命令（exec格式，推荐）
# 等价于在命令行执行：java -jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
