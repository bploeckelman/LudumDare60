#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoord;
uniform float u_healthPct; // Target / current actual health
uniform float u_damagePct; // Catch-up trailing health (orange)

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {

    float x = v_texCoord.x;
    vec4 baseColor = texture2D(u_texture, v_texCoord) * v_color;

    // Optional border margin: keep 1px padding
    // If within actual health: primary bar color (e.g., green/cyan)
    if (x <= u_healthPct) {
        // Map the health percentage to a Hue value.
        // Hue 0.33 is Green, 0.16 is Yellow, 0.0 is Red.
        float hue = u_healthPct * 0.33;

        // Convert the Hue (with fixed Saturation and Brightness/Value) to RGB
        vec3 healthColor = hsv2rgb(vec3(hue, 0.85, 0.9));

        gl_FragColor = vec4(healthColor, 1.0) * baseColor;
    }
    // If between actual health and trailing health: damage catch-up (orange)
    else if (x <= u_damagePct) {
        gl_FragColor = vec4(1.0, 0.55, 0.1, 1.0) * baseColor;
    }
    // Empty / lost health: dark background
    else {
        gl_FragColor = vec4(0.12, 0.12, 0.14, 0.8) * baseColor;
    }
}
