#!/usr/bin/env bash
# ============================================================
# start-desafio.sh - Script de start rápido do Desafio
# Sobe infra (PostgreSQL + Keycloak) no Docker e inicia
# backend (Quarkus) e frontend (Angular/Nx) em modo dev.
# ============================================================
set -e

DEV_DIR="$HOME/dev"
COMPOSE_DIR="$DEV_DIR/desafio-completo"
BACKEND_DIR="$DEV_DIR/desafio-backend"
FRONTEND_DIR="$DEV_DIR/desafio-frontend"

echo "=============================================="
echo "  DESAFIO - Ambiente de desenvolvimento"
echo "=============================================="

# 1. Subir infraestrutura (PostgreSQL + Keycloak)
echo ""
echo "[1/5] Subindo PostgreSQL e Keycloak (Docker)..."
cd "$COMPOSE_DIR"
docker compose up -d postgres keycloak

# 2. Aguardar serviços de infra
echo ""
echo "[2/5] Aguardando infraestrutura ficar saudável..."
for i in $(seq 1 60); do
  PG_OK=$(docker inspect --format='{{.State.Health.Status}}' desafio-postgres 2>/dev/null || echo "starting")
  KC_OK=$(docker inspect --format='{{.State.Health.Status}}' desafio-keycloak 2>/dev/null || echo "starting")
  if [ "$PG_OK" = "healthy" ]; then
    echo "  PostgreSQL: pronto ✔"
    break
  fi
  sleep 2
done

for i in $(seq 1 90); do
  KC_OK=$(docker inspect --format='{{.State.Health.Status}}' desafio-keycloak 2>/dev/null || echo "starting")
  if [ "$KC_OK" = "healthy" ]; then
    echo "  Keycloak: pronto ✔"
    break
  fi
  sleep 2
done

# 3. Iniciar backend Quarkus (dev)
echo ""
echo "[3/5] Iniciando Backend Quarkus (mvn quarkus:dev)..."
cd "$BACKEND_DIR"
mvn quarkus:dev > "$DEV_DIR/backend-dev.log" 2>&1 &
BACKEND_PID=$!
echo "  Backend rodando (PID $BACKEND_PID) - log: $DEV_DIR/backend-dev.log"

# 4. Iniciar frontend Angular (nx serve)
echo ""
echo "[4/5] Iniciando Frontend Angular (nx serve)..."
cd "$FRONTEND_DIR"
npx nx serve frontend > "$DEV_DIR/frontend-dev.log" 2>&1 &
FRONTEND_PID=$!
echo "  Frontend rodando (PID $FRONTEND_PID) - log: $DEV_DIR/frontend-dev.log"

# 5. Mostrar URLs
echo ""
echo "[5/5] URLs de acesso:"
echo "  Keycloak : http://localhost:8081  (admin/admin)"
echo "  Backend  : http://localhost:8080"
echo "  Swagger  : http://localhost:8080/q/swagger-ui"
echo "  Frontend : http://localhost:4200"
echo ""
echo "Usuários Keycloak (senha padrão: 123456):"
echo "  coordenador1..3@email.com (role: coordenador)"
echo "  aluno1..5@email.com      (role: aluno)"
echo ""
echo "Para parar: kill $BACKEND_PID $FRONTEND_PID && docker compose -f $COMPOSE_DIR/docker-compose.yml down"
echo "=============================================="