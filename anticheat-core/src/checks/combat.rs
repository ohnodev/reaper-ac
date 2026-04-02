use super::Check;
use crate::schema::PlayerTickSnapshot;
use std::collections::HashMap;

const REASON_ATTACK_RATE: u16 = 200;
/// Reserved for future reach detection.
#[allow(dead_code)]
const REASON_ATTACK_REACH: u16 = 201;

/// Vanilla max CPS is effectively capped at 20 (one per tick).
/// Sustained high rates across multiple ticks indicate automation.
const MAX_ATTACKS_PER_TICK: u16 = 1;
const SUSTAINED_WINDOW: usize = 20;

pub struct CombatCheck {
    attack_history: HashMap<u128, Vec<u16>>,
}

impl CombatCheck {
    pub fn new() -> Self {
        Self {
            attack_history: HashMap::new(),
        }
    }
}

impl Check for CombatCheck {
    fn evaluate(&mut self, snap: &PlayerTickSnapshot) -> (f32, f32, u16) {
        if snap.attack_count == 0 {
            return (0.0, 0.0, 0);
        }

        let pid = snap.player_id();
        let history = self.attack_history.entry(pid).or_insert_with(Vec::new);
        history.push(snap.attack_count);
        if history.len() > SUSTAINED_WINDOW {
            history.remove(0);
        }

        let mut risk = 0.0f32;
        let mut confidence = 0.0f32;
        let mut reason = 0u16;

        // Single-tick burst
        if snap.attack_count > MAX_ATTACKS_PER_TICK {
            risk = ((snap.attack_count - MAX_ATTACKS_PER_TICK) as f32 * 0.3).min(1.0);
            confidence = 0.75;
            reason = REASON_ATTACK_RATE;
        }

        // Sustained high rate (CPS > 16 equivalent over the window)
        if history.len() >= SUSTAINED_WINDOW {
            let total: u16 = history.iter().sum();
            let avg = total as f32 / SUSTAINED_WINDOW as f32;
            if avg > 0.8 {
                let sustained_risk = ((avg - 0.8) * 2.0).min(1.0);
                if sustained_risk > risk {
                    risk = sustained_risk;
                    confidence = 0.9;
                    reason = REASON_ATTACK_RATE;
                }
            }
        }

        (risk, confidence, reason)
    }

    fn name(&self) -> &'static str {
        "combat"
    }
}
