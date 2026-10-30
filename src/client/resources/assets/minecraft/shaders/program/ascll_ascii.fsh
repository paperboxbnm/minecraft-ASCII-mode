#version 150

// ASCLL post effect: turns the whole frame into ASCII glyphs.
// The screen is split into character cells, every cell is reduced to an average
// colour + brightness, and the brightness selects one glyph from a density ramp
// which is then sampled from a 16x16 glyph atlas (8x8 pixel cells, see
// assets/minecraft/textures/effect/ascll_ascii_atlas.png).

uniform sampler2D DiffuseSampler; // the rendered frame
uniform sampler2D AsciiAtlas;     // 128x128 glyph atlas, 16x16 cells of 8x8 pixels

uniform vec2  OutSize;     // output resolution in pixels
uniform float CellHeight;  // height of one character cell in pixels
uniform float Aspect;      // cell width / cell height
uniform float MonoMix;     // 0 = keep screen colours, 1 = monochrome terminal
uniform float ColorBoost;  // ink brightness / saturation boost
uniform float Blend;       // 0 = original frame, 1 = full ASCII

in vec2 texCoord;
out vec4 fragColor;

// Density ramp, darkest -> brightest. Values are glyph indices (charCode - 32),
// i.e. index 0 is ' ' and index 94 is '~'. The order was measured from the ink
// coverage of every glyph in ascll_ascii_atlas.png, so brightness increases
// monotonically and gradients stay smooth.
const int RAMP_LENGTH = 95;
const int RAMP[RAMP_LENGTH] = int[RAMP_LENGTH](
     0, 14, 64,  7, 26, 12, 13,  2, 27,  1,
    28, 30, 62, 63, 15, 60, 67, 86,  8,  9,
    10, 29, 31, 44, 79, 82, 90, 92, 94, 11,
    17, 23, 57, 88, 35, 38, 41, 42, 52, 56,
    59, 61, 73, 75, 78, 84, 85, 87, 89, 91,
    93, 54, 69, 70, 74, 18, 19, 33, 37, 43,
    51, 53, 58, 72, 76, 83, 21, 48, 55, 65,
    66, 68, 80, 81, 16, 20, 25, 36, 39, 40,
    46, 47, 50, 77, 22, 24,  6, 34,  4, 45,
    49,  3, 71,  5, 32
);

float luma(vec3 color) {
    return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

void main() {
    vec2 outSize = max(OutSize, vec2(1.0));
    float cellHeight = clamp(CellHeight, 4.0, 64.0);
    float cellWidth = max(2.0, cellHeight * clamp(Aspect, 0.25, 1.5));

    // Position inside the character grid.
    vec2 cells = outSize / vec2(cellWidth, cellHeight);
    vec2 cellPos = texCoord * cells;
    vec2 cellId = floor(cellPos);
    vec2 inCell = cellPos - cellId;

    // Average a 3x3 pattern inside the cell: its colour becomes the ink colour
    // and its brightness picks the glyph.
    vec2 cellUv = 1.0 / cells;
    vec2 cellCenter = (cellId + 0.5) * cellUv;
    vec3 cellColor = vec3(0.0);
    float cellLuma = 0.0;

    for (int y = 0; y < 3; y++) {
        for (int x = 0; x < 3; x++) {
            vec2 offset = (vec2(float(x), float(y)) - 1.0) * 0.25 * cellUv;
            vec3 sampleColor = texture(DiffuseSampler, cellCenter + offset).rgb;
            cellColor += sampleColor;
            cellLuma += luma(sampleColor);
        }
    }

    cellColor /= 9.0;
    cellLuma /= 9.0;

    // Pick a glyph of the density ramp.
    int rampIndex = int(clamp(cellLuma * float(RAMP_LENGTH), 0.0, float(RAMP_LENGTH - 1)));
    int glyph = RAMP[rampIndex];

    // Glyphs start at row 2 of the atlas: index = charCode - 32.
    float glyphColumn = float(glyph % 16);
    float glyphRow = 2.0 + float(glyph / 16);

    // Sample a single texel of the 8x8 glyph. texCoord.y grows upwards while the
    // glyph bitmap is stored with its first row at v = 0, so the row inside the
    // cell is counted from the top of the cell and used directly as v.
    vec2 local = vec2(inCell.x, 1.0 - inCell.y);
    vec2 pixel = clamp(floor(local * 8.0), 0.0, 7.0);
    vec2 atlasUv = vec2(
        (glyphColumn * 8.0 + pixel.x + 0.5) / 128.0,
        (glyphRow * 8.0 + pixel.y + 0.5) / 128.0
    );
    float ink = texture(AsciiAtlas, atlasUv).a;

    // Ink colour follows the cell colour, optionally forced to terminal green.
    vec3 inkColor = clamp(cellColor * ColorBoost, 0.0, 1.0);
    vec3 monoColor = vec3(0.25, 1.0, 0.35) * (0.45 + 0.55 * cellLuma);
    inkColor = mix(inkColor, monoColor, clamp(MonoMix, 0.0, 1.0));

    vec3 ascii = inkColor * ink;
    vec3 original = texture(DiffuseSampler, texCoord).rgb;

    fragColor = vec4(mix(original, ascii, clamp(Blend, 0.0, 1.0)), 1.0);
}
