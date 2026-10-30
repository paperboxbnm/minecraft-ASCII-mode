package com.example.client;

import com.example.client.ascii.AsciiConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ASCLLModClient implements ClientModInitializer {
	/**
	 * Toggles the ASCII shader. The key can be changed in the vanilla controls screen,
	 * the category is "ASCLL mod".
	 */
	private static final KeyMapping TOGGLE_ASCII = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.ascll-mod.toggle_ascii",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_F6,
			"category.ascll-mod"
	));

	@Override
	public void onInitializeClient() {
		// Reads config/ascll-mod.json (creates it with defaults on first launch).
		AsciiConfig.load();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (TOGGLE_ASCII.consumeClick()) {
				boolean enabled = AsciiConfig.toggle();
				if (client.player != null) {
					client.gui.setOverlayMessage(Component.translatable(enabled ? "ascll-mod.enabled" : "ascll-mod.disabled"), false);
				}
			}
		});
	}
}
