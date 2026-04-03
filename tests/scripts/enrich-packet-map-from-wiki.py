#!/usr/bin/env python3
"""
Attach Minecraft Wiki packet references to packet-map-773-to-775.json.

This script maps Grim packet keys (PacketEvents naming) to the closest
Play packet section in the Minecraft Wiki protocol page markdown export.
It also extracts field-name rows when the wiki table shape is parseable.
"""

from __future__ import annotations

import argparse
import html
import json
import re
from dataclasses import dataclass
from difflib import SequenceMatcher
from pathlib import Path


SIDE_CLIENTBOUND = "clientbound"
SIDE_SERVERBOUND = "serverbound"


ALIAS_BY_KEY: dict[str, str] = {
    "Client.ANIMATION": "Swing Arm",
    "Client.CHUNK_BATCH_ACK": "Chunk Batch Received",
    "Client.CLICK_WINDOW": "Click Container",
    "Client.CLIENT_SETTINGS": "Client Information (play)",
    "Client.CREATIVE_INVENTORY_ACTION": "Set Creative Mode Slot",
    "Client.HELD_ITEM_CHANGE": "Set Held Item (serverbound)",
    "Client.PLAYER_BLOCK_PLACEMENT": "Use Item On",
    "Client.PLAYER_DIGGING": "Player Action",
    "Client.PLAYER_FLYING": "Set Player Movement Flags",
    "Client.SPECTATE": "Teleport To Entity",
    "Client.STEER_VEHICLE": "Move Vehicle (serverbound)",
    "Client.TAB_COMPLETE": "Command Suggestions Request",
    "Client.WINDOW_CONFIRMATION": "Change Container Slot State",
    "Server.ACKNOWLEDGE_BLOCK_CHANGES": "Acknowledge Block Change",
    "Server.ACKNOWLEDGE_PLAYER_DIGGING": "Acknowledge Block Change",
    "Server.CHANGE_GAME_STATE": "Game Event",
    "Server.CHUNK_BATCH_END": "Chunk Batch Finished",
    "Server.CHUNK_DATA": "Chunk Data and Update Light",
    "Server.DESTROY_ENTITIES": "Remove Entities",
    "Server.ENTITY_POSITION_SYNC": "Teleport Entity",
    "Server.JOIN_GAME": "Login (play)",
    "Server.MAP_CHUNK_BULK": "Chunk Data and Update Light",
    "Server.MULTI_BLOCK_CHANGE": "Update Section Blocks",
    "Server.OPEN_HORSE_WINDOW": "Open Horse Screen",
    "Server.OPEN_WINDOW": "Open Screen",
    "Server.PLAYER_INFO_REMOVE": "Player Info Remove",
    "Server.PLAYER_INFO_UPDATE": "Player Info Update",
    "Server.PLAYER_POSITION_AND_LOOK": "Synchronize Player Position",
    "Server.TAGS": "Update Tags (play)",
    "Server.UNLOAD_CHUNK": "Unload Chunk",
    "Server.WINDOW_CONFIRMATION": "Set Container Slot",
    "Server.WINDOW_ITEMS": "Set Container Content",
}


def norm(s: str) -> str:
    return re.sub(r"[^a-z0-9]+", " ", s.lower()).strip()


def tokens(s: str) -> set[str]:
    return {t for t in norm(s).split() if t}


def title_from_key(key: str) -> str:
    # Client.PLAYER_POSITION_AND_ROTATION -> "Player Position And Rotation"
    short = key.split(".", 1)[1] if "." in key else key
    return short.replace("_", " ").title()


@dataclass
class WikiSection:
    side: str
    title: str
    anchor: str
    start: int
    end: int
    fields: list[str]


def anchor_from_title(title: str) -> str:
    # Follows a simple MediaWiki-like style that works for most headings.
    a = re.sub(r"[^\w\s-]", "", title).strip().replace(" ", "_")
    return a


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Enrich packet map from wiki markdown export.")
    parser.add_argument("--map", default="tests/protocol/packet-map-773-to-775.json")
    parser.add_argument("--wiki-md", required=True, help="Path to markdown or HTML export")
    parser.add_argument(
        "--wiki-url",
        default="https://minecraft.wiki/w/Java_Edition_protocol/Packets",
    )
    return parser.parse_args()


