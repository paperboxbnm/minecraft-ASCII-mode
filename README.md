<a id="english"></a>

# ASCLL mod

**English** · [Русский](#русский)

A Fabric 1.21 client mod that renders **all** of Minecraft as ASCII characters: the world,
entities, particles, the HUD, chat, inventory and every menu are converted into glyphs by a
post-processing shader.

---

## Requirements

| | |
| --- | --- |
| Minecraft | **1.21** (Java Edition) |
| Loader | **Fabric Loader 0.20.1** or newer |
| Dependency | **[Fabric API](https://modrinth.com/mod/fabric-api)** for 1.21 (`0.102.0+1.21` or newer) |
| Java | 21 or newer |
| Side | **Client only** — put it in `.minecraft/mods`; servers don't need it, it works on any server |

## Install

1. Install Fabric Loader for Minecraft 1.21.
2. Drop **Fabric API** and **`ascll-mod-1.0.0.jar`** (from the [releases](../../releases) page or
   `build/libs/`) into `.minecraft/mods`.
3. Start the game — the whole screen is ASCII. Press **F6** to toggle it off and on.

## Usage

* **F6** — toggle the shader (rebindable in *Options → Controls → Key Binds → ASCLL mod*).
* `config/ascll-mod.json` is created on first launch:

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
| `enabled` | Whether the effect is applied at startup. F6 flips this and writes the file back. |
| `cellHeight` | Glyph size in pixels. `8` = very dense (1:1 with the atlas), `16` = default, `24` = big readable characters. |
| `aspect` | Cell width ÷ cell height. `1.0` keeps the square 8×8 glyphs undistorted, `0.5` gives the narrow terminal look. |
| `monoMix` | `0` keeps the colours of the world, `1` gives a green monochrome terminal. |
| `colorBoost` | Ink brightness / saturation multiplier. |
| `blend` | `0` = original frame, `1` = full ASCII. Useful for smooth transitions. |

The values are pushed to the shader every frame, but the JSON is read once at startup, so after
editing it just restart the game — no rebuild is required.

## How it works

| File | Purpose |
| --- | --- |
| `src/client/java/com/example/client/mixin/GameRendererMixin.java` | Injects at the end of `GameRenderer#render`, i.e. when the world *and* the GUI are already drawn into `minecraft:main`. |
| `src/client/java/com/example/client/ascii/AsciiPostEffect.java` | Owns the `PostChain`, resizes it with the window and pushes the config values as uniforms. |
| `src/client/java/com/example/client/ascii/AsciiConfig.java` | Reads/writes `config/ascll-mod.json`. |
| `src/client/java/com/example/client/ASCLLModClient.java` | Registers the F6 key binding. |
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
rows are addressed as `(row * 8 + pixel + 0.5) / 128` without an extra flip. Getting this wrong
turns the whole screen black.

## Customisation

* **Different characters** — edit the `RAMP` array in `ascll_ascii.fsh` (values are
  `charCode - 32`, darkest → brightest) and/or replace `ascll_ascii_atlas.png` with your own
  128×128 atlas that keeps the same cell layout (16×16 cells, printable ASCII from row 2).
* **Different look** — `monoMix` for the green terminal, `blend` for partial ASCII, `cellHeight`
  and `aspect` for the grid.

## Build

```bash
./gradlew build        # Windows: gradlew.bat build
```

The jar ends up in `build/libs/ascll-mod-1.0.0.jar`. Loom 1.19 requires **Java 25** to run
Gradle itself (the mod still targets Java 21).

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it
in your own projects.

<br>

---

<a id="russian"></a>

# ASCLL мод

[English](#english) · **Русский**

Клиентский мод для Fabric 1.21, который рисует **всю** игру ASCII-символами: мир, сущности,
частицы, HUD, чат, инвентарь и все меню превращаются в глифы пост-обрабатывающим шейдером.

---

## Требования

| | |
| --- | --- |
| Minecraft | **1.21** (Java Edition) |
| Загрузчик | **Fabric Loader 0.20.1** или новее |
| Зависимость | **[Fabric API](https://modrinth.com/mod/fabric-api)** для 1.21 (`0.102.0+1.21` или новее) |
| Java | 21 или новее |
| Сторона | **Только клиент** — кидать в `.minecraft/mods`; серверу мод не нужен, работает на любом сервере |

## Установка

1. Установить Fabric Loader для Minecraft 1.21.
2. Положить **Fabric API** и **`ascll-mod-1.0.0.jar`** (со страницы [releases](../../releases) или
   из `build/libs/`) в `.minecraft/mods`.
3. Запустить игру — весь экран станет ASCII. **F6** включает и выключает эффект.

## Использование

* **F6** — включить/выключить шейдер (переназначается в *Настройки → Управление → Клавиши →
  ASCLL mod*).
* `config/ascll-mod.json` создаётся при первом запуске:

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

| Параметр | Что делает |
| --- | --- |
| `enabled` | Применять эффект при запуске. F6 переключает это значение и сохраняет файл. |
| `cellHeight` | Размер символа в пикселях. `8` — очень плотно (1:1 с атласом), `16` — по умолчанию, `24` — крупно и читаемо. |
| `aspect` | Ширина ячейки ÷ высота. `1.0` — квадратные ячейки, глифы 8×8 не искажаются; `0.5` — узкий «терминальный» вид. |
| `monoMix` | `0` — цвета как в игре, `1` — зелёный монохромный терминал. |
| `colorBoost` | Яркость и насыщенность «чернил». |
| `blend` | `0` — исходный кадр, `1` — полный ASCII. Удобно для плавного перехода. |

Значения уходят в шейдер каждый кадр, но сам JSON читается один раз при запуске: после правки
достаточно перезапустить игру, пересборка не нужна.

## Как это работает

| Файл | Назначение |
| --- | --- |
| `src/client/java/com/example/client/mixin/GameRendererMixin.java` | Инъекция в конец `GameRenderer#render`, когда мир *и* интерфейс уже нарисованы в `minecraft:main`. |
| `src/client/java/com/example/client/ascii/AsciiPostEffect.java` | Держит `PostChain`, подгоняет его под размер окна и передаёт значения конфига в юниформы. |
| `src/client/java/com/example/client/ascii/AsciiConfig.java` | Чтение/запись `config/ascll-mod.json`. |
| `src/client/java/com/example/client/ASCLLModClient.java` | Регистрирует привязку клавиши F6. |
| `src/client/resources/assets/ascll-mod/shaders/post/ascll_ascii.json` | Пост-цепочка: `minecraft:main` → программа `ascll_ascii` → `ascll_swap` → `blit` → `minecraft:main`. |
| `src/client/resources/assets/minecraft/shaders/program/ascll_ascii.json` | Описание программы (сэмплеры, атрибуты, юниформы по умолчанию). |
| `src/client/resources/assets/minecraft/shaders/program/ascll_ascii.vsh` / `.fsh` | Сам шейдер. |
| `src/client/resources/assets/minecraft/textures/effect/ascll_ascii_atlas.png` | Атлас глифов 128×128: 16×16 ячеек по 8×8 px, печатный ASCII (32–126) начиная со 2-й строки. |

Minecraft всегда грузит программы пост-эффектов и вспомогательные текстуры `textures/effect/*`
из пространства имён `minecraft` (`Identifier.ofVanilla(...)` в `EffectInstance` / `PostChain`),
поэтому программа шейдера и атлас лежат в `assets/minecraft/`, а сама пост-цепочка — в
пространстве имён `ascll-mod`.

Фрагментный шейдер:

1. разбивает кадр на ячейки (`CellHeight` пикселей в высоту, `Aspect` = ширина / высота);
2. усредняет шаблон 3×3 внутри ячейки — получает цвет и яркость ячейки;
3. выбирает глиф из рампы плотности на 95 уровней (`RAMP`, отсортирована по измеренному
   заполнению чернилами);
4. берёт выбранный глиф из атласа и красит его в цвет ячейки;
5. смешивает результат с нетронутым кадром через `Blend`.

Про ориентацию текстур: кадр в `minecraft:main` хранится «снизу вверх», поэтому читается
напрямую по `texCoord`, а PNG-атлас загружается первой строкой в `v = 0`, поэтому строки глифов
адресуются как `(row * 8 + pixel + 0.5) / 128` — без дополнительного переворота. Если сделать
наоборот, экран становится полностью чёрным.

## Кастомизация

* **Другие символы** — правь массив `RAMP` в `ascll_ascii.fsh` (значения = `charCode - 32`,
  от тёмного к светлому) и/или подмени `ascll_ascii_atlas.png` своим атласом 128×128 с той же
  раскладкой (16×16 ячеек, печатный ASCII со 2-й строки).
* **Другой вид** — `monoMix` для зелёного терминала, `blend` для частичного ASCII, `cellHeight`
  и `aspect` для размера сетки.

## Сборка

```bash
./gradlew build        # Windows: gradlew.bat build
```

Готовый jar появится в `build/libs/ascll-mod-1.0.0.jar`. Для запуска Gradle нужен **JDK 25**
(Loom 1.19), сам мод при этом собирается под Java 21.

## Лицензия

Шаблон распространяется по лицензии CC0. Берите, учитесь, вставляйте в свои проекты.
