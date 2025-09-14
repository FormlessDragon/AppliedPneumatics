package com.wintercogs.appliedpneumatics.common.init;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

public class APBlockStates
{
    public enum WallState implements StringRepresentable
    {
        COMMON("common"),
        CENTER("center"),
        XEDGE("xedge"), YEDGE("yedge"), ZEDGE("zedge"),
        XMIN_YMIN_ZMIN("xmin_ymin_zmin"),
        XMIN_YMIN_ZMAX("xmin_ymin_zmax"),
        XMIN_YMAX_ZMIN("xmin_ymax_zmin"),
        XMIN_YMAX_ZMAX("xmin_ymax_zmax"),
        XMAX_YMIN_ZMIN("xmax_ymin_zmin"),
        XMAX_YMIN_ZMAX("xmax_ymin_zmax"),
        XMAX_YMAX_ZMIN("xmax_ymax_zmin"),
        XMAX_YMAX_ZMAX("xmax_ymax_zmax");

        private final String name;
        WallState(String n)
        {
            this.name = n;
        }

        @Override
        public @NotNull String getSerializedName()
        {
            return name;
        }
    }
    public static final EnumProperty<WallState> WALL_STATE = EnumProperty.create("wall_state", WallState.class);
}
