#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if ! command -v java >/dev/null 2>&1; then
  echo "No se encontró Java. Instala un JDK 17 o superior." >&2
  exit 1
fi

java_major=$(java -version 2>&1 | awk -F '[\".]' '/version/ {print $2}')
if [[ -z "$java_major" || "$java_major" -lt 17 ]]; then
  echo "El backend requiere Java 17 o superior." >&2
  exit 1
fi

mvn clean package
mkdir -p target/distribution
cp target/crm-asociaciones-backend-*.jar target/distribution/crm-asociaciones-backend.jar

echo "Backend empaquetado en target/distribution/crm-asociaciones-backend.jar"
