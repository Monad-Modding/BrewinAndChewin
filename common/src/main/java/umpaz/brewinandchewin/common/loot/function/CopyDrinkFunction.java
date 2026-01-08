package umpaz.brewinandchewin.common.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.registry.BnCLootFunctions;
import vectorwing.farmersdelight.common.registry.ModLootFunctions;

@MethodsReturnNonnullByDefault
public class CopyDrinkFunction extends LootItemConditionalFunction {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("copy_drink");

    private CopyDrinkFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(CopyDrinkFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        BlockEntity tile = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof KegBlockEntity kegTile) {
            CompoundTag tag = kegTile.writeDrink(new CompoundTag());
            if (!tag.isEmpty()) {
                stack.addTagElement("BlockEntityTag", tag);
            }
        }

        return stack;
    }

    public LootItemFunctionType getType() {
        return BnCLootFunctions.COPY_DRINK.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyDrinkFunction> {
        public Serializer() {}

        public CopyDrinkFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new CopyDrinkFunction(conditions);
        }
    }
}