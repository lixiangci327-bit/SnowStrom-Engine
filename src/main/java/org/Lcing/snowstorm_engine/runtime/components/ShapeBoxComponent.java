package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * Implements minecraft:emitter_shape_box
 * Particles spawn within a box volume or on its surface.
 */
public class ShapeBoxComponent implements IParticleComponent {

    private IMolangExpression offsetX, offsetY, offsetZ;
    private IMolangExpression halfX, halfY, halfZ;
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

        // Parse half_dimensions [x, y, z]
        if (comp.has("half_dimensions") && comp.get("half_dimensions").isJsonArray()) {
            var arr = comp.getAsJsonArray("half_dimensions");
            halfX = MolangParser.parseJson(arr.get(0));
            halfY = MolangParser.parseJson(arr.get(1));
            halfZ = MolangParser.parseJson(arr.get(2));
        } else {
            halfX = halfY = halfZ = IMolangExpression.constant(0.5f);
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

        float hx = halfX.eval(ctx);
        float hy = halfY.eval(ctx);
        float hz = halfZ.eval(ctx);

        float px, py, pz;
        float nx = 0, ny = 0, nz = 0; // Normal/direction

        if (surfaceOnly) {
            // Pick a random face and random point on that face
            int face = rand.nextInt(6);
            switch (face) {
                case 0: // +X
                    px = hx;
                    py = rand.nextFloat() * 2 * hy - hy;
                    pz = rand.nextFloat() * 2 * hz - hz;
                    nx = 1;
                    break;
                case 1: // -X
                    px = -hx;
                    py = rand.nextFloat() * 2 * hy - hy;
                    pz = rand.nextFloat() * 2 * hz - hz;
                    nx = -1;
                    break;
                case 2: // +Y
                    px = rand.nextFloat() * 2 * hx - hx;
                    py = hy;
                    pz = rand.nextFloat() * 2 * hz - hz;
                    ny = 1;
                    break;
                case 3: // -Y
                    px = rand.nextFloat() * 2 * hx - hx;
                    py = -hy;
                    pz = rand.nextFloat() * 2 * hz - hz;
                    ny = -1;
                    break;
                case 4: // +Z
                    px = rand.nextFloat() * 2 * hx - hx;
                    py = rand.nextFloat() * 2 * hy - hy;
                    pz = hz;
                    nz = 1;
                    break;
                default: // -Z
                    px = rand.nextFloat() * 2 * hx - hx;
                    py = rand.nextFloat() * 2 * hy - hy;
                    pz = -hz;
                    nz = -1;
                    break;
            }
        } else {
            // Random point inside box
            px = rand.nextFloat() * 2 * hx - hx;
            py = rand.nextFloat() * 2 * hy - hy;
            pz = rand.nextFloat() * 2 * hz - hz;
            // Direction: normalize position from center
            float len = (float) Math.sqrt(px * px + py * py + pz * pz);
            if (len > 0.001f) {
                nx = px / len;
                ny = py / len;
                nz = pz / len;
            }
        }

        // Apply offset
        float ox = offsetX.eval(ctx);
        float oy = offsetY.eval(ctx);
        float oz = offsetZ.eval(ctx);

        particle.x += ox + px;
        particle.y += oy + py;
        particle.z += oz + pz;

        // Set direction based on mode
        switch (directionMode) {
            case "outwards":
                particle.vx = nx;
                particle.vy = ny;
                particle.vz = nz;
                break;
            case "inwards":
                particle.vx = -nx;
                particle.vy = -ny;
                particle.vz = -nz;
                break;
            case "custom":
                particle.vx = dirX != null ? dirX.eval(ctx) : 0;
                particle.vy = dirY != null ? dirY.eval(ctx) : 0;
                particle.vz = dirZ != null ? dirZ.eval(ctx) : 0;
                break;
            default:
                particle.vx = nx;
                particle.vy = ny;
                particle.vz = nz;
        }
    }
}
