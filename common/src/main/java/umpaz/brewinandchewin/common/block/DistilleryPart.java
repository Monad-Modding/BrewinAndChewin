package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum DistilleryPart implements StringRepresentable {
    BURNER("burner", false, false),
    CONDENSER("condenser", true, false),
    BURNER_TOP("burner_top", false, true),
    CONDENSER_TOP("condenser_top", true, true);

    private final String name;
    private final boolean condenserSide;
    private final boolean upper;

    DistilleryPart(String name, boolean condenserSide, boolean upper) {
        this.name = name;
        this.condenserSide = condenserSide;
        this.upper = upper;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public boolean isBurner() {
        return this == BURNER;
    }

    public BlockPos offsetFromBurner(Direction condenserDirection) {
        BlockPos offset = this.condenserSide ? BlockPos.ZERO.relative(condenserDirection) : BlockPos.ZERO;
        return this.upper ? offset.above() : offset;
    }
}
