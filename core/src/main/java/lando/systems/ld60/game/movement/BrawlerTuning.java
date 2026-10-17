package lando.systems.ld60.game.movement;

/**
 * Tuning for a {@link BrawlerMotion}, in designer units ({@link #unit} pixels, typically one tile)
 * and seconds. Like the library's {@code TopDownTuning} and {@code PlatformerTuning}, derived values
 * are computed on demand so a game swaps the whole record on {@link BrawlerMotion#tuning} to change
 * how a character moves. Jump physics follow Pittman's "Building a Better Jump": apex height and
 * time to apex fix the gravity and the take-off speed as a pair.
 *
 * @param unit           pixels per tuning unit, typically the tile size
 * @param speedX         max walking speed left / right, units per second
 * @param speedY         max walking speed up / down the floor, units per second (classic brawlers: about half of speedX)
 * @param accelTime      seconds from rest to top speed, both axes
 * @param stopTime       seconds from top speed to rest with no input, both axes
 * @param jumpHeight     apex of a jump above the floor, units
 * @param jumpTimeToPeak seconds from take-off to the apex
 */
public record BrawlerTuning(
        float unit,
        float speedX,
        float speedY,
        float accelTime,
        float stopTime,
        float jumpHeight,
        float jumpTimeToPeak
) {
    /** A brawler, {@code unit} pixels per tile: six tiles a second across, three up the floor, a two and a half tile jump. */
    public static BrawlerTuning brawler(float unit) {
        return new BrawlerTuning(unit, 6, 3, /*accelTime*/ 0.08f, /*stopTime*/ 0.08f, 2.5f, 0.35f);
    }

    public float maxSpeedX() { return unit * speedX; }
    public float maxSpeedY() { return unit * speedY; }
    public float accelX()    { return maxSpeedX() * accelTime; }
    public float accelY()    { return maxSpeedY() * accelTime; }
    public float frictionX() { return maxSpeedX() / stopTime; }
    public float frictionY() { return maxSpeedY() / stopTime; }

    /** Gravity on the height axis in px/s^2, negative. Derived so a jump peaks after {@link #jumpTimeToPeak} seconds. */
    public float gravity() { return -2f * jumpHeight * unit / (jumpTimeToPeak * jumpTimeToPeak); }

    /** Take-off speed in px/s for a jump of {@link #jumpHeight}. Paired with {@link #gravity()}. */
    public float jumpImpulse() { return 2f * jumpHeight * unit / jumpTimeToPeak; }

    /** Take-off speed in px/s to peak {@code heightUnits} above the floor under {@link #gravity()}. */
    public float impulseForHeight(float heightUnits) {
        return (float) Math.sqrt(2f * -gravity() * heightUnits * unit);
    }

    /** Seconds in the air for a hop that peaks {@code heightUnits} above the floor. */
    public float hangTime(float heightUnits) {
        return 2f * impulseForHeight(heightUnits) / -gravity();
    }
}
