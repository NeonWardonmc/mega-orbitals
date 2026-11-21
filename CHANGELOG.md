# Changelog

Every MegaOrbitals release. The newest is at the top.
When a version tag is pushed, GitHub Actions copies that version's section below onto its release page.

## [Unreleased]

- Added `CANNONS.md`, the full list of cannons. It is rewritten on every build.
- Added this changelog, a contributing guide, `CLAUDE.md` project notes and GitHub issue templates.

## [1.5.0] - 2026-11-21

**1000 arsenal cannons (1322 in total).**

- Added the arsenal: 40 payloads (Blast, Inferno, Thunder, Frost, Gravity, Undead, Mending, Dragonfire...)
  combined with 25 patterns (Strike, Shower, Ring, Nova, Spiral, Chain, Mines, Comet, Volley...).
  This gives 1000 new cannons, such as Frost Spiral and Undead Volley.
- Every arsenal cannon has its own id, config file and menu entry, with the settings `power`, `radius`,
  `count` and `interval-ticks`.
- Arsenal tiers and menu tabs are worked out automatically from the payload and pattern.
- Omega skips arsenal cannons unless its new `include-arsenal` setting is `true`.
- New tool for developers: `tools/generate_arsenal.py` rebuilds the arsenal cannon classes from `Arsenal.java`.

## [1.4.0] - 2026-11-21

**20 new cannons (322 in total).**

- Traps: Mimic Chests, Booby-Trapped Ores.
- Minigames: Bomb Defusal (cut the right wire), Parkour Tower, Wave Survival, Capture the Flag, Mirror Match.
- Building: Space Station, Orbital Elevator, Upside-Down (flips the terrain), Structure Copier, Asteroid Mine.
- Combat and movement: Projectile Deflector, Echo Strike, Linked Portals, Mech Pilot, Kaiju.
- Other: Loot Pinata, Bounty Hunter.
- New: the **Orbital Defence Grid** shoots down other players' strikes before they land in its area.
  Cannons can now cancel incoming strikes through the new `StrikeInterceptor` hook.

## [1.3.0] - 2026-11-21

**Undo, crafting tiers, spotter, satellites and more.**

- Undo and rollback: every block a strike changes is recorded. Added `/orbital undo`, `/orbital history`
  and optional auto-restore. When Omega or Roulette fire other cannons, one undo reverts the lot.
- Every cannon has a tier (common, rare, epic, legendary) and a menu category.
- Craftable Orbital Cores for each tier, plus boss drops. Right-click a core to get a random cannon of that tier.
- Spotter binoculars: mark a target, then fire at it from cover.
- Satellites (off by default) that recharge over time. With them on, firing a cannon item needs a charged satellite.
- The menu gained category tabs, a tier filter and a chat search.
- Discord webhook alerts, `lang/en.yml` message files and an in-game update checker.
- GitHub Actions builds every push and publishes a release when a `v*` tag is pushed.

## [1.2.0] - 2026-11-21

**302 orbital strike cannons for Paper 26.1.2.** This is the first version in this repository.

- Each cannon is its own class with a generated `configs/<cannon>.yml`.
- `config.yml` rules for worlds, safety, cooldowns, items and messages.
- `/orbital menu`, `list`, `search`, `info` and `reload`.
- Omega, the cannon that fires every other cannon.

[Unreleased]: https://github.com/NeonWardonmc/mega-orbitals/compare/v1.5.0...HEAD
[1.5.0]: https://github.com/NeonWardonmc/mega-orbitals/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/NeonWardonmc/mega-orbitals/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/NeonWardonmc/mega-orbitals/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/NeonWardonmc/mega-orbitals/releases/tag/v1.2.0
