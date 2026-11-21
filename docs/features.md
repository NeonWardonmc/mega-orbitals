# Features

## Undo and rollback

Every block a strike breaks or changes is remembered, so it can be put back.

| Command | What it does |
|---|---|
| `/orbital undo` | Undo your own last strike. With `megaorbitals.undo.others`, it undoes the last strike by anyone. |
| `/orbital undo <player>` | Undo that player's last strike. |
| `/orbital undo all` | Undo every remembered strike. |
| `/orbital history` | List strikes that can still be undone. |

Settings (`rollback:` in `config.yml`):

- `auto-restore-seconds`: repair the terrain automatically this long after every strike (0 = never).
  A cannon's own `auto-restore-seconds` overrides it.
- `keep-minutes`: strikes older than this are forgotten and can't be undone.
- `max-blocks-per-strike`: a safety cap. The mega nuke can change millions of blocks; anything past the cap can't be undone.

When Omega or Roulette fire other cannons, one undo reverts all of them.

**Not covered:** fire spreading and water or lava flowing on their own after the strike, and the contents of chests that get destroyed.

## Tiers, crafting and drops

Every cannon has a **tier**: common, rare, epic or legendary. It shows on the item, sets which Orbital Core can give the cannon,
and sets how much satellite charge it costs.

**Orbital Cores** come in the same four tiers.

- Craft them (recipes in `crafting.recipes`). The common core needs iron and redstone.
  Each higher core is crafted from the one below plus rarer materials.
- Or get them as drops from mobs killed by players (`drops:`). By default:
  - the Ender Dragon always drops a legendary core
  - the Wither, Warden, Elder Guardian, Evoker and Ravager have good chances
  - any monster has a tiny chance of a common core

  Mobs spawned by cannons never drop cores.
- **Right-click a core** to turn it into a random cannon of that tier.

Turn crafting off with `crafting.enabled: false`, and drops with `drops-enabled: false`.

## Spotter binoculars

Craft them (spyglass, redstone and compass) or get them with `/orbitalgive binoculars`.

- **Left-click** marks the block you are looking at, up to `spotter.range` blocks away.
  You see the mark as a red beam and ring; nobody else does.
- While the mark is active (`spotter.mark-seconds`), every cannon you fire lands on the mark instead of where you are looking.
  That lets you fire from behind cover.
- **Sneak + left-click** clears the mark. Right-click still zooms like a normal spyglass.

## Satellites

Off by default (`satellites.enabled: false`). When on, firing a cannon **item** needs one of your satellites over the target
with enough charge. Admin `/orbital` commands and players with `megaorbitals.bypass.satellite` skip this.

- Craft a satellite (glass, daylight detectors, iron blocks and a beacon) or use `/orbitalgive satellite-item`.
  Right-click it to launch it from where you stand.
- Each satellite covers `radius` blocks around its launch point, holds up to `max-charge`, starts with `start-charge`,
  and refills `recharge-per-minute`.
- Firing costs charge by tier (`cost:`). By default common is 10, rare 25, epic 50 and legendary 100.
- A player can own up to `max-per-player` satellites. With `shared: true`, anyone can use anyone's satellites.
- `/orbital satellites` lists yours.

## The arsenal

1000 of the cannons are "arsenal" cannons. Each one combines a **payload** (what happens at every impact) with a
**pattern** (where and when the impacts land). The id is the two names joined: Frost + Spiral = `/orbital frostspiral`.

- **40 payloads**: explosions, fire, lightning, frost, poison, wither, gravity wells, shockwaves, levitation, launch, blindness,
  nausea, cobwebs, anvils, arrows, evoker fangs, sonic booms, electric shocks, fireworks, healing, speed, protection,
  bouncy slime, water, magma, crystal shards, sand, boulders, XP, gold, flowers, wind, zombies, creepers, bees, vexes,
  shulker bullets, dragon's breath, teleporting and TNT.
- **25 patterns**: one big Strike, Shower, Ring, Nova (rings spreading out), Implosion (rings closing in), Lance (a line where you aim),
  Cross, Spiral, Grid, Barrage, Chain (jumps between targets), Seeker (hits everyone in range in turn), Carpet,
  Star, Orbit, Pillar, Cluster, Mines (hidden until someone walks by), Pulse, Zigzag, Comet, Halo, Fan, Volley and Delta.

Their tier comes from how strong the payload and pattern are, and they are listed under the payload's menu tab.
Each has `power`, `radius`, `count` and `interval-ticks` settings. The full list is in [CANNONS.md](../CANNONS.md#the-arsenal).

## Omega and Roulette

- **Omega** (`/orbital omega`) fires every other cannon at the same spot, one per tick.
  By default it skips the 1000 arsenal cannons (`include-arsenal: false` in `configs/omega.yml`), so it fires about 320.
  Use `max-cannons` to pick fewer at random, and `exclude` to skip specific ones.
- **Roulette** (`/orbital roulette`) fires one random cannon (or more, with `spins`).

## Orbital Defence Grid

The `defencegrid` cannon protects an area. Strikes that other players fire into it are shot down in the sky before they land.
It lasts 10 minutes and stops 5 strikes by default. Your own strikes still get through unless `block-own-strikes` is on.

## Discord alerts

Post a message to a Discord channel when big cannons are fired. Create a webhook in your Discord channel settings, then:

```yaml
discord:
  enabled: true
  webhook-url: "https://discord.com/api/webhooks/..."
  tiers: [legendary]          # alert for every cannon of these tiers
  cannons: [omega, meganuke]  # and for these cannons
```

## Announcements and logging

- `announce.enabled: true` sends a chat message (and optional sound) whenever a cannon is fired, to the whole server or within `announce.radius`.
- `logging.log-fires` writes who fired what, and where, to the console.

## Lag protection

- Big block changes are spread over several ticks. `general.blocks-per-tick` sets the budget for all strikes together.
- `safety.max-fires-per-minute` limits how many cannons the whole server can fire per minute.
- Cooldowns: `cooldowns.item-seconds`, `cooldowns.command-seconds` and `cooldowns.per-cannon`.

## Update checker

Ops with `megaorbitals.update` are told in-game when a newer release is on GitHub. It checks every `update-checker.every-hours`.
