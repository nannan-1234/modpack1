#!/usr/bin/env bash
set -euo pipefail

# Primary Linux/WSL build entry. ForgeGradle supplies the official mappings,
# access transformers and reobfuscation; the wrapper pins Gradle 8.1.1.

root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd -- "$root/../.." && pwd)"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$repo_root/.gradle-home}"

cd "$root"
has_task=false
for arg in "$@"; do
    if [[ "$arg" != -* ]]; then
        has_task=true
        break
    fi
done

if $has_task; then
    exec ./gradlew "$@"
fi
exec ./gradlew build "$@"
