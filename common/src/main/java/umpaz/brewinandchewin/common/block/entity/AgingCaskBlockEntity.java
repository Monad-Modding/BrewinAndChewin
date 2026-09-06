package umpaz.brewinandchewin.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.block.AgingCaskBlock;
import umpaz.brewinandchewin.common.block.entity.container.AgingCaskMenu;
import umpaz.brewinandchewin.common.item.WineItem;
import umpaz.brewinandchewin.common.item.component.WineContents;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import umpaz.brewinandchewin.common.utility.BnCWineUtils;

import java.util.ArrayList;
import java.util.List;

public class AgingCaskBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int WINE_SLOT = 0;
    public static final int FIRST_DISTILLATE_SLOT = 1;
    public static final int DISTILLATE_SLOTS = 8;
    public static final int OUTPUT_SLOT = 9;
    public static final int INVENTORY_SIZE = 10;

    public static final int AGING_TIME_TOTAL = 6000;
    public static final int DATA_COUNT = 2;

    private static final int[] SLOTS_UP = new int[]{WINE_SLOT, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final int[] SLOTS_DOWN = new int[]{OUTPUT_SLOT};
    private static final int[] SLOTS_SIDES = new int[]{1, 2, 3, 4, 5, 6, 7, 8};

    private NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private final NonNullList<ItemStack> lastInputs = NonNullList.withSize(OUTPUT_SLOT, ItemStack.EMPTY);
    private int agingTime;

    private final ContainerData caskData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> AgingCaskBlockEntity.this.agingTime;
                case 1 -> AGING_TIME_TOTAL;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0)
                AgingCaskBlockEntity.this.agingTime = value;
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public AgingCaskBlockEntity(BlockPos pos, BlockState state) {
        super(BnCBlockEntityTypes.AGING_CASK, pos, state);
    }

    public static int getOpenDistillateSlots(ItemStack wine) {
        if (!(wine.getItem() instanceof WineItem wineItem))
            return 0;
        WineContents contents = BnCWineUtils.getContents(wine);
        return Math.min(DISTILLATE_SLOTS, contents.remainingCapacity(wineItem.getWineType()));
    }

    public static int getFirstOpenDistillateSlot(ItemStack wine) {
        if (!(wine.getItem() instanceof WineItem))
            return 0;
        return Math.floorMod(BnCWineUtils.getContents(wine).distillates(), DISTILLATE_SLOTS);
    }

    public static boolean isDistillateSlotUnlocked(ItemStack wine, int slot) {
        int open = getOpenDistillateSlots(wine);
        if (open <= 0)
            return false;
        int offset = Math.floorMod(slot - FIRST_DISTILLATE_SLOT - getFirstOpenDistillateSlot(wine), DISTILLATE_SLOTS);
        return offset < open;
    }

    public ContainerData getCaskData() {
        return this.caskData;
    }

    public int getAgingTime() {
        return this.agingTime;
    }

    public ItemStack getWine() {
        return this.inventory.get(WINE_SLOT);
    }

    public int countDistillates() {
        int count = 0;
        for (int i = FIRST_DISTILLATE_SLOT; i < OUTPUT_SLOT; ++i) {
            if (BnCWineUtils.isDistillate(this.inventory.get(i)))
                ++count;
        }
        return count;
    }

    public boolean areDistillateSlotsEmpty() {
        for (int i = FIRST_DISTILLATE_SLOT; i < OUTPUT_SLOT; ++i) {
            if (!this.inventory.get(i).isEmpty())
                return false;
        }
        return true;
    }

    public static void agingTick(Level level, BlockPos pos, BlockState state, AgingCaskBlockEntity cask) {
        boolean occupied = !cask.getWine().isEmpty();
        if (state.getValue(AgingCaskBlock.OCCUPIED) != occupied) {
            level.setBlock(pos, state.setValue(AgingCaskBlock.OCCUPIED, occupied), Block_UPDATE_ALL);
        }

        if (cask.snapshotInputs() && cask.agingTime != 0) {
            cask.agingTime = 0;
            cask.setChanged();
        }

        if (!cask.canAge()) {
            if (cask.agingTime != 0) {
                cask.agingTime = 0;
                cask.setChanged();
            }
            return;
        }

        ++cask.agingTime;
        if (cask.agingTime < AGING_TIME_TOTAL) {
            cask.setChanged();
            return;
        }

        cask.agingTime = 0;
        cask.age(level, pos);
    }

    private static final int Block_UPDATE_ALL = 3;

    private boolean snapshotInputs() {
        boolean changed = false;
        for (int i = 0; i < OUTPUT_SLOT; ++i) {
            if (ItemStack.matches(this.inventory.get(i), this.lastInputs.get(i)))
                continue;
            this.lastInputs.set(i, this.inventory.get(i).copy());
            changed = true;
        }
        return changed;
    }

    private boolean canAge() {
        ItemStack wine = this.getWine();
        if (!(wine.getItem() instanceof WineItem wineItem))
            return false;
        if (!this.inventory.get(OUTPUT_SLOT).isEmpty())
            return false;
        WineContents contents = BnCWineUtils.getContents(wine);
        if (!contents.canBeAged(wineItem.getWineType()))
            return false;
        return this.countDistillates() > 0 || contents.canUpgradeEffects();
    }

    private void age(Level level, BlockPos pos) {
        ItemStack wine = this.getWine();
        if (!(wine.getItem() instanceof WineItem wineItem))
            return;

        List<ItemStack> distillates = new ArrayList<>();
        for (int i = FIRST_DISTILLATE_SLOT; i < OUTPUT_SLOT; ++i) {
            if (BnCWineUtils.isDistillate(this.inventory.get(i)))
                distillates.add(this.inventory.get(i));
        }

        ItemStack aged = wine.copy();
        BnCWineUtils.setContents(aged, BnCWineUtils.age(wineItem.getWineType(),
                BnCWineUtils.getContents(wine), distillates, level.random));

        this.inventory.set(OUTPUT_SLOT, aged);
        this.inventory.set(WINE_SLOT, ItemStack.EMPTY);
        for (int i = FIRST_DISTILLATE_SLOT; i < OUTPUT_SLOT; ++i) {
            ItemStack distillate = this.inventory.get(i);
            if (!BnCWineUtils.isDistillate(distillate))
                continue;
            ItemStack container = getContainerItem(distillate);
            if (!container.isEmpty())
                container.setCount(Math.min(distillate.getCount(), container.getMaxStackSize()));
            this.inventory.set(i, container);
        }
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.6F, 0.7F);
        this.setChanged();
    }

    public static ItemStack getContainerItem(ItemStack stack) {
        if (stack.getItem() instanceof PotionItem)
            return new ItemStack(Items.GLASS_BOTTLE);
        if (stack.getItem().hasCraftingRemainingItem())
            return new ItemStack(stack.getItem().getCraftingRemainingItem());
        return ItemStack.EMPTY;
    }

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.inventory = items;
    }

    public NonNullList<ItemStack> getDroppableInventory() {
        return this.inventory;
    }

    @Override
    protected Component getDefaultName() {
        return BnCTextUtils.getTranslation("container.aging_cask");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new AgingCaskMenu(id, playerInventory, this, this.caskData);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == WINE_SLOT)
            return stack.getItem() instanceof WineItem && this.areDistillateSlotsEmpty();
        if (slot == OUTPUT_SLOT)
            return false;
        return BnCWineUtils.isDistillate(stack) && isDistillateSlotUnlocked(this.getWine(), slot);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case UP -> SLOTS_UP;
            case DOWN -> SLOTS_DOWN;
            default -> SLOTS_SIDES;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        if (slot == OUTPUT_SLOT)
            return true;
        return slot >= FIRST_DISTILLATE_SLOT && slot < OUTPUT_SLOT && !isDistillateSlotUnlocked(this.getWine(), slot);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        this.inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, this.inventory, provider);
        this.agingTime = nbt.getInt("AgingTime");
        this.snapshotInputs();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        ContainerHelper.saveAllItems(nbt, this.inventory, provider);
        nbt.putInt("AgingTime", this.agingTime);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, provider);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