def parse_sections(lines: list[str]) -> list[WikiSection]:
    sections: list[WikiSection] = []
    in_play = False
    side: str | None = None
    current_idx: int | None = None
    current_title: str | None = None
    current_side: str | None = None

    for i, line in enumerate(lines):
        if line.startswith("## "):
            in_play = line.strip() == "## Play"
            side = None
        elif in_play and line.startswith("### "):
            h = line[4:].strip().lower()
            if h == "clientbound":
                side = SIDE_CLIENTBOUND
            elif h == "serverbound":
                side = SIDE_SERVERBOUND
            else:
                side = None
        elif in_play and side and line.startswith("#### "):
            # finalize previous section
            if current_idx is not None and current_title and current_side:
                fields = extract_fields(lines[current_idx + 1 : i])
                sections.append(
                    WikiSection(
                        side=current_side,
                        title=current_title,
                        anchor=anchor_from_title(current_title),
                        start=current_idx,
                        end=i,
                        fields=fields,
                    )
                )
            current_idx = i
            current_title = line[5:].strip()
            current_side = side

    if current_idx is not None and current_title and current_side:
        fields = extract_fields(lines[current_idx + 1 : len(lines)])
        sections.append(
            WikiSection(
                side=current_side,
                title=current_title,
                anchor=anchor_from_title(current_title),
                start=current_idx,
                end=len(lines),
                fields=fields,
            )
        )
    return sections


TAG_RE = re.compile(r"<[^>]+>")


def strip_tags(s: str) -> str:
    return html.unescape(TAG_RE.sub("", s)).strip()


def parse_sections_from_html(text: str) -> list[WikiSection]:
    # Scan heading tags in order and track hierarchy.
    heading_re = re.compile(r"<h([234])[^>]*id=\"([^\"]+)\"[^>]*>(.*?)</h\1>", re.IGNORECASE | re.DOTALL)
    tags = list(heading_re.finditer(text))
    sections: list[WikiSection] = []

    in_play = False
    side: str | None = None

    for idx, m in enumerate(tags):
        level = int(m.group(1))
        tag_id = m.group(2)
        title = strip_tags(m.group(3))
        start = m.end()
        end = tags[idx + 1].start() if idx + 1 < len(tags) else len(text)

        if level == 2:
            in_play = title == "Play"
            side = None
            continue
        if not in_play:
            continue

        if level == 3:
            if title == "Clientbound":
                side = SIDE_CLIENTBOUND
            elif title == "Serverbound":
                side = SIDE_SERVERBOUND
            else:
                side = None
            continue
        if level != 4 or not side:
            continue

        body = text[start:end]
        fields = extract_fields_from_html(body)
        sections.append(
            WikiSection(
                side=side,
                title=title,
                anchor=tag_id,
                start=start,
                end=end,
                fields=fields,
            )
        )
    return sections


def extract_fields_from_html(section_html: str) -> list[str]:
    table_re = re.compile(r"<table[^>]*>(.*?)</table>", re.IGNORECASE | re.DOTALL)
    row_re = re.compile(r"<tr[^>]*>(.*?)</tr>", re.IGNORECASE | re.DOTALL)
    cell_re = re.compile(r"<t[hd][^>]*>(.*?)</t[hd]>", re.IGNORECASE | re.DOTALL)

    out: list[str] = []
    seen: set[str] = set()

    # Scan tables and keep rows under a "Field Name" column.
    for tm in table_re.finditer(section_html):
        rows = row_re.findall(tm.group(1))
        field_col: int | None = None
        for row in rows:
            cells = [strip_tags(c) for c in cell_re.findall(row)]
            if not cells:
                continue
            lowered = [c.lower() for c in cells]
            if "field name" in lowered:
                field_col = lowered.index("field name")
                continue
            if field_col is None or field_col >= len(cells):
                continue
            name = cells[field_col].strip()
            if not name or name in {"---", "_no fields_"}:
                continue
            lname = name.lower()
            if lname in {"field name", "packet id", "state", "bound to", "notes"}:
                continue
            if lname.startswith("protocol:"):
                continue
            key = norm(name)
            if key and key not in seen:
                seen.add(key)
                out.append(name)
        if out:
            break
    return out


