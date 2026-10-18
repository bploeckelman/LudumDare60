#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform float u_time;

varying vec4 v_color;
varying vec2 v_texCoord;

// --- SETTINGS ---
// change these values to 0.0 to turn off individual effects
const float vertJerkOpt = 1.0;
const float vertMovementOpt = 1.0;
const float bottomStaticOpt = 1.0;
const float scalinesOpt = 1.0;
const float rgbOffsetOpt = 1.0;
const float horzFuzzOpt = 1.0;

// --- CRT GLASS MATH ---
vec2 crt(vec2 coord, float warpAmount) {
    // put in symmetrical coords
    coord = (coord - 0.5) * 2.0;
    coord *= 1.01;

    // deform coords
    coord.x *= 1.0 + (coord.y * coord.y) * warpAmount;
    coord.y *= 1.0 + (coord.x * coord.x) * warpAmount;

    // transform back to 0.0 - 1.0 space
    return (coord / 2.0) + 0.5;
}

// --- ASHIMA SIMPLEX NOISE ---
vec3 mod289(vec3 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec2 mod289(vec2 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec3 permute(vec3 x) {
    return mod289(((x*34.0)+1.0)*x);
}

float snoise(vec2 v) {
    const vec4 C = vec4(0.211324865405187,  // (3.0-sqrt(3.0))/6.0
            0.366025403784439,  // 0.5*(sqrt(3.0)-1.0)
            -0.577350269189626,  // -1.0 + 2.0 * C.x
            0.024390243902439); // 1.0 / 41.0
    // First corner
    vec2 i  = floor(v + dot(v, C.yy) );
    vec2 x0 = v -   i + dot(i, C.xx);

    // Other corners
    vec2 i1;
    i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;

    // Permutations
    i = mod289(i); // Avoid truncation effects in permutation
    vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 ))
    + i.x + vec3(0.0, i1.x, 1.0 ));

    vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
    m = m*m ;
    m = m*m ;

    // Gradients: 41 points uniformly over a line, mapped onto a diamond.
    vec3 x = 2.0 * fract(p * C.www) - 1.0;
    vec3 h = abs(x) - 0.5;
    vec3 ox = floor(x + 0.5);
    vec3 a0 = x - ox;

    m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );

    vec3 g;
    g.x  = a0.x  * x0.x  + h.x  * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;
    return 130.0 * dot(m, g);
}

float staticV(vec2 uv) {
    float staticHeight = snoise(vec2(9.0, u_time*1.2+3.0))*0.3+5.0;
    float staticAmount = snoise(vec2(1.0, u_time*1.2-6.0))*0.1+0.3;
    float staticStrength = snoise(vec2(-9.75, u_time*0.6-3.0))*2.0+2.0;
    return (1.0-step(snoise(vec2(5.0*pow(u_time,2.0)+pow(uv.x*7.0,1.2),pow((mod(u_time,100.0)+100.0)*uv.y*0.3+3.0,staticHeight))),staticAmount))*staticStrength;
}

void main() {
    // libGDX v_texCoord is already normalized (0.0 to 1.0), replicating fragCoord/iResolution
    vec2 uv = v_texCoord.xy;

    // 1. PHYSICAL SCREEN COORDS: These dictate the curved glass and edge fading
    vec2 screenCoords = crt(uv, 0.1);

    // 2. ALPHA FADE AT EDGES
    float fadeSize = 0.08;
    float alphaX = smoothstep(0.0, fadeSize, screenCoords.x) * smoothstep(1.0, 1.0 - fadeSize, screenCoords.x);
    float alphaY = smoothstep(0.0, fadeSize, screenCoords.y) * smoothstep(1.0, 1.0 - fadeSize, screenCoords.y);
    float edgeAlpha = alphaX * alphaY;


    // 3. APPLY SHADERTOY VCR LOGIC TO SCREEN COORDS
    float jerkOffset = (1.0-step(snoise(vec2(u_time*1.3,5.0)),0.8))*0.05;

    float fuzzOffset = snoise(vec2(u_time*15.0,screenCoords.y*80.0))*0.003;
    float largeFuzzOffset = snoise(vec2(u_time*1.0,screenCoords.y*25.0))*0.004;

    float vertMovementOn = (1.0-step(snoise(vec2(u_time*0.2,8.0)),0.4))*vertMovementOpt;
    float vertJerk = (1.0-step(snoise(vec2(u_time*1.5,5.0)),0.6))*vertJerkOpt;
    float vertJerk2 = (1.0-step(snoise(vec2(u_time*5.5,5.0)),0.2))*vertJerkOpt;

    float yOffset = abs(sin(u_time)*4.0)*vertMovementOn+vertJerk*vertJerk2*0.3;

    // fract() safely handles negative wraps where mod() sometimes fails in older GLES
    float y = fract(screenCoords.y + yOffset);

    float xOffset = (fuzzOffset + largeFuzzOffset) * horzFuzzOpt;

    float staticVal = 0.0;

    for (int i = -1; i <= 1; i++) {
        float maxDist = 5.0/200.0;
        float dist = float(i)/200.0;
        staticVal += staticV(vec2(screenCoords.x, screenCoords.y+dist))*(maxDist-abs(dist))*1.5;
    }

    staticVal *= bottomStaticOpt;

    float red   = texture2D(u_texture, vec2(screenCoords.x + xOffset -0.01*rgbOffsetOpt, y)).r+staticVal;
    float green = texture2D(u_texture, vec2(screenCoords.x + xOffset, y)).g+staticVal;
    float blue  = texture2D(u_texture, vec2(screenCoords.x + xOffset +0.01*rgbOffsetOpt, y)).b+staticVal;

    vec3 color = vec3(red, green, blue);

    float scanline = sin(screenCoords.y*800.0)*0.04*scalinesOpt;
    color -= scanline;

    // Output color, respecting the physical edge fade and base vertex alpha
    gl_FragColor = vec4(color, edgeAlpha * v_color.a);
}
