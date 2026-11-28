package umpaz.brewinandchewin.fabric.container;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import vectorwing.farmersdelight.refabricated.inventory.ItemStackHandler;

public class KegItemHandlerFabric extends ItemStackHandler implements AbstractedItemHandler {
    public KegItemHandlerFabric(int size) {
        super(size);
    }

    @Override
    public void readFromValue(ValueInput input) {
        deserialize(input);
    }

    @Override
    public void writeToValue(ValueOutput output) {
        serialize(output);
    }
}
