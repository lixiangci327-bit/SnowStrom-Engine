package org.Lcing.snowstorm_engine.integration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages a lookup table for Blockbench locators.
 * Since GeckoLib discards locator data at runtime, we parse geo.json files
 * ourselves
 * and build an index: locatorName -> (parentBoneName, localOffset).
 */
public class LocatorIndexManager {
    private static final LocatorIndexManager INSTANCE = new LocatorIndexManager();
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();

    // Key: locatorName, Value: LocatorInfo
    private final Map<String, LocatorInfo> locatorIndex = new HashMap<>();

    public static LocatorIndexManager getInstance() {
        return INSTANCE;
    }

    /**
     * Holds information about a locator's parent bone and local offset.
     */
    public static class LocatorInfo {
        public final String parentBoneName;
        public final double offsetX;
        public final double offsetY;
        public final double offsetZ;

        public LocatorInfo(String parentBoneName, double offsetX, double offsetY, double offsetZ) {
            this.parentBoneName = parentBoneName;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }
    }

    /**
     * Clears and rebuilds the locator index from all geo.json files.
     */
    public void rebuildIndex(ResourceManager resourceManager) {
        locatorIndex.clear();

        // Scan all geo.json files in assets/*/geo/
        Collection<ResourceLocation> geoFiles = resourceManager.listResources("geo",
                path -> path.endsWith(".geo.json"));

        for (ResourceLocation location : geoFiles) {
            try {
                parseGeoFile(resourceManager, location);
            } catch (Exception e) {
                LOGGER.warn("[Snowstorm] Failed to parse locators from: " + location, e);
            }
        }

        LOGGER.info("[Snowstorm] Locator index built. Total locators: " + locatorIndex.size());
    }

    private void parseGeoFile(ResourceManager resourceManager, ResourceLocation location) throws Exception {
        InputStream stream = resourceManager.getResource(location).getInputStream();
        InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        JsonObject root = GSON.fromJson(reader, JsonObject.class);
        reader.close();

        // Navigate to minecraft:geometry array
        JsonArray geometryArray = root.getAsJsonArray("minecraft:geometry");
        if (geometryArray == null) {
            return;
        }

        for (JsonElement geoElement : geometryArray) {
            JsonObject geometry = geoElement.getAsJsonObject();
            JsonArray bones = geometry.getAsJsonArray("bones");
            if (bones == null) {
                continue;
            }

            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.has("name") ? bone.get("name").getAsString() : null;
                if (boneName == null) {
                    continue;
                }

                // Check for locators
                JsonObject locators = bone.getAsJsonObject("locators");
                if (locators == null) {
                    continue;
                }

                // Iterate through each locator in this bone
                for (Map.Entry<String, JsonElement> entry : locators.entrySet()) {
                    String locatorName = entry.getKey();
                    JsonElement locatorValue = entry.getValue();

                    double ox = 0, oy = 0, oz = 0;

                    // Locator can be an array [x,y,z] or an object {offset: [...], rotation: [...]}
                    if (locatorValue.isJsonArray()) {
                        JsonArray arr = locatorValue.getAsJsonArray();
                        if (arr.size() >= 3) {
                            ox = arr.get(0).getAsDouble();
                            oy = arr.get(1).getAsDouble();
                            oz = arr.get(2).getAsDouble();
                        }
                    } else if (locatorValue.isJsonObject()) {
                        JsonObject locatorObj = locatorValue.getAsJsonObject();
                        if (locatorObj.has("offset")) {
                            JsonArray arr = locatorObj.getAsJsonArray("offset");
                            if (arr != null && arr.size() >= 3) {
                                ox = arr.get(0).getAsDouble();
                                oy = arr.get(1).getAsDouble();
                                oz = arr.get(2).getAsDouble();
                            }
                        }
                    }

                    locatorIndex.put(locatorName, new LocatorInfo(boneName, ox, oy, oz));
                }
            }
        }
    }

    /**
     * Looks up a locator by name.
     * 
     * @return LocatorInfo if found, null otherwise.
     */
    public LocatorInfo getLocator(String locatorName) {
        return locatorIndex.get(locatorName);
    }
}
