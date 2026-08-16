# Royale Gaming

A polished Android fantasy + arena gaming app (Compose + Room + real wallet flows).

## Build & Run

**Prerequisites:** Android Studio (recommended) or JDK 17 + Android SDK.

### With Android Studio (easiest)
1. Open Android Studio → Open → select this folder (`royale-gaming`).
2. Let it sync / download Gradle wrapper + dependencies (first time may take a few minutes).
3. (Optional) The project seeds a demo wallet on first launch via Room.
4. Run on an emulator or device (API 24+).

### From CLI (after wrapper is present)
```powershell
# Windows
.\gradlew.bat assembleDebug

# Or install + run on connected device/emulator
.\gradlew.bat installDebug
```

The Gradle wrapper scripts + jar are included (or will be auto-generated on first `./gradlew` if missing).

### Notes
- All money is simulated. Wallet uses local Room DB (balances persist across runs until app data cleared).
- Fantasy cricket: full team builder (11 players + C/VC + credits). Joining a paid contest now **deducts** from your deposit/bonus balance.
- KYC submit now progresses: NOT_SUBMITTED → PENDING (brief) → VERIFIED. Withdrawals are still open in demo (add your own gate for prod).
- Crash game (Aviator-style) in Live Arena: real multiplier simulation + cash-out that credits **winnings** balance.
- Share referral uses the system share sheet.
- Settings toggles are live in-memory.

## Key Improvements Made
- Fixed critical enum typo (WINNING_PAYOUT) and broken payout crediting.
- Fantasy join now actually costs money and marks contests as JOINED.
- KYC and Settings are now functional (live state).
- Referral share works.
- Currency normalized to ₹ for consistency with Indian payment context (UPI, PAN, Aadhaar).
- Gradle wrapper bootstrapped for reproducible CLI builds.

## Wallet Demo Seed (first run)
- Deposit: ₹250
- Winnings: ₹120
- Bonus: ₹15

Have fun battling in the arena!
