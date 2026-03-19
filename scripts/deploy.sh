#!/bin/bash
set -e

echo "======================================"
echo "Food Traceability System Deployment"
echo "======================================"

# Check if .env file exists
if [ ! -f .env ]; then
    echo "Error: .env file not found!"
    echo "Please copy .env.example to .env and configure it."
    exit 1
fi

# Load environment variables
source .env

# Validate required variables
required_vars=("MYSQL_ROOT_PASSWORD" "MYSQL_DATABASE" "MYSQL_USER" "MYSQL_PASSWORD")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "Error: $var is not set in .env"
        exit 1
    fi
done

echo "Stopping existing containers..."
docker-compose down || true

echo "Building images..."
docker-compose build --no-cache

echo "Starting services..."
docker-compose up -d

echo "Waiting for backend to be ready..."
for i in {1..30}; do
    if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "Backend is ready!"
        break
    fi
    echo "Waiting for backend... ($i/30)"
    sleep 2
done

echo "======================================"
echo "Deployment completed!"
echo "Frontend: http://localhost:${FRONTEND_EXPOSED_PORT:-80}"
echo "Backend:  http://localhost:8080"
echo "======================================"

echo ""
echo "Useful commands:"
echo "  docker-compose logs -f backend  # View backend logs"
echo "  docker-compose logs -f frontend # View frontend logs"
echo "  docker-compose down             # Stop services"
echo "  docker-compose restart           # Restart services"
