#!/usr/bin/env bash
set -euo pipefail

# Usage: ./run_skill_workflow.sh [run_id] [test_scope] [artifact_dir] [mvn_cmd]
# Example: ./run_skill_workflow.sh 4538 changed docs/reports mvn

run_id="${1:-$(date -u +%Y%m%d%H%M%S)}"
test_scope="${2:-changed}"
artifact_dir="${3:-docs/reports}"
mvn_cmd="${4:-mvn}"

run_dir="$artifact_dir/$run_id"
mkdir -p "$run_dir/reports"

branch="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")"
commit="$(git rev-parse HEAD 2>/dev/null || echo "")"
started_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

cat > "$run_dir/meta.json" <<EOF
{"run_id":"$run_id","test_scope":"$test_scope","branch":"$branch","commit":"$commit","started_at":"$started_at"}
EOF

# Create backup branch (no-op if already exists)
backup_branch="skill/${run_id}-backup"
echo "Creating backup branch $backup_branch"
if ! git rev-parse --verify --quiet "$backup_branch" >/dev/null; then
  git branch "$backup_branch" || true
fi

git rev-parse HEAD > "$run_dir/commit.txt" 2>/dev/null || true
git diff > "$run_dir/change.patch" 2>/dev/null || true

run_maven() {
  local cmd="$1"
  local log="$run_dir/mvn.log"
  echo "Running: $cmd"
  # Run and append output to log (preserve previous runs)
  (eval "$cmd") >> "$log" 2>&1 || return 1
  return 0
}

if [ "$test_scope" = "full" ]; then
  run_maven "$mvn_cmd clean test"
elif [ "$test_scope" = "smoke" ]; then
  run_maven "$mvn_cmd -Dtest=*Smoke* test"
elif [[ "$test_scope" == module:* ]]; then
  module_name="${test_scope#module:}"
  run_maven "$mvn_cmd -pl $module_name -am test"
else
  # changed
  echo "Detecting changed files against origin/main..."
  git fetch origin main --quiet || true
  changed=$(git diff --name-only origin/main...HEAD || true)
  if [ -z "$changed" ]; then
    echo "No changed files detected; falling back to smoke tests"
    run_maven "$mvn_cmd -Dtest=*Smoke* test"
  else
    # naive module mapping: first path segment as module
    modules=$(echo "$changed" | awk -F'/' '{print $1}' | sort -u)
    for m in $modules; do
      if [ -n "$m" ]; then
        echo "Running tests for module: $m"
        run_maven "$mvn_cmd -pl $m -am test" || true
      fi
    done
  fi
fi

# Collect surefire/failsafe reports
# Typical locations: **/target/surefire-reports/*.xml or **/target/failsafe-reports/*.xml
find . -type f -path "*/target/surefire-reports/*" -name "*.xml" -print0 | while IFS= read -r -d '' f; do
  cp -f "$f" "$run_dir/reports/"
done || true
find . -type f -path "*/target/failsafe-reports/*" -name "*.xml" -print0 | while IFS= read -r -d '' f; do
  cp -f "$f" "$run_dir/reports/"
done || true

# Optionally copy other report formats (html)
find . -type f -path "*/target/site/*" -name "*.html" -print0 | while IFS= read -r -d '' f; do
  cp -f "$f" "$run_dir/reports/"
done || true

finished_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
# Update meta.json with finished time
python - <<PY
import json
p='''$run_dir/meta.json'''
with open(p,'r') as f:
    m=json.load(f)
m['finished_at']='$finished_at'
with open(p,'w') as f:
    json.dump(m,f)
PY

echo "Artifacts stored at: $run_dir"
exit 0
