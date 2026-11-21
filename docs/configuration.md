# Configuration

All files are in `plugins/MegaOrbitals/`. After editing, run `/orbital reload`.
Every option in the files has a comment above it explaining what it does.

## config.yml

The main settings. The sections are:

| Section | What it controls |
|---|---|
| `general` | `max-range` (how far players can aim, 0 = unlimited), `blocks-per-tick` (how many block changes all strikes may make per tick; lower = less lag but slower big cannons), `debug`. |
| `worlds` | `mode: blacklist` (cannons work everywhere except `list`) or `whitelist` (only in `list`). |
| `safety` | `allow-block-damage` (master switch for every cannon's block changes), `damage-multiplier`, `hurt-shooter` (`default`, `always` or `never`), `respect-immunity`, `spawn-protection-radius`, `max-fires-per-minute`. |
| `cooldowns` | `item-seconds`, `command-seconds`, and `per-cannon` extra cooldowns, e.g. `meganuke: 300`. |
| `items` | `consume-on-use`, `glow`, `require-sneak`, `allow-off-hand`, and `usage-lore` (lines added under every item's description). |
| `cannons` | `disabled` (ids that can't be fired or given) and `per-cannon-permission`. |
| `announce` | Chat announcement when a cannon is fired: `enabled`, `radius`, `message`, `sound`. |
| `logging` | `log-fires` writes each fire to the console. |
| `language` | Which `lang/<language>.yml` to use. |
| `rollback` | Undo: `enabled`, `auto-restore-seconds`, `keep-minutes`, `max-blocks-per-strike`. See [Features → Undo](features.md#undo-and-rollback). |
| `crafting`, `drops-enabled`, `drops` | Orbital Core recipes and boss drops. See [Features → Tiers, crafting and drops](features.md#tiers-crafting-and-drops). |
| `spotter` | Binocular range and how long marks last. |
| `satellites` | Off by default. See [Features → Satellites](features.md#satellites). |
| `discord` | Webhook alerts for big cannons. |
| `update-checker` | Tells ops when a new release is out. |

Placeholders in messages: `{player}`, `{cannon}`, `{x}`, `{y}`, `{z}`, and for Discord also `{tier}` and `{world}`.
Colours use `&` codes (`&c` red, `&l` bold...).

## Per-cannon files: configs/&lt;id&gt;.yml

Every cannon has its own file. For example, `configs/meteor.yml` belongs to `/orbital meteor`.
Every file has these keys:

| Key | Meaning |
|---|---|
| `name` | Display name of the cannon item (`&` colour codes). |
| `lore` | Description lines on the item. |
| `item` | The item type, e.g. `FIRE_CHARGE`. |
| `tier` | `common`, `rare`, `epic` or `legendary`. Sets which Orbital Core can give it, and its satellite charge cost. |
| `category` | The menu tab it is listed under. A new name creates a new tab. |
| `auto-restore-seconds` | Put the terrain back this many seconds after the strike. `-1` = use `rollback.auto-restore-seconds`, `0` = never. |
| `warning-ticks` | How long the warning lasts before the strike lands (20 ticks = 1 second). |
| `warning-color` | Colour of the targeting ring, e.g. `'#FF2020'`. |
| `break-blocks` | Whether this cannon may break or change blocks. `safety.allow-block-damage: false` overrides it. |
| `hurt-shooter` | Whether the person who fired it can be hurt by it. `safety.hurt-shooter` can override it. |
| `settings` | The cannon's own options: radius, damage, duration, mob counts... Each one is commented. |

If you delete a key, it comes back with its default value next time the plugin loads.
Delete the whole file to reset a cannon.

### Arsenal cannons

The 1000 [arsenal cannons](features.md#the-arsenal) all share four settings:

| Setting | Meaning |
|---|---|
| `power` | Strength of every impact (damage, size, effect length). `1.0` = normal, `2.0` = double. |
| `radius` | Size of the pattern: the area radius, or the length of a line. |
| `count` | How many impacts, rings or steps the pattern has. |
| `interval-ticks` | Ticks between steps. |

## Messages: lang/en.yml

Every message the plugin sends is in `lang/en.yml`. To translate:

1. Copy it to, for example, `lang/de.yml` and translate the text (keep the `{placeholders}`).
2. Set `language: de` in `config.yml`.
3. Run `/orbital reload`.

Cannon names and descriptions are not in the language file. Change them with `name` and `lore` in each cannon's file.

## Upgrading

New settings are added to your files automatically when you update. Your existing values are kept.
Very old (1.0.x) `config.yml` files are moved to `config-old.yml` and replaced with a fresh one.
