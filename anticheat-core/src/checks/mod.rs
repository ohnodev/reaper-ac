pub mod movement;
pub mod combat;
pub mod interaction;

use crate::schema::PlayerTickSnapshot;

/// Every check module implements this trait.
pub trait Check: Send + Sync {
    /// Evaluate one snapshot. Returns (risk_contribution, confidence, reason_code).
    /// risk_contribution is in [0.0, 1.0]; the scorer aggregates across checks.
    fn evaluate(&mut self, snap: &PlayerTickSnapshot) -> (f32, f32, u16);

    fn name(&self) -> &'static str;
}
