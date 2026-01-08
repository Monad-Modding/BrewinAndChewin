package umpaz.brewinandchewin.common.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.access.LootParamsParamSetAccess;
import umpaz.brewinandchewin.common.mixin.LootContextAccessor;
import umpaz.brewinandchewin.common.mixin.LootParamsAccessor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class AreaLocationCheckCondition implements LootItemCondition {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("area_location_check");
    public static final LootItemConditionType TYPE = new LootItemConditionType(null);

    protected final List<LootItemCondition> terms;
    private final Predicate<LootContext> composedPredicate;
    private final int range;

    protected AreaLocationCheckCondition(List<LootItemCondition> predicates, int range) {
        this.terms = predicates;
        this.composedPredicate = lootContext ->
                this.terms.stream().allMatch(condition -> condition.test(lootContext));
        this.range = range;
    }

    public LootItemConditionType getType() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext context) {
        Vec3 vec3 = context.getParamOrNull(LootContextParams.ORIGIN);
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    Vec3 offset = vec3.add(x, y, z);
                    LootParams.Builder paramBuilder = new LootParams.Builder(context.getLevel());
                    LootParams originalParams = ((LootContextAccessor)context).brewinandchewin$getParams();
                    for (Map.Entry<LootContextParam<?>, Object> entry : ((LootParamsAccessor)originalParams).brewinandchewin$getParams().entrySet()) {
                        paramBuilder.withParameter((LootContextParam) entry.getKey(), entry.getValue());
                    }
                    paramBuilder.withParameter(LootContextParams.ORIGIN, offset);
                    if (context.hasParam(LootContextParams.BLOCK_STATE))
                        paramBuilder.withOptionalParameter(LootContextParams.BLOCK_STATE, context.getLevel().getBlockState(BlockPos.containing(offset)));
                    if (context.hasParam(LootContextParams.BLOCK_ENTITY))
                        paramBuilder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, context.getLevel().getBlockEntity(BlockPos.containing(offset)));

                    LootParams newParams = paramBuilder.create(((LootParamsParamSetAccess) originalParams).brewinandchewin$getParamSet());

                    LootContext newCtx = new LootContext.Builder(newParams).create(ID);
                    if (composedPredicate.test(newCtx))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ORIGIN);
    }

    @Override
    public void validate(ValidationContext pContext) {
        LootItemCondition.super.validate(pContext);

        for(int i = 0; i < terms.size(); ++i)
            this.terms.get(i).validate(pContext.forChild(".term[" + i + "]"));

    }

    public static LootItemCondition.Builder checkArea(int range, LootItemCondition.Builder... predicateBuilder) {
        return () -> new AreaLocationCheckCondition(Arrays.stream(predicateBuilder).map(Builder::build).toList(), range);
    }
}