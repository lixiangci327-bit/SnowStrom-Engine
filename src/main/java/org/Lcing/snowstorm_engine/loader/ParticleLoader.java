package org.Lcing.snowstorm_engine.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.Lcing.snowstorm_engine.definition.ParticleDefinition;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class ParticleLoader {

    // Create a Gson instance configured to be lenient (accepting non-standard JSON
    // if needed)
    private static final Gson GSON = new GsonBuilder()
            .setLenient()
            .create();

    /**
     * Parses a .particle.json input stream into a ParticleDefinition object.
     * 
     * @param inputStream The stream containing the JSON data
     * @return The parsed ParticleDefinition
     * @throws RuntimeException if parsing fails
     */
    public static ParticleDefinition load(InputStream inputStream) {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            ParticleDefinition definition = GSON.fromJson(reader, ParticleDefinition.class);

            // Basic validation to ensure it's a valid particle file
            if (definition == null || definition.getEffect() == null) {
                throw new IllegalArgumentException("Invalid particle JSON structure: Missing 'particle_effect'");
            }

            return definition;
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode particle definition", e);
        }
    }
}
