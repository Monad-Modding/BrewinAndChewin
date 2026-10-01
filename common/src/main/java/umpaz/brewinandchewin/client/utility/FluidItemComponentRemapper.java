package umpaz.brewinandchewin.client.utility;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record FluidItemComponentRemapper(ItemStack baseItem,
                                         Map<DataComponentType<?>, Pair<DataComponentType<?>, Map<List<TagReference>, List<TagReference>>>> map) {
    public static final Codec<FluidItemComponentRemapper> DIRECT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ItemStack.STRICT_SINGLE_ITEM_CODEC.fieldOf("base").forGetter(FluidItemComponentRemapper::baseItem),
            Codec.unboundedMap(BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec(), ValueCodec.INSTANCE).optionalFieldOf("remaps", Map.of()).forGetter(FluidItemComponentRemapper::map)
    ).apply(inst, FluidItemComponentRemapper::new));
    public static final Codec<FluidItemComponentRemapper> CODEC = Codec.withAlternative(DIRECT_CODEC, Codec.withAlternative(ItemStack.STRICT_SINGLE_ITEM_CODEC, BuiltInRegistries.ITEM.byNameCodec(), ItemStack::new), stack -> new FluidItemComponentRemapper(stack, Map.of()));

    public ItemStack convert(HolderLookup.Provider lookup, AbstractedFluidStack fluid) throws IllegalStateException {
        ItemStack stack = baseItem.copy();

        if (fluid.components().isEmpty())
            return stack;

        RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, lookup);
        CompoundTag fluidTag = (CompoundTag) DataComponentMap.CODEC.encodeStart(registryOps, fluid.components()).getOrThrow();
        CompoundTag itemTag = (CompoundTag) DataComponentMap.CODEC.encodeStart(registryOps, stack.getComponents()).getOrThrow();

        DataComponentPatch patch = DataComponentPatch.CODEC.decode(registryOps, encodeTag(fluidTag, itemTag)).getOrThrow().getFirst();
        stack.applyComponents(patch);

        return stack;
    }

    private CompoundTag encodeTag(CompoundTag fluidTag, CompoundTag itemTag) {
        if (map.isEmpty())
            return itemTag;

        for (var entry : map.entrySet()) {
            Tag current;
            if (entry.getValue().getSecond().isEmpty()) {
                current = itemTag;
                if (current instanceof CompoundTag tag) {
                    var id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(entry.getValue().getFirst());
                    if (id == null)
                        // TODO: PUt proper error handling here.
                        continue;
                    var innerFluidTag = fluidTag.get(id.toString());
                    if (innerFluidTag == null)
                        continue;
                    tag.put(id.toString(), innerFluidTag);
                }
            }
            for (int i = 0; i < entry.getValue().getSecond().size(); ++i) {
                current = itemTag.copy();

                for (var referenceEntry : entry.getValue().getSecond().entrySet()) {
                    List<TagReference> fluidKeys = referenceEntry.getKey();
                    List<TagReference> itemKeys = referenceEntry.getValue();
                    for (int j = 0; j < itemKeys.size(); ++j) {
                        TagReference key = itemKeys.get(j);
                        if (i == itemKeys.size() - 1) {
                            Tag newElement = getTag(fluidTag, fluidKeys);
                            if (newElement instanceof ListTag) {
                                if (current instanceof ListTag listTag) {
                                    listTag.set(key.index(), newElement);
                                } else {
                                    BrewinAndChewin.LOG.error("Unable to map tag {}. Tag {} is not a list tag.", String.join(".", fluidKeys.stream().map(TagReference::key).toList()), String.join(".", itemKeys.stream().map(TagReference::key).toList()));
                                }
                            } else {
                                if (current instanceof CompoundTag compoundTag) {
                                    compoundTag.put(key.key(), newElement);
                                } else {
                                    BrewinAndChewin.LOG.error("Unable to map tag {}. Tag {} is not a compound tag.", String.join(".", fluidKeys.stream().map(TagReference::key).toList()), String.join(".", itemKeys.stream().map(TagReference::key).toList()));
                                }
                            }
                            break;
                        }
                        if (key.isArrayValue())
                            if (current instanceof ListTag listTag) {
                                if (listTag.size() < key.index()) {
                                    current = listTag.get(key.index());
                                } else {
                                    BrewinAndChewin.LOG.error("Unable to find tag {} in output json.", String.join(".", itemKeys.stream().map(TagReference::key).toList()));
                                }
                            } else {
                                BrewinAndChewin.LOG.error("Unable to map tag {}. Tag {} is not a list tag.", String.join(".", fluidKeys.stream().map(TagReference::key).toList()), String.join(".", itemKeys.subList(0, i).stream().map(TagReference::key).toList()));
                            }
                        else {
                            if (current instanceof CompoundTag compoundTag) {
                                if (compoundTag.contains(key.key())) {
                                    current = compoundTag.get(key.key());
                                } else {
                                    BrewinAndChewin.LOG.error("Unable to find tag {} in output json.", String.join(".", itemKeys.stream().map(TagReference::key).toList()));
                                }
                            } else {
                                BrewinAndChewin.LOG.error("Unable to map tag {}. Tag {} is not a compound tag.", String.join(".", fluidKeys.stream().map(TagReference::key).toList()), String.join(".", String.join(".", itemKeys.subList(0, i).stream().map(TagReference::key).toList())));
                            }
                        }
                    }
                }
            }
        }

        return itemTag;
    }

    private Tag getTag(Tag keyTag, List<TagReference> references) {
        Tag tag = EndTag.INSTANCE;
        for (int i = 0; i < references.size(); ++i) {
            TagReference key = references.get(i);
            if (i == references.size() - 1) {
                if (key.isArrayValue()) {
                    if (keyTag instanceof ListTag listTag) {
                        if (listTag.size() < key.index()) {
                            tag = listTag.get(key.index());
                        } else {
                            throw new RuntimeException("Unable to find tag " + String.join(".", references.stream().map(TagReference::key).toList()) + " in fluid stack.");
                        }
                    } else {
                        throw new RuntimeException("Unable to map tag " + String.join(".", references.stream().map(TagReference::key).toList()) + ". Tag " + String.join(".", references.stream().map(TagReference::key).toList()) + " is not a list tag.");
                    }
                } else {
                    if (keyTag instanceof CompoundTag compoundTag) {
                        if (compoundTag.contains(key.key())) {
                            tag = compoundTag.get(key.key());
                        } else {
                            throw new RuntimeException("Unable to find tag " + String.join(".", references.stream().map(TagReference::key).toList()) + " in fluid stack.");
                        }
                    } else {
                        throw new RuntimeException("Unable to map tag " + String.join(".", references.stream().map(TagReference::key).toList()) + ". Tag " + String.join(".", references.stream().map(TagReference::key).toList()) + " is not a compound tag.");
                    }
                }
                break;
            }
            if (key.isArrayValue())
                if (keyTag instanceof ListTag listTag) {
                    if (listTag.size() < key.index()) {
                        tag = listTag.get(key.index());
                    } else {
                        throw new RuntimeException("Unable to find tag " + String.join(".", references.subList(0, i).stream().map(TagReference::key).toList()) + " in fluid stack.");
                    }
                } else {
                    throw new RuntimeException("Unable to map tag " + String.join(".", references.subList(0, i).stream().map(TagReference::key).toList()) + ". Tag " + String.join(".", references.subList(0, i).stream().map(TagReference::key).toList()) + " is not a list tag.");
                }
            else {
                if (keyTag instanceof CompoundTag compoundTag) {
                    if (compoundTag.contains(key.key())) {
                        tag = compoundTag.get(key.key());
                    } else {
                        throw new RuntimeException("Unable to find tag " + String.join(".", references.subList(0, i).stream().map(TagReference::key).toList()) + " in fluid stack.");
                    }
                } else {
                    throw new RuntimeException("Unable to map tag " + String.join(".", references.subList(0, i).stream().map(TagReference::key).toList()) + ". Tag " + String.join(".", references.subList(0, i).stream().map(TagReference::key).toList()) + " is not a compound tag.");
                }
            }
        }
        return tag;
    }

    static class ValueCodec implements Codec<Pair<DataComponentType<?>, Map<List<TagReference>, List<TagReference>>>> {
        private static final ValueCodec INSTANCE = new ValueCodec();

        protected ValueCodec() {
        }

        @Override
        public <T> DataResult<Pair<Pair<DataComponentType<?>, Map<List<TagReference>, List<TagReference>>>, T>> decode(DynamicOps<T> ops, T input) {
            MapLike<T> map = ops.getMap(input).getOrThrow();

            var componentTypeResult = BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().decode(ops, map.get("component"));
            if (componentTypeResult.isError()) {
                return DataResult.error(() -> "Failed to decode 'component' field within fluid to item remapping.");
            }
            DataComponentType<?> componentType = componentTypeResult.getOrThrow().getFirst();

            Map<List<TagReference>, List<TagReference>> references = new HashMap<>();

            if (map.entries().anyMatch(p -> p.getFirst() == ops.createString("functions"))) {
                if (!ops.getStream(map.get("functions")).isError()) {
                    List<T> functions = ops.getStream(map.get("functions")).getOrThrow().toList();
                    for (T innerFunction : functions) {
                        var dataResult = getReferences(ops, innerFunction);
                        if (dataResult.isError()) {
                            BrewinAndChewin.LOG.error("Failed to decode remap value within fluid to item remapping: {}", dataResult.error().get().message());
                            continue;
                        }
                        references.putAll(dataResult.getOrThrow());
                    }
                } else {
                    var dataResult = getReferences(ops, map.get("functions"));
                    if (dataResult.isSuccess())
                        references.putAll(dataResult.getOrThrow());
                    else
                        BrewinAndChewin.LOG.error("Failed to decode remap value within fluid to item remapping: {}", dataResult.error().get().message());
                }
            }

            return DataResult.success(Pair.of(Pair.of(componentType, references), input));
        }

        private static <T> DataResult<Map<List<TagReference>, List<TagReference>>> getReferences(DynamicOps<T> ops, T input) {
            Map<List<TagReference>, List<TagReference>> map = new HashMap<>();
            if (!ops.getMap(input).isError()) {
                List<TagReference> fromValues;
                MapLike<T> innerMap = ops.getMap(input).getOrThrow();
                T from = innerMap.get("from");
                if (!ops.getStringValue(from).isError()) {
                    fromValues = TagReference.createFromString(ops.getStringValue(from).getOrThrow());
                } else
                    return DataResult.error(() -> "Failed to decode 'from' value. Must be a string that points to a NBT path.\n\nObjects are denoted with '.' (for example 'root.substring') and array values are denoted with '[<index_number>]' (for example '[0]').");

                T to = innerMap.get("to");
                if (!ops.getStringValue(to).isError()) {
                    map.put(fromValues, TagReference.createFromString(ops.getStringValue(to).getOrThrow()));
                } else
                    return DataResult.error(() -> "Failed to decode 'to' value. Must be a string that points to a NBT path.\n\nObjects are denoted with '.' (for example 'root.substring') and array values are denoted with '[<index_number>]' (for example '[0]').");
            }
            return DataResult.success(ImmutableMap.copyOf(map));
        }

        @Override
        public <T> DataResult<T> encode(Pair<DataComponentType<?>, Map<List<TagReference>, List<TagReference>>> input, DynamicOps<T> ops, T prefix) {
            Map<T, T> map = new HashMap<>();

            map.put(ops.createString("component"), BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().encodeStart(ops, input.getFirst()).getOrThrow());

            List<T> functionList = new ArrayList<>();
            for (var entry : input.getSecond().entrySet()) {
                Map<T, T> functionMap = new HashMap<>();
                map.put(ops.createString("from"), ops.createString(entry.getKey().stream().map(TagReference::key).collect(Collectors.joining("."))));
                map.put(ops.createString("to"), ops.createString(entry.getValue().stream().map(TagReference::key).collect(Collectors.joining("."))));
                functionList.add(ops.createMap(functionMap));
            }

            if (!functionList.isEmpty())
                map.put(ops.createString("functions"), ops.createList(functionList.stream()));

            return DataResult.success(ops.createMap(map));
        }
    }
}
