do_compile:append() {
    if [ -z "${RC_CAR_DATA_PARTLABEL}" ] || [ -z "${RC_CAR_DATA_PART_FSTYPE}" ] || [ -z "${RC_CAR_DATA_PART_SIZE_BYTES}" ]; then
        exit 0
    fi

    RC_CAR_DATA_PARTLABEL="${RC_CAR_DATA_PARTLABEL}" \
    RC_CAR_DATA_PART_FSTYPE="${RC_CAR_DATA_PART_FSTYPE}" \
    RC_CAR_DATA_PART_SIZE_BYTES="${RC_CAR_DATA_PART_SIZE_BYTES}" \
    EXTERNAL_FLASH_XML="${B}/external-flash.xml" \
    python3 - <<'PY'
import os
import re

partlabel = (os.environ.get("RC_CAR_DATA_PARTLABEL") or "").strip()
new_fstype = (os.environ.get("RC_CAR_DATA_PART_FSTYPE") or "").strip()
new_size = (os.environ.get("RC_CAR_DATA_PART_SIZE_BYTES") or "").strip()
external_xml = (os.environ.get("EXTERNAL_FLASH_XML") or "").strip()
if not partlabel or not new_fstype or not new_size or not external_xml:
    raise SystemExit(0)

try:
    new_size_int = int(new_size)
except ValueError:
    raise SystemExit(f"RC_CAR_DATA_PART_SIZE_BYTES must be an integer byte count, got: {new_size!r}")

if new_size_int <= 0:
    raise SystemExit(f"RC_CAR_DATA_PART_SIZE_BYTES must be > 0, got: {new_size_int}")

if not os.path.exists(external_xml):
    raise SystemExit(0)

with open(external_xml, "r", encoding="utf-8", errors="replace") as f:
    lines = f.readlines()

in_target_partition = False
updated_size = False
updated_fstype = False

partition_start = f'<partition name="{partlabel}"'
size_re = re.compile(r"(<size>\s*)([^<]*?)(\s*</size>)")
fstype_re = re.compile(r"(<filesystem_type>\s*)([^<]*?)(\s*</filesystem_type>)")

out = []
for line in lines:
    if partition_start in line:
        in_target_partition = True

    if in_target_partition and (not updated_fstype):
        m = fstype_re.search(line)
        if m:
            def _fstype_repl(match: "re.Match[str]") -> str:
                return f"{match.group(1)}{new_fstype}{match.group(3)}"

            out.append(fstype_re.sub(_fstype_repl, line, count=1))
            updated_fstype = True
            continue

    if in_target_partition and (not updated_size):
        m = size_re.search(line)
        if m:
            def _size_repl(match: "re.Match[str]") -> str:
                return f"{match.group(1)}{new_size_int}{match.group(3)}"

            out.append(size_re.sub(_size_repl, line, count=1))
            updated_size = True
            continue

    if in_target_partition and "</partition>" in line:
        in_target_partition = False

    out.append(line)

if not updated_size:
    raise SystemExit(f"RC-car: did not find <size> for partition {partlabel} in {external_xml}")

if not updated_fstype:
    raise SystemExit(f"RC-car: did not find <filesystem_type> for partition {partlabel} in {external_xml}")

with open(external_xml, "w", encoding="utf-8") as f:
    f.writelines(out)

print(f"RC-car: set {partlabel} filesystem_type={new_fstype} size={new_size_int} in {external_xml}")
PY
}
