package umpaz.brewinandchewin.common.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import umpaz.brewinandchewin.BrewinAndChewin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Used for checking block states with a value whilst also returning true for null values.
 */
public class NullTrueBlockStateCondition implements LootItemCondition {
    public static final MapCodec<NullTrueBlockStateCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            PropertyMatcher.LIST_CODEC.fieldOf("state").forGetter(cond -> cond.matchers)
    ).apply(inst, NullTrueBlockStateCondition::new));

    public static final Identifier ID = BrewinAndChewin.asResource("null_true_block_state");

    private final List<PropertyMatcher> matchers;

    protected NullTrueBlockStateCondition(List<PropertyMatcher> matchers) {
        this.matchers = matchers;
    }

    @Override
    public MapCodec<NullTrueBlockStateCondition> codec() {
        return CODEC;
    }

    public boolean test(LootContext context) {
        BlockState blockState = context.getOptional(LootContextParams.BLOCK_STATE);
        if (blockState == null)
            return false;
        return matchers.stream().allMatch(propertyMatcher -> propertyMatcher.match(blockState.getBlock().getStateDefinition(), blockState));
    }

    public static Builder checkState(PropertyMatcher... matchers) {
        return () -> new NullTrueBlockStateCondition(Arrays.stream(matchers).toList());
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_STATE);
    }

    public static class PropertyMatcher {
        public static final Codec<List<PropertyMatcher>> LIST_CODEC = Codec.unboundedMap(Codec.STRING, ValueMatcher.CODEC).xmap(map -> {
            List<PropertyMatcher> list = new ArrayList<>();
            for (var entry : map.entrySet()) {
                list.add(new PropertyMatcher(entry.getKey(), entry.getValue()));
            }
            return list;
        }, propertyMatchers -> propertyMatchers.stream().map(propertyMatcher -> Pair.of(propertyMatcher.name, propertyMatcher.value)).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (valueMatcher, valueMatcher2) -> valueMatcher)));

        private final String name;
        private final ValueMatcher value;

        public PropertyMatcher(String pName, ValueMatcher value) {
            this.name = pName;
            this.value = value;
        }

        public static PropertyMatcher exact(String name, String value) {
            return new PropertyMatcher(name, new ExactMatcher(value));
        }

        public static PropertyMatcher min(String name, String min) {
            return new PropertyMatcher(name, new RangedMatcher(Optional.of(min), Optional.empty()));
        }

        public static PropertyMatcher max(String name, String max) {
            return new PropertyMatcher(name, new RangedMatcher(Optional.empty(), Optional.of(max)));
        }

        public static PropertyMatcher ranged(String name, String min, String max) {
            return new PropertyMatcher(name, new RangedMatcher(Optional.of(min), Optional.of(max)));
        }

        public <S extends StateHolder<?, S>> boolean match(StateDefinition<?, S> pProperties, S pPropertyToMatch) {
            Property<?> property = pProperties.getProperty(this.name);
            return property == null || value.match(pPropertyToMatch, property);
        }
    }

    public interface ValueMatcher {
        Codec<ValueMatcher> CODEC = Codec.either(ExactMatcher.CODEC, RangedMatcher.CODEC).xmap(Either::unwrap, valueMatcher -> {
            if (valueMatcher instanceof ExactMatcher exact)
                return Either.left(exact);
            if (valueMatcher instanceof RangedMatcher ranged)
                return Either.right(ranged);
            throw new UnsupportedOperationException("Could not map NullTrueBlockStateCondition.ValueMatcher to a specific instance.");
        });

        <S extends StateHolder<?, S>, T extends Comparable<T>> boolean match(S pProperties, Property<T> pPropertyTarget);
    }

    public record ExactMatcher(String value) implements ValueMatcher {
        public static final Codec<ExactMatcher> CODEC = Codec.STRING.xmap(ExactMatcher::new, ExactMatcher::value);
        public <S extends StateHolder<?, S>, T extends Comparable<T>> boolean match(S pProperties, Property<T> pProperty) {
            T t = pProperties.getValue(pProperty);
            Optional<T> optional = pProperty.getValue(value);
            return optional.isPresent() && t.compareTo(optional.get()) == 0;
        }
    }

    public record RangedMatcher(Optional<String> min, Optional<String> max) implements ValueMatcher {
        public static final Codec<RangedMatcher> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.optionalFieldOf("min").forGetter(RangedMatcher::min),
                Codec.STRING.optionalFieldOf("max").forGetter(RangedMatcher::max)
        ).apply(inst, RangedMatcher::new));

        @Override
        public <S extends StateHolder<?, S>, T extends Comparable<T>> boolean match(S pProperties, Property<T> pPropertyTarget) {
            T t = pProperties.getValue(pPropertyTarget);
            if (min.isPresent()) {
                Optional<T> optional = pPropertyTarget.getValue(min.get());
                if (optional.isEmpty() || t.compareTo(optional.get()) < 0) {
                    return false;
                }
            }

            if (max.isPresent()) {
                Optional<T> optional1 = pPropertyTarget.getValue(max.get());
                return optional1.isPresent() && t.compareTo(optional1.get()) <= 0;
            }

            return true;
        }
    }
}