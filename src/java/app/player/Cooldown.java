package app.player;

public class Cooldown {
    private int timeRemainingMillis = 0;
    private int cooldownDurationMillis;
    private final int defaultCooldownDurationMillis;

    public Cooldown(int defaultCooldownDurationMillis) {
        this.defaultCooldownDurationMillis = defaultCooldownDurationMillis;
    }

    public double getFractionRemaining() {
        return (double) timeRemainingMillis / cooldownDurationMillis;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isFinished() {
        return timeRemainingMillis == 0;
    }

    public void update(int elapsedMillis) {
        timeRemainingMillis = Math.max(timeRemainingMillis - elapsedMillis, 0);
    }

    public void reset() {
        reset(defaultCooldownDurationMillis);
    }

    public void reset(int duration) {
        timeRemainingMillis = duration;
        cooldownDurationMillis = duration;
    }
}
