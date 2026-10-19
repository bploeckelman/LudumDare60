#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoord;

uniform float u_healthPct; // Target / current actual health
uniform float u_damagePct; // Catch-up trailing health (orange)
uniform vec2 u_size;       // Width and Height in pixels (passed from Java)

// Convert HSV to RGB
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

// Signed Distance Field for a rounded box
float roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {
    return length(max(abs(centerPos) - size + radius, 0.0)) - radius;
}

void main() {
    vec2 uv = v_texCoord;
    vec4 baseColor = texture2D(u_texture, uv) * v_color;

    // Calculate rounded corners (e.g., 6 pixel radius)
    float radius = 6.0;
    vec2 pixelPos = uv * u_size;
    vec2 halfSize = u_size * 0.5;
    float dist = roundedBoxSDF(pixelPos - halfSize, halfSize, radius);

    // Smoothstep provides clean anti-aliasing on the rounded edges
    float alphaMask = 1.0 - smoothstep(0.0, 1.0, dist);

    // Discard pixels completely outside the rounded corners
    if (alphaMask <= 0.0) {
//        discard;
    }

    float x = uv.x;
    vec4 outColor;

    // Calculate a 2-pixel wide line based on the total width
    float lineWidth = 2.0 / u_size.x;

    // 1. Black delineator line (only draws if damage is actively trailing)
    if (x > u_healthPct && x <= u_healthPct + lineWidth && u_healthPct < u_damagePct) {
        outColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
    // 2. Current actual health (Green -> Yellow -> Red)
    else if (x <= u_healthPct) {
        float hue = u_healthPct * 0.33; // 0.33 is Green, 0.16 is Yellow, 0.0 is Red
        vec3 healthColor = hsv2rgb(vec3(hue, 0.85, 0.9));
        outColor = vec4(healthColor, 1.0);
    }
    // 3. Trailing damage catch-up (Orange)
    else if (x <= u_damagePct) {
        outColor = vec4(1.0, 0.55, 0.1, 1.0);
    }
    // 4. Empty / lost health (Dark background)
    else {
        outColor = vec4(0.12, 0.12, 0.14, 0.8);
    }

    // Apply the color, base texture, and the rounded corner alpha mask
    gl_FragColor = outColor * baseColor * vec4(1.0, 1.0, 1.0, alphaMask);
}
