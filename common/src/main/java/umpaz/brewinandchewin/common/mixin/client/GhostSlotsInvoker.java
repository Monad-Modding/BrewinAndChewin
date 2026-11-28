package umpaz.brewinandchewin.common.mixin.client;

import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GhostSlots.class)
public interface GhostSlotsInvoker {
    @Invoker("setInput")
    void brewinandchewin$setInput(Slot slot, ContextMap context, SlotDisplay display);

    @Invoker("setResult")
    void brewinandchewin$setResult(Slot slot, ContextMap context, SlotDisplay display);
}
