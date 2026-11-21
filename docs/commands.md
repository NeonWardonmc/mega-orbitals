# Commands and permissions

## /orbital

Needs `megaorbitals.orbital` (operators by default).

| Command | What it does |
|---|---|
| `/orbital <cannon>` | Fires a cannon at the block you are looking at (or your [spotter mark](features.md#spotter-binoculars)). Players only. |
| `/orbital menu` | Opens the cannon menu. Click a cannon to take its item (shift-click for 16; needs `megaorbitals.give`). |
| `/orbital list [page]` | Lists cannon ids, 40 per page. |
| `/orbital search <word>` | Finds cannons whose id, name or description contains the word. |
| `/orbital info <cannon>` | Shows a cannon's description, warning time, whether it breaks blocks, and its settings file. |
| `/orbital undo` | Puts back every block your last strike changed. With `megaorbitals.undo.others`, undoes the last strike by anyone. |
| `/orbital undo <player>` | Undoes that player's last strike. |
| `/orbital undo all` | Undoes every strike that is still remembered. |
| `/orbital history` | Lists the last 10 strikes that can still be undone. |
| `/orbital satellites` | Lists your satellites and their charge. |
| `/orbital reload` | Reloads `config.yml`, every cannon file, the language file and the crafting recipes. |

The undo and history commands also need `megaorbitals.undo`.

Firing with `/orbital` follows the same rules as items (worlds, range, spawn protection, rate limit), with these differences:
- It uses `cooldowns.command-seconds` instead of the item cooldown.
- It never needs a satellite.

## /orbitalgive

Needs `megaorbitals.give` (operators by default).

| Command | What it gives |
|---|---|
| `/orbitalgive <cannon> [player] [amount]` | A cannon item. Amount is 1 to 64. |
| `/orbitalgive all [player]` | One of every cannon that isn't disabled. With 1322 cannons, most will drop on the ground. |
| `/orbitalgive binoculars [player] [amount]` | [Spotter binoculars](features.md#spotter-binoculars). |
| `/orbitalgive satellite-item [player] [amount]` | A [satellite](features.md#satellites) to launch. |
| `/orbitalgive core:<tier> [player] [amount]` | An Orbital Core: `core:common`, `core:rare`, `core:epic` or `core:legendary`. |

With no player name, the item goes to you.

## Permissions

| Permission | Default | Meaning |
|---|---|---|
| `megaorbitals.orbital` | op | Use `/orbital`. |
| `megaorbitals.give` | op | Use `/orbitalgive` and take items from the menu. |
| `megaorbitals.use` | everyone | Fire cannon items by right-clicking them. |
| `megaorbitals.cannon.<id>` | none | Needed to fire that cannon's item, but only when `cannons.per-cannon-permission` is `true`. |
| `megaorbitals.undo` | op | Use `/orbital undo` and `/orbital history`. |
| `megaorbitals.undo.others` | op | `/orbital undo` with no name undoes anyone's last strike, not just your own. |
| `megaorbitals.immune` | nobody | Never hurt or affected by cannons (when `safety.respect-immunity` is on). |
| `megaorbitals.bypass.cooldown` | op | Ignore all cooldowns. |
| `megaorbitals.bypass.satellite` | op | Fire cannon items without a satellite when satellites are on. |
| `megaorbitals.update` | op | Get told in-game when a new version is released. |

## Examples

Give every player a reusable cannon kit with LuckPerms:

```
/lp group default permission set megaorbitals.use true
/orbitalgive meteor Steve 1
```

Only allow specific cannons from items:

```yaml
# config.yml
cannons:
  per-cannon-permission: true
```

```
/lp group default permission set megaorbitals.cannon.classic true
/lp group vip permission set megaorbitals.cannon.meteor true
```
