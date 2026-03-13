#!/bin/bash
# =============================================================================
# Backup automatique MySQL pour Saloons (VPS)
#
# Usage :
#   ./backup-mysql.sh staging     → backup du conteneur mysql-staging
#   ./backup-mysql.sh prod        → backup du conteneur mysql-prod
#
# Cron :
#   0 3 * * * /opt/saloons/scripts/backup-mysql.sh staging
#   0 4 * * * /opt/saloons/scripts/backup-mysql.sh prod
# =============================================================================

set -euo pipefail

# --- Paramètre obligatoire : environnement ----------------------------------
ENV="${1:-}"
if [[ -z "$ENV" || ! "$ENV" =~ ^(staging|prod)$ ]]; then
  echo "Usage: $0 <staging|prod>"
  exit 1
fi

# --- Charger le .env ---------------------------------------------------------
ENV_FILE="/opt/saloons/.env"
if [ -f "$ENV_FILE" ]; then
  # shellcheck disable=SC1090
  set -a
  source "$ENV_FILE"
  set +a
else
  echo "ERREUR : fichier $ENV_FILE introuvable."
  exit 1
fi

# --- Configuration par environnement ----------------------------------------
CONTAINER_NAME="mysql-${ENV}"
BACKUP_DIR="/opt/backups/saloons/${ENV}"
RETENTION_DAYS=7
DATE=$(date +%Y%m%d_%H%M%S)
LOG_FILE="$BACKUP_DIR/backup.log"

# Résoudre le nom de la base selon l'environnement (DB_NAME_STAGING ou DB_NAME_PROD)
ENV_UPPER=$(echo "$ENV" | tr '[:lower:]' '[:upper:]')
DB_NAME_VAR="DB_NAME_${ENV_UPPER}"
DB_NAME="${!DB_NAME_VAR:?ERREUR : la variable $DB_NAME_VAR n'est pas définie dans $ENV_FILE}"

# Le mot de passe root DOIT être défini — pas de valeur par défaut
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:?ERREUR : MYSQL_ROOT_PASSWORD n'est pas défini dans $ENV_FILE}"

# --- Fonctions ---------------------------------------------------------------
log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] [$ENV] $1" | tee -a "$LOG_FILE"
}

# --- Pré-vérifications -------------------------------------------------------
mkdir -p "$BACKUP_DIR"

if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
  log "ERREUR : Le conteneur $CONTAINER_NAME n'est pas en cours d'exécution."
  exit 1
fi

# --- Dump --------------------------------------------------------------------
BACKUP_FILE="$BACKUP_DIR/${DB_NAME}_${DATE}.sql.gz"
STDERR_LOG="$BACKUP_DIR/mysqldump_stderr_${DATE}.log"

log "Début du backup de '$DB_NAME' (conteneur: $CONTAINER_NAME)..."

docker exec "$CONTAINER_NAME" mysqldump \
  -u root \
  -p"$MYSQL_ROOT_PASSWORD" \
  --single-transaction \
  --routines \
  --triggers \
  --set-gtid-purged=OFF \
  "$DB_NAME" 2>"$STDERR_LOG" \
  | gzip > "$BACKUP_FILE"

# Vérifier si mysqldump a remonté des erreurs
if [ -s "$STDERR_LOG" ]; then
  log "ATTENTION : mysqldump a produit des avertissements (voir $STDERR_LOG)"
else
  rm -f "$STDERR_LOG"
fi

BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
log "Backup créé : $BACKUP_FILE ($BACKUP_SIZE)"

# --- Vérification du fichier -------------------------------------------------
if [ ! -s "$BACKUP_FILE" ]; then
  log "ERREUR : Le fichier de backup est vide. Suppression."
  rm -f "$BACKUP_FILE"
  exit 1
fi

# --- Vérification d'intégrité gzip ------------------------------------------
if ! gunzip -t "$BACKUP_FILE" 2>/dev/null; then
  log "ERREUR : L'archive $BACKUP_FILE est corrompue. Suppression."
  rm -f "$BACKUP_FILE"
  exit 1
fi

# --- Rotation (suppression des backups > RETENTION_DAYS jours) ---------------
DELETED=$(find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -mtime +${RETENTION_DAYS} -print -delete | wc -l | tr -d ' ')
if [ "$DELETED" -gt 0 ]; then
  log "Rotation : $DELETED ancien(s) backup(s) supprimé(s) (> ${RETENTION_DAYS} jours)"
fi

# --- Résumé ------------------------------------------------------------------
TOTAL=$(find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" | wc -l | tr -d ' ')
log "Terminé. $TOTAL backup(s) conservé(s)."
