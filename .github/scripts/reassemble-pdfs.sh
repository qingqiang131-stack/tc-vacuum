#!/usr/bin/env bash
set -euo pipefail

parts_dir="build-assets/pdf-parts"
web_content="ChanKhongIOS/ChanKhong/WebContent"
shopt -s nullglob
first_parts=("$parts_dir"/*.part000)

if ((${#first_parts[@]} == 0)); then
  echo "No lesson PDF parts found in $parts_dir" >&2
  exit 1
fi

for first_part in "${first_parts[@]}"; do
  filename="${first_part##*/}"
  filename="${filename%.part000}"
  part_files=("$parts_dir/$filename".part*)
  expected=0

  for part_file in "${part_files[@]}"; do
    part_number="${part_file##*.part}"
    expected_number=$(printf '%03d' "$expected")
    if [[ "$part_number" != "$expected_number" ]]; then
      echo "Missing or out-of-order part for $filename: expected $expected_number" >&2
      exit 1
    fi
    expected=$((expected + 1))
  done

  cat "${part_files[@]}" > "$web_content/$filename"
done
