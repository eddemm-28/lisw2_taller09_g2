#!/usr/bin/env bash
# Compila y levanta los 6 microservicios en segundo plano (logs en ./logs).
# Requisitos: Java 17+ y RabbitMQ en ejecucion (docker compose up -d).
# Uso: ./start-all.sh            compila y arranca
#      ./start-all.sh --no-build arranca los .jar ya compilados
set -euo pipefail
cd "$(dirname "$0")"

SERVICES=(user-service validation-service question-service lifecycle-service review-service notification-service)
PORTS=(8081 8082 8083 8084 8085 8086)

if [[ "${1:-}" != "--no-build" ]]; then
  echo "Compilando los microservicios..."
  ./mvnw -q -B -DskipTests package
fi

mkdir -p logs
for s in "${SERVICES[@]}"; do
  nohup java -jar "$s/target/$s-1.0.0.jar" > "logs/$s.log" 2>&1 &
  echo $! > "logs/$s.pid"
  echo "Iniciado $s (PID $!) -> logs/$s.log"
done

echo "Esperando a que los servicios respondan..."
for i in "${!SERVICES[@]}"; do
  port=${PORTS[$i]}
  for _ in $(seq 1 60); do
    code=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:$port/" || true)
    [[ "$code" != "000" ]] && break
    sleep 1
  done
  [[ "$code" != "000" ]] && echo "  ${SERVICES[$i]} listo en :$port" || echo "  ${SERVICES[$i]} no respondió en :$port (revise logs/${SERVICES[$i]}.log)"
done
