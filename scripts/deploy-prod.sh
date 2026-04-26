#!/bin/bash
set -e

# ===================== 食品溯源系统 - Docker 部署脚本 =====================
# 用途：生产环境一键部署
# 使用方法：./scripts/deploy-prod.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
COMPOSE_FILE="$PROJECT_ROOT/docker-compose.prod.yml"

echo "========================================"
echo "  食品溯源系统 - 生产环境部署"
echo "========================================"
echo ""

# 检查.env 文件
if [ ! -f "$PROJECT_ROOT/.env" ]; then
    echo "❌ 错误：未找到 .env 文件"
    echo "   请复制 .env.example 并修改配置："
    echo "   cp .env.example .env"
    echo ""
    exit 1
fi

# 加载环境变量
set -a
source "$PROJECT_ROOT/.env"
set +a

echo "✅ 环境变量已加载"
echo ""

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo "❌ 错误：未找到 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ 错误：未找到 docker-compose"
    exit 1
fi

echo "🐳 Docker 版本：$(docker --version)"
echo "🐳 Docker Compose 版本：$(docker-compose --version)"
echo ""

# 停止旧容器
echo "📦 停止并移除旧容器..."
cd "$PROJECT_ROOT"
docker-compose -f "$COMPOSE_FILE" down 2>/dev/null || true
echo ""

# 重新构建镜像
echo "🔨 重新构建镜像..."
docker-compose -f "$COMPOSE_FILE" build
echo ""

# 启动服务
echo "🚀 启动服务..."
docker-compose -f "$COMPOSE_FILE" up -d
echo ""

# 等待服务启动
echo "⏳ 等待服务启动 (90 秒)..."
sleep 90
echo ""

# 检查服务状态
echo "📊 服务状态:"
echo "----------------------------------------"
docker-compose -f "$COMPOSE_FILE" ps
echo "----------------------------------------"
echo ""

# 健康检查
echo "🏥 执行健康检查..."

# 检查 Edge Nginx
if curl -f http://localhost/health &> /dev/null; then
    echo "✅ Edge Nginx: 正常"
else
    echo "❌ Edge Nginx: 失败"
fi

# 检查后端 Actuator
if curl -f http://localhost/actuator/health &> /dev/null; then
    echo "✅ Backend: 正常"
else
    echo "❌ Backend: 失败"
fi

# 检查前端
if curl -f http://localhost/ &> /dev/null; then
    echo "✅ Frontend: 正常"
else
    echo "❌ Frontend: 失败"
fi

echo ""
echo "========================================"
echo "  部署完成！"
echo "========================================"
echo ""
echo "查看日志：docker-compose logs -f"
echo "停止服务：docker-compose -f $COMPOSE_FILE down"
echo "重启服务：docker-compose -f $COMPOSE_FILE restart"
echo ""
