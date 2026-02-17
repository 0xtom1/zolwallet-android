# CLAUDE.md - Project Guidelines for AI Assistants

## Critical Rules

1. **Audited Codebase**: The existing Zcash/Zashi code has been audited and thoroughly reviewed. Minimize changes to existing files. When adding new functionality (e.g., Solana support), prefer creating new files over modifying audited code.

2. **Working Directory**: The Android Studio project is at `C:\Users\{user}\StudioProjects\zolwallet-android` 
   (NOT the Documents copy).

3. **Git Remote**: Uses SSH — `git@github.com:0xtom1/zolwallet-android.git`

4. **Branch**: Development happens on the `dev` branch.

## Project Info

- **App Name**: Zol Wallet
- **Package Name**: `xyz.zolapp.wallet`
- **Support Email**: `0xtomv@gmail.com`
- **Domain**: zolapp.xyz
- **Fork of**: Zashi Zcash Wallet (Electric Coin Company)

## Architecture

- **Pattern**: Provider → DataSource → Repository → UseCase → ViewModel
- **DI**: Koin (modules in `ui-lib/src/main/java/co/electriccoin/zcash/di/`)
- **UI**: Jetpack Compose
- **Navigation**: Type-safe with Kotlin serialization
- **Storage**: EncryptedSharedPreferences (AES256-GCM via Android Keystore)
- **Reactive**: Kotlin Flow / StateFlow throughout
