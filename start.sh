#!/bin/bash
echo "========================================"
echo "  Hotel Management System - Starting..."
echo "========================================"
echo ""

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "[1/2] Starting Backend (Spring Boot)..."
(cd "$SCRIPT_DIR/backend" && mvn spring-boot:run) &

echo "Waiting for backend to be ready..."
until curl -s --fail http://localhost:8080/api >/dev/null 2>&1; do
  sleep 3
done

echo "Backend is ready!"

echo ""
echo "[2/2] Starting Frontend (Vite)..."
(cd "$SCRIPT_DIR/frontend" && npm run dev) &

echo ""
echo "========================================"
echo "  Backend:  http://localhost:8080"
echo "  Frontend: http://localhost:3000"
echo "  Login:    admin / 123456"
echo "========================================"
echo ""
echo "Press Ctrl+C to stop both services."
wait
