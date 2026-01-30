# ArefyEconomy

A fork of [Ecotale](https://github.com/Tera-bytez/Ecotale) adapted for use with the [Premium Survival Setup for Hytale](https://builtbybit.com/resources/premium-survival-setup-hytale.90671/) on BuiltByBit.

## Why this fork?

This fork was created to provide better compatibility and integration with the custom plugins included in the Premium Survival Setup:

- **ArefyShop** - In-game shop system
- **ArefyAuctions** - Player auction house
- **ArefyCrates** - Crate/lootbox system
- **ArefyJobs** - Jobs and rewards system
- **ArefyCoins** - Physical coin items
- **ArefyRandomTeleport** - Random teleport with economy integration
- **ArefyAFK** - AFK rewards system

All these plugins use ArefyEconomy as their economy backend.

## For Plugin Developers

If you want to add compatibility with ArefyEconomy in your own plugin, you can use our API:

### Dependency

Add ArefyEconomy as a dependency in your `build.gradle`:

```gradle
dependencies {
    compileOnly(files("libs/ArefyEconomy.jar"))
}
```

### API Usage

```java
import com.arefyeconomy.api.ArefyEconomyAPI;

// Check if ArefyEconomy is available
if (ArefyEconomyAPI.isAvailable()) {

    // Get player balance
    double balance = ArefyEconomyAPI.getBalance(playerUuid);

    // Deposit money
    ArefyEconomyAPI.deposit(playerUuid, 100.0, "Reward from MyPlugin");

    // Withdraw money
    ArefyEconomyAPI.withdraw(playerUuid, 50.0, "Purchase from MyPlugin");

    // Transfer between players
    ArefyEconomyAPI.transfer(fromUuid, toUuid, 100.0, "Trade");

    // Check if player can afford
    boolean canAfford = ArefyEconomyAPI.has(playerUuid, 100.0);
}
```

### Events

You can listen to economy events:

```java
import com.arefyeconomy.api.events.ArefyEconomyEvents;
import com.arefyeconomy.api.events.BalanceChangeEvent;
import com.arefyeconomy.api.events.TransactionEvent;

// Listen to balance changes
ArefyEconomyEvents.onBalanceChange(event -> {
    UUID player = event.getPlayerUuid();
    double oldBalance = event.getOldBalance();
    double newBalance = event.getNewBalance();
    String reason = event.getReason();

    // Cancel the transaction if needed
    if (someCondition) {
        event.setCancelled(true);
    }
});

// Listen to player-to-player transactions
ArefyEconomyEvents.onTransaction(event -> {
    UUID from = event.getFromUuid();
    UUID to = event.getToUuid();
    double amount = event.getAmount();
});
```

### API Methods Reference

| Method | Description |
|--------|-------------|
| `isAvailable()` | Check if ArefyEconomy is loaded |
| `getBalance(UUID)` | Get player's current balance |
| `setBalance(UUID, double, String)` | Set player's balance |
| `deposit(UUID, double, String)` | Add money to player |
| `withdraw(UUID, double, String)` | Remove money from player |
| `transfer(UUID, UUID, double, String)` | Transfer between players |
| `has(UUID, double)` | Check if player has enough money |
| `format(double)` | Format amount with currency symbol |

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/balance` | View your balance | None (all players) |
| `/pay <player> <amount>` | Send money to player | None (all players) |
| `/eco` | Open admin panel | `arefyeconomy.admin` |
| `/eco give <player> <amount>` | Give money | `arefyeconomy.admin` |
| `/eco take <player> <amount>` | Take money | `arefyeconomy.admin` |
| `/eco set <player> <amount>` | Set balance | `arefyeconomy.admin` |

## Configuration

Located in `mods/ArefyEconomy/config.json`:

```json
{
  "currencySymbol": "$",
  "currencyName": "Coins",
  "startingBalance": 100.0,
  "maxBalance": 1000000000.0,
  "transferFee": 0.05,
  "decimalPlaces": 2,
  "enableHudDisplay": true,
  "language": "en-US",
  "storageProvider": "h2"
}
```

### Storage Providers

| Provider | Description |
|----------|-------------|
| `h2` | Embedded database (default, fastest) |
| `json` | JSON files (human-readable) |
| `mysql` | MySQL database (for multi-server setups) |

## Supported Languages

- English (en-US)
- Spanish (es-ES)
- Portuguese (pt-BR)
- French (fr-FR)
- German (de-DE)
- Russian (ru-RU)
- Japanese (ja-JP)
- Chinese (zh-CN)

## Building from Source

```bash
./gradlew shadowJar
```

Output: `build/libs/ArefyEconomy-x.x.x.jar`

**Note:** You need to place `HytaleServer.jar` in the `libs/` folder to compile.

## Links

- [Premium Survival Setup on BuiltByBit](https://builtbybit.com/resources/premium-survival-setup-hytale.90671/)
- [Discord Support](https://discord.nekiosetups.com)

---

## Credits

This plugin is a fork of **[Ecotale](https://github.com/Tera-bytez/Ecotale)** by **[Tera-bytez](https://github.com/Tera-bytez)**.

Original project: https://www.curseforge.com/hytale/mods/ecotale

Thank you to Tera-bytez for creating the original Ecotale economy plugin that made this fork possible.

## License

MIT License - See [LICENSE](LICENSE) for details.
