#!/bin/bash
set -e

echo "=== 正在拉取所有镜像 ==="

echo "拉取 mysql:8.0..."
docker pull mysql:8.0

echo "拉取 nginx:alpine..."
docker pull nginx:alpine

echo "拉取 backend 镜像 (待构建)..."
docker pull openjdk:17-slim || true

echo "拉取 node:18 (用于构建前端)..."
docker pull node:18 || true

echo ""
echo "=== 所有镜像拉取完成 ==="
echo ""
echo "=== 启动服务 ==="
docker-compose -f docker-compose.prod.yml up -d

echo ""
echo "=== 服务状态 ==="
docker-compose -f docker-compose.prod.yml ps
