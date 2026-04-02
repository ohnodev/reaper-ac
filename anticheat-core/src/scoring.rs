use crate::checks::{Check, combat::CombatCheck, interaction::InteractionCheck, movement::MovementCheck};
use crate::schema::{Action, ActionResponse, PlayerTickSnapshot};

const SETBACK_THRESHOLD: f32 = 0.70;
const KICK_THRESHOLD: f32 = 0.95;

pub struct ScoringPipeline {
    checks: Vec<Box<dyn Check>>,
}

impl ScoringPipeline {
    pub fn new() -> Self {
        Self {
            checks: vec![
                Box::new(MovementCheck::new()),
                Box::new(CombatCheck::new()),
                Box::new(InteractionCheck::new()),
            ],
        }
    }

    pub fn evaluate(&mut self, snap: &PlayerTickSnapshot) -> ActionResponse {
        let mut max_risk = 0.0f32;
        let mut best_confidence = 0.0f32;
        let mut best_reason = 0u16;

        for check in &mut self.checks {
            let (risk, conf, reason) = check.evaluate(snap);
            if risk > max_risk {
                max_risk = risk;
                best_confidence = conf;
                best_reason = reason;
            }
        }

        let action = if max_risk >= KICK_THRESHOLD {
            Action::Kick
        } else if max_risk >= SETBACK_THRESHOLD {
            Action::Setback
        } else if max_risk > 0.0 {
            Action::Flag
        } else {
            Action::None
        };

        ActionResponse {
            player_id_msb: snap.player_id_msb,
            player_id_lsb: snap.player_id_lsb,
            risk_score: max_risk,
            confidence: best_confidence,
            action,
            reason_code: best_reason,
        }
    }
}
