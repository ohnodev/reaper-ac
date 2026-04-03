#!/usr/bin/env python3
"""
Generate a protocol mapping checklist seeded from Grim's packet surface.

This scans Java sources for PacketEvents constants:
  PacketType.Play.Client.FOO
  PacketType.Play.Server.BAR

Output is a JSON file suitable for tracking protocol migration from 773 -> 775.
"""

from __future__ import annotations

import argparse
import json
import re
from dataclasses import asdict, dataclass
from datetime import datetime, timezone
from pathlib import Path


PACKET_RE = re.compile(r"PacketType\.Play\.(Client|Server)\.([A-Z0-9_]+)")

MOVEMENT_PRIORITY = {
    "Client.PLAYER_FLYING",
    "Client.PLAYER_POSITION",
    "Client.PLAYER_POSITION_AND_ROTATION",
    "Client.PLAYER_ROTATION",
    "Client.VEHICLE_MOVE",
    "Client.CLIENT_TICK_END",
    "Client.PLAYER_INPUT",
    "Server.PLAYER_POSITION_AND_LOOK",
    "Server.ENTITY_POSITION_SYNC",
    "Server.VEHICLE_MOVE",
}


@dataclass
class PacketEntry:
    key: str
    direction: str
    state: str
    packet_name_773: str
    packet_name_775: str
    packet_id_773: int | None
    packet_id_775: int | None
    grim_usage: bool
    priority: str
    status: str
    tests: dict
    notes: str
    packet_structure: dict
    examples: dict


def collect_used_packets(java_root: Path) -> list[str]:
    found: set[str] = set()
    for path in java_root.rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        for match in PACKET_RE.finditer(text):
            side, name = match.groups()
            found.add(f"{side}.{name}")
    return sorted(found)


def make_entry(key: str) -> PacketEntry:
    direction = "serverbound" if key.startswith("Client.") else "clientbound"
    priority = "movement-core" if key in MOVEMENT_PRIORITY else "grim-required"
    return PacketEntry(
        key=key,
        direction=direction,
        state="play",
        packet_name_773=key,
        packet_name_775=key,
        packet_id_773=None,
        packet_id_775=None,
        grim_usage=True,
        priority=priority,
        status="seeded_773",
        tests={
            "captured_775": False,
            "decoded_775": False,
            "legacy_773_compatible": False,
            "validated_grim_path": False,
            "sample_count": 0,
            "last_tested_at": None,
        },
        notes="",
        packet_structure={
            "fields_773": [],
            "fields_775": [],
            "added_in_775": [],
            "removed_in_775": [],
            "legacy_field_overlap": [],
            "source": "seed",
        },
        examples={
            "decode_773": None,
            "decode_775": None,
        },
    )


def build_doc(packet_keys: list[str]) -> dict:
    now = datetime.now(timezone.utc).isoformat()
    entries = [asdict(make_entry(key)) for key in packet_keys]
    return {
        "meta": {
            "title": "Grim packet mapping checklist (protocol 773 -> 775)",
            "generated_at": now,
            "source": "code-scan",
            "owner": "grim-26-1-official-namespace",
            "status_values": [
                "seeded_773",
                "captured_775",
                "decoded_775",
                "legacy_773_compatible",
                "needs_remap",
                "validated_grim_path",
            ],
            "priority_values": ["movement-core", "grim-required"],
        },
        "summary": {
            "grim_packets_total": len(entries),
            "movement_core_total": sum(1 for e in entries if e["priority"] == "movement-core"),
        },
        "packets": entries,
    }


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate a 773->775 packet mapping checklist from Grim packet usage."
    )
    parser.add_argument(
        "--repo-root",
        default=".",
        help="Repository root containing common/src/main/java",
    )
    parser.add_argument(
        "--output",
        default="tests/protocol/packet-map-773-to-775.json",
        help="Output JSON path",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    repo_root = Path(args.repo_root).resolve()
    java_root = repo_root / "common" / "src" / "main" / "java"
    if not java_root.exists():
        raise SystemExit(f"Java source root not found: {java_root}")

    packet_keys = collect_used_packets(java_root)
    doc = build_doc(packet_keys)

    out = (repo_root / args.output).resolve()
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(doc, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {out} with {doc['summary']['grim_packets_total']} packets")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
