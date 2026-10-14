package dev.artmapgrid;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class GridSettingsScreen extends Screen {
    private final Screen parent;
    private boolean expanded;
    public GridSettingsScreen(Screen parent) {
        super(Component.translatable("settings.artmap_grid.title"));
        this.parent = parent;
    }
    @Override protected void init() {
        int w = Math.min(300, width - 24), x = (width - w) / 2;
        int y = Math.max(32, (height - 196) / 2);
        var config = ArtMapGridClient.config;
        addRenderableWidget(Button.builder(Component.translatable("settings.artmap_grid.size",
                config.sectionSize, config.sectionSize, 32/config.sectionSize, 32/config.sectionSize,
                expanded ? "▲" : "▼"), button -> {
            expanded = !expanded;
            rebuildWidgets();
        }).bounds(x,y,w,20).build());
        if (expanded) {
            int row = 0;
            for (int size : new int[]{2,4,8,16,32}) {
                addRenderableWidget(Button.builder(Component.translatable("settings.artmap_grid.option",
                        size,size,32/size,32/size), button -> {
                    config.sectionSize = size;
                    config.save();
                    expanded = false;
                    rebuildWidgets();
                }).bounds(x,y+24+row++*24,w,20).build());
            }
        } else {
            toggle(x,y+24,w,"settings.artmap_grid.grid",config.enabled, () -> config.enabled = !config.enabled);
            toggle(x,y+48,w,"settings.artmap_grid.center",config.centerEnabled, () -> config.centerEnabled = !config.centerEnabled);
            toggle(x,y+72,w,"settings.artmap_grid.sections",config.sectionsEnabled, () -> config.sectionsEnabled = !config.sectionsEnabled);
            addRenderableWidget(Button.builder(Component.translatable("message.artmap_grid.opacity",Math.round(config.opacity*100)), button -> {
                config.opacity = config.opacity >= .59f ? .15f : config.opacity + .15f;
                config.save(); rebuildWidgets();
            }).bounds(x,y+96,w,20).build());
            toggle(x,y+120,w,"settings.artmap_grid.seated",config.onlyWhileSeated, () -> config.onlyWhileSeated = !config.onlyWhileSeated);
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x,height-28,w,20).build());
    }
    private void toggle(int x,int y,int width,String label,boolean value,Runnable change) {
        addRenderableWidget(Button.builder(Component.translatable(label,
                Component.translatable(value ? "options.on" : "options.off")), button -> {
            change.run(); ArtMapGridClient.config.save(); rebuildWidgets();
        }).bounds(x,y,width,20).build());
    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float delta) {
        super.render(graphics,mouseX,mouseY,delta);
        graphics.drawCenteredString(font,title,width/2,12,0xFFFFFFFF);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
}

