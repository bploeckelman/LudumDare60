#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoord;
uniform float u_healthPct; // Target / current actual health
uniform float u_damagePct; // Catch-up trailing health (orange)


void main() {

    float x = v_texCoord.x;
    vec4 baseColor = texture2D(u_texture, v_texCoord) * v_color;

    // Optional border margin: keep 1px padding
    // If within actual health: primary bar color (e.g., green/cyan)
    if (x <= u_healthPct) {
        gl_FragColor = vec4(0.2, 0.9, 0.3, 1.0) * baseColor;
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
