# syntax=docker/dockerfile:1
# Imagem do frontend da Matriz (Ficha 05, Ex. 2). Construir a partir da pasta frontend/:
#   docker build -t matriz-frontend --build-arg VITE_API_URL=http://localhost:8080/api/v1 frontend
# O Vite fixa o endereço da API na build. O browser chama a API diretamente e o backend autoriza a
# origem do frontend por CORS (MATRIZ_CORS_ORIGENS).

# 1) Build: instala as dependências exatas do package-lock.json e gera os ficheiros estáticos
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
# --ignore-scripts: as dependências não correm scripts de instalação (nenhuma precisa deles)
RUN npm ci --ignore-scripts --no-audit --no-fund
COPY . .
ARG VITE_API_URL=http://localhost:8080/api/v1
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

# 2) Execução: nginx sem privilégios (utilizador nginx, porta 8080) a servir dist/
# Imagem oficial do nginx (variante slim), na linha estável 1.30 (1.30.7 ou superior).
# Até à 0.8.0 usava-se nginxinc/nginx-unprivileged:1.30-alpine, mas essa imagem ainda não tem o nginx 1.30.7
# (corrige 6 CVEs HIGH/CRITICAL encontradas pelo Trivy em 2026-11-19). Os ajustes abaixo fazem o mesmo que
# essa imagem: o nginx corre como utilizador nginx (uid 101), sem root, com o PID e os temporários em /tmp.
FROM nginx:1.30-alpine-slim
# Correções de segurança do Alpine publicadas depois da imagem base (encontradas pelo Trivy)
RUN apk upgrade --no-cache \
 && sed -i -e '/^user /d' -e 's,^pid .*,pid /tmp/nginx.pid;,' /etc/nginx/nginx.conf \
 && sed -i '/^http {/a\    client_body_temp_path /tmp/client_temp;\n    proxy_temp_path /tmp/proxy_temp;\n    fastcgi_temp_path /tmp/fastcgi_temp;\n    uwsgi_temp_path /tmp/uwsgi_temp;\n    scgi_temp_path /tmp/scgi_temp;' /etc/nginx/nginx.conf \
 && chown -R nginx:nginx /var/cache/nginx /etc/nginx/conf.d
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
# Corre como nginx (uid 101), sem root; fica explícito para quem lê e para o Trivy (DS-0002)
USER nginx
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/ > /dev/null || exit 1
