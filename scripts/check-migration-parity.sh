#!/usr/bin/env bash
# check-migration-parity.sh
# CI guardrail: ensures every MariaDB Flyway migration version has a matching H2 mirror.
# Exit 0 = parity OK, Exit 1 = mismatch found.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

MARIADB_DIR="$PROJECT_ROOT/src/main/resources/db/migration"
H2_DIR="$PROJECT_ROOT/src/main/resources/db/migration-h2"

# H2-only versions allowlist (e.g. seed data scripts that don't exist in MariaDB)
H2_ONLY_ALLOWLIST=("V9000")

# --- Extract version numbers from filenames ---
extract_versions() {
  local dir="$1"
  ls "$dir" | grep -oP '^V\d+' | sort -t'V' -k2 -n | uniq
}

mariadb_versions=$(extract_versions "$MARIADB_DIR")
h2_versions=$(extract_versions "$H2_DIR")

errors=0

# --- Check: every MariaDB version must exist in H2 ---
missing_in_h2=()
while IFS= read -r ver; do
  if ! echo "$h2_versions" | grep -qx "$ver"; then
    missing_in_h2+=("$ver")
  fi
done <<< "$mariadb_versions"

# --- Check: every H2 version must exist in MariaDB OR be in allowlist ---
unexpected_h2_only=()
while IFS= read -r ver; do
  if ! echo "$mariadb_versions" | grep -qx "$ver"; then
    # Check allowlist
    allowed=false
    for allowed_ver in "${H2_ONLY_ALLOWLIST[@]}"; do
      if [[ "$ver" == "$allowed_ver" ]]; then
        allowed=true
        break
      fi
    done
    if [[ "$allowed" == "false" ]]; then
      unexpected_h2_only+=("$ver")
    fi
  fi
done <<< "$h2_versions"

# --- Report ---
if [[ ${#missing_in_h2[@]} -gt 0 ]] || [[ ${#unexpected_h2_only[@]} -gt 0 ]]; then
  echo "ERROR: Migration parity check FAILED"
  echo ""

  if [[ ${#missing_in_h2[@]} -gt 0 ]]; then
    echo "MariaDB versions missing from H2 mirror:"
    for ver in "${missing_in_h2[@]}"; do
      echo "  - $ver"
    done
    echo ""
  fi

  if [[ ${#unexpected_h2_only[@]} -gt 0 ]]; then
    echo "Unexpected H2-only versions (not in allowlist):"
    for ver in "${unexpected_h2_only[@]}"; do
      echo "  - $ver"
    done
    echo ""
  fi

  echo "H2-only allowlist: ${H2_ONLY_ALLOWLIST[*]}"
  echo ""
  echo "To fix: add the missing H2 migration file(s) to $H2_DIR"
  echo "        or add the H2-only version to the allowlist in this script."
  exit 1
fi

echo "Migration parity check passed ($(echo "$mariadb_versions" | wc -l) MariaDB versions, $(echo "$h2_versions" | wc -l) H2 versions)"
exit 0
