# Movement packet differences: protocol 773 -> 775

This document summarizes movement-related packet differences observed in live captures, with focus on what changed from `773` to `775`.

## Quick answer

For core movement packets, `775` is legacy-compatible with `773` for existing movement values.  
The main wire-level decode difference is that `775` includes additional boolean flags on client movement packets.

## New `775` fields on client movement packets

Observed on:

- `Client.PLAYER_FLYING`
- `Client.PLAYER_POSITION`
- `Client.PLAYER_POSITION_AND_ROTATION`
- `Client.PLAYER_ROTATION`

Added fields:

- `has_position`
- `has_rotation`
- `horizontal_collision`

Compatibility note:

- Existing `773` fields (for example `x`, `y`, `z`, `yaw`, `pitch`, `on_ground`) still decode and match expected legacy behavior.
- These new booleans are additive and can be ignored by legacy-compatible logic if not needed.

## Packet-by-packet movement status from live captures

- `Client.PLAYER_FLYING`: compatible; plus new `775` flags.
- `Client.PLAYER_POSITION`: compatible; plus new `775` flags.
- `Client.PLAYER_POSITION_AND_ROTATION`: compatible; plus new `775` flags.
- `Client.PLAYER_ROTATION`: compatible; plus new `775` flags.
- `Client.CLIENT_TICK_END`: no payload difference observed.
- `Client.PLAYER_INPUT`: no payload difference observed.
- `Server.ENTITY_POSITION_SYNC`: no payload difference observed.
- `Server.PLAYER_POSITION_AND_LOOK`: no payload difference observed.
- `Client.VEHICLE_MOVE`: not observed in current live capture set.
- `Server.VEHICLE_MOVE`: not observed in current live capture set.

## Practical mapping guidance

- Treat `775` movement as `773`-compatible by default.
- For client movement packets, tolerate/read the three extra booleans in `775`.
- Keep vehicle movement marked as pending until captures include those packets.
