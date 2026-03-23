FROM registry.cn-hangzhou.aliyuncs.com/acs/node:20-alpine AS builder

RUN npm config set registry https://registry.npmmirror.com && \
    apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

COPY package*.json ./

RUN npm install --registry https://registry.npmmirror.com

COPY . .

ARG NGINX_HOST
ARG NGINX_PORT
ENV VUE_APP_API_BASE_URL="https://${NGINX_HOST}:${NGINX_PORT}/api"
RUN npm run build

FROM registry.cn-hangzhou.aliyuncs.com/acs/nginx:alpine

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

HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
