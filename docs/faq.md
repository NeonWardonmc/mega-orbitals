# FAQ and troubleshooting

### A cannon doesn't break any blocks.

Check, in order:
1. `safety.allow-block-damage` in `config.yml` must be `true`. It switches block damage off for every cannon.
2. `break-blocks` in that cannon's `configs/<id>.yml` must be `true`. Some cannons default to `false` because they only affect players.
3. Cannons that build things (Space Station, Parkour Tower...) tell the shooter when block changes are off, and don't fire.

### "No target" when I fire.

You are looking at the sky, or further than `general.max-range`. Look at a block. With spotter binoculars, mark one first.

### Nothing happens when a player right-clicks a cannon item.

The action bar usually says why. Common causes:
- **No permission:** they need `megaorbitals.use`, plus `megaorbitals.cannon.<id>` if `per-cannon-permission` is on.
- **Sneaking:** `items.require-sneak` is on, so they must sneak.
- **Off hand:** `items.allow-off-hand` is off and the item is in their off hand.
- **Cooldown:** a cooldown is still running.
- **World:** the world is blocked by `worlds`.
- **Spawn:** the target is inside `safety.spawn-protection-radius`.
- **Satellites:** they are on and the player has no charged satellite over the target.
- **Disabled:** the cannon is listed in `cannons.disabled`.
- **Cancelled by another plugin:** an addon cancelled the `OrbitalFireEvent`.

### How do I stop players griefing with cannons?

- **Keep cannons from players:** don't give `megaorbitals.give` or `megaorbitals.orbital` to normal players, so they can only use items they are given or craft.
- **Disable the crafting:** turn off crafting and drops, so items only come from staff.
- **Repair terrain automatically:** set `rollback.auto-restore-seconds`, or `break-blocks: false` on the worst cannons.
- **Fix damage afterwards:** `/orbital undo <player>` reverts a player's last strike.
- **Region protection:** MegaOrbitals doesn't check WorldGuard or other protection plugins by itself.
  A small [addon](addons.md#orbitalfireevent) can cancel `OrbitalFireEvent` when the target is in a protected region.

### Undo didn't put everything back.

Undo restores blocks the strike changed. It doesn't cover:
- fire that spread on its own
- water and lava that flowed afterwards
- items inside chests that were destroyed

Strikes older than `rollback.keep-minutes` are forgotten. Very large strikes stop recording at `rollback.max-blocks-per-strike`.

### The server lags when big cannons fire.

- **Block work:** lower `general.blocks-per-tick`. Big cannons take longer to finish but spread the work out.
- **Rate limit:** set `safety.max-fires-per-minute`.
- **Cooldowns:** add `cooldowns.per-cannon` entries for the heavy hitters, e.g. `meganuke`, `omega`, `upsidedown` or `kaiju`.
- **Omega:** set `max-cannons` and keep `include-arsenal: false`.
- **Disable:** list the worst offenders in `cannons.disabled`.

### The menu and /orbital list are huge.

There are 1322 cannons. In the menu, use the category and tier filters or the search box. In chat, use `/orbital search <word>`.
To hide the 1000 arsenal cannons, add their ids to `cannons.disabled`. Disabled cannons don't show in the menu.

### I changed a cannon's file and nothing happened.

Run `/orbital reload`. If a value is invalid (for example an unknown item name), the default is used instead.
Delete the file to reset that cannon.

### Can I rename cannons or translate the plugin?

Yes. Change `name` and `lore` in each cannon's file, and translate `lang/en.yml` (see [Configuration](configuration.md#messages-langenyml)).

### Can I add my own cannons?

Yes, with an addon plugin. See [Making an addon](addons.md).

### Where do I report a bug or suggest a cannon?

On [GitHub issues](https://github.com/NeonWardonmc/mega-orbitals/issues/new/choose). Include your MegaOrbitals and Paper versions and any console errors.
