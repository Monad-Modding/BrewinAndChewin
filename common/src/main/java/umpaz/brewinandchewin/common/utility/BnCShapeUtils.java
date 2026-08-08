package umpaz.brewinandchewin.common.utility;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BnCShapeUtils {
    public static VoxelShape rotate(VoxelShape shape, Direction facing) {
        VoxelShape result = shape;
        for (int turns = quarterTurns(facing); turns > 0; --turns) {
            result = rotateClockwise(result);
        }
        return result;
    }

    public static VoxelShape mirrorX(VoxelShape shape) {
        VoxelShape[] result = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                result[0] = Shapes.or(result[0], Shapes.box(1.0D - maxX, minY, minZ, 1.0D - minX, maxY, maxZ)));
        return result[0];
    }

    private static int quarterTurns(Direction facing) {
        return switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    private static VoxelShape rotateClockwise(VoxelShape shape) {
        VoxelShape[] result = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                result[0] = Shapes.or(result[0], Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX)));
        return result[0];
    }
}
