#!/usr/bin/env bash
# After agent edits Java or frontend sources, compile/typecheck and feed
# truncated output back into the agent loop via additional_context.
set -u

input=$(cat || true)
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT" || exit 0

file=$(printf '%s' "$input" | jq -r '
  .file_path
  // .tool_input.path
  // .tool_input.file_path
  // .tool_input.filePath
  // empty
' 2>/dev/null || true)

[[ -z "${file}" || "${file}" == "null" ]] && exit 0

rel="${file#"$ROOT"/}"
# Drop leading ./ if present
rel="${rel#./}"

run_and_capture() {
  local title="$1"
  shift
  local out ec=0
  out=$("$@" 2>&1) || ec=$?
  out=$(printf '%s\n' "$out" | tail -n 80)
  printf '%s\nexit_code=%s\n\n%s\n' "$title" "$ec" "$out"
}

report=""
module="${rel%%/*}"

if [[ "$rel" == *.java ]]; then
  case "$module" in
    rfr-auth)
      report=$(run_and_capture "[hook] compile rfr-auth ($rel)" ./gradlew --quiet :rfr-auth:compileJava)
      ;;
    rfr-api)
      report=$(run_and_capture "[hook] compile rfr-api ($rel)" ./gradlew --quiet :rfr-api:compileJava)
      ;;
    rfr-e2e)
      report=$(run_and_capture "[hook] compile rfr-e2e tests ($rel)" ./gradlew --quiet :rfr-e2e:compileTestJava)
      ;;
    *)
      exit 0
      ;;
  esac
elif [[ "$rel" == rfr-api/src/main/resources/graphql/* ]]; then
  report=$(run_and_capture "[hook] compile rfr-api graphql ($rel)" ./gradlew --quiet :rfr-api:compileJava)
elif [[ "$rel" == rfr-gql-client/src/* ]]; then
  case "$rel" in
    *.ts|*.tsx|*.js|*.jsx|*.css)
      lint_out=$(run_and_capture "[hook] lint rfr-gql-client ($rel)" npm --prefix rfr-gql-client run lint)
      tsc_ec=0
      tsc_raw=$(
        cd rfr-gql-client && npm exec -- tsc -p tsconfig.json --noEmit 2>&1
      ) || tsc_ec=$?
      tsc_out=$(printf '[hook] tsc --noEmit rfr-gql-client (%s)\nexit_code=%s\n\n%s\n' \
        "$rel" "$tsc_ec" "$(printf '%s\n' "$tsc_raw" | tail -n 80)")
      report=$(printf '%s\n---\n%s\n' "$lint_out" "$tsc_out")
      ;;
    *)
      exit 0
      ;;
  esac
else
  exit 0
fi

[[ -z "$report" ]] && exit 0

jq -n --arg ctx "$report" '{ additional_context: $ctx }'

exit 0
