package lando.systems.ld60.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ecs.components.Position;
import lando.systems.ld60.utils.SimplePath;

public class ParticleData implements Pool.Poolable {

    public static final String TAG = ParticleData.class.getSimpleName();

    public static Initializer initializer(ParticleData particleData) {
        return new Initializer(particleData);
    }

    // --- Path & Motion ---
    public SimplePath path;
    public Interpolation interpolation;
    public Interpolation interpAlpha;
    public Interpolation interpSize;
    public Interpolation interpRotation;

    // --- Visuals ---
    public TextureRegion keyframe;
    public Animation<TextureRegion> animation;
    public float animTime;
    public boolean animUnlocked;

    // --- Drop Shadow ---
    public boolean hasDropShadow;
    public float shadowOffsetX;
    public float shadowOffsetY;
    public final Color shadowColor = new Color(0f, 0f, 0f, 0.5f);

    // --- Coordinates ---
    public float xStart;
    public float yStart;
    public float px;
    public float py;

    public boolean targeted;
    public float xTarget;
    public float yTarget;
    public Position targetPosition;

    public final Vector2 velocity;
    public float bounceScale;

    public final Vector2 accel;
    public float accDamp;

    // --- Dimensions & Multi-Stop Keyframing ---
    public float widthStart;
    public float widthEnd;
    public float width;

    public float heightStart;
    public float heightEnd;
    public float height;

    // Stores interleaved [w0, h0, w1, h1, ...] for multi-point sizing
    public final FloatArray sizeKeyframes = new FloatArray(8);

    // --- Rotation & Multi-Stop Keyframing ---
    public float rotationStart;
    public float rotationEnd;
    public float rotation;

    // Stores [r0, r1, r2, ...] for multi-point rotations
    public final FloatArray rotationKeyframes = new FloatArray(4);

    // --- Colors & Alpha ---
    public float rStart, gStart, bStart, aStart;
    public float rEnd, gEnd, bEnd, aEnd;
    public float r, g, b, a;

    // --- Lifecycle & Collision ---
    public boolean timed;
    public float ttlMax;
    public float ttl;

    public boolean dead;
    public boolean persistent;
    public final Circle collisionBounds;
    public final Rectangle collisionRect;

    public ParticleData() {
        velocity = new Vector2();
        accel = new Vector2();
        collisionBounds = new Circle();
        collisionRect = new Rectangle();
        targetPosition = null;
        reset();
    }

    public boolean isDead() {
        return dead;
    }

    /**
     * Samples size across arbitrary keyframes given normalized progress t (0.0 to 1.0).
     */
    public void evaluateSize(float t, Vector2 out) {
        int pairCount = sizeKeyframes.size / 2;
        if (pairCount == 0) {
            float alpha = (interpSize != null ? interpSize : interpolation).apply(t);
            out.x = MathUtils.lerp(widthStart, widthEnd, alpha);
            out.y = MathUtils.lerp(heightStart, heightEnd, alpha);
            return;
        }
        if (pairCount == 1) {
            out.x = sizeKeyframes.items[0];
            out.y = sizeKeyframes.items[1];
            return;
        }

        float progress = MathUtils.clamp(t, 0f, 1f) * (pairCount - 1);
        int index = Math.min((int) progress, pairCount - 2);
        float segT = progress - index;
        float alpha = (interpSize != null ? interpSize : interpolation).apply(segT);

        int baseA = index * 2;
        int baseB = (index + 1) * 2;
        out.x = MathUtils.lerp(sizeKeyframes.items[baseA], sizeKeyframes.items[baseB], alpha);
        out.y = MathUtils.lerp(sizeKeyframes.items[baseA + 1], sizeKeyframes.items[baseB + 1], alpha);
    }

    /**
     * Samples rotation across arbitrary keyframes given normalized progress t (0.0 to 1.0).
     */
    public float evaluateRotation(float t) {
        int count = rotationKeyframes.size;
        if (count == 0) {
            float alpha = (interpRotation != null ? interpRotation : interpolation).apply(t);
            return MathUtils.lerp(rotationStart, rotationEnd, alpha);
        }
        if (count == 1) return rotationKeyframes.items[0];

        float progress = MathUtils.clamp(t, 0f, 1f) * (count - 1);
        int index = Math.min((int) progress, count - 2);
        float segT = progress - index;
        float alpha = (interpRotation != null ? interpRotation : interpolation).apply(segT);

        return MathUtils.lerp(rotationKeyframes.items[index], rotationKeyframes.items[index + 1], alpha);
    }

