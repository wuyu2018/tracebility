#!/bin/bash
echo "=== 停止所有服务 ==="
docker-compose -f docker-compose.prod.yml down

echo ""
echo "=== 清理未使用的镜像 (可选) ==="
read -p "是否清理未使用的镜像? (y/n): " confirm
if [ "$confirm" = "y" ]; then
    docker image prune -f
fi

echo ""
echo "=== 停止完成 ==="
