package umpaz.brewinandchewin.common.network.serverbound;

import com.google.common.collect.Lists;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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

import java.util.List;

/**
 * Code here has been modified from EMI internals.
 * <br>
 * EMI is licensed under the MIT license.
 * <a href="https://github.com/emilyploszaj/emi/blob/1.21/LICENSE">You may read the license here.</a>
 */
public record EMIFillPouringRecipeServerboundPacket(int syncId, int action, List<ItemStack> stacks) implements CustomPacketPayload {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("emi_fill_pouring_recipe");
    public static final CustomPacketPayload.Type<EMIFillPouringRecipeServerboundPacket> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, EMIFillPouringRecipeServerboundPacket> STREAM_CODEC = StreamCodec.of(EMIFillPouringRecipeServerboundPacket::encode, EMIFillPouringRecipeServerboundPacket::new);

    public EMIFillPouringRecipeServerboundPacket(KegMenu menu, int action, List<ItemStack> stacks) {
        this(menu.containerId, action, stacks);
    }

    public EMIFillPouringRecipeServerboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readByte(), ItemStack.LIST_STREAM_CODEC.decode(buf));
    }

    public static void encode(RegistryFriendlyByteBuf buf, EMIFillPouringRecipeServerboundPacket packet) {
        buf.writeInt(packet.syncId);
        buf.writeByte(packet.action);
        ItemStack.LIST_STREAM_CODEC.encode(buf, packet.stacks);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
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
                            ItemStack existing = s.getItem();
                            if (existing.isEmpty()) {
                                int limit = s.getMaxStackSize(item);
                                if (item.getCount() > limit)
                                    sender.getInventory().placeItemBackInInventory(item.split(item.getCount() - limit));
                                s.set(item);
                            } else if (ItemStack.isSameItemSameComponents(existing, item)) {
                                int moved = Math.min(s.getMaxStackSize(existing) - existing.getCount(), item.getCount());
                                if (moved > 0) {
                                    existing.grow(moved);
                                    item.shrink(moved);
                                    s.setChanged();
                                }
                                if (!item.isEmpty())
                                    sender.getInventory().placeItemBackInInventory(item);
                            } else {
                                sender.getInventory().placeItemBackInInventory(item);
                            }
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
            if (ItemStack.isSameItemSameComponents(stack, r)) {
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
            if (ItemStack.isSameItemSameComponents(stack, st)) {
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
