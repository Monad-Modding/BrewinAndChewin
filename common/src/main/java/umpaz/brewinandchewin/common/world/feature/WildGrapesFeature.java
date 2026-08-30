package umpaz.brewinandchewin.common.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import umpaz.brewinandchewin.common.block.WildGrapesBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.world.BnCBiomeFeatures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WildGrapesFeature extends Feature<NoneFeatureConfiguration> {
    public static final int SCAN_DEPTH = 24;
    public static final int SCAN_RADIUS = 3;
    public static final int MIN_TRUNKS = 1;
    public static final int MAX_TRUNKS = 2;

    public WildGrapesFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        if (level.getBiome(origin).is(BnCBiomeFeatures.IS_SNOWY))
            return false;

        Map<Long, List<BlockPos>> columns = new HashMap<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; ++x) {
            for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; ++z) {
                int cx = origin.getX() + x;
                int cz = origin.getZ() + z;
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, cx, cz);
                for (int depth = 0; depth < SCAN_DEPTH; ++depth) {
                    cursor.set(cx, top - depth, cz);
                    if (cursor.getY() < level.getMinBuildHeight())
                        break;
                    if (WildGrapesBlock.canGrowOn(level.getBlockState(cursor)))
                        columns.computeIfAbsent(BlockPos.asLong(cx, 0, cz),
                                key -> new ArrayList<>()).add(cursor.immutable());
                }
            }
        }
        if (columns.isEmpty())
            return false;

        List<List<BlockPos>> trunks = new ArrayList<>(columns.values());
        Util.shuffle(trunks, random);
        int wanted = Math.min(trunks.size(),
                MIN_TRUNKS + random.nextInt(MAX_TRUNKS - MIN_TRUNKS + 1));

        boolean placed = false;
        for (int i = 0; i < wanted; ++i)
            placed |= this.growOnTrunk(level, random, trunks.get(i));
        return placed;
    }

    private boolean growOnTrunk(WorldGenLevel level, RandomSource random, List<BlockPos> column) {
        BlockPos trunk = column.get(random.nextInt(column.size()));
        List<Direction> faces = new ArrayList<>();
        for (Direction facing : Direction.Plane.HORIZONTAL)
            if (level.isEmptyBlock(trunk.relative(facing)))
                faces.add(facing);
        if (faces.isEmpty())
            return false;

        Direction facing = faces.get(random.nextInt(faces.size()));
        BlockState grapes = BnCBlocks.WILD_GRAPES.defaultBlockState().setValue(WildGrapesBlock.FACING, facing.getOpposite());
        int height = WildGrapesBlock.MIN_HEIGHT
                + random.nextInt(WildGrapesBlock.MAX_HEIGHT - WildGrapesBlock.MIN_HEIGHT + 1);

        int placed = 0;
        for (int offset = 0; offset < height; ++offset) {
            BlockPos support = trunk.above(offset);
            BlockPos target = support.relative(facing);
            if (!WildGrapesBlock.canGrowOn(level.getBlockState(support)) || !level.isEmptyBlock(target))
                break;
            level.setBlock(target, grapes, Block.UPDATE_CLIENTS);
            ++placed;
        }
        return placed > 0;
    }
}
