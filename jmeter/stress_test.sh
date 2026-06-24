#!/bin/bash
# CloudBlog 压力测试脚本
# 使用方式: bash jmeter/stress_test.sh [并发数] [循环次数]

CONCURRENCY=${1:-30}
LOOPS=${2:-20}
BASE_URL="http://localhost:8080"
REPORT_DIR="jmeter/report"
mkdir -p "$REPORT_DIR"

echo "=========================================="
echo " CloudBlog 压力测试"
echo " 并发数: $CONCURRENCY"
echo " 循环次数: $LOOPS"
echo " 总请求数(点赞): $((CONCURRENCY * LOOPS))"
echo "=========================================="

# 测试1: 博客分页查询
echo ""
echo "[测试1] 博客分页查询 GET /api/blog/page"
START_TIME=$(date +%s%N)
SUCCESS=0
FAIL=0
TOTAL_TIME=0
for ((i=1; i<=CONCURRENCY; i++)); do
  for ((j=1; j<=LOOPS; j++)); do
    (
      T0=$(date +%s%N)
      RESP=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/blog/page?page=1&size=10" 2>/dev/null)
      T1=$(date +%s%N)
      ELAPSED=$(( (T1 - T0) / 1000000 ))
      echo "PAGE:$i:$j:$RESP:${ELAPSED}ms" >> "$REPORT_DIR/page_results.txt"
    ) &
  done
  if (( i % 5 == 0 )); then wait; fi
done
wait
echo "  完成"

# 测试2: 博客详情
echo ""
echo "[测试2] 博客详情 GET /api/blog/{id}"
for ((i=1; i<=CONCURRENCY; i++)); do
  for ((j=1; j<=LOOPS; j++)); do
    BLOG_ID=$(( (i + j) % 10 + 1 ))
    (
      T0=$(date +%s%N)
      RESP=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/blog/$BLOG_ID" 2>/dev/null)
      T1=$(date +%s%N)
      ELAPSED=$(( (T1 - T0) / 1000000 ))
      echo "DETAIL:$i:$j:$RESP:${ELAPSED}ms" >> "$REPORT_DIR/detail_results.txt"
    ) &
  done
  if (( i % 5 == 0 )); then wait; fi
done
wait
echo "  完成"

# 测试3: 点赞压力测试
echo ""
echo "[测试3] 点赞接口 POST /api/blog/{id}/like"
for ((i=1; i<=CONCURRENCY; i++)); do
  # 每个并发用户登录一次获取token
  TOKEN=$(curl -s -X POST "$BASE_URL/api/user/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"jmeter_user_$((i % 3 + 1))\",\"password\":\"test123\"}" 2>/dev/null | \
    python -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('token',''))" 2>/dev/null)

  for ((j=1; j<=LOOPS; j++)); do
    (
      T0=$(date +%s%N)
      RESP=$(curl -s -X POST "$BASE_URL/api/blog/10/like" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -o /dev/null -w "%{http_code}" 2>/dev/null)
      T1=$(date +%s%N)
      ELAPSED=$(( (T1 - T0) / 1000000 ))
      echo "LIKE:$i:$j:$RESP:${ELAPSED}ms" >> "$REPORT_DIR/like_results.txt"
    ) &
  done
  wait
done
wait
echo "  完成"

# ========== 生成报告 ==========
echo ""
echo "=========================================="
echo " 测试结果汇总"
echo "=========================================="

for test_name in page detail like; do
  RESULTS_FILE="$REPORT_DIR/${test_name}_results.txt"
  if [ ! -f "$RESULTS_FILE" ]; then
    echo "[${test_name}] 无数据"
    continue
  fi

  TOTAL=$(wc -l < "$RESULTS_FILE")
  OK=$(grep -c ":200:" "$RESULTS_FILE" 2>/dev/null || echo 0)
  FAIL=$((TOTAL - OK))

  # 提取响应时间（毫秒）
  TIMES=$(grep -oP ':\K\d+(?=ms)' "$RESULTS_FILE" 2>/dev/null | sort -n)
  if [ -n "$TIMES" ]; then
    COUNT=$(echo "$TIMES" | wc -l)
    AVG=$(echo "$TIMES" | awk '{sum+=$1} END{printf "%.0f", sum/NR}')
    P50=$(echo "$TIMES" | awk 'BEGIN{i=0} {arr[i++]=$1} END{print arr[int(i*0.5)]}')
    P90=$(echo "$TIMES" | awk 'BEGIN{i=0} {arr[i++]=$1} END{print arr[int(i*0.9)]}')
    P99=$(echo "$TIMES" | awk 'BEGIN{i=0} {arr[i++]=$1} END{print arr[int(i*0.99)]}')
    MAX_VAL=$(echo "$TIMES" | tail -1)
  else
    COUNT=0; AVG=0; P50=0; P90=0; P99=0; MAX_VAL=0
  fi

  echo ""
  case $test_name in
    page)   echo "测试1: 博客分页查询" ;;
    detail) echo "测试2: 博客详情" ;;
    like)   echo "测试3: 点赞接口" ;;
  esac
  echo "  总请求: $TOTAL | 成功: $OK | 失败: $FAIL | 成功率: $(echo "scale=1; $OK*100/$TOTAL" | bc)%"
  echo "  响应时间: 平均=${AVG}ms P50=${P50}ms P90=${P90}ms P99=${P99}ms 最大=${MAX_VAL}ms"
  echo "  QPS: $(echo "scale=0; $TOTAL*1000/($AVG*$COUNT/$CONCURRENCY)" | bc 2>/dev/null || echo "N/A")"
done

# 清理中间文件
echo ""
echo "测试完成！详细结果保存在 $REPORT_DIR/"
