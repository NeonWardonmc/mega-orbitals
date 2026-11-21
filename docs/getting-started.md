# Getting started

## Install

1. You need a **Paper 26.1.2** server running **Java 25**.
2. Download `MegaOrbitals-<version>.jar` from the [releases page](https://github.com/NeonWardonmc/mega-orbitals/releases).
3. Put it in your server's `plugins/` folder and start (or restart) the server.
4. The plugin creates `plugins/MegaOrbitals/` with:
   - `config.yml`, the main settings ([Configuration](configuration.md))
   - `configs/`, one settings file per cannon
   - `lang/en.yml`, every message

## Fire your first cannon

As an operator, look at a block and run:

```
/orbital classic
```

A targeting ring and beam appear for a couple of seconds, then the strike lands.
Replace `classic` with any cannon id. `/orbital list` and `/orbital search <word>` help you find one,
and [CANNONS.md](../CANNONS.md) lists them all.

If you didn't mean to do that, `/orbital undo` puts the blocks back.

## Browse the cannons

```
/orbital menu
```

This opens a chest menu with every cannon. The bottom row has:

- arrows for the previous and next page
- a **category** button (explosives, mobs, terrain...)
- a **tier** filter (common, rare, epic, legendary)
- a **search** box
- a counter showing how many cannons match

Click a cannon to take its item (shift-click for 16). This needs `megaorbitals.give`.

## Cannon items

Anyone holding a cannon item can **right-click** to call the strike on the block they are looking at.
By default the item is used up. Set `items.consume-on-use: false` to make items reusable.

Give items with:

```
/orbitalgive <cannon> [player] [amount]
```

Normal players can fire items (`megaorbitals.use` is on for everyone) but can't use `/orbital` or `/orbitalgive`.
See [Commands and permissions](commands.md).

## Make it survival-friendly

Out of the box, only operators can get cannons. For a survival server you might:

- **Let players craft cannons.** Orbital Cores are craftable, and right-clicking a core gives a random cannon of its tier.
  Bosses drop cores too. See [Features → Tiers, crafting and drops](features.md#tiers-crafting-and-drops).
- **Add cooldowns.** Use `cooldowns.item-seconds`, plus `cooldowns.per-cannon` for the big ones.
- **Turn on satellites.** Players then need a charged satellite over the target to fire.
  See [Features → Satellites](features.md#satellites).
- **Disable the cannons you don't want.** List them in `cannons.disabled`.
- **Protect builds.** Set `safety.allow-block-damage: false` (no cannon changes blocks) or `rollback.auto-restore-seconds`
  (terrain repairs itself after a strike).
