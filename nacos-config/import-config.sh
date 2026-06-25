#!/bin/bash
# CloudBlog Nacos 配置导入脚本
# 使用 Nacos Open API 将本地配置文件导入 Nacos 配置中心
#
# 前置条件：
#   1. Nacos 已启动（docker compose up -d nacos）
#   2. curl 已安装
#
# 使用方法：
#   bash nacos-config/import-config.sh
#
# 验证导入：
#   打开 http://localhost:8848/nacos → 配置管理 → 配置列表
#   应能看到 Data ID 为 blog-service.yaml 的配置

set -e

NACOS_ADDR="http://localhost:8848"
NACOS_USER="nacos"
NACOS_PASS="nacos"

echo "=== 登录 Nacos ==="
LOGIN_RESP=$(curl -s -X POST "${NACOS_ADDR}/nacos/v1/auth/login" \
  -d "username=${NACOS_USER}&password=${NACOS_PASS}")
ACCESS_TOKEN=$(echo "$LOGIN_RESP" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
  echo "❌ Nacos 登录失败，请检查 Nacos 是否已启动"
  exit 1
fi
echo "✓ Nacos 登录成功"

echo ""
echo "=== 导入 blog-service.yaml ==="
CONFIG_CONTENT=$(cat "$(dirname "$0")/blog-service.yaml")

curl -s -X POST "${NACOS_ADDR}/nacos/v1/cs/config" \
  -d "dataId=blog-service.yaml" \
  -d "group=DEFAULT_GROUP" \
  -d "type=yaml" \
  -d "content=${CONFIG_CONTENT}" \
  -d "accessToken=${ACCESS_TOKEN}"

echo ""
echo "✓ blog-service.yaml 已导入 Nacos 配置中心"
echo "blog-service 已默认启用 Nacos Config，可直接启动"
