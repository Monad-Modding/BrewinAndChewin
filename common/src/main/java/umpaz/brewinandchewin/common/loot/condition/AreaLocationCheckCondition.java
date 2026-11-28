package umpaz.brewinandchewin.common.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
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
    public static final MapCodec<AreaLocationCheckCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            LootItemCondition.DIRECT_CODEC.listOf().fieldOf("terms").forGetter(cond -> cond.terms),
            ExtraCodecs.POSITIVE_INT.fieldOf("range").forGetter(cond -> cond.range)
    ).apply(inst, AreaLocationCheckCondition::new));

    public static final Identifier ID = BrewinAndChewin.asResource("area_location_check");

    protected final List<LootItemCondition> terms;
    private final Predicate<LootContext> composedPredicate;
    private final int range;

    protected AreaLocationCheckCondition(List<LootItemCondition> predicates, int range) {
        this.terms = predicates;
        this.composedPredicate = Util.allOf(terms);
        this.range = range;
    }

    @Override
    public MapCodec<AreaLocationCheckCondition> codec() {
        return CODEC;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean test(LootContext context) {
        Vec3 vec3 = context.getOptional(LootContextParams.ORIGIN);
        if (vec3 == null)
            return false;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    Vec3 offset = vec3.add(x, y, z);
                    LootParams.Builder paramBuilder = new LootParams.Builder(context.getLevel());
                    LootParams originalParams = ((LootContextAccessor)context).brewinandchewin$getParams();
                    for (Map.Entry<ContextKey<?>, Object> entry : ((LootParamsAccessor)(Object)originalParams.contextMap()).brewinandchewin$getParams().entrySet()) {
                        paramBuilder.withParameter((ContextKey) entry.getKey(), entry.getValue());
                    }
                    paramBuilder.withParameter(LootContextParams.ORIGIN, offset);
                    if (context.hasParameter(LootContextParams.BLOCK_STATE))
                        paramBuilder.withOptionalParameter(LootContextParams.BLOCK_STATE, context.getLevel().getBlockState(BlockPos.containing(offset)));
                    if (context.hasParameter(LootContextParams.BLOCK_ENTITY))
                        paramBuilder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, context.getLevel().getBlockEntity(BlockPos.containing(offset)));
                    LootContext newCtx = new LootContext.Builder(paramBuilder.create(((LootParamsParamSetAccess) originalParams).brewinandchewin$getParamSet())).create(Optional.empty());
                    if (composedPredicate.test(newCtx))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ORIGIN);
    }

    @Override
    public void validate(ValidationContext pContext) {
        LootItemCondition.super.validate(pContext);

        for(int i = 0; i < terms.size(); ++i) {
            final int idx = i;
            this.terms.get(i).validate(pContext.forChild(() -> ".term[" + idx + "]"));
        }

    }

    public static LootItemCondition.Builder checkArea(int range, LootItemCondition.Builder... predicateBuilder) {
        return () -> new AreaLocationCheckCondition(Arrays.stream(predicateBuilder).map(Builder::build).toList(), range);
    }
}