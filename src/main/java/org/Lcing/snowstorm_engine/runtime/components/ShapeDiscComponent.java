package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * Implements minecraft:emitter_shape_disc
 * Particles spawn on a disc (flat circle).
 */
public class ShapeDiscComponent implements IParticleComponent {

    private IMolangExpression offsetX, offsetY, offsetZ;
    private IMolangExpression radius;
    private IMolangExpression normalX, normalY, normalZ; // Disc normal
    private boolean surfaceOnly = false;
    private String directionMode = "outwards";
    private IMolangExpression dirX, dirY, dirZ;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json is already the component value
        JsonObject comp = json.getAsJsonObject();

        // Parse offset [x, y, z]
        if (comp.has("offset") && comp.get("offset").isJsonArray()) {
            var arr = comp.getAsJsonArray("offset");
            offsetX = MolangParser.parseJson(arr.get(0));
            offsetY = MolangParser.parseJson(arr.get(1));
            offsetZ = MolangParser.parseJson(arr.get(2));
        } else {
            offsetX = offsetY = offsetZ = IMolangExpression.ZERO;
        }

        // Parse radius
        radius = MolangParser.parseJson(comp.get("radius"));
        if (radius == null) {
            radius = IMolangExpression.constant(1);
        }

        // Parse plane_normal (defaults to Y-up)
        if (comp.has("plane_normal") && comp.get("plane_normal").isJsonArray()) {
            var arr = comp.getAsJsonArray("plane_normal");
            normalX = MolangParser.parseJson(arr.get(0));
            normalY = MolangParser.parseJson(arr.get(1));
            normalZ = MolangParser.parseJson(arr.get(2));
        } else {
            normalX = IMolangExpression.ZERO;
            normalY = IMolangExpression.constant(1);
            normalZ = IMolangExpression.ZERO;
        }

        // Parse surface_only
        if (comp.has("surface_only")) {
            surfaceOnly = comp.get("surface_only").getAsBoolean();
        }

        // Parse direction
        if (comp.has("direction")) {
            JsonElement dirElem = comp.get("direction");
            if (dirElem.isJsonPrimitive()) {
                directionMode = dirElem.getAsString();
            } else if (dirElem.isJsonArray()) {
                directionMode = "custom";
                var arr = dirElem.getAsJsonArray();
                dirX = MolangParser.parseJson(arr.get(0));
                dirY = MolangParser.parseJson(arr.get(1));
                dirZ = MolangParser.parseJson(arr.get(2));
            }
        }
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        var ctx = particle.getContext();
        var rand = ctx.getRandom();

        float r = radius.eval(ctx);

        // Random angle on disc
        double angle = rand.nextDouble() * 2 * Math.PI;

        // Distance from center
        float dist;
        if (surfaceOnly) {
            dist = r;
        } else {
            // sqrt for uniform distribution on disc
            dist = r * (float) Math.sqrt(rand.nextDouble());
        }

        // Get normal vector
        float nx = normalX.eval(ctx);
        float ny = normalY.eval(ctx);
        float nz = normalZ.eval(ctx);

        // Normalize
        float nLen = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (nLen > 0.001f) {
            nx /= nLen;
            ny /= nLen;
            nz /= nLen;
        } else {
            ny = 1; // Default Y-up
        }

        // Create two vectors perpendicular to normal
        float ux, uy, uz, vx, vy, vz;
        if (Math.abs(ny) > 0.9f) {
            // Normal is close to Y, use X as reference
            ux = 1;
            uy = 0;
            uz = 0;
        } else {
            ux = 0;
            uy = 1;
            uz = 0;
        }
        // v = normal x u
        vx = ny * uz - nz * uy;
        vy = nz * ux - nx * uz;
        vz = nx * uy - ny * ux;
        // Normalize v
        float vLen = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
        vx /= vLen;
        vy /= vLen;
        vz /= vLen;
        // u = v x normal
        ux = vy * nz - vz * ny;
        uy = vz * nx - vx * nz;
        uz = vx * ny - vy * nx;

        // Position on disc: offset + (cos(angle)*u + sin(angle)*v) * dist
        float cosA = (float) Math.cos(angle);
        float sinA = (float) Math.sin(angle);

        float px = (cosA * ux + sinA * vx) * dist;
        float py = (cosA * uy + sinA * vy) * dist;
        float pz = (cosA * uz + sinA * vz) * dist;

        // Apply offset
        float ox = offsetX.eval(ctx);
        float oy = offsetY.eval(ctx);
        float oz = offsetZ.eval(ctx);

        particle.x += ox + px;
        particle.y += oy + py;
        particle.z += oz + pz;

        // Set direction
        switch (directionMode) {
            case "outwards":
                // Direction is along the disc (perpendicular to normal, outward from center)
                float len = (float) Math.sqrt(px * px + py * py + pz * pz);
                if (len > 0.001f) {
                    particle.vx = px / len;
                    particle.vy = py / len;
                    particle.vz = pz / len;
                }
                break;
            case "inwards":
                len = (float) Math.sqrt(px * px + py * py + pz * pz);
                if (len > 0.001f) {
                    particle.vx = -px / len;
                    particle.vy = -py / len;
                    particle.vz = -pz / len;
                }
                break;
            case "custom":
                particle.vx = dirX != null ? dirX.eval(ctx) : 0;
                particle.vy = dirY != null ? dirY.eval(ctx) : 0;
                particle.vz = dirZ != null ? dirZ.eval(ctx) : 0;
                break;
            default:
                // Use normal direction
                particle.vx = nx;
                particle.vy = ny;
                particle.vz = nz;
        }
    }
}
