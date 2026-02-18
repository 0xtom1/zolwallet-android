# Zol RPC Proxy

A server-side proxy for Helius RPC and API endpoints. The Helius API key never leaves the server.

## Dev URL

```
https://wallet-api-5arl64l7ia-uc.a.run.app
```

---

## Endpoints

### `POST /helius` — Solana JSON-RPC

Proxies standard Solana JSON-RPC requests to Helius mainnet.

```http
POST https://wallet-api-5arl64l7ia-uc.a.run.app/helius
Content-Type: application/json

{"jsonrpc":"2.0","id":1,"method":"getBalance","params":["<wallet_address>"]}
```

Example methods: `getBalance`, `getAccountInfo`, `getTransaction`, `sendTransaction`, `getHealth`, etc.

---

### `POST /helius/{path}` — Helius REST API

Proxies requests to the Helius enhanced API. The path after `/helius` is forwarded as-is to `api.helius.xyz`.

```http
POST https://wallet-api-5arl64l7ia-uc.a.run.app/helius/v0/transactions
Content-Type: application/json

{"transactions":["<tx_signature>"]}
```

```http
POST https://wallet-api-5arl64l7ia-uc.a.run.app/helius/v0/addresses/<address>/transactions
Content-Type: application/json

{}
```

---

### `GET /helius` — WebSocket

Proxies WebSocket connections to `wss://mainnet.helius-rpc.com` for real-time subscriptions.

```
wss://wallet-api-5arl64l7ia-uc.a.run.app/helius
```

Example subscription:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "accountSubscribe",
  "params": ["<wallet_address>", {"encoding": "jsonParsed", "commitment": "confirmed"}]
}
```

Supported subscription methods: `accountSubscribe`, `logsSubscribe`, `signatureSubscribe`, `slotSubscribe`, etc.

WebSocket features:
- Messages sent before the upstream connection opens are buffered (up to 10, 10s timeout)
- Keepalive ping every 20s to prevent idle disconnects
- Subprotocol negotiation passthrough

---

## Notes

- No authentication required — the proxy is publicly accessible
- The API key is injected server-side; do not pass it from the client
- CORS is open (`*`) on dev
- The service runs on GCP Cloud Run and scales to zero — the first request after idle may take ~1–2s to cold start
