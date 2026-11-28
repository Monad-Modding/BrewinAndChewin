package umpaz.brewinandchewin.client.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.IdentifierException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class BnCFluidItemDisplays {
    private static final Map<Either<TagKey<Fluid>, Fluid>, FluidBasedItemStack> FLUID_TYPE_TO_ITEM_MAP = new HashMap<>();

    public static ItemStack getFluidItemDisplay(HolderLookup.Provider lookup, AbstractedFluidStack fluid) {
        if (FLUID_TYPE_TO_ITEM_MAP.containsKey(Either.right(fluid.fluid())))
            return FLUID_TYPE_TO_ITEM_MAP.get(Either.right(fluid.fluid())).getStack(lookup, fluid);
        Optional<Map.Entry<Either<TagKey<Fluid>, Fluid>, FluidBasedItemStack>> validTagKey = FLUID_TYPE_TO_ITEM_MAP.entrySet().stream().filter(entry -> entry.getKey().map(fluidTagKey -> fluid.fluid().is(fluidTagKey), fluid1 -> false)).findFirst();
        if (validTagKey.isPresent())
            return validTagKey.get().getValue().getStack(lookup, fluid);
        if (fluid.fluid().getBucket() != Items.AIR)
            return fluid.fluid().getBucket().getDefaultInstance();
        return ItemStack.EMPTY;
    }

    public static class Loader extends SimplePreparableReloadListener<Map<Either<TagKey<Fluid>, Fluid>, FluidBasedItemStack>> implements IdentifiableListener {
        public static final Loader INSTANCE = new Loader();
        private static final Gson GSON = new GsonBuilder().create();

        protected Loader() {}

        @Override
        protected Map<Either<TagKey<Fluid>, Fluid>, FluidBasedItemStack> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
            FileToIdConverter fileToIdConverter = FileToIdConverter.json("brewinandchewin/fluid_item_displays");
            FluidBasedItemStack.CACHE.clear();
            Map<Either<TagKey<Fluid>, Fluid>, FluidBasedItemStack> map = new HashMap<>();
            for (Map.Entry<Identifier, List<Resource>> entry : fileToIdConverter.listMatchingResourceStacks(resourceManager).entrySet()) {
                for (Resource resource : entry.getValue()) {
                    try (Reader reader = resource.openAsReader()) {
                        JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                        for (var e : jsonObject.entrySet()) {
                            Either<TagKey<Fluid>, Fluid> either;
                            boolean isCurrentOptional = e.getValue().isJsonObject() && e.getValue().getAsJsonObject().has("optional") && e.getValue().getAsJsonObject().get("optional").getAsBoolean();
                            if (e.getKey().startsWith("#")) {
                                either = Either.left(TagKey.create(Registries.FLUID, Identifier.parse(e.getKey().substring(1))));
                            } else {
                                Identifier fluidLocation = Identifier.parse(e.getKey());
                                if (!BuiltInRegistries.FLUID.containsKey(fluidLocation)) {
                                    if (isCurrentOptional)
                                        continue;
                                    BrewinAndChewin.LOG.error("Could not find fluid '{}' from fluid item display JSON at location '{}' from pack '{}'.", e.getKey(), entry.getKey(), resource.sourcePackId());
                                    continue;
                                }
                                either = Either.right(BuiltInRegistries.FLUID.getValue(fluidLocation));
                            }
                            try {
                                map.put(either, FluidBasedItemStack.createFromJson(e.getValue(), either));
                            } catch (RuntimeException ex) {
                                if (!isCurrentOptional)
                                    BrewinAndChewin.LOG.error("Couldn't parse fluid item display JSON at location '{}' from pack '{}'. ", entry.getKey(), resource.sourcePackId(), ex);
                            }
                        }
                    } catch (RuntimeException | IOException ex) {
                        BrewinAndChewin.LOG.error("Couldn't parse fluid item display JSON at location '{}' from pack '{}'. ", entry.getKey(), resource.sourcePackId(), ex);
                    }
                }
            }
            return map;
        }

        @Override
        protected void apply(Map<Either<TagKey<Fluid>, Fluid>, FluidBasedItemStack> obj, ResourceManager resourceManager, ProfilerFiller profiler) {
            FLUID_TYPE_TO_ITEM_MAP.clear();
            FLUID_TYPE_TO_ITEM_MAP.putAll(obj);
        }

        @Override
        public Identifier getId() {
            return BrewinAndChewin.asResource("coaster_models");
        }
    }

    public record FluidBasedItemStack(Either<TagKey<Fluid>, Fluid> fluid, FluidItemComponentRemapper dataComponentRemapper) {
        private static final HashMap<Pair<Fluid, DataComponentMap>, ItemStack> CACHE = new HashMap<>(32);

        private static FluidBasedItemStack createFromJson(JsonElement json, Either<TagKey<Fluid>, Fluid> fluid) {
            return new FluidBasedItemStack(fluid, FluidItemComponentRemapper.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst());
        }

        private ItemStack getStack(HolderLookup.Provider lookup, AbstractedFluidStack stack) {
            var pair = Pair.of(stack.fluid(), stack.components());
            if (CACHE.containsKey(pair))
                return CACHE.get(pair);

            ItemStack item = dataComponentRemapper.convert(lookup, stack);
            CACHE.put(pair, item);
            return item;
        }
    }
}
