package umpaz.brewinandchewin.common.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class BnCCompatTags {

    public static final TagKey<Item> ORIGINS_IGNORE_DIET = compatItemTag("origins", "ignore_diet");
    public static final TagKey<Item> ORIGINS_MEAT = compatItemTag("origins", "meat");
    public static final TagKey<Item> CREATE_UPRIGHT_ON_BELT = compatItemTag("create", "upright_on_belt");

    private static TagKey<Item> compatItemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, path));
    }
}
