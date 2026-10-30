package com.example.client.ascii;

import com.example.ASCLLMod;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs the ASCII post-processing chain over the finished frame.
 *
 * <p>{@code assets/ascll-mod/shaders/post/ascll_ascii.json} describes the chain:
 * the frame is read from {@code minecraft:main}, converted to glyphs by the
 * {@code ascll_ascii} program and copied back into {@code minecraft:main}, so the
 * HUD, chat, menus and inventory are converted as well.
 */
public final class AsciiPostEffect implements AutoCloseable {
    public static final AsciiPostEffect INSTANCE = new AsciiPostEffect();

    private static final Logger LOGGER = LoggerFactory.getLogger("ascll-mod/ascii");
    private static final ResourceLocation CHAIN = ASCLLMod.id("shaders/post/ascll_ascii.json");

    private PostChain chain;
    private boolean dirty = true;
    private boolean failed;
    private int width = -1;
    private int height = -1;

    private AsciiPostEffect() {
    }

    /** Requests a rebuild on the next frame, e.g. after a resource reload. */
    public void markDirty() {
        this.dirty = true;
        this.failed = false;
    }

    /**
     * Drops the current chain before vanilla releases all shader stages (F3+T), so the
     * shader is compiled again from the reloaded resources on the next frame.
     */
    public void closeForReload() {
        this.close();
        this.markDirty();
    }

    /** Called at the very end of {@code GameRenderer#render}, once every frame is drawn. */
    public void render(Minecraft minecraft, DeltaTracker deltaTracker) {
        if (!AsciiConfig.get().enabled) {
            return;
        }

        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        if (mainTarget == null || mainTarget.width <= 0 || mainTarget.height <= 0) {
            return;
        }

        if (this.dirty) {
            if (this.failed) {
                return;
            }

            this.rebuild(minecraft, mainTarget);
            this.dirty = false;
        }

        if (this.chain == null) {
            return;
        }

        if (mainTarget.width != this.width || mainTarget.height != this.height) {
            this.width = mainTarget.width;
            this.height = mainTarget.height;
            this.chain.resize(this.width, this.height);
        }

        this.applyUniforms();

        // Same state vanilla uses right before running a post chain.
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.resetTextureMatrix();

        this.chain.process(deltaTracker.getGameTimeDeltaPartialTick(false));

        // Leave the main target bound, exactly like the vanilla post-effect path does.
        mainTarget.bindWrite(true);
    }

    private void rebuild(Minecraft minecraft, RenderTarget mainTarget) {
        this.close();

        try {
            this.chain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), mainTarget, CHAIN);
            this.width = mainTarget.width;
            this.height = mainTarget.height;
            this.chain.resize(this.width, this.height);
            this.applyUniforms();
        } catch (Exception exception) {
            this.failed = true;
            this.chain = null;
            LOGGER.error("Could not load the ASCII shader {}", CHAIN, exception);
        }
    }

    private void applyUniforms() {
        AsciiConfig config = AsciiConfig.get();

        this.chain.setUniform("CellHeight", config.cellHeight);
        this.chain.setUniform("Aspect", config.aspect);
        this.chain.setUniform("MonoMix", config.monoMix);
        this.chain.setUniform("ColorBoost", config.colorBoost);
        this.chain.setUniform("Blend", config.blend);
    }

    @Override
    public void close() {
        if (this.chain != null) {
            this.chain.close();
            this.chain = null;
        }
    }
}
