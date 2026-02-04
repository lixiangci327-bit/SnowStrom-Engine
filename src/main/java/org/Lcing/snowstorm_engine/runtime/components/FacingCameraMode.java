package org.Lcing.snowstorm_engine.runtime.components;

public enum FacingCameraMode {
    ROTATE_XYZ("rotate_xyz"),
    ROTATE_Y("rotate_y"),
    LOOKAT_XYZ("lookat_xyz"),
    LOOKAT_Y("lookat_y"),
    LOOKAT_DIRECTION("lookat_direction"),
    DIRECTION_X("direction_x"),
    DIRECTION_Y("direction_y"),
    DIRECTION_Z("direction_z"),
    EMITTER_TRANSFORM_XY("emitter_transform_xy"),
    EMITTER_TRANSFORM_XZ("emitter_transform_xz"),
    EMITTER_TRANSFORM_YZ("emitter_transform_yz");

    private final String id;

    FacingCameraMode(String id) {
        this.id = id;
    }

    public static FacingCameraMode fromString(String id) {
        for (FacingCameraMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }
        return ROTATE_XYZ; // Default
    }
}
