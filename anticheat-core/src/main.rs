use tracing_subscriber::EnvFilter;

#[tokio::main]
async fn main() -> std::io::Result<()> {
    tracing_subscriber::fmt()
        .with_env_filter(
            EnvFilter::try_from_default_env().unwrap_or_else(|_| EnvFilter::new("info")),
        )
        .init();

    let socket_path = std::env::var("REAPER_SOCKET")
        .unwrap_or_else(|_| "/tmp/reaper-anticheat.sock".to_string());

    anticheat_core::server::run(&socket_path).await
}
