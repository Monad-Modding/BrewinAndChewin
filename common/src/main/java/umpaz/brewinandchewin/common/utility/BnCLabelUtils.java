package umpaz.brewinandchewin.common.utility;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import umpaz.brewinandchewin.common.item.LabelItem;
import umpaz.brewinandchewin.common.item.component.LabelContents;
import umpaz.brewinandchewin.common.registry.BnCDataComponents;

import java.util.List;
import java.util.Optional;

public class BnCLabelUtils {
    public static final int DEFAULT_LABEL_COLOR = 0xF3E6C5;

    public static Optional<LabelContents> getLabel(ItemStack stack) {
        return Optional.ofNullable(stack.get(BnCDataComponents.LABEL));
    }

    public static void setLabel(ItemStack stack, LabelContents contents) {
        stack.set(BnCDataComponents.LABEL, contents);
    }

    public static void removeLabel(ItemStack stack) {
        stack.remove(BnCDataComponents.LABEL);
    }

    public static boolean canBeLabelled(ItemStack stack) {
        return !stack.isEmpty();
    }

    public static int getDyeColor(ItemStack stack) {
        DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
        return dyed == null ? -1 : dyed.rgb();
    }

    public static int getLabelColor(ItemStack stack) {
        Optional<LabelContents> contents = getLabel(stack);
        if (contents.isPresent() && contents.get().color().isPresent())
            return contents.get().color().get();
        return DyedItemColor.getOrDefault(stack, DEFAULT_LABEL_COLOR);
    }

    public static LabelContents readFromItem(ItemStack stack) {
        LabelContents contents = stack.getOrDefault(BnCDataComponents.LABEL, LabelContents.EMPTY);
        if (!(stack.getItem() instanceof LabelItem))
            return contents;
        DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
        return dyed != null ? contents.withColor(dyed.rgb()) : contents;
    }

    public static void appendLabelTooltip(ItemStack stack, List<Component> tooltip) {
        LabelContents label = readFromItem(stack);
        if (label.isBlank())
            return;
        int index = Math.min(1, tooltip.size());
        tooltip.add(index, label.getDisplayName());
        if (label.showAuthenticity() && label.generation() > 0)
            tooltip.add(index + 1, label.getGenerationName());
    }
}
