package umpaz.brewinandchewin.fabric.loot.modifier;

import com.mojang.serialization.Codec;
import io.github.fabricators_of_create.porting_lib.loot.IGlobalLootModifier;
import io.github.fabricators_of_create.porting_lib.loot.LootModifier;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.CheeseWheelBlock;
import umpaz.brewinandchewin.common.block.PizzaBlock;
import vectorwing.farmersdelight.common.tag.ModTags;

// TODO: Port Me!
@Deprecated
public class BnCSlicingModifier extends LootModifier {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("slicing");
    private final Item slice;

    protected BnCSlicingModifier(LootItemCondition[] conditionsIn, Item sliceIn) {
        super(conditionsIn);
        this.slice = sliceIn;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state != null) {
            Block targetBlock = state.getBlock();
            if (targetBlock instanceof PizzaBlock) {
                int servings = state.getValue(PizzaBlock.SERVINGS);
                generatedLoot.add(new ItemStack(slice, servings + 1));
            } else if (targetBlock instanceof CheeseWheelBlock) {
                int servings = state.getValue(CheeseWheelBlock.SERVINGS);
                if (servings == 3 && !context.getParam(LootContextParams.TOOL).is(ModTags.KNIVES)) {
                    generatedLoot.add(new ItemStack(targetBlock.asItem()));
                } else {
                    generatedLoot.add(new ItemStack(slice, servings + 1));
                }
            }
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return null;
    }
}
