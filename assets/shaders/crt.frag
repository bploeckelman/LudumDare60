#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoord;
uniform float u_kWarp;
uniform float u_time;

vec3 scanline(vec2 coord, vec3 screen)
{
    screen.rgb -= sin((coord.y + (u_time * 2.0))) * 0.01;
    return screen;
}

vec2 crt(vec2 coord, float warpAmount)
{
    // put in symmetrical coords
    coord = (coord - 0.5) * 2.0;

    coord *= 1.01;

    // deform coords (using multiplication avoids divide-by-zero explosions)
    // and allows negative numbers to "pinch" the screen inward!
    coord.x *= 1.0 + (coord.y * coord.y) * warpAmount;
    coord.y *= 1.0 + (coord.x * coord.x) * warpAmount;

    // transform back to 0.0 - 1.0 space
    coord  = (coord / 2.0) + 0.5;

    return coord;
}

vec3 sampleSplit(sampler2D tex, vec2 coord)
{
    vec3 frag;
    frag.r = texture2D(tex, vec2(coord.x - 0.002 * sin(u_time), coord.y)).r;
    frag.g = texture2D(tex, vec2(coord.x                      , coord.y)).g;
    frag.b = texture2D(tex, vec2(coord.x + 0.004 * sin(u_time), coord.y)).b;
    return frag;
}

// Generates an RGB phosphor subpixel pattern
vec3 apertureGrille(vec2 fragCoord) {
    // mod by 3 to cycle through Red, Green, Blue subpixels
    float pattern = mod(fragCoord.x, 3.0);
    vec3 mask = vec3(1.0);

    if (pattern < 1.0) {
        mask = vec3(1.0, 0.4, 0.4); // Red subpixel
    } else if (pattern < 2.0) {
        mask = vec3(0.4, 1.0, 0.4); // Green subpixel
    } else {
        mask = vec3(0.4, 0.4, 1.0); // Blue subpixel
    }

    // Add a very slight horizontal line dimming to mimic a grid/shadow mask
    mask *= 0.9 + 0.1 * sin(fragCoord.y * 3.1415);

    return mask;
}

void main() {
    vec2 uv = v_texCoord.xy;
    vec2 crtCoords = crt(uv, 0.056 * u_kWarp);

    float vHoldRoll = max(0.0, sin(crtCoords.y * 3.0 - u_time * 2.0));

    float vHoldBand = pow(vHoldRoll, 15.0);


    crtCoords.x += vHoldBand * 0.002;

    if (crtCoords.x < 0.0 || crtCoords.x > 1.0 || crtCoords.y < 0.0 || crtCoords.y > 1.0) {
        discard;
    }

    vec4 fragColor = vec4(1.0);

    fragColor.rgb = sampleSplit(u_texture, crtCoords);

    vec2 screenSpace = crtCoords;
    fragColor.rgb = scanline(screenSpace, fragColor.rgb);


    fragColor.rgb += vec3(vHoldBand * 0.05);


    fragColor.rgb *= apertureGrille(gl_FragCoord.xy);


    fragColor.rgb *= 1.2;

    gl_FragColor = fragColor;
}