def extract_fields(section_lines: list[str]) -> list[str]:
    rows: list[list[str]] = []
    for line in section_lines:
        if line.startswith("|"):
            cells = [c.strip() for c in line.strip().strip("|").split("|")]
            if cells:
                rows.append(cells)

    field_names: list[str] = []
    field_col: int | None = None
    for row in rows:
        lowered = [c.lower() for c in row]
        if "field name" in lowered:
            field_col = lowered.index("field name")
            continue
        if field_col is None:
            continue
        if field_col >= len(row):
            continue
        name = row[field_col].strip()
        if not name:
            continue
        if name in {"---", "_no fields_"}:
            continue
        lname = name.lower()
        if lname in {"field name", "packet id", "state", "bound to", "notes"}:
            continue
        if lname.startswith("protocol:"):
            continue
        field_names.append(name)

    deduped: list[str] = []
    seen: set[str] = set()
    for n in field_names:
        k = norm(n)
        if not k or k in seen:
            continue
        seen.add(k)
        deduped.append(n)
    return deduped


def score_match(packet_label: str, section_title: str) -> float:
    a = norm(packet_label)
    b = norm(section_title)
    ratio = SequenceMatcher(None, a, b).ratio()
    ta = tokens(a)
    tb = tokens(b)
    if not ta and not tb:
        return ratio
    overlap = len(ta & tb) / max(1, len(ta | tb))
    return (ratio * 0.55) + (overlap * 0.45)


def pick_best(packet_key: str, side: str, sections: list[WikiSection]) -> tuple[WikiSection | None, float, str]:
    side_sections = [s for s in sections if s.side == side]
    alias = ALIAS_BY_KEY.get(packet_key)
    if alias:
        for s in side_sections:
            if norm(s.title) == norm(alias):
                return s, 1.0, "alias"

    candidate_name = title_from_key(packet_key)
    best: WikiSection | None = None
    best_score = 0.0
    for s in side_sections:
        sc = score_match(candidate_name, s.title)
        if sc > best_score:
            best = s
            best_score = sc
    if best and best_score >= 0.45:
        return best, best_score, "fuzzy"
    return None, best_score, "unmatched"


def main() -> int:
    args = parse_args()
    map_path = Path(args.map).resolve()
    wiki_path = Path(args.wiki_md).resolve()
    if not map_path.exists():
        raise SystemExit(f"Map file does not exist: {map_path}")
    if not wiki_path.exists():
        raise SystemExit(f"Wiki markdown file does not exist: {wiki_path}")

    wiki_text = wiki_path.read_text(encoding="utf-8")
    if "<html" in wiki_text.lower() and "<table" in wiki_text.lower():
        sections = parse_sections_from_html(wiki_text)
    else:
        sections = parse_sections(wiki_text.splitlines())
    doc = json.loads(map_path.read_text(encoding="utf-8"))

    matched = 0
    for entry in doc.get("packets", []):
        side = SIDE_SERVERBOUND if entry.get("direction") == "serverbound" else SIDE_CLIENTBOUND
        key = entry.get("key", "")
        best, score, method = pick_best(key, side, sections)
        if best is None:
            entry["wiki_reference"] = {
                "url": args.wiki_url,
                "match_status": "unmatched",
                "match_method": method,
                "match_score": round(score, 4),
                "section_title": None,
                "section_anchor": None,
                "field_names": [],
            }
            continue

        matched += 1
        entry["wiki_reference"] = {
            "url": args.wiki_url,
            "match_status": "matched",
            "match_method": method,
            "match_score": round(score, 4),
            "section_title": best.title,
            "section_anchor": best.anchor,
            "field_names": best.fields,
        }

    map_path.write_text(json.dumps(doc, indent=2) + "\n", encoding="utf-8")
    print(f"Matched {matched}/{len(doc.get('packets', []))} packets to wiki sections")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
