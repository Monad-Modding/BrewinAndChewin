package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Direction;
import umpaz.brewinandchewin.common.registry.BnCItems;

public class CornCropBlock extends CropBlock {
    public static final int MAX_SECTIONS = 3;
    public static final IntegerProperty SECTION = IntegerProperty.create("section", 0, MAX_SECTIONS - 1);

    private static final int[] SECTIONS_BY_AGE = {1, 1, 1, 2, 3, 3, 3, 3};
    private static final int[][] HEIGHTS = {
            {4, 8, 16, 16, 16, 16, 16, 16},
            {0, 0, 0, 10, 16, 16, 16, 16},
            {0, 0, 0, 0, 5, 14, 13, 11}
    };
    private static final VoxelShape[][] SHAPES = buildShapes();

    public CornCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(SECTION, 0));
    }

    private static VoxelShape[][] buildShapes() {
        VoxelShape[][] shapes = new VoxelShape[MAX_SECTIONS][MAX_AGE + 1];
        for (int section = 0; section < MAX_SECTIONS; ++section)
            for (int age = 0; age <= MAX_AGE; ++age)
                shapes[section][age] = Block.box(0.0D, 0.0D, 0.0D, 16.0D, Math.max(HEIGHTS[section][age], 1), 16.0D);
        return shapes;
    }

    public static int sectionsForAge(int age) {
        return SECTIONS_BY_AGE[age];
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return BnCItems.CORN_KERNELS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SECTION);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(SECTION)][state.getValue(this.getAgeProperty())];
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        int section = state.getValue(SECTION);
        if (section == 0)
            return super.canSurvive(state, level, pos);
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) && below.getValue(SECTION) == section - 1;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(SECTION) != 0)
            return;
        super.randomTick(state, level, pos, random);
        updateSections(level, pos);
    }

    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        BlockPos bottom = getBottomPos(level, pos, state);
        super.growCrops(level, bottom, level.getBlockState(bottom));
        updateSections(level, bottom);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        BlockPos bottom = getBottomPos(level, pos, state);
        BlockState bottomState = level.getBlockState(bottom);
        return bottomState.is(this) && !this.isMaxAge(bottomState);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && state.getValue(SECTION) > 0) {
            BlockPos bottom = getBottomPos(level, pos, state);
            BlockState bottomState = level.getBlockState(bottom);
            if (bottomState.is(this) && bottomState.getValue(SECTION) == 0) {
                level.setBlock(bottom, Blocks.AIR.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_ALL);
                level.levelEvent(player, 2001, bottom, Block.getId(bottomState));
                dropResources(bottomState, level, bottom, null, player, player.getMainHandItem());
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private BlockPos getBottomPos(BlockGetter level, BlockPos pos, BlockState state) {
        BlockPos bottom = pos;
        for (int section = state.getValue(SECTION); section > 0; --section) {
            BlockPos below = bottom.below();
            BlockState belowState = level.getBlockState(below);
            if (!belowState.is(this))
                break;
            bottom = below;
        }
        return bottom;
    }

    private void updateSections(Level level, BlockPos bottom) {
        BlockState bottomState = level.getBlockState(bottom);
        if (!bottomState.is(this) || bottomState.getValue(SECTION) != 0)
            return;
        int age = bottomState.getValue(this.getAgeProperty());
        int required = SECTIONS_BY_AGE[age];
        for (int section = 1; section < MAX_SECTIONS; ++section) {
            BlockPos pos = bottom.above(section);
            BlockState current = level.getBlockState(pos);
            boolean ours = current.is(this) && current.getValue(SECTION) == section;
            if (section < required) {
                if (ours) {
                    if (current.getValue(this.getAgeProperty()) != age)
                        level.setBlock(pos, current.setValue(this.getAgeProperty(), age), Block.UPDATE_CLIENTS);
                } else if (current.isAir()) {
                    level.setBlock(pos, this.defaultBlockState().setValue(SECTION, section).setValue(this.getAgeProperty(), age), Block.UPDATE_ALL);
                } else {
                    return;
                }
            } else if (ours) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }
}
