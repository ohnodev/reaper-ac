#!/usr/bin/env python3
"""
Compare 773 vs 775 decode captures for all packets present in packet-map.

Input rows are NDJSON:
{
  "packet": "Client.PLAYER_POSITION",
  "decode_773": {...},
  "decode_775": {...}
}
"""

from __future__ import annotations

import argparse
import json
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


def normalize_number(value: Any) -> Any:
    if isinstance(value, float):
        return round(value, 6)
    return value


def normalize_obj(value: Any) -> Any:
    if isinstance(value, dict):
        return {k: normalize_obj(value[k]) for k in sorted(value)}
    if isinstance(value, list):
        return [normalize_obj(v) for v in value]
    return normalize_number(value)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Compare 773 and 775 decode captures for packet map.")
    parser.add_argument("--captures", required=True, help="NDJSON captures path")
    parser.add_argument("--map", default="tests/protocol/packet-map-773-to-775.json", help="Checklist JSON path")
    parser.add_argument("--strict", action="store_true", help="Require exact normalized equality")
    return parser.parse_args()


def load_ndjson(path: Path) -> list[dict]:
    rows = []
    for idx, line in enumerate(path.read_text(encoding="utf-8").splitlines(), start=1):
        line = line.strip()
        if not line:
            continue
        try:
            rows.append(json.loads(line))
        except json.JSONDecodeError as exc:
            raise SystemExit(f"Invalid JSON on line {idx}: {exc}") from exc
    return rows


def ensure_structure_fields(entry: dict) -> None:
    entry.setdefault(
        "packet_structure",
        {
            "fields_773": [],
            "fields_775": [],
            "added_in_775": [],
            "removed_in_775": [],
            "legacy_field_overlap": [],
            "source": "seed",
        },
    )
    entry.setdefault("examples", {"decode_773": None, "decode_775": None})


def extract_fields(rows: list[dict], key: str) -> list[str]:
    fields: set[str] = set()
    for row in rows:
        obj = row.get(key)
        if isinstance(obj, dict):
            fields.update(obj.keys())
    return sorted(fields)


def first_example(rows: list[dict], key: str) -> dict[str, Any] | None:
    for row in rows:
        obj = row.get(key)
        if isinstance(obj, dict):
            return normalize_obj(obj)
    return None


def update_entry(entry: dict, rows: list[dict], strict: bool) -> None:
    now = datetime.now(timezone.utc).isoformat()
    sample_count = len(rows)
    decoded_rows = [
        r
        for r in rows
        if isinstance(r.get("decode_775"), dict)
        and not r.get("decode_775", {}).get("_capture_only", False)
    ]
    decoded_ok = len(decoded_rows) > 0 and all("decode_775" in r for r in decoded_rows)

    compat = True
    for row in decoded_rows:
        left = normalize_obj(row.get("decode_773", {}))
        right = normalize_obj(row.get("decode_775", {}))
        if strict:
            if left != right:
                compat = False
                break
        else:
            for key, value in left.items():
                if key not in right or right[key] != value:
                    compat = False
                    break
            if not compat:
                break

    tests = entry["tests"]
    tests["captured_775"] = sample_count > 0
    tests["decoded_775"] = decoded_ok
    tests["legacy_773_compatible"] = compat and decoded_ok
    tests["validated_grim_path"] = compat and decoded_ok
    tests["sample_count"] = sample_count
    tests["last_tested_at"] = now

    if decoded_rows:
        fields_773 = extract_fields(decoded_rows, "decode_773")
        fields_775 = extract_fields(decoded_rows, "decode_775")
        entry["packet_structure"]["fields_773"] = fields_773
        entry["packet_structure"]["fields_775"] = fields_775
        entry["packet_structure"]["added_in_775"] = [k for k in fields_775 if k not in fields_773]
        entry["packet_structure"]["removed_in_775"] = [k for k in fields_773 if k not in fields_775]
        entry["packet_structure"]["legacy_field_overlap"] = [k for k in fields_773 if k in fields_775]
        entry["packet_structure"]["source"] = "capture"
        entry["examples"]["decode_773"] = first_example(decoded_rows, "decode_773")
        entry["examples"]["decode_775"] = first_example(decoded_rows, "decode_775")

    if tests["legacy_773_compatible"]:
        entry["status"] = "legacy_773_compatible"
    elif tests["decoded_775"]:
        entry["status"] = "needs_remap"
    elif tests["captured_775"]:
        entry["status"] = "captured_775"


def main() -> int:
    args = parse_args()
    captures_path = Path(args.captures).resolve()
    map_path = Path(args.map).resolve()
    if not captures_path.exists():
        raise SystemExit(f"Capture file does not exist: {captures_path}")
    if not map_path.exists():
        raise SystemExit(f"Map file does not exist: {map_path}")

    rows = load_ndjson(captures_path)
    grouped: dict[str, list[dict]] = {}
    for row in rows:
        packet = row.get("packet")
        if packet:
            grouped.setdefault(packet, []).append(row)

    doc = json.loads(map_path.read_text(encoding="utf-8"))
    touched = 0
    for entry in doc.get("packets", []):
        ensure_structure_fields(entry)
        key = entry.get("key")
        if key in grouped:
            touched += 1
            update_entry(entry, grouped[key], args.strict)

    map_path.write_text(json.dumps(doc, indent=2) + "\n", encoding="utf-8")
    print(f"Updated {map_path} using {len(rows)} capture rows")
    print(f"Packet entries touched: {touched}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
