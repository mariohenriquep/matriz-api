# syntax=docker/dockerfile:1
# Imagem do frontend da Matriz (Ficha 05, Ex. 2). Construir a partir da pasta frontend/:
#   docker build -t matriz-frontend --build-arg VITE_API_URL=http://localhost:8080/api/v1 frontend
# O Vite fixa o endereço da API na build. O browser chama a API diretamente e o backend autoriza a
# origem do frontend por CORS (MATRIZ_CORS_ORIGENS).

# 1) Build: instala as dependências exatas do package-lock.json e gera os ficheiros estáticos
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
ARG VITE_API_URL=http://localhost:8080/api/v1
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

# 2) Execução: nginx sem privilégios (utilizador nginx, porta 8080) a servir dist/
FROM nginxinc/nginx-unprivileged:1.30-alpine
# Correções de segurança do Alpine publicadas depois da imagem base (encontradas pelo Trivy)
USER root
RUN apk upgrade --no-cache
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
# A imagem base já corre como nginx (uid 101); fica explícito para quem lê e para o Trivy (DS-0002)
USER nginx
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/ > /dev/null || exit 1
