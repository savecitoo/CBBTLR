#!/bin/bash

TEST_ROOT="src/test/java/tests"

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

    for input in "$dir"/input-*.txt; do
        [ -f "$input" ] || continue

        num=$(basename "$input" | sed -E 's/input-([0-9]+)\.txt/\1/')
        expected="$dir/expected-$num.txt"

        if [ ! -f "$expected" ]; then
            echo "  [SKIP] expected-$num.txt non trovato"
            continue
        fi

        output=$(mktemp)

        gradle -q interpreter -Pfile="$program" \
            < "$input" \
            > "$output"

        if diff -q "$expected" "$output" >/dev/null; then
            echo "  [PASS] test $num"
            ((PASS++))
        else
            echo "  [FAIL] test $num"
            diff -u "$expected" "$output"
            ((FAIL++))
        fi

        rm -f "$output"
    done

    echo
done

echo "=========================="
echo "PASS: $PASS"
echo "FAIL: $FAIL"

if [ "$FAIL" -ne 0 ]; then
    exit 1
fi