package umpaz.brewinandchewin.neoforge.loot.modifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import umpaz.brewinandchewin.BrewinAndChewin;

import javax.annotation.Nonnull;

public class BnCAddTableModifier extends LootModifier {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("add_table");
    public static final MapCodec<BnCAddTableModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
                    .and(ResourceLocation.CODEC.fieldOf("table").forGetter(modifier -> modifier.table))
                    .apply(inst, BnCAddTableModifier::new));

    private final ResourceLocation table;

    protected BnCAddTableModifier(LootItemCondition[] conditions, ResourceLocation table) {
        super(conditions);
        this.table = table;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        LootTable extra = context.getResolver()
                .get(Registries.LOOT_TABLE, ResourceKey.create(Registries.LOOT_TABLE, this.table))
                .<LootTable>map(Holder::value)
                .orElse(LootTable.EMPTY);
        LootParams params = new LootParams.Builder(context.getLevel()).create(extra.getParamSet());
        extra.getRandomItems(params, context.getRandom().nextLong(), generatedLoot::add);
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
