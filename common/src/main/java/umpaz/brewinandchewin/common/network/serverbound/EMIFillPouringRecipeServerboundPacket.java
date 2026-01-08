package umpaz.brewinandchewin.common.network.serverbound;

import com.google.common.collect.Lists;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.platform.BnCPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * Code here has been modified from EMI internals.
 * <br>
 * EMI is licensed under the MIT license.
 * <a href="https://github.com/emilyploszaj/emi/blob/1.21/LICENSE">You may read the license here.</a>
 */
public record EMIFillPouringRecipeServerboundPacket(int syncId, int action, List<ItemStack> stacks) implements BnCPacket {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("emi_fill_pouring_recipe");

    public EMIFillPouringRecipeServerboundPacket(KegMenu menu, int action, List<ItemStack> stacks) {
        this(menu.containerId, action, stacks);
    }

    public EMIFillPouringRecipeServerboundPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readByte(), new ArrayList<>());

        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            this.stacks.add(buf.readItem());
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.syncId);
        buf.writeByte(this.action);

        buf.writeInt(this.stacks.size());
        for (ItemStack stack : this.stacks) {
            buf.writeItem(stack);
        }
    }

    public void handle(ServerPlayer sender) {
        sender.getServer().execute(() -> {
            if (!BrewinAndChewin.getHelper().isModLoaded("emi"))
                return;
            AbstractContainerMenu menu = sender.containerMenu;
            if (menu.containerId != syncId || !(menu instanceof KegMenu kegMenu)) {
                BrewinAndChewin.LOG.error("Attempted to transfer fermenting recipe to an incorrect menu");
                return;
            }

            List<ItemStack> rubble = Lists.newArrayList();

            try {
                for (ItemStack stack : stacks) {
                    if (stack.isEmpty())
                        continue;

                    int gotten = grabMatching(kegMenu, sender, List.of(menu.getSlot(4)), rubble, stack);
                    if (gotten != stack.getCount()) {
                        if (gotten > 0) {
                            stack.setCount(gotten);
                            sender.getInventory().placeItemBackInInventory(stack);
                        }
                        break;
                    } else {
                        Slot s = menu.getSlot(KegBlockEntity.OUTPUT_SLOT);
                        for (ItemStack item : kegMenu.blockEntity.extractInGui(stack, gotten)) {
                            if ((s.getItem().isEmpty() || ItemStack.isSameItemSameTags(item, s.getItem())) && s.getItem().getCount() < item.getMaxStackSize() && s.getItem().getCount() + item.getCount() < s.getMaxStackSize())
                                s.set(item);
                            else
                                sender.getInventory().placeItemBackInInventory(item);
                        }
                    }
                    if (action == 1) {
                        menu.clicked(KegBlockEntity.OUTPUT_SLOT, 0, ClickType.PICKUP, sender);
                    } else if (action == 2) {
                        menu.clicked(KegBlockEntity.OUTPUT_SLOT, 0, ClickType.QUICK_MOVE, sender);
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
