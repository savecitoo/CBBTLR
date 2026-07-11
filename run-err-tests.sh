#!/bin/bash

TEST_ROOT="src/test/java/err-test"

PASS=0
FAIL=0

for dir in "$TEST_ROOT"/*; do
    [ -d "$dir" ] || continue

    program="$dir/sorgente.codine"

    if [ ! -f "$program" ]; then
        echo "[SKIP] $(basename "$dir"): sorgente.codine non trovato"
        continue
    fi

    echo "===== $(basename "$dir") ====="

    output=$(mktemp)

    # input-1.txt e' opzionale (serve solo ai test di runtime)
    input="$dir/input-1.txt"
    if [ -f "$input" ]; then
        gradle -q interpreter -Pfile="$program" \
            < "$input" \
            > "$output" 2>&1
    else
        gradle -q interpreter -Pfile="$program" \
            < /dev/null \
            > "$output" 2>&1
    fi

    # il test passa se nell'output (stdout+stderr) compare ERROR
    if grep -q "ERROR" "$output"; then
        echo "  [PASS]"
        ((PASS++))
    else
        echo "  [FAIL] nessun ERROR nell'output"
        head -n 5 "$output" | sed 's/^/    | /'
        ((FAIL++))
    fi

    rm -f "$output"

    echo
done

echo "=========================="
echo "PASS: $PASS"
echo "FAIL: $FAIL"

if [ "$FAIL" -ne 0 ]; then
    exit 1
fi