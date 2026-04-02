use std::hint::black_box;
use std::time::Instant;

use anticheat_core::schema::{PlayerTickSnapshot, SNAPSHOT_SIZE};
use anticheat_core::scoring::ScoringPipeline;

fn make_snapshot(tick: i32) -> [u8; SNAPSHOT_SIZE] {
    let mut buf = [0u8; SNAPSHOT_SIZE];
    // schema version 1
    buf[0] = 1;
    buf[1] = 0;
    // tick
    let tick_bytes = tick.to_le_bytes();
    buf[2..6].copy_from_slice(&tick_bytes);
    // player id
    buf[6..14].copy_from_slice(&1u64.to_le_bytes());
    buf[14..22].copy_from_slice(&42u64.to_le_bytes());
    // pos (100.0, 64.0, 200.0)
    buf[22..30].copy_from_slice(&100.0f64.to_le_bytes());
    buf[30..38].copy_from_slice(&64.0f64.to_le_bytes());
    buf[38..46].copy_from_slice(&200.0f64.to_le_bytes());
    // delta (0.1, 0.0, 0.1)
    buf[46..54].copy_from_slice(&0.1f64.to_le_bytes());
    buf[54..62].copy_from_slice(&0.0f64.to_le_bytes());
    buf[62..70].copy_from_slice(&0.1f64.to_le_bytes());
    // flags: on_ground
    buf[94] = 0x01;
    buf
}

fn main() {
    let mut pipeline = ScoringPipeline::new();

    // Warm up
    for i in 0..1000 {
        let raw = make_snapshot(i);
        let snap = PlayerTickSnapshot::from_bytes(&raw);
        let _ = black_box(pipeline.evaluate(&snap));
    }

    // Benchmark: 100 players * 1000 ticks
    let players = 100;
    let ticks = 1000;
    let total = players * ticks;

    let snapshots: Vec<[u8; SNAPSHOT_SIZE]> = (0..total)
        .map(|i| {
            let mut raw = make_snapshot(i as i32);
            // Vary player ID
            let pid = (i % players) as u64;
            raw[6..14].copy_from_slice(&pid.to_le_bytes());
            raw
        })
        .collect();

    let start = Instant::now();
    for raw in &snapshots {
        let snap = PlayerTickSnapshot::from_bytes(raw);
        let _ = black_box(pipeline.evaluate(&snap));
    }
    let elapsed = start.elapsed();

    let per_snap_ns = elapsed.as_nanos() as f64 / total as f64;
    let per_tick_100p_us = (per_snap_ns * players as f64) / 1000.0;

    println!("--- Scoring Benchmark ---");
    println!("Total snapshots: {}", total);
    println!("Elapsed: {:?}", elapsed);
    println!("Per snapshot: {:.0} ns", per_snap_ns);
    println!("Per tick (100 players): {:.1} µs", per_tick_100p_us);
    println!(
        "Budget check (< 500 µs/tick): {}",
        if per_tick_100p_us < 500.0 { "PASS" } else { "FAIL" }
    );
}
