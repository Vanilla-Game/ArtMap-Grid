package dev.artmapgrid.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.artmapgrid.ArtMapGridClient;
import dev.artmapgrid.GridOverlay;
import dev.artmapgrid.GridRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameRenderer.class)
public class ItemFrameRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ItemFrame;Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;F)V", at = @At("TAIL"))
    private void artmapGrid$extract(ItemFrame frame, ItemFrameRenderState state, float tick, CallbackInfo ci) {
        ((GridRenderState) state).artmapGrid$setStyle(ArtMapGridClient.styleFor(frame));
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MapRenderer;render(Lnet/minecraft/client/renderer/state/MapRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ZI)V", shift = At.Shift.AFTER))
    private void artmapGrid$submit(ItemFrameRenderState state, PoseStack stack,
                                    SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        var style = ((GridRenderState) state).artmapGrid$getStyle();
        if (style != null && state.mapId != null) {
            GridOverlay.submit(stack, collector, style, state.isGlowFrame ? 15728850 : state.lightCoords);
        }
    }
}


