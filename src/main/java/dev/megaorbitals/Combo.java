package dev.megaorbitals;

/**
 * Base of the 1000 arsenal cannons added in 1.5.0. Each one pairs an {@link Arsenal.Payload}
 * (what happens at every impact) with an {@link Arsenal.Pattern} (where and when the impacts land).
 * The cannon classes themselves live in dev.megaorbitals.cannons,
 * one tiny class per combination, so each gets its own id, config file and menu entry.
 */
public abstract class Combo extends Cannon {

    private final Arsenal.Payload payload;
    private final Arsenal.Pattern pattern;

    protected Combo(Arsenal.Payload payload, Arsenal.Pattern pattern) {
        super(payload.colour + "&l" + payload.word + " " + pattern.word, payload.icon,
                payload.lore + ",", pattern.lore + ".");
        this.payload = payload;
        this.pattern = pattern;
        warning(pattern.warning, payload.hex);
        if (!payload.breaks) noBlockDamage();
        option("power", 1.0, "Strength of every impact (damage, size, effect length...). 1 = normal.");
        option("radius", pattern.radius, "Size of the pattern (area radius or line length) in blocks.");
        option("count", pattern.count, "How many impacts / rings / steps the pattern has.");
        option("interval-ticks", pattern.interval, "Ticks between steps of the pattern (20 ticks = 1 second).");
        int score = payload.weight + pattern.weight;
        String tier = score <= 1 ? "common" : score <= 3 ? "rare" : score == 4 ? "epic" : "legendary";
        Catalog.define(id(), tier, payload.category);
    }

    public Arsenal.Payload payload() { return payload; }
    public Arsenal.Pattern pattern() { return pattern; }

    @Override
    protected double warningRadius() {
        return Math.min(40, Math.max(4, d("radius")));
    }

    @Override
    public void strike(Strike s) {
        double power = Math.max(0.1, d("power"));
        pattern.runner.run(s, this, (l, scale) -> payload.hit.hit(s, l, power * scale));
    }
}
