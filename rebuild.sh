#!/bin/bash
echo "=== 重建并启动所有服务 ==="
docker-compose -f docker-compose.prod.yml down

echo ""
echo "=== 重新构建镜像 ==="
docker-compose -f docker-compose.prod.yml build --no-cache

echo ""
echo "=== 启动服务 ==="
docker-compose -f docker-compose.prod.yml up -d

echo ""
echo "=== 服务状态 ==="
docker-compose -f docker-compose.prod.yml ps
