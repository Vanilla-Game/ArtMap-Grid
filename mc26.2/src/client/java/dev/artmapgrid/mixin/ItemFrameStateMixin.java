package dev.artmapgrid.mixin;

import dev.artmapgrid.GridRenderState;
import dev.artmapgrid.GridStyle;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemFrameRenderState.class)
public class ItemFrameStateMixin implements GridRenderState {
    @Unique private GridStyle artmapGrid$style;
    public GridStyle artmapGrid$getStyle() { return artmapGrid$style; }
    public void artmapGrid$setStyle(GridStyle style) { artmapGrid$style = style; }
}