    @Override
    public void reset() {
        path = null;
        interpolation = Interpolation.linear;
        interpAlpha = null;
        interpSize = null;
        interpRotation = null;

        keyframe = null;
        animation = null;
        animUnlocked = false;
        animTime = 0f;

        hasDropShadow = false;
        shadowOffsetX = 1f;
        shadowOffsetY = -1f;
        shadowColor.set(0f, 0f, 0f, 0.5f);

        xStart = 0f;
        yStart = 0f;
        px = 0f;
        py = 0f;

        targeted = false;
        xTarget = 0f;
        yTarget = 0f;
        targetPosition = null;

        velocity.set(0f, 0f);
        bounceScale = 0.8f;

        accel.set(0f, 0f);
        accDamp = 1f;

        widthStart = 0f;
        widthEnd = 0f;
        width = 0f;

        heightStart = 0f;
        heightEnd = 0f;
        height = 0f;
        sizeKeyframes.clear();

        rotationStart = 0f;
        rotationEnd = 0f;
        rotation = 0f;
        rotationKeyframes.clear();

        rStart = gStart = bStart = aStart = 1f;
        rEnd = gEnd = bEnd = aEnd = 1f;
        r = g = b = a = 1f;

        timed = false;
        ttlMax = 0f;
        ttl = 0f;

        dead = true;
        persistent = false;
        collisionBounds.set(0f, 0f, 0f);
        collisionRect.set(0f, 0f, 0f, 0f);
    }

    // ------------------------------------------------------------------------

    public static class Initializer {

        private final ParticleData particleData;

        private SimplePath path = null;
        private TextureRegion keyframe = null;
        private Animation<TextureRegion> animation = null;
        private Interpolation interpolation = Interpolation.linear;
        private Interpolation interpAlpha = null;
        private Interpolation interpSize = null;
        private Interpolation interpRotation = null;
        private boolean animUnlocked = false;

        private boolean hasDropShadow = false;
        private float shadowOffsetX = 1f;
        private float shadowOffsetY = -1f;
        private final Color shadowColor = new Color(0f, 0f, 0f, 0.5f);

        private float xStart = 0f;
        private float yStart = 0f;
        private float xJitter = 0f;
        private float yJitter = 0f;

        private boolean targeted = false;
        private float xTarget = 0f;
        private float yTarget = 0f;
        private Position targetPosition;

        private float xVel = 0f;
        private float yVel = 0f;
        private float bounceScale = 0.8f;

        private float xAcc = 0f;
        private float yAcc = 0f;
        private float accDamp = 1f;

        private float widthStart = 0f;
        private float widthEnd = 0f;
        private boolean setWidthEnd = false;

        private float heightStart = 0f;
        private float heightEnd = 0f;
        private boolean setHeightEnd = false;
        private final FloatArray sizeArray = new FloatArray();

        private float rotationStart = 0f;
        private float rotationEnd = 0f;
        private boolean setRotationEnd = false;
        private final FloatArray rotationArray = new FloatArray();

        private float rStart = 1f, gStart = 1f, bStart = 1f, aStart = 1f;
        private float rEnd = 1f, gEnd = 1f, bEnd = 1f, aEnd = 1f;
        private boolean setColorEnd = false;
        private boolean setAlphaEnd = false;

        private boolean persistent = false;
        private boolean timed = false;
        private float ttlMax = 0f;

        public Initializer(ParticleData particleData) {
            this.particleData = particleData;
            this.particleData.reset();
        }

        public Initializer interpolation(Interpolation interpolation) {
            this.interpolation = interpolation;
            return this;
        }

        public Initializer interpAlpha(Interpolation interp) {
            this.interpAlpha = interp;
            return this;
        }

        public Initializer interpSize(Interpolation interp) {
            this.interpSize = interp;
            return this;
        }

        public Initializer interpRotation(Interpolation interp) {
            this.interpRotation = interp;
            return this;
        }

