package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implements minecraft:particle_appearance_tinting
 * Sets color tinting for particles.
 */
public class TintingComponent implements IParticleComponent {

    private IMolangExpression colorR, colorG, colorB, colorA;
    private boolean useGradient = false;

    // Gradient support
    private IMolangExpression interpolant;
    private final List<Float> gradientKeys = new ArrayList<>();
    private final List<int[]> gradientColors = new ArrayList<>();

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // Note: json is the component VALUE, not wrapped with key
        JsonObject comp = json.getAsJsonObject();

        JsonElement colorElem = comp.get("color");
        if (colorElem == null)
            return;

        // Direct color: can be [r, g, b, a] or "#RRGGBB" / "#AARRGGBB"
        if (colorElem.isJsonArray()) {
            JsonArray arr = colorElem.getAsJsonArray();
            colorR = MolangParser.parseJson(arr.get(0));
            colorG = MolangParser.parseJson(arr.get(1));
            colorB = MolangParser.parseJson(arr.get(2));
            if (arr.size() >= 4) {
                colorA = MolangParser.parseJson(arr.get(3));
            } else {
                colorA = IMolangExpression.constant(1.0f);
            }
        } else if (colorElem.isJsonPrimitive() && colorElem.getAsString().startsWith("#")) {
            // Parse hex color
            String hex = colorElem.getAsString();
            int[] rgba = parseHexColor(hex);
            colorR = IMolangExpression.constant(rgba[0] / 255.0f);
            colorG = IMolangExpression.constant(rgba[1] / 255.0f);
            colorB = IMolangExpression.constant(rgba[2] / 255.0f);
            colorA = IMolangExpression.constant(rgba[3] / 255.0f);
        } else if (colorElem.isJsonObject()) {
            // Gradient with interpolant
            JsonObject colorObj = colorElem.getAsJsonObject();
            useGradient = true;

            // Parse interpolant (Molang expression for gradient position)
            if (colorObj.has("interpolant")) {
                interpolant = MolangParser.parseJson(colorObj.get("interpolant"));
            } else {
                interpolant = IMolangExpression.constant(0);
            }

            // Parse gradient colors
            if (colorObj.has("gradient")) {
                JsonObject gradient = colorObj.getAsJsonObject("gradient");
                for (Map.Entry<String, JsonElement> entry : gradient.entrySet()) {
                    float key = Float.parseFloat(entry.getKey());
                    String hexColor = entry.getValue().getAsString();
                    gradientKeys.add(key);
                    gradientColors.add(parseHexColor(hexColor));
                }
            }

            // Default to white if no gradient parsed
            if (gradientKeys.isEmpty()) {
                colorR = colorG = colorB = colorA = IMolangExpression.constant(1.0f);
                useGradient = false;
            }
        }
    }

    private int[] parseHexColor(String hex) {
        int[] rgba = { 255, 255, 255, 255 };
        hex = hex.substring(1); // Remove #

        if (hex.length() == 6) {
            // RRGGBB
            rgba[0] = Integer.parseInt(hex.substring(0, 2), 16);
            rgba[1] = Integer.parseInt(hex.substring(2, 4), 16);
            rgba[2] = Integer.parseInt(hex.substring(4, 6), 16);
        } else if (hex.length() == 8) {
            // AARRGGBB
            rgba[3] = Integer.parseInt(hex.substring(0, 2), 16);
            rgba[0] = Integer.parseInt(hex.substring(2, 4), 16);
            rgba[1] = Integer.parseInt(hex.substring(4, 6), 16);
            rgba[2] = Integer.parseInt(hex.substring(6, 8), 16);
        }
        return rgba;
    }

    private void applyGradient(SnowstormParticle particle) {
        if (gradientKeys.isEmpty())
            return;

        float t = interpolant.eval(particle.getContext());

        // Find surrounding keys for interpolation
        int lowerIdx = 0;
        int upperIdx = gradientKeys.size() - 1;

        for (int i = 0; i < gradientKeys.size(); i++) {
            if (gradientKeys.get(i) <= t) {
                lowerIdx = i;
            }
            if (gradientKeys.get(i) >= t && upperIdx == gradientKeys.size() - 1) {
                upperIdx = i;
                break;
            }
        }

        int[] lowerColor = gradientColors.get(lowerIdx);
        int[] upperColor = gradientColors.get(upperIdx);

        float blend = 0;
        if (lowerIdx != upperIdx) {
            float lowerKey = gradientKeys.get(lowerIdx);
            float upperKey = gradientKeys.get(upperIdx);
            blend = (t - lowerKey) / (upperKey - lowerKey);
            blend = Math.max(0, Math.min(1, blend));
        }

        particle.colorR = (lowerColor[0] + (upperColor[0] - lowerColor[0]) * blend) / 255.0f;
        particle.colorG = (lowerColor[1] + (upperColor[1] - lowerColor[1]) * blend) / 255.0f;
        particle.colorB = (lowerColor[2] + (upperColor[2] - lowerColor[2]) * blend) / 255.0f;
        particle.colorA = (lowerColor[3] + (upperColor[3] - lowerColor[3]) * blend) / 255.0f;

        // Debug: Log color calculation occasionally
        if (Math.random() < 0.01) {
            System.out.println("[Snowstorm] Color: t=" + String.format("%.2f", t) +
                    " -> RGBA(" + String.format("%.2f", particle.colorR) +
                    ", " + String.format("%.2f", particle.colorG) +
                    ", " + String.format("%.2f", particle.colorB) +
                    ", " + String.format("%.2f", particle.colorA) + ")");
        }
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        if (useGradient) {
            applyGradient(particle);
        } else if (colorR != null) {
            var ctx = particle.getContext();
            particle.colorR = colorR.eval(ctx);
            particle.colorG = colorG.eval(ctx);
            particle.colorB = colorB.eval(ctx);
            particle.colorA = colorA.eval(ctx);
        }
    }

    @Override
    public void onRenderParticle(SnowstormParticle particle, float partialTick) {
        // For gradient, update every frame based on interpolant
        if (useGradient) {
            applyGradient(particle);
        }
    }
}
