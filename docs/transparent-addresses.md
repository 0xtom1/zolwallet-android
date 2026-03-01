# Transparent Addresses in the Zcash SDK

## Two Types of Transparent Addresses

### 1. Standard Derived (non-ephemeral)
Generated as part of a unified address via `getNextAvailableAddress()`. These are your "normal" t-address receivers embedded in a unified address.

### 2. Ephemeral / Single-Use
Generated via `getSingleUseTransparentAddress()`. These are one-time-use t-addresses with a **gap limit** — you can only generate N of them without any receiving funds before the SDK refuses to create more.

---

## How Balance Checking Works

### During sync (all t-addresses)

In `CompactBlockProcessor`, every sync cycle does this:

1. Calls `backend.listTransparentReceivers(account)` — returns **all** derived t-addresses for the account
2. Calls `downloader.fetchUtxos(tAddresses, startHeight)` — queries the lightwalletd server for UTXOs belonging to those addresses
3. Each UTXO found is stored via `backend.putUtxo()` (upsert — spent UTXOs are NOT deleted, they stay in the DB like shielded notes)

Once a t-address is generated, it is checked every sync cycle because `listTransparentReceivers` returns all of them.

### Ephemeral address monitoring (via Tor)

Ephemeral addresses get additional periodic checking through Tor for privacy:

- The SDK picks the ephemeral address with the earliest `transparent_receiver_next_check_time`
- Fetches its UTXOs over Tor (so the server can't link addresses to the same wallet)
- If **no UTXOs found**: reschedules with exponential backoff — `offset = log2(blocks_since_exposure) * 75 seconds`
- If **UTXOs found**: stores them and updates the address state

---

## What This Looks Like in the `addresses` Table

For a wallet with a standard address and two ephemeral addresses:

| Field | Standard t-addr | Ephemeral #1 | Ephemeral #2 |
|-------|----------------|--------------|--------------|
| `account_id` | 1 | 1 | 1 |
| `key_scope` | 0 (external) | ephemeral scope | ephemeral scope |
| `transparent_child_index` | 0 | 0 | 1 |
| `cached_transparent_receiver_address` | `t1abc...` | `t1def...` | `t1ghi...` |
| `exposed_at_height` | NULL or height | 2500000 | 2500100 |
| `transparent_receiver_next_check_time` | NULL | 1709123456 | 1709125000 |
| `receiver_flags` | has transparent bit | has transparent bit | has transparent bit |
| `diversifier_index_be` | set | set | set |

### Key fields

- **`exposed_at_height`** — the block height when the address was first given out. Used to calculate how aggressively to re-check it.
- **`transparent_receiver_next_check_time`** — epoch timestamp for next Tor-based UTXO check. Exponential backoff means old unused addresses get checked less frequently.
- **`transparent_child_index`** — BIP44 derivation index (`m/44'/133'/account'/0/index`).

---

## Gap Limit

When you call `getSingleUseTransparentAddress()`:

- You get back `(address, gapPosition, gapLimit)`
- `gapPosition` increments each call (0, 1, 2, ...)
- If `gapPosition` reaches `gapLimit` without any of the addresses receiving funds, the SDK **refuses to generate more**
- Once one of the addresses receives a transaction, the gap resets

This prevents address exhaustion and matches how HD wallet gap limits work in Bitcoin.

---

## Database Schema Reference

### `accounts` table

| Column | Type | Description |
|--------|------|-------------|
| `id` | INTEGER PK | Auto-increment ID |
| `name` | TEXT | Human-readable account name |
| `uuid` | BLOB (UNIQUE) | Stable 16-byte identifier |
| `account_kind` | INTEGER | 0 = HD (from seed), 1 = imported (view key) |
| `key_source` | TEXT | Key source metadata |
| `hd_seed_fingerprint` | BLOB | BIP32 seed fingerprint (required for kind=0) |
| `hd_account_index` | INTEGER | ZIP 32 derivation index (required for kind=0) |
| `ufvk` | TEXT (UNIQUE) | Unified Full Viewing Key |
| `uivk` | TEXT (UNIQUE) | Unified Incoming Viewing Key |
| `orchard_fvk_item_cache` | BLOB | Cached Orchard FVK component |
| `sapling_fvk_item_cache` | BLOB | Cached Sapling FVK component |
| `p2pkh_fvk_item_cache` | BLOB | Cached transparent FVK component |
| `birthday_height` | INTEGER | Block height at account creation |
| `birthday_sapling_tree_size` | INTEGER | Sapling tree size at birthday |
| `birthday_orchard_tree_size` | INTEGER | Orchard tree size at birthday |
| `recover_until_height` | INTEGER | Scan recovery target height |
| `has_spend_key` | INTEGER | 1 = can spend, 0 = view-only |

### `addresses` table

| Column | Type | Description |
|--------|------|-------------|
| `id` | INTEGER PK | Primary key |
| `account_id` | INTEGER FK | References `accounts(id)` with CASCADE delete |
| `key_scope` | INTEGER | Key scope (-1 = foreign/imported) |
| `diversifier_index_be` | BLOB | Big-endian diversifier index |
| `address` | TEXT | The actual address string |
| `transparent_child_index` | INTEGER | BIP44 child derivation index |
| `cached_transparent_receiver_address` | TEXT | Cached t-address |
| `exposed_at_height` | INTEGER | Block height where address was first exposed |
| `receiver_flags` | INTEGER | Which receivers are present (transparent/sapling/orchard) |
| `transparent_receiver_next_check_time` | INTEGER | Next UTXO check time |
| `imported_transparent_receiver_pubkey` | BLOB | Pubkey for imported t-addresses |

---

## Summary

Once generated, a t-address is **always monitored** — standard addresses via the normal sync UTXO fetch against lightwalletd, and ephemeral addresses additionally via Tor with exponential backoff. The `addresses` table tracks the derivation path, cached address string, when it was exposed, and when to next check it.