        public Initializer dropShadow(boolean enable, float xOff, float yOff, Color color) {
            this.hasDropShadow = enable;
            this.shadowOffsetX = xOff;
            this.shadowOffsetY = yOff;
            if (color != null) this.shadowColor.set(color);
            return this;
        }

        public Initializer dropShadow(boolean enable) {
            this.hasDropShadow = enable;
            return this;
        }

        public Initializer path(SimplePath path) {
            this.path = path;
            return this;
        }

        public Initializer keyframe(TextureRegion keyframe) {
            this.keyframe = keyframe;
            return this;
        }

        public Initializer animation(Animation<TextureRegion> animation) {
            this.animation = animation;
            return this;
        }

        public Initializer animUnlocked(boolean animUnlocked) {
            this.animUnlocked = animUnlocked;
            return this;
        }

        public Initializer startPos(float x, float y) {
            this.xStart = x;
            this.yStart = y;
            return this;
        }

        public Initializer jitter(float x, float y) {
            this.xJitter = x;
            this.yJitter = y;
            return this;
        }

        public Initializer jitter(float radius) {
            this.xJitter = radius;
            this.yJitter = radius;
            return this;
        }

        public Initializer targetPos(float x, float y) {
            this.xTarget = x;
            this.yTarget = y;
            this.targeted = true;
            return this;
        }

        public Initializer targetPos(Position targetPosition) {
            this.targetPosition = targetPosition;
            this.xTarget = targetPosition.x;
            this.yTarget = targetPosition.y;
            this.targeted = true;
            return this;
        }

        public Initializer velocity(float x, float y) {
            this.xVel = x;
            this.yVel = y;
            return this;
        }

        public Initializer velocityDirection(float angle, float magnitude) {
            this.xVel = MathUtils.cosDeg(angle) * magnitude;
            this.yVel = MathUtils.sinDeg(angle) * magnitude;
            return this;
        }

        public Initializer bounce(float bounceScale) {
            this.bounceScale = bounceScale;
            return this;
        }

        public Initializer acceleration(float x, float y) {
            this.xAcc = x;
            this.yAcc = y;
            return this;
        }

        public Initializer accelerationDamping(float damp) {
            this.accDamp = damp;
            return this;
        }

        public Initializer startSize(float width, float height) {
            this.widthStart = width;
            this.heightStart = height;
            return this;
        }

        public Initializer startSize(float size) {
            return startSize(size, size);
        }

        public Initializer endSize(float width, float height) {
            this.widthEnd = width;
            this.heightEnd = height;
            this.setWidthEnd = true;
            this.setHeightEnd = true;
            return this;
        }

        public Initializer endSize(float size) {
            return endSize(size, size);
        }

        /**
         * Keyframed sizes throughout life. Pass flat values: [w0, h0, w1, h1, ...]
         */
        public Initializer keyframeSizes(float... sizes) {
            if (sizes.length % 2 != 0) {
                throw new GdxRuntimeException("keyframeSizes requires an even number of values [w0, h0, w1, h1, ...]");
            }
            this.sizeArray.clear();
            this.sizeArray.addAll(sizes);
            return this;
        }

        public Initializer startRotation(float rotation) {
            this.rotationStart = rotation;
            return this;
        }

        public Initializer endRotation(float rotation) {
            this.rotationEnd = rotation;
            this.setRotationEnd = true;
            return this;
        }

        public Initializer keyframeRotations(float... rotations) {
            this.rotationArray.clear();
            this.rotationArray.addAll(rotations);
            return this;
        }

        public Initializer startColor(float r, float g, float b, float a) {
            this.rStart = r;
            this.gStart = g;
            this.bStart = b;
            this.aStart = a;
            return this;
        }

        public Initializer startColor(Color color) {
            return startColor(color.r, color.g, color.b, color.a);
        }

        public Initializer endColor(float r, float g, float b, float a) {
            this.rEnd = r;
            this.gEnd = g;
            this.bEnd = b;
            this.aEnd = a;
            this.setColorEnd = true;
            return this;
        }

        public Initializer endColor(Color color) {
            return endColor(color.r, color.g, color.b, color.a);
        }

        public Initializer startAlpha(float a) {
            this.aStart = a;
            return this;
        }

        public Initializer endAlpha(float a) {
            this.aEnd = a;
            this.setAlphaEnd = true;
            return this;
        }

        public Initializer timeToLive(float ttl) {
            this.ttlMax = ttl;
            this.timed = true;
            return this;
        }

