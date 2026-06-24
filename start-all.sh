#!/bin/bash
# CloudBlog 一键启动脚本
# 在 Git Bash 中运行：bash start-all.sh

set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== 1. 编译项目 ==="
cd "$ROOT_DIR"
mvn install -DskipTests -q

echo ""
echo "=== 2. 启动 Docker 中间件 ==="
cd "$ROOT_DIR"
docker compose up -d mysql redis nacos minio sentinel

echo ""
echo "=== 3. 等待中间件就绪（30秒）==="
sleep 30

echo ""
echo "=== 4. 启动微服务（后台运行）==="
mkdir -p "$ROOT_DIR/logs"
for svc in user-service blog-service comment-service file-service; do
    echo "  → $svc ..."
    nohup mvn -f "$ROOT_DIR/pom.xml" spring-boot:run -pl $svc -q > "$ROOT_DIR/logs/$svc.log" 2>&1 &
    sleep 3
done

echo "等待微服务注册到 Nacos（40秒）..."
sleep 40

echo ""
echo "=== 5. 启动网关 ==="
echo "  → cloud-gateway ..."
nohup mvn -f "$ROOT_DIR/pom.xml" spring-boot:run -pl cloud-gateway -q > "$ROOT_DIR/logs/cloud-gateway.log" 2>&1 &

echo "等待网关就绪（20秒）..."
sleep 20

echo ""
echo "=== 6. 启动前端 ==="
cd "$ROOT_DIR/frontend"
nohup npm run dev > "$ROOT_DIR/logs/frontend.log" 2>&1 &

echo ""
echo "============================================"
echo "  全部启动完成！进程已在后台运行"
echo "  前端:    http://localhost:3000"
echo "  网关:    http://localhost:8080"
echo "  Nacos:   http://localhost:8848/nacos"
echo "  MinIO:   http://localhost:9001"
echo "  Sentinel: http://localhost:8858"
echo ""
echo "  日志目录: logs/"
echo "  停止命令: taskkill //F //IM java.exe //IM node.exe"
echo "============================================"

cd "$ROOT_DIR"
