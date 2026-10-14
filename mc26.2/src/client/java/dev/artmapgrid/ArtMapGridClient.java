package dev.artmapgrid;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;

public final class ArtMapGridClient implements ClientModInitializer {
    public static GridConfig config;
    private static int selectedFrame = -1;

    @Override
    public void onInitializeClient() {
        config = GridConfig.load();
        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("artmap_grid", "controls"));
        var toggle = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.artmap_grid.toggle", InputConstants.Type.KEYSYM, InputConstants.KEY_G, category));
        var center = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.artmap_grid.center", InputConstants.Type.KEYSYM, InputConstants.KEY_F, category));
        var sections = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.artmap_grid.sections", InputConstants.Type.KEYSYM, InputConstants.KEY_N, category));
        var opacity = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.artmap_grid.opacity", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), category));
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            selectCanvas(client);
            while (toggle.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    config.enabled = !config.enabled;
                    config.save();
                    client.gui.hud.setOverlayMessage(Component.translatable(config.enabled
                            ? "message.artmap_grid.enabled" : "message.artmap_grid.disabled"), false);
                }
            }
            while (center.consumeClick()) {
                if (client.player != null && client.gui.screen() == null && selectedFrame != -1) {
                    config.centerEnabled = !config.centerEnabled;
                    config.save();
                    client.gui.hud.setOverlayMessage(Component.translatable(config.centerEnabled
                            ? "message.artmap_grid.center_enabled" : "message.artmap_grid.center_disabled"), false);
                    // F also swaps hands in vanilla. Consume that action only at a detected canvas
                    // and only if the player assigned both actions to the same key.
                    if (center.same(client.options.keySwapOffhand)) {
                        while (client.options.keySwapOffhand.consumeClick()) { }
                    }
                }
            }
            while (sections.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    config.sectionsEnabled = !config.sectionsEnabled;
                    config.save();
                    client.gui.hud.setOverlayMessage(Component.translatable(config.sectionsEnabled
                            ? "message.artmap_grid.sections_enabled" : "message.artmap_grid.sections_disabled"), false);
                }
            }
            while (opacity.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    config.opacity = config.opacity >= 0.59f ? 0.15f : config.opacity + 0.15f;
                    config.save();
                    client.gui.hud.setOverlayMessage(Component.translatable(
                            "message.artmap_grid.opacity", Math.round(config.opacity * 100)), false);
                }
            }
            selectCanvas(client);
        });
        ClientTickEvents.END_CLIENT_TICK.register(ArtMapGridClient::selectCanvas);
    }

    private static void selectCanvas(Minecraft client) {
        selectedFrame = -1;
        if (client.player == null || client.level == null) return;
        if (config.onlyWhileSeated && !(client.player.getVehicle() instanceof ArmorStand)) return;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (ItemFrame frame : client.level.getEntitiesOfClass(ItemFrame.class,
                client.player.getBoundingBox().inflate(3))) {
            if (frame.getFramedMapId(frame.getItem()) == null) continue;
            // An easel is upright. Reject horizontal maps and maps facing away from the artist.
            var direction = frame.getDirection();
            if (!direction.getAxis().isHorizontal()) continue;
            var toArtist = client.player.position().subtract(frame.position());
            if (toArtist.x * direction.getStepX() + toArtist.z * direction.getStepZ() <= 0) continue;
            double distance = client.player.getEyePosition().distanceToSqr(frame.position());
            if (distance < bestDistance) {
                bestDistance = distance;
                selectedFrame = frame.getId();
            }
        }
    }

    public static GridStyle styleFor(ItemFrame frame) {
        return config != null && (config.enabled || config.centerEnabled || config.sectionsEnabled) && frame.getId() == selectedFrame
                ? new GridStyle(config.enabled, config.centerEnabled, config.sectionsEnabled,
                    config.sectionSize, config.opacity, config.lineWidth) : null;
    }
}

