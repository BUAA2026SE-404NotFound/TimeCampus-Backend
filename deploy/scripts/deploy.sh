#!/usr/bin/env bash
# Compatibility entrypoint. The frontend has moved to the standalone
# TimeCampus-Portal repository, so backend deployment is handled by
# deploy-backend.sh only.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/deploy-backend.sh" "$@"
