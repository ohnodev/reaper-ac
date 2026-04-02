use super::Check;
use crate::schema::PlayerTickSnapshot;
use std::collections::HashMap;

const REASON_SPEED: u16 = 100;
const REASON_FLY: u16 = 101;
const REASON_NOFALL: u16 = 102;

/// Horizontal speed limit per tick for a sprinting player on ground.
/// Vanilla sprint: ~5.612 m/s ≈ 0.2806 blocks/tick; with slack for latency.
const MAX_HORIZONTAL_SPEED_SQ: f64 = 0.45 * 0.45;

/// Maximum upward delta for a single-tick jump.
const MAX_JUMP_DELTA_Y: f64 = 0.42 + 0.05; // vanilla jump + tolerance

/// Minimum expected downward velocity when airborne and not recently knocked back.
const MIN_GRAVITY_DELTA: f64 = -0.0784;

pub struct MovementCheck {
    airborne_ticks: HashMap<u128, u32>,
}

impl MovementCheck {
    pub fn new() -> Self {
        Self {
            airborne_ticks: HashMap::new(),
        }
    }
}

impl Check for MovementCheck {
    fn evaluate(&mut self, snap: &PlayerTickSnapshot) -> (f32, f32, u16) {
        if snap.recent_teleport || snap.recent_knockback || snap.in_vehicle {
            self.airborne_ticks.remove(&snap.player_id());
            return (0.0, 0.0, 0);
        }

        let pid = snap.player_id();
        let mut risk = 0.0f32;
        let mut confidence = 0.0f32;
        let mut reason = 0u16;

        // --- Speed check ---
        let h_speed_sq = snap.delta_x * snap.delta_x + snap.delta_z * snap.delta_z;
        if h_speed_sq > MAX_HORIZONTAL_SPEED_SQ {
            let excess = (h_speed_sq.sqrt() - MAX_HORIZONTAL_SPEED_SQ.sqrt()) as f32;
            risk = (excess * 4.0).min(1.0);
            confidence = 0.8;
            reason = REASON_SPEED;
        }

        // --- Fly / NoFall check ---
        let air = self.airborne_ticks.entry(pid).or_insert(0);
        if snap.on_ground {
            *air = 0;
        } else {
            *air += 1;

            if *air > 4 && snap.delta_y > 0.0 && snap.delta_y > MAX_JUMP_DELTA_Y {
                let fly_risk = ((snap.delta_y - MAX_JUMP_DELTA_Y) as f32 * 5.0).min(1.0);
                if fly_risk > risk {
                    risk = fly_risk;
                    confidence = 0.85;
                    reason = REASON_FLY;
                }
            }

            if *air > 8 && snap.vel_y > MIN_GRAVITY_DELTA && !snap.in_liquid {
                let nofall_risk = 0.6f32;
                if nofall_risk > risk {
                    risk = nofall_risk;
                    confidence = 0.7;
                    reason = REASON_NOFALL;
                }
            }
        }

        (risk, confidence, reason)
    }

    fn name(&self) -> &'static str {
        "movement"
    }
}
