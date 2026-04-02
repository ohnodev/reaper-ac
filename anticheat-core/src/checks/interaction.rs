use super::Check;
use crate::schema::PlayerTickSnapshot;
use std::collections::HashMap;

const REASON_FAST_BREAK: u16 = 300;
const REASON_FAST_USE: u16 = 301;

const MAX_DIGS_PER_TICK: u16 = 1;
const MAX_BLOCK_USE_PER_TICK: u16 = 2;

const SUSTAINED_WINDOW: usize = 10;

pub struct InteractionCheck {
    dig_history: HashMap<u128, Vec<u16>>,
    use_history: HashMap<u128, Vec<u16>>,
}

impl InteractionCheck {
    pub fn new() -> Self {
        Self {
            dig_history: HashMap::new(),
            use_history: HashMap::new(),
        }
    }
}

impl Check for InteractionCheck {
    fn evaluate(&mut self, snap: &PlayerTickSnapshot) -> (f32, f32, u16) {
        let pid = snap.player_id();
        let mut risk = 0.0f32;
        let mut confidence = 0.0f32;
        let mut reason = 0u16;

        // --- Fast break ---
        if snap.dig_action_count > 0 {
            let history = self.dig_history.entry(pid).or_insert_with(Vec::new);
            history.push(snap.dig_action_count);
            if history.len() > SUSTAINED_WINDOW {
                history.remove(0);
            }

            if snap.dig_action_count > MAX_DIGS_PER_TICK {
                let r = ((snap.dig_action_count - MAX_DIGS_PER_TICK) as f32 * 0.4).min(1.0);
                if r > risk {
                    risk = r;
                    confidence = 0.7;
                    reason = REASON_FAST_BREAK;
                }
            }

            if history.len() >= SUSTAINED_WINDOW {
                let total: u16 = history.iter().sum();
                let avg = total as f32 / SUSTAINED_WINDOW as f32;
                if avg > 0.9 {
                    let r = ((avg - 0.9) * 3.0).min(1.0);
                    if r > risk {
                        risk = r;
                        confidence = 0.85;
                        reason = REASON_FAST_BREAK;
                    }
                }
            }
        }

        // --- Fast block/item use ---
        if snap.block_use_count > 0 || snap.item_use_count > 0 {
            let combined = snap.block_use_count + snap.item_use_count;
            let history = self.use_history.entry(pid).or_insert_with(Vec::new);
            history.push(combined);
            if history.len() > SUSTAINED_WINDOW {
                history.remove(0);
            }

            if combined > MAX_BLOCK_USE_PER_TICK {
                let r = ((combined - MAX_BLOCK_USE_PER_TICK) as f32 * 0.3).min(1.0);
                if r > risk {
                    risk = r;
                    confidence = 0.65;
                    reason = REASON_FAST_USE;
                }
            }
        }

        (risk, confidence, reason)
    }

    fn name(&self) -> &'static str {
        "interaction"
    }
}