        public Initializer persist() {
            this.persistent = true;
            return this;
        }

        public ParticleData init() {
            if (keyframe != null) {
                particleData.keyframe = keyframe;
            }
            if (animation != null) {
                particleData.animation = animation;
                particleData.animTime = 0f;
                particleData.animUnlocked = animUnlocked;
            }
            if (path != null) {
                if (!timed) {
                    throw new GdxRuntimeException("Particles with a path must have a time to live (call timeToLive())");
                }
                particleData.path = path;
            }

            particleData.interpolation = interpolation;
            particleData.interpAlpha = interpAlpha;
            particleData.interpSize = interpSize;
            particleData.interpRotation = interpRotation;

            particleData.hasDropShadow = hasDropShadow;
            particleData.shadowOffsetX = shadowOffsetX;
            particleData.shadowOffsetY = shadowOffsetY;
            particleData.shadowColor.set(shadowColor);

            float px = xStart + (xJitter != 0f ? MathUtils.random(-xJitter, xJitter) : 0f);
            float py = yStart + (yJitter != 0f ? MathUtils.random(-yJitter, yJitter) : 0f);
            particleData.xStart = px;
            particleData.yStart = py;
            particleData.px = px;
            particleData.py = py;

            particleData.targeted = targeted;
            particleData.xTarget = xTarget;
            particleData.yTarget = yTarget;
            particleData.targetPosition = targetPosition;
            if (targeted && !timed) {
                throw new GdxRuntimeException("Particles with a target must have a time to live (call timeToLive())");
            }

            particleData.velocity.set(xVel, yVel);
            particleData.bounceScale = bounceScale;
            particleData.accel.set(xAcc, yAcc);
            particleData.accDamp = accDamp;

            // Size handling
            particleData.sizeKeyframes.clear();
            if (sizeArray.size > 0) {
                particleData.sizeKeyframes.addAll(sizeArray);
                particleData.widthStart = sizeArray.items[0];
                particleData.heightStart = sizeArray.items[1];
                particleData.widthEnd = sizeArray.items[sizeArray.size - 2];
                particleData.heightEnd = sizeArray.items[sizeArray.size - 1];
            } else {
                particleData.widthStart = widthStart;
                particleData.widthEnd = setWidthEnd ? widthEnd : widthStart;
                particleData.heightStart = heightStart;
                particleData.heightEnd = setHeightEnd ? heightEnd : heightStart;
            }
            particleData.width = particleData.widthStart;
            particleData.height = particleData.heightStart;

            if ((particleData.widthStart == 0f && particleData.widthEnd == 0f)
                || (particleData.heightStart == 0f && particleData.heightEnd == 0f)) {
                Log.warn(TAG, "Particle initialized with zero width/height, will be invisible.");
            }

            // Rotation handling
            particleData.rotationKeyframes.clear();
            if (rotationArray.size > 0) {
                particleData.rotationKeyframes.addAll(rotationArray);
                particleData.rotationStart = rotationArray.items[0];
                particleData.rotationEnd = rotationArray.items[rotationArray.size - 1];
            } else {
                particleData.rotationStart = rotationStart;
                particleData.rotationEnd = setRotationEnd ? rotationEnd : rotationStart;
            }
            particleData.rotation = particleData.rotationStart;

            // Color handling
            particleData.rStart = rStart;
            particleData.gStart = gStart;
            particleData.bStart = bStart;
            particleData.aStart = aStart;
            particleData.rEnd = setColorEnd ? rEnd : rStart;
            particleData.gEnd = setColorEnd ? gEnd : gStart;
            particleData.bEnd = setColorEnd ? bEnd : bStart;
            particleData.aEnd = (setColorEnd || setAlphaEnd) ? aEnd : aStart;
            particleData.r = rStart;
            particleData.g = gStart;
            particleData.b = bStart;
            particleData.a = aStart;

            if (particleData.aStart == 0f && particleData.aEnd == 0f) {
                Log.warn(TAG, "Particle initialized with zero alpha, will be invisible.");
            }

            particleData.timed = timed;
            particleData.ttlMax = ttlMax;
            particleData.ttl = ttlMax;

            particleData.persistent = persistent;
            particleData.dead = false;

            return particleData;
        }
    }
}
