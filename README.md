# ASCLL mod

A Fabric 1.21 client mod that renders **all** of Minecraft as ASCII characters: the world,
entities, particles, the HUD, chat, inventory and every menu are converted into glyphs by a
post-processing shader.

## How it works

| File | Purpose |
| --- | --- |
| `src/client/java/com/example/client/mixin/GameRendererMixin.java` | Injects at the end of `GameRenderer#render`, i.e. when the world *and* the GUI are already drawn into `minecraft:main`. |
| `src/client/java/com/example/client/ascii/AsciiPostEffect.java` | Owns the `PostChain`, resizes it with the window and pushes the config values as uniforms. |
| `src/client/java/com/example/client/ascii/AsciiConfig.java` | Reads/writes `config/ascll-mod.json`. |
| `src/client/resources/assets/ascll-mod/shaders/post/ascll_ascii.json` | The post chain: `minecraft:main` → `ascll_ascii` program → `ascll_swap` → `blit` → `minecraft:main`. |
| `src/client/resources/assets/minecraft/shaders/program/ascll_ascii.json` | Program definition (samplers, attributes, default uniforms). |
| `src/client/resources/assets/minecraft/shaders/program/ascll_ascii.vsh` / `.fsh` | The shader itself. |
| `src/client/resources/assets/minecraft/textures/effect/ascll_ascii_atlas.png` | 128×128 glyph atlas: 16×16 cells of 8×8 px, printable ASCII (32–126) starting at row 2. |

Minecraft always loads post-effect programs and `textures/effect/*` aux textures from the
`minecraft` namespace (`Identifier.ofVanilla(...)` in `EffectInstance` / `PostChain`), which is
why the shader program and the atlas live under `assets/minecraft/` while the post chain itself
is namespaced as `ascll-mod`.

The fragment shader:

1. splits the frame into cells (`CellHeight` pixels high, `Aspect` = width / height),
2. averages a 3×3 pattern per cell to get the cell colour and brightness,
3. picks a glyph from a 95-level density ramp (`RAMP`, sorted by measured ink coverage),
4. samples that glyph from the atlas and tints it with the cell colour,
5. mixes the result with the untouched frame by `Blend`.

Note on texture orientation: the frame in `minecraft:main` is stored bottom-up, so it is sampled
with `texCoord` directly, while the atlas PNG is uploaded with its first row at `v = 0`, so glyph
rows are addressed as `(row * 8 + pixel + 0.5) / 128` without an extra flip.

## Usage

* **F6** – toggle the shader (rebindable in *Options → Controls → Key Binds → ASCLL mod*).
* `config/ascll-mod.json` is created on first launch and re-read on startup:

```json
{
  "enabled": true,
  "cellHeight": 16.0,
  "aspect": 1.0,
  "monoMix": 0.0,
  "colorBoost": 1.15,
  "blend": 1.0
}
```

| Option | Effect |
| --- | --- |
| `cellHeight` | Glyph size in pixels. `8` = very dense (1:1 with the atlas), `24` = big readable characters. |
| `aspect` | Cell width ÷ cell height. `1.0` keeps the square 8×8 glyphs undistorted, `0.5` gives the narrow terminal look. |
| `monoMix` | `0` keeps the colours of the world, `1` gives a green monochrome terminal. |
| `colorBoost` | Ink brightness / saturation multiplier. |
| `blend` | `0` = original frame, `1` = full ASCII. |

The values are pushed to the shader every frame, so editing the JSON and restarting (or
reopening the world after a resource reload) is enough – no rebuild required.

Want a different character set? Edit the `RAMP` array in `ascll_ascii.fsh` (values are
`charCode - 32`) and/or replace `ascll_ascii_atlas.png` with your own 128×128 atlas that keeps
the same cell layout.

## Build

```bash
./gradlew build
```

The jar ends up in `build/libs/`. Loom 1.19 requires **Java 25** to run Gradle itself
(the mod still targets Java 21).

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
