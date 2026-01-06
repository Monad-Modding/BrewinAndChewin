package umpaz.brewinandchewin.client.utility;

import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
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
            for (Map.Entry<ResourceLocation, List<Resource>> entry : fileToIdConverter.listMatchingResourceStacks(resourceManager).entrySet()) {
                for (Resource resource : entry.getValue()) {
                    try (Reader reader = resource.openAsReader()) {
                        JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                        for (var e : jsonObject.entrySet()) {
                            Either<TagKey<Fluid>, Fluid> either;
                            boolean isCurrentOptional = e.getValue().isJsonObject() && e.getValue().getAsJsonObject().has("optional") && e.getValue().getAsJsonObject().get("optional").getAsBoolean();
                            if (e.getKey().startsWith("#")) {
                                either = Either.left(TagKey.create(Registries.FLUID, new ResourceLocation(e.getKey().substring(1))));
                            } else {
                                ResourceLocation fluidLocation = new ResourceLocation(e.getKey());
                                if (!BuiltInRegistries.FLUID.containsKey(fluidLocation)) {
                                    if (isCurrentOptional)
                                        continue;
                                    BrewinAndChewin.LOG.error("Could not find fluid '{}' from fluid item display JSON at location '{}' from pack '{}'.", e.getKey(), entry.getKey(), resource.sourcePackId());
                                    continue;
                                }
                                either = Either.right(BuiltInRegistries.FLUID.get(fluidLocation));
                            }
                            try {
                                map.put(either, FluidBasedItemStack.createFromJson(e.getValue(), either));
                            } catch (IllegalArgumentException | IllegalStateException |
                                     JsonParseException | ResourceLocationException ex) {
                                if (!isCurrentOptional)
                                    BrewinAndChewin.LOG.error("Couldn't parse fluid item display JSON at location '{}' from pack '{}'. ", entry.getKey(), resource.sourcePackId(), ex);
                            }
                        }
                    } catch (IllegalArgumentException | IllegalStateException | IOException | JsonParseException | ResourceLocationException ex) {
                        BrewinAndChewin.LOG.error("Couldn't parse fluid item display JSON at location '{}' from pack '{}'. ", entry.getKey(), resource.sourcePackId(), ex);
                    }
                }
            }
            return map;
        }

        @Override
        protected void apply(Map<Either<TagKey<Fluid>, Fluid>, FluidBasedItemStack> obj, ResourceManager resourceManager, ProfilerFiller profiler) {
            FLUID_TYPE_TO_ITEM_MAP.putAll(obj);
        }

        @Override
        public ResourceLocation getId() {
            return BrewinAndChewin.asResource("coaster_models");
        }
    }

    public record FluidBasedItemStack(Either<TagKey<Fluid>, Fluid> fluid, ItemStack baseItem) {
        private static final HashMap<Fluid, ItemStack> CACHE = new HashMap<>(32);

        private static FluidBasedItemStack createFromJson(JsonElement json, Either<TagKey<Fluid>, Fluid> fluid) {
            ItemStack base = ItemStack.EMPTY;

            try {
                if (json.isJsonObject()) {
                    JsonObject obj = json.getAsJsonObject();

                    // optional field is ignored here since Loader already handles skipping
                    if (obj.has("id")) {
                        String id = obj.get("id").getAsString();
                        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
                        if (item != null) {
                            base = new ItemStack(item);
                        }
                    }
                } else if (json.isJsonPrimitive()) {
                    JsonPrimitive prim = json.getAsJsonPrimitive();
                    String id = prim.getAsString();
                    Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
                    if (item != null) {
                        base = new ItemStack(item);
                    }
                }
            } catch (IllegalArgumentException | JsonParseException ex) {
                throw new IllegalArgumentException("Failed to parse fluid item display JSON", ex);
            }

            return new FluidBasedItemStack(fluid, new ItemStack(net.minecraft.world.item.Items.BUCKET));
        }

        private ItemStack getStack(HolderLookup.Provider lookup, AbstractedFluidStack stack) {
            var fluid = stack.fluid();
            if (CACHE.containsKey(fluid))
                return CACHE.get(fluid);

            ItemStack item = baseItem.copy();
            CACHE.put(fluid, item);
            return item;
        }
    }
}
