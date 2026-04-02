/// Binary layout mirrors the Java `PlayerTickSnapshot` exactly.
/// All multi-byte fields are little-endian.
#[derive(Debug, Clone)]
pub struct PlayerTickSnapshot {
    pub schema_version: u16,
    pub tick: i32,
    pub player_id_msb: u64,
    pub player_id_lsb: u64,

    pub pos_x: f64,
    pub pos_y: f64,
    pub pos_z: f64,
    pub delta_x: f64,
    pub delta_y: f64,
    pub delta_z: f64,
    pub vel_x: f64,
    pub vel_y: f64,
    pub vel_z: f64,

    pub on_ground: bool,
    pub in_vehicle: bool,
    pub in_liquid: bool,
    pub recent_teleport: bool,
    pub recent_knockback: bool,
    pub sprinting: bool,
    pub sneaking: bool,

    pub attack_count: u16,
    pub block_use_count: u16,
    pub item_use_count: u16,
    pub dig_action_count: u16,

    pub ping_bucket: u16,
    pub skipped_ticks: u8,

    pub yaw: f32,
    pub pitch: f32,
}

pub const SNAPSHOT_SIZE: usize = 128;

impl PlayerTickSnapshot {
    pub fn from_bytes(buf: &[u8; SNAPSHOT_SIZE]) -> Self {
        let flags = buf[94];
        Self {
            schema_version: u16::from_le_bytes([buf[0], buf[1]]),
            tick: i32::from_le_bytes([buf[2], buf[3], buf[4], buf[5]]),
            player_id_msb: u64::from_le_bytes(buf[6..14].try_into().unwrap()),
            player_id_lsb: u64::from_le_bytes(buf[14..22].try_into().unwrap()),

            pos_x: f64::from_le_bytes(buf[22..30].try_into().unwrap()),
            pos_y: f64::from_le_bytes(buf[30..38].try_into().unwrap()),
            pos_z: f64::from_le_bytes(buf[38..46].try_into().unwrap()),
            delta_x: f64::from_le_bytes(buf[46..54].try_into().unwrap()),
            delta_y: f64::from_le_bytes(buf[54..62].try_into().unwrap()),
            delta_z: f64::from_le_bytes(buf[62..70].try_into().unwrap()),
            vel_x: f64::from_le_bytes(buf[70..78].try_into().unwrap()),
            vel_y: f64::from_le_bytes(buf[78..86].try_into().unwrap()),
            vel_z: f64::from_le_bytes(buf[86..94].try_into().unwrap()),

            on_ground: flags & 1 != 0,
            in_vehicle: flags & (1 << 1) != 0,
            in_liquid: flags & (1 << 2) != 0,
            recent_teleport: flags & (1 << 3) != 0,
            recent_knockback: flags & (1 << 4) != 0,
            sprinting: flags & (1 << 5) != 0,
            sneaking: flags & (1 << 6) != 0,

            attack_count: u16::from_le_bytes([buf[95], buf[96]]),
            block_use_count: u16::from_le_bytes([buf[97], buf[98]]),
            item_use_count: u16::from_le_bytes([buf[99], buf[100]]),
            dig_action_count: u16::from_le_bytes([buf[101], buf[102]]),

            ping_bucket: u16::from_le_bytes([buf[103], buf[104]]),
            skipped_ticks: buf[105],

            yaw: f32::from_le_bytes(buf[106..110].try_into().unwrap()),
            pitch: f32::from_le_bytes(buf[110..114].try_into().unwrap()),
        }
    }

    pub fn player_id(&self) -> u128 {
        ((self.player_id_msb as u128) << 64) | (self.player_id_lsb as u128)
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
#[repr(u8)]
pub enum Action {
    None = 0,
    Flag = 1,
    Setback = 2,
    Kick = 3,
}

/// Binary response frame sent back to Java.
#[derive(Debug, Clone)]
pub struct ActionResponse {
    pub player_id_msb: u64,
    pub player_id_lsb: u64,
    pub risk_score: f32,
    pub confidence: f32,
    pub action: Action,
    pub reason_code: u16,
}

pub const RESPONSE_SIZE: usize = 32;

impl ActionResponse {
    pub fn to_bytes(&self) -> [u8; RESPONSE_SIZE] {
        let mut buf = [0u8; RESPONSE_SIZE];
        buf[0..8].copy_from_slice(&self.player_id_msb.to_le_bytes());
        buf[8..16].copy_from_slice(&self.player_id_lsb.to_le_bytes());
        buf[16..20].copy_from_slice(&self.risk_score.to_le_bytes());
        buf[20..24].copy_from_slice(&self.confidence.to_le_bytes());
        buf[24] = self.action as u8;
        buf[25..27].copy_from_slice(&self.reason_code.to_le_bytes());
        buf
    }

    pub fn none_for(snap: &PlayerTickSnapshot) -> Self {
        Self {
            player_id_msb: snap.player_id_msb,
            player_id_lsb: snap.player_id_lsb,
            risk_score: 0.0,
            confidence: 0.0,
            action: Action::None,
            reason_code: 0,
        }
    }
}
