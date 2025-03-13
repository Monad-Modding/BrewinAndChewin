package umpaz.brewinandchewin.common.tag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class BnCCompatTags {

    public static final TagKey<Item> ORIGINS_IGNORE_DIET = compatItemTag("origins", "ignore_diet");
    public static final TagKey<Item> ORIGINS_MEAT = compatItemTag("origins", "meat");

    public static final TagKey<Fluid> FORGE_HONEY = compatFluidTag("forge", "honey");


    private static TagKey<Fluid> compatFluidTag(String namespace, String path){
        return FluidTags.create(new ResourceLocation(namespace, path));
    }

    private static TagKey<Item> compatItemTag(String namespace, String path) {
        return ItemTags.create(new ResourceLocation(namespace, path));
    }


}
