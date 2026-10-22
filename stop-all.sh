#!/usr/bin/env bash
# Detiene los microservicios iniciados con start-all.sh.
cd "$(dirname "$0")"
for pidfile in logs/*.pid; do
  [[ -e "$pidfile" ]] || continue
  pid=$(cat "$pidfile")
  if kill "$pid" 2>/dev/null; then echo "Detenido $(basename "$pidfile" .pid) (PID $pid)"; fi
  rm -f "$pidfile"
done
