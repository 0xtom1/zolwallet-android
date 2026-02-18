# Audited / Security-Critical Files

These files are inherited from the Zashi (Electric Coin Company) codebase and are considered
audited and battle-tested. **Do not modify them without a clear, specific reason.** When adding
new functionality, create new files rather than changing these.

---

## Encrypted Storage (Android Keystore / AES256-GCM)

```
preference-impl-android-lib/src/main/java/co/electriccoin/zcash/preference/AndroidPreferenceProvider.kt
preference-impl-android-lib/src/main/java/co/electriccoin/zcash/preference/EncryptedPreferenceProvider.kt
preference-impl-android-lib/src/main/java/co/electriccoin/zcash/preference/StandardPreferenceProvider.kt
preference-impl-android-lib/src/main/java/co/electriccoin/zcash/preference/PreferenceHolder.kt
```

## Seed Phrase / Wallet Init & Restore

```
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/provider/PersistableWalletProvider.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/repository/WalletRepository.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/usecase/ValidateSeedUseCase.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/usecase/RestoreWalletUseCase.kt
```

## Zcash SDK Synchronizer

```
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/provider/SynchronizerProvider.kt
```

## Transaction Proposals — Native (Zashi)

```
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/repository/ZashiProposalRepository.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/datasource/ProposalDataSource.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/datasource/ZashiSpendingKeyDataSource.kt
```

## Transaction Proposals — Hardware Wallet (Keystone / PCZT)

```
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/repository/KeystoneProposalRepository.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/model/KeystoneAccount.kt
```

## Shielding (Transparent → Shielded)

```
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/usecase/ShieldFundsUseCase.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/usecase/ShieldFundsFromMessageUseCase.kt
```

## Transaction Submission & Biometrics

```
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/usecase/SubmitProposalUseCase.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/repository/BiometricRepository.kt
```

## Address Derivation & Account Management

```
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/model/WalletAccount.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/model/ZashiAccount.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/datasource/AccountDataSource.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/usecase/DeriveKeystoneAccountUnifiedAddressUseCase.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/usecase/GetSelectedWalletAccountUseCase.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/usecase/GetWalletAccountsUseCase.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/usecase/CreateKeystoneAccountUseCase.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/provider/SelectedAccountUUIDProvider.kt
```

## Wallet Backup Integrity

```
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/provider/WalletBackupFlagStorageProvider.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/provider/WalletBackupConsentStorageProvider.kt
```

## Encrypted Metadata & Address Book

```
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/repository/MetadataRepository.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/datasource/MetadataDataSource.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/provider/MetadataStorageProvider.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/provider/MetadataKeyStorageProvider.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/repository/AddressBookRepository.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/datasource/AddressBookDataSource.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/provider/AddressBookStorageProvider.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/provider/AddressBookKeyStorageProvider.kt
```

## Network / RPC & Tor

```
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/provider/LightWalletEndpointProvider.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/usecase/PersistEndpointUseCase.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/usecase/GetSelectedEndpointUseCase.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/provider/PersistableWalletTorProvider.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/provider/IsTorEnabledStorageProvider.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/usecase/IsTorEnabledUseCase.kt
```

## Screen Security

```
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/compose/ScreenSecurity.kt
ui-lib/src/main/java/co/electriccoin/zcash/ui/common/compose/ScreenTimeout.kt
```

## DI Wiring (ties all the above together)

```
ui-lib/src/main/java/co/electriccoin/zcash/di/ProviderModule.kt
ui-lib/src/main/java/co/electriccoin/zcash/di/RepositoryModule.kt
ui-lib/src/main/java/co/electriccoin/zcash/di/UseCaseModule.kt
ui-lib/src/main/java/co/electriccoin/zcash/di/CoreModule.kt
ui-lib/src/main/java/co/electriccoin/zcash/di/ViewModelModule.kt
```

## SDK Extensions

```
sdk-ext-lib/src/main/java/cash/z/ecc/sdk/type/ZcashNetwork.kt
sdk-ext-lib/src/main/java/cash/z/ecc/sdk/Constants.kt
```
