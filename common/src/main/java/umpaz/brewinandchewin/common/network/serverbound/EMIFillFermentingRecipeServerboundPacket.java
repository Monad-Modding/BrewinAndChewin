package umpaz.brewinandchewin.common.network.serverbound;

import com.google.common.collect.Lists;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.integration.emi.handler.KegEmiRecipeHandler;
import umpaz.brewinandchewin.platform.BnCPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Code here has been modified from EMI internals.
 * <br>
 * EMI is licensed under the MIT license.
 * <a href="https://github.com/emilyploszaj/emi/blob/1.21/LICENSE">You may read the license here.</a>
 */
public record EMIFillFermentingRecipeServerboundPacket(int syncId,
                                                       Map<KegEmiRecipeHandler.InputType, List<ItemStack>> stacks) implements BnCPacket {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("emi_fill_fermenting_recipe");

    public EMIFillFermentingRecipeServerboundPacket(KegMenu menu, Map<KegEmiRecipeHandler.InputType, List<ItemStack>> stacks) {
        this(menu.containerId, stacks);
    }

    public EMIFillFermentingRecipeServerboundPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), new HashMap<>());
        int mapSize = buf.readInt();
        for (int i = 0; i < mapSize; i++) {
            KegEmiRecipeHandler.InputType type = buf.readEnum(KegEmiRecipeHandler.InputType.class);
            int listSize = buf.readInt();
            List<ItemStack> stacksList = new ArrayList<>(listSize);
            for (int j = 0; j < listSize; j++) {
                stacksList.add(buf.readItem());
            }
            this.stacks.put(type, stacksList);
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.syncId);
        buf.writeInt(this.stacks.size());
        for (Map.Entry<KegEmiRecipeHandler.InputType, List<ItemStack>> entry : stacks.entrySet()) {
            buf.writeEnum(entry.getKey());
            List<ItemStack> list = entry.getValue();
            buf.writeInt(list.size());
            for (ItemStack stack : list) {
                buf.writeItem(stack);
            }
        }
    }

    public void handle(ServerPlayer sender) {
        sender.server.execute(() -> {
            if (!BrewinAndChewin.getHelper().isModLoaded("emi"))
                return;
            AbstractContainerMenu menu = sender.containerMenu;
            if (menu.containerId != syncId || !(menu instanceof KegMenu kegMenu)) {
                BrewinAndChewin.LOG.error("Attempted to transfer fermenting recipe to an incorrect menu");
                return;
            }

            List<ItemStack> rubble = Lists.newArrayList();

            try {
                if (stacks.containsKey(KegEmiRecipeHandler.InputType.EMPTY)) {
                    List<Slot> crafting = List.of(menu.getSlot(4));
                    for (Slot s : crafting) {
                        if (s != null && s.mayPickup(sender) && !s.getItem().isEmpty()) {
                            ItemStack taken = s.getItem();
                            rubble.add(taken.copy());
                            s.setByPlayer(ItemStack.EMPTY);
                            s.onTake(sender, taken);
                        }
                    }

                    for (ItemStack stack : stacks.get(KegEmiRecipeHandler.InputType.EMPTY)) {
                        if (stack.isEmpty())
                            continue;

                        int gotten = grabMatching(kegMenu, sender, crafting, rubble, stack);
                        if (gotten != stack.getCount()) {
                            if (gotten > 0) {
                                stack.setCount(gotten);
                                sender.getInventory().placeItemBackInInventory(stack);
                            }
                            break;
                        } else {
                            for (ItemStack items : kegMenu.blockEntity.extractInGui(stack, gotten))
                                sender.getInventory().placeItemBackInInventory(items);
                        }
                    }
                }

                if (stacks.containsKey(KegEmiRecipeHandler.InputType.FILL)) {
                    List<Slot> crafting = List.of(menu.getSlot(4));
                    for (Slot s : crafting) {
                        if (s != null && s.mayPickup(sender) && !s.getItem().isEmpty()) {
                            ItemStack taken = s.getItem();
                            rubble.add(taken.copy());
                            s.setByPlayer(ItemStack.EMPTY);
                            s.onTake(sender, taken);
                        }
                    }

                    for (ItemStack stack : stacks.get(KegEmiRecipeHandler.InputType.FILL)) {
                        if (stack.isEmpty())
                            continue;

                        int gotten = grabMatching(kegMenu, sender, crafting, rubble, stack);
                        if (gotten != stack.getCount()) {
                            if (gotten > 0) {
                                stack.setCount(gotten);
                                sender.getInventory().placeItemBackInInventory(stack);
                            }
                            break;
                        } else {
                            for (ItemStack items : kegMenu.blockEntity.extractInGui(stack, gotten))
                                sender.getInventory().placeItemBackInInventory(items);
                        }
                    }
                }

                if (stacks.containsKey(KegEmiRecipeHandler.InputType.ITEM)) {
                    List<Slot> crafting = menu.slots.subList(0, 3);
                    for (Slot s : crafting) {
                        if (s != null && s.mayPickup(sender) && !s.getItem().isEmpty()) {
                            ItemStack taken = s.getItem();
                            rubble.add(taken.copy());
                            s.setByPlayer(ItemStack.EMPTY);
                            s.onTake(sender, taken);
                        }
                    }

                    for (int i = 0; i < stacks.get(KegEmiRecipeHandler.InputType.ITEM).size(); ++i) {
                        ItemStack stack = stacks.get(KegEmiRecipeHandler.InputType.ITEM).get(i);
                        if (stack.isEmpty())
                            continue;

                        int gotten = grabMatching(kegMenu, sender, crafting, rubble, stack);
                        if (gotten != stack.getCount()) {
                            if (gotten > 0) {
                                stack.setCount(gotten);
                                sender.getInventory().placeItemBackInInventory(stack);
                            }
                            break;
                        } else {
                            Slot s = menu.getSlot(i);
                            if (s.mayPlace(stack) && stack.getCount() <= s.getMaxStackSize())
                                s.setByPlayer(stack);
                            else
                                sender.getInventory().placeItemBackInInventory(stack);
                        }
                    }
                }
            } finally {
                for (ItemStack stack : rubble) {
                    sender.getInventory().placeItemBackInInventory(stack);
                }
            }
        });
    }

    private static int grabMatching(KegMenu menu, Player player, List<Slot> crafting, List<ItemStack> rubble, ItemStack stack) {
        int amount = stack.getCount();
        int grabbed = 0;

        for (int i = 0; i < rubble.size(); i++) {
            if (grabbed >= amount) {
                return grabbed;
            }
            ItemStack r = rubble.get(i);
            if (ItemStack.isSameItemSameTags(stack, r)) {
                int wanted = amount - grabbed;
                if (r.getCount() <= wanted) {
                    grabbed += r.getCount();
                    rubble.remove(i);
                    i--;
                } else {
                    grabbed = amount;
                    r.setCount(r.getCount() - wanted);
                }
            }
        }
        for (Slot s : menu.slots) {
            if (grabbed >= amount) {
                return grabbed;
            }
            if (crafting.contains(s) || !s.mayPickup(player)) {
                continue;
            }
            ItemStack st = s.getItem();
            if (ItemStack.isSameItemSameTags(stack, st)) {
                int wanted = amount - grabbed;
                ItemStack taken = st.copy();
                if (st.getCount() <= wanted) {
                    grabbed += st.getCount();
                    s.setByPlayer(ItemStack.EMPTY);
                } else {
                    grabbed = amount;
                    st.setCount(st.getCount() - wanted);
                }
                s.onTake(player, taken);
            }
        }
        return grabbed;
    }
}
