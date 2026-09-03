#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if [[ "$(uname -s)" != "Darwin" || "$(uname -m)" != "arm64" ]]; then
  echo "Este empaquetado requiere macOS Apple Silicon (arm64)." >&2
  exit 1
fi

command -v jpackage >/dev/null || {
  echo "No se encontró jpackage. Instala un JDK 17 o superior." >&2
  exit 1
}

mvn clean package dependency:copy-dependencies \
  -DincludeScope=runtime \
  -DoutputDirectory=target/jpackage-input/lib

main_jar=$(find target -maxdepth 1 -type f -name '*.jar' -print -quit)
if [[ -z "$main_jar" ]]; then
  echo "No se encontró el jar del cliente." >&2
  exit 1
fi
main_jar_name=$(basename "$main_jar")
cp "$main_jar" target/jpackage-input/
rm -rf target/jpackage-output

jpackage \
  --type dmg \
  --name "CRM Asociaciones" \
  --app-version 1.0.0 \
  --input target/jpackage-input \
  --main-jar "$main_jar_name" \
  --main-class com.aitsolutions.crmclient.Launcher \
  --dest target/jpackage-output \
  --mac-package-identifier com.aitsolutions.crmclient \
  --mac-package-name "CRM Asociaciones"

echo "DMG generado en target/jpackage-output/"
