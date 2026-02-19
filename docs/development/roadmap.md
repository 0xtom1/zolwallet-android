# Zol Wallet - Development Roadmap

## Phase 1
### Proxy Service for Helius ✅
- All Solana RPC/API calls route through our own proxy to protect API keys and user IPs
- Repo: `0xtom1/zol-rpc-proxy` (forked from `helius-labs/helius-rpc-proxy`)
- Local path: `C:\Users\thomas\Documents\git\helius-rpc-proxy`
- **Status: COMPLETE** — Live on GCP Cloud Run (`wallet-api-5arl64l7ia-uc.a.run.app`)
- Supports: JSON-RPC, REST API passthrough, WebSocket with buffering/keepalive

### Tor Routing for Solana Transactions ✅
- Route Solana traffic through Tor when the user has Tor enabled
- **Status: COMPLETE** — `SolanaHttpClientFactory` bridges into `HttpClientProvider`; Solana automatically inherits Tor when enabled, identical to Zcash behaviour
- Key files: `solana-lib/.../rpc/SolanaRpcProvider.kt`, `ui-lib/.../di/SolanaViewModelModule.kt`

### UI Redesign
- Full UI overhaul once all features are functionally complete
- Design screens around the final feature set to avoid rework

### Logos and Collateral
- Final branding and visual assets
- Done last for cohesion with the new UI

## Phase 2 ← NEXT
### ZEC Transparent Address Rotation
- Allow users to rotate their transparent Zcash addresses for improved privacy

### Multiple Solana Addresses
- Support multiple Solana addresses (derivation, storage, switching)
