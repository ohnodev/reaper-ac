# Protocol Mapping Workflow (773 -> 775)

This directory tracks Grim's packet surface migration from protocol `773` to `775`.

## 1) Generate seed checklist from Grim usage

From repository root:

```bash
python3 tests/scripts/generate-packet-map.py
```

This writes:

- `tests/protocol/packet-map-773-to-775.json`

The file is seeded with only packets Grim currently references in code and marks movement-critical packets as `movement-core`.

Each packet entry now includes structure fields:

- `packet_structure.fields_773`
- `packet_structure.fields_775`
- `packet_structure.added_in_775`
- `packet_structure.removed_in_775`
- `packet_structure.legacy_field_overlap`
- `examples.decode_773`
- `examples.decode_775`

Seeded packets that have not been captured yet keep these fields empty (`source: "seed"`). Captured packets are populated from observed decode payloads (`source: "capture"`).

## 2) Capture movement packets in test environment

Capture rows should be NDJSON, one JSON object per line:

```json
{"packet":"Client.PLAYER_POSITION","phase":"play","decode_773":{"x":1.0,"y":64.0,"z":1.0,"on_ground":true},"decode_775":{"x":1.0,"y":64.0,"z":1.0,"on_ground":true}}
```

To enable live capture from Grim at runtime:

- `GRIM_MOVEMENT_CAPTURE=1`
- optional output path: `GRIM_MOVEMENT_CAPTURE_FILE=/absolute/path/captures.ndjson`

Or JVM props:

- `-Dgrim.movementCapture=true`
- `-Dgrim.movementCaptureFile=/absolute/path/captures.ndjson`

Example:

```bash
GRIM_MOVEMENT_CAPTURE=1 \
GRIM_MOVEMENT_CAPTURE_FILE=/root/grim-26-1-official-namespace/tests/protocol/live-movement-captures.ndjson \
./tests/scripts/run-server.sh --server-dir tests/runs/grim-pe-26.1 --java-bin /path/to/java25
```

Start with movement packets first:

- `Client.PLAYER_FLYING`
- `Client.PLAYER_POSITION`
- `Client.PLAYER_POSITION_AND_ROTATION`
- `Client.PLAYER_ROTATION`
- `Client.VEHICLE_MOVE`
- `Client.CLIENT_TICK_END`
- `Client.PLAYER_INPUT`
- `Server.PLAYER_POSITION_AND_LOOK`
- `Server.ENTITY_POSITION_SYNC`
- `Server.VEHICLE_MOVE`

## 3) Update checklist from captures

```bash
python3 tests/scripts/compare-movement-captures.py --captures /path/to/captures.ndjson
```

Use strict mode for exact normalized equality:

```bash
python3 tests/scripts/compare-movement-captures.py --captures /path/to/captures.ndjson --strict
```

## 4) Attach wiki packet structure references

You can enrich each packet entry with a canonical protocol section match from the Minecraft Wiki packet spec:

```bash
curl -L "https://minecraft.wiki/w/Java_Edition_protocol/Packets" -o /tmp/mc_packets.html
python3 tests/scripts/enrich-packet-map-from-wiki.py \
  --wiki-md /tmp/mc_packets.html \
  --map tests/protocol/packet-map-773-to-775.json
```

This adds a `wiki_reference` block per packet with:

- `match_status`, `match_method`, `match_score`
- `section_title`, `section_anchor`
- parsed `field_names` when table extraction is available

## Status flow

- `seeded_773` -> baseline known packet
- `captured_775` -> packet observed in test run
- `decoded_775` -> parser produced normalized output
- `legacy_773_compatible` -> 775 output matches 773 expectations
- `needs_remap` -> captured/decoded but incompatible with 773
- `validated_grim_path` -> remap validated for Grim consumption
