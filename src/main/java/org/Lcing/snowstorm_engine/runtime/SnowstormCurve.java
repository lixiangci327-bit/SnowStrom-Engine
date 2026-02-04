package org.Lcing.snowstorm_engine.runtime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;

public class SnowstormCurve {
    private String type; // "linear" (others: "bezier", "catmull_rom" not implemented yet)
    private IMolangExpression input;
    private IMolangExpression horizontalRange;
    private float[] nodes;

    public static SnowstormCurve fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return null;
        JsonObject obj = json.getAsJsonObject();

        SnowstormCurve curve = new SnowstormCurve();
        curve.type = obj.has("type") ? obj.get("type").getAsString() : "linear";

        if (obj.has("input")) {
            curve.input = MolangParser.parseJson(obj.get("input"));
        } else {
            curve.input = IMolangExpression.constant(0);
        }

        if (obj.has("horizontal_range")) {
            curve.horizontalRange = MolangParser.parseJson(obj.get("horizontal_range"));
        } else {
            curve.horizontalRange = IMolangExpression.constant(1);
        }

        if (obj.has("nodes") && obj.get("nodes").isJsonArray()) {
            JsonArray arr = obj.getAsJsonArray("nodes");
            curve.nodes = new float[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                curve.nodes[i] = arr.get(i).getAsFloat();
            }
        } else {
            curve.nodes = new float[] { 0 };
        }

        return curve;
    }

    public float eval(org.Lcing.snowstorm_engine.molang.MolangContext ctx) {
        float in = input.eval(ctx);
        float range = horizontalRange.eval(ctx);

        // Normalized input (0.0 to 1.0)
        // Usually curves are mapped from input [0, range] to nodes index
        float t = (range != 0) ? (in / range) : 0;

        // Clamp t to [0, 1]? Assuming input/range is strictly growing/clamped?
        // Usually particle_age / particle_lifetime is 0..1.
        // Let's implement clamp just in case.
        if (t < 0)
            t = 0;
        if (t > 1)
            t = 1;

        if (nodes == null || nodes.length == 0)
            return 0;
        if (nodes.length == 1)
            return nodes[0];

        // Linear interpolation
        // nodes[0] is at t=0, nodes[len-1] is at t=1
        float scaledT = t * (nodes.length - 1);
        int index = (int) scaledT;
        float frac = scaledT - index;

        if (index < 0)
            return nodes[0];
        if (index >= nodes.length - 1)
            return nodes[nodes.length - 1];

        float v0 = nodes[index];
        float v1 = nodes[index + 1];

        return v0 + (v1 - v0) * frac;
    }
}
