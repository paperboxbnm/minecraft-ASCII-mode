package com.example.client.ascii;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Settings of the ASCII post effect.
 *
 * <p>The file lives in {@code config/ascll-mod.json}. Every value is pushed to the
 * shader as a uniform, so editing the file (or calling {@link #save()}) is enough
 * to tweak the look - no restart needed.
 */
public final class AsciiConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("ascll-mod/config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("ascll-mod.json");

    private static AsciiConfig instance = new AsciiConfig();

    /** Whether the effect is currently applied. */
    public boolean enabled = true;
    /** Height of a single character cell in framebuffer pixels (4 - 64). */
    public float cellHeight = 16.0F;
    /** Cell width / cell height. 1.0 keeps the square 8x8 glyphs undistorted. */
    public float aspect = 1.0F;
    /** 0 = keep the colours of the world, 1 = green monochrome terminal. */
    public float monoMix = 0.0F;
    /** Brightness / saturation multiplier for the ink. */
    public float colorBoost = 1.15F;
    /** 0 = original frame, 1 = full ASCII. Useful for smooth toggles. */
    public float blend = 1.0F;

    private AsciiConfig() {
    }

    public static AsciiConfig get() {
        return instance;
    }

    public static void load() {
        if (!Files.isRegularFile(FILE)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(FILE)) {
            AsciiConfig loaded = GSON.fromJson(reader, AsciiConfig.class);
            if (loaded != null) {
                instance = loaded;
            }
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Could not read {}, using defaults", FILE, exception);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException exception) {
            LOGGER.warn("Could not write {}", FILE, exception);
        }
    }

    /** Flips {@link #enabled}, stores the result and returns the new state. */
    public static boolean toggle() {
        instance.enabled = !instance.enabled;
        save();
        return instance.enabled;
    }
}
