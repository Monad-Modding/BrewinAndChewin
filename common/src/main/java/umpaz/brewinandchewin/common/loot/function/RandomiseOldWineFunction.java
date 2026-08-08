package umpaz.brewinandchewin.common.loot.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.item.WineItem;
import umpaz.brewinandchewin.common.utility.BnCWineUtils;

import java.util.List;

public class RandomiseOldWineFunction extends LootItemConditionalFunction {
    public static final MapCodec<RandomiseOldWineFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
            commonFields(inst).apply(inst, RandomiseOldWineFunction::new));

    public static final ResourceLocation ID = BrewinAndChewin.asResource("randomise_old_wine");
    public static final LootItemFunctionType<RandomiseOldWineFunction> TYPE = new LootItemFunctionType<>(CODEC);

    private RandomiseOldWineFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(RandomiseOldWineFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (stack.getItem() instanceof WineItem) {
            BnCWineUtils.setContents(stack, BnCWineUtils.createOldWine(context.getRandom()));
        }
        return stack;
    }

    @Override
    public LootItemFunctionType<RandomiseOldWineFunction> getType() {
        return TYPE;
    }
}
