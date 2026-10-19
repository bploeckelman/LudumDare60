#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;
// 0.0 = completely off, 1.0 = fully on and settled
uniform float u_progress;

void main() {
    // Map texture coordinates from [0, 1] to [-1, 1] for center-based math
    vec2 uv = v_texCoord * 2.0 - 1.0;

    // 1. Calculate expansion scales based on progress
    // Phase 1: Horizontal beam expands instantly (0.0 to 0.15)
    float scaleX = smoothstep(0.0, 0.15, u_progress);

    // Phase 2: Vertical expansion starts after X finishes (0.1 to 0.5)
    // Starts at a tiny value (0.005) to represent the thin electron beam line
    float scaleY = mix(0.005, 1.0, smoothstep(0.1, 0.5, u_progress));

    // Hard clamp to 0 if progress is exactly 0
    if (u_progress <= 0.0) {
        scaleX = 0.0;
        scaleY = 0.0;
    }

    // 2. Create the rectangular screen mask with slightly soft glowing edges
    float maskX = 1.0 - smoothstep(scaleX - 0.02, scaleX, abs(uv.x));
    float maskY = 1.0 - smoothstep(scaleY - 0.02, scaleY, abs(uv.y));
    float mask = maskX * maskY;

    // 3. Flash Intensity: super bright while expanding, fading to 0 as it settles
    float flash = 0.0;
    if (u_progress > 0.0 && u_progress < 0.8) {
        flash = 1.0 - smoothstep(0.2, 0.8, u_progress);
    }

    // 4. Idle Screen Styling (Dark greyish-blue with scanlines and a vignette)
    vec3 idleColor = vec3(0.08, 0.10, 0.12);
    float scanlines = 0.85 + 0.15 * sin(v_texCoord.y * 300.0);
    float vignette = 1.0 - (dot(uv, uv) * 0.3);
    idleColor *= scanlines * vignette;

    // 5. Combine and colorize
    // Add bright white/blue to the screen based on the flash intensity
    vec3 beamColor = vec3(0.8, 0.9, 1.0);
    vec3 finalColor = idleColor + (beamColor * flash * 0.8);

    // 6. Glowing horizontal edge where the beam is tearing open
    float edgeGlow = smoothstep(scaleY - 0.05, scaleY, abs(uv.y)) * maskX;
    if (u_progress > 0.0 && u_progress < 0.6) {
        finalColor += vec3(0.6, 0.8, 1.0) * edgeGlow * flash * 3.0;
    }

    // Apply the rectangular mask to hide anything outside the expanding beam
    finalColor *= mask;

    gl_FragColor = vec4(finalColor, 1.0);
}
