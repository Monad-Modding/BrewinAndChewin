package umpaz.brewinandchewin.common.utility;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import umpaz.brewinandchewin.common.item.LabelItem;
import umpaz.brewinandchewin.common.item.component.LabelContents;
import umpaz.brewinandchewin.common.registry.BnCDataComponents;

import java.util.ArrayList;
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
        return dyed == null ? -1 : FastColor.ARGB32.opaque(dyed.rgb());
    }

    public static int getLabelColor(ItemStack stack) {
        Optional<LabelContents> contents = getLabel(stack);
        if (contents.isPresent() && contents.get().color().isPresent())
            return FastColor.ARGB32.opaque(contents.get().color().get());
        return FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(stack, DEFAULT_LABEL_COLOR));
    }

    public static LabelContents readFromItem(ItemStack stack) {
        LabelContents contents = stack.getOrDefault(BnCDataComponents.LABEL, LabelContents.EMPTY);
        if (!(stack.getItem() instanceof LabelItem))
            return contents;
        DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
        return dyed != null ? contents.withColor(dyed.rgb()) : contents;
    }

    public static boolean hidesEffects(ItemStack stack) {
        LabelContents label = stack.get(BnCDataComponents.LABEL);
        return label != null && label.hideEffects();
    }

    public static void appendLabelTooltip(ItemStack stack, List<Component> tooltip, float tickRate) {
        LabelContents label = readFromItem(stack);
        if (!label.isBlank()) {
            int index = Math.min(1, tooltip.size());
            tooltip.add(index, label.getDisplayName());
            if (label.showAuthenticity() && label.generation() > 0)
                tooltip.add(index + 1, label.getGenerationName());
        }
        if (hidesEffects(stack))
            removeEffectLines(stack, tooltip, tickRate);
    }

    private static void removeEffectLines(ItemStack stack, List<Component> tooltip, float tickRate) {
        PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
        if (potion == null)
            return;
        List<Component> effectLines = new ArrayList<>();
        PotionContents.addPotionTooltip(potion.getAllEffects(), effectLines::add, 1.0F, tickRate);
        effectLines.removeIf(line -> line.getString().isEmpty());
        tooltip.removeAll(effectLines);
    }
}
