package umpaz.brewinandchewin.neoforge.loot.modifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.CheeseWheelBlock;
import umpaz.brewinandchewin.common.block.PizzaBlock;
import vectorwing.farmersdelight.common.tag.CommonTags;

import javax.annotation.Nonnull;

public class BnCSlicingModifier extends LootModifier
{
    public static final ResourceLocation ID = BrewinAndChewin.asResource("slicing");
    public static final MapCodec<BnCSlicingModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
                    .and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("slice").forGetter((m) -> m.slice))
                    .apply(inst, BnCSlicingModifier::new));

    private final Item slice;

    protected BnCSlicingModifier(LootItemCondition[] conditionsIn, Item sliceIn) {
        super(conditionsIn);
        this.slice = sliceIn;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state != null) {
            Block targetBlock = state.getBlock();
            if (targetBlock instanceof PizzaBlock) {
                int servings = state.getValue(PizzaBlock.SERVINGS);
                generatedLoot.add(new ItemStack(slice, servings + 1));
            }
            else if (targetBlock instanceof CheeseWheelBlock) {
                    int servings = state.getValue(CheeseWheelBlock.SERVINGS);
                if (servings == 3 && !context.getParam(LootContextParams.TOOL).is(CommonTags.Items.TOOLS_KNIFE)) {
                    generatedLoot.add(new ItemStack(targetBlock.asItem()));
                }
                else {
                    generatedLoot.add(new ItemStack(slice, servings + 1));
                }
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
