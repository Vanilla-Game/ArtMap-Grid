package dev.artmapgrid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GridConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("artmap-grid.json"); }
    public boolean enabled = false;
    public boolean centerEnabled = false;
    public boolean sectionsEnabled = false;
    public int sectionSize = 8;
    public float opacity = 0.30f;
    public float lineWidth = 0.18f;
    public boolean onlyWhileSeated = true;

    public static GridConfig load() {
        Path PATH = path();
        try {
            if (Files.exists(PATH)) {
                GridConfig config = GSON.fromJson(Files.readString(PATH), GridConfig.class);
                if (config != null) {
                    if (!validSectionSize(config.sectionSize)) config.sectionSize = 8;
                    config.opacity = Float.isFinite(config.opacity) ? Math.clamp(config.opacity, 0.05f, 0.9f) : 0.30f;
                    config.lineWidth = Float.isFinite(config.lineWidth) ? Math.clamp(config.lineWidth, 0.05f, 0.8f) : 0.18f;
                    return config;
                }
            }
        } catch (Exception exception) {
            LoggerFactory.getLogger("artmap_grid").warn("Cannot load grid settings; using defaults", exception);
        }
        GridConfig config = new GridConfig();
        config.save();
        return config;
    }

    public static boolean validSectionSize(int size) {
        return size == 2 || size == 4 || size == 8 || size == 16 || size == 32;
    }

    public void save() {
        Path PATH = path();
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this));
        } catch (Exception exception) {
            LoggerFactory.getLogger("artmap_grid").warn("Cannot save grid settings", exception);
        }
    }
}
