use std::path::Path;
use tokio::io::{AsyncReadExt, AsyncWriteExt};
use tokio::net::UnixListener;
use tracing::{info, warn, debug};

use crate::schema::{RESPONSE_SIZE, SNAPSHOT_SIZE, PlayerTickSnapshot};
use crate::scoring::ScoringPipeline;

pub async fn run(socket_path: &str) -> std::io::Result<()> {
    let path = Path::new(socket_path);
    if path.exists() {
        std::fs::remove_file(path)?;
    }

    let listener = UnixListener::bind(path)?;
    info!("Listening on {}", socket_path);

    loop {
        let (stream, _) = listener.accept().await?;
        tokio::spawn(async move {
            if let Err(e) = handle_connection(stream).await {
                warn!("Connection error: {}", e);
            }
        });
    }
}

async fn handle_connection(mut stream: tokio::net::UnixStream) -> std::io::Result<()> {
    let mut pipeline = ScoringPipeline::new();
    let mut frame_buf = [0u8; SNAPSHOT_SIZE];

    loop {
        // Read a 4-byte little-endian count header (number of snapshots in batch).
        let mut header = [0u8; 4];
        match stream.read_exact(&mut header).await {
            Ok(_) => {}
            Err(e) if e.kind() == std::io::ErrorKind::UnexpectedEof => return Ok(()),
            Err(e) => return Err(e),
        }
        let count = u32::from_le_bytes(header) as usize;

        if count == 0 {
            // Heartbeat; respond with zero-count header.
            stream.write_all(&0u32.to_le_bytes()).await?;
            continue;
        }

        debug!("Processing batch of {} snapshots", count);

        let mut responses: Vec<[u8; RESPONSE_SIZE]> = Vec::with_capacity(count);

        for _ in 0..count {
            stream.read_exact(&mut frame_buf).await?;
            let snap = PlayerTickSnapshot::from_bytes(&frame_buf);
            let resp = pipeline.evaluate(&snap);
            responses.push(resp.to_bytes());
        }

        // Write response: 4-byte count header + N response frames.
        stream.write_all(&(responses.len() as u32).to_le_bytes()).await?;
        for resp in &responses {
            stream.write_all(resp).await?;
        }
        stream.flush().await?;
    }
}
