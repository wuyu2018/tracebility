FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

ARG NGINX_HOST
ARG NGINX_PORT
ENV VUE_APP_API_BASE_URL="https://${NGINX_HOST}:${NGINX_PORT}/api"
RUN npm run build

FROM nginx:alpine

RUN addgroup -g 1001 -S nginxgroup && \
    adduser -u 1001 -S nginxuser -G nginxgroup

RUN chown -R nginxuser:nginxgroup /usr/share/nginx/html && \
    chown -R nginxuser:nginxgroup /var/cache/nginx && \
    chown -R nginxuser:nginxgroup /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown -R nginxuser:nginxgroup /var/run/nginx.pid

COPY --from=builder --chown=nginxuser:nginxgroup /app/dist /usr/share/nginx/html
COPY --chown=nginxuser:nginxgroup nginx.conf /etc/nginx/conf.d/default.conf

USER nginxuser

EXPOSE 80

# 健康检查：访问 /health 端点（已在 nginx.conf 中定义）
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/health || exit 1

CMD ["nginx", "-g", "daemon off;"]