package umpaz.brewinandchewin.common.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.compress.utils.Lists;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.integration.emi.handler.KegEmiRecipeHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Code here has been modified from EMI internals.
 * <br>
 * EMI is licensed under the MIT license.
 * <a href="https://github.com/emilyploszaj/emi/blob/1.21/LICENSE">You may read the license here.</a>
 */
public record EMIFillFermentingRecipeServerboundPacket(int syncId,
                                                       Map<KegEmiRecipeHandler.InputType, List<ItemStack>> stacks) {
    public EMIFillFermentingRecipeServerboundPacket(KegMenu menu, Map<KegEmiRecipeHandler.InputType, List<ItemStack>> stacks) {
        this(menu.containerId, stacks);
    }

    public static EMIFillFermentingRecipeServerboundPacket decode(FriendlyByteBuf buf) {
        return new EMIFillFermentingRecipeServerboundPacket(
                buf.readInt(),
                decodeStacks(buf)
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(syncId);
        encodeStacks(buf, stacks);
    }

    public static class Handler {
        public static void handle(EMIFillFermentingRecipeServerboundPacket packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                ServerPlayer sender = context.get().getSender();
                if (sender == null || !ModList.get().isLoaded("emi"))
                    return;
                AbstractContainerMenu menu = sender.containerMenu;
                if (menu.containerId != packet.syncId || !(menu instanceof KegMenu kegMenu)) {
                    BrewinAndChewin.LOG.error("Attempted to transfer fermenting recipe to an incorrect menu");
                    return;
                }

                List<ItemStack> rubble = Lists.newArrayList();

                try {
                    if (packet.stacks.containsKey(KegEmiRecipeHandler.InputType.EMPTY)) {
                        for (ItemStack stack : packet.stacks.get(KegEmiRecipeHandler.InputType.EMPTY)) {
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
                                for (ItemStack items : kegMenu.blockEntity.extractInGui(stack, gotten))
                                    sender.getInventory().placeItemBackInInventory(items);
                            }
                        }
                    }

                    if (packet.stacks.containsKey(KegEmiRecipeHandler.InputType.FILL)) {
                        for (ItemStack stack : packet.stacks.get(KegEmiRecipeHandler.InputType.FILL)) {
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
                                for (ItemStack items : kegMenu.blockEntity.extractInGui(stack, gotten))
                                    sender.getInventory().placeItemBackInInventory(items);
                            }
                        }
                    }

                    if (packet.stacks.containsKey(KegEmiRecipeHandler.InputType.ITEM)) {
                        for (int i = 0; i < packet.stacks.get(KegEmiRecipeHandler.InputType.ITEM).size(); ++i) {
                            ItemStack stack = packet.stacks.get(KegEmiRecipeHandler.InputType.ITEM).get(i);
                            if (stack.isEmpty())
                                continue;

                            int gotten = grabMatching(kegMenu, sender, menu.slots.subList(0, 3), rubble, stack);
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

    private static Map<KegEmiRecipeHandler.InputType, List<ItemStack>> decodeStacks(FriendlyByteBuf buf) {
        Map<KegEmiRecipeHandler.InputType, List<ItemStack>> stacks = new HashMap<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; ++i) {
            KegEmiRecipeHandler.InputType inputType = KegEmiRecipeHandler.InputType.BY_ID.apply(buf.readVarInt());
            List<ItemStack> innerStacks = buf.readList(FriendlyByteBuf::readItem);
            stacks.put(inputType, innerStacks);
        }
        return stacks;
    }

    private static void encodeStacks(FriendlyByteBuf buf, Map<KegEmiRecipeHandler.InputType, List<ItemStack>> stacks) {
        buf.writeVarInt(stacks.size());
        for (var entry : stacks.entrySet()) {
            buf.writeVarInt(entry.getKey().ordinal());
            buf.writeVarInt(entry.getValue().size());
            for (var stack : entry.getValue()) {
                buf.writeItem(stack);
            }
        }
    }
}
