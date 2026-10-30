package com.example.client.mixin;

import com.example.client.ascii.AsciiPostEffect;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	/**
	 * {@code GameRenderer#render} is the last place where the whole frame (world + HUD +
	 * open screens) lives in {@code minecraft:main}, so the ASCII post chain is applied
	 * here, right before the frame is blitted to the window.
	 */
	@Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("TAIL"))
	private void ascll$applyAsciiShader(DeltaTracker deltaTracker, boolean tick, CallbackInfo ci) {
		AsciiPostEffect.INSTANCE.render(Minecraft.getInstance(), deltaTracker);
	}

	/**
	 * Vanilla releases (and recompiles) every loaded shader stage on a resource reload, so
	 * the ASCII chain - which owns compiled shader stages - has to be dropped first and is
	 * then rebuilt lazily on the next frame.
	 */
	@Inject(method = "reloadShaders(Lnet/minecraft/server/packs/resources/ResourceProvider;)V", at = @At("HEAD"))
	private void ascll$onShaderReload(ResourceProvider resourceProvider, CallbackInfo ci) {
		AsciiPostEffect.INSTANCE.closeForReload();
	}
}
