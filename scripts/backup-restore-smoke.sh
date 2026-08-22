#!/usr/bin/env bash
set -euo pipefail
backup="$(./scripts/backup.sh)"
echo "Backup verificado: $backup"
echo "Restauração destrutiva não é executada automaticamente. Use um banco efêmero de teste com scripts/restore.sh."
