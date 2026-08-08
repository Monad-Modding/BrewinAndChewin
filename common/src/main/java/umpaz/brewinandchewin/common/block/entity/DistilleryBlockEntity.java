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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.DistilleryBlock;
import umpaz.brewinandchewin.common.block.entity.container.DistilleryMenu;
import umpaz.brewinandchewin.common.crafting.DistillingRecipe;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;

import java.util.Optional;

public class DistilleryBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int WATER_SLOT = 2;
    public static final int OUTPUT_SLOT = 3;
    public static final int INVENTORY_SIZE = 4;

    public static final int MAX_WATER = 8;
    public static final int BUCKET_WATER = 4;
    public static final int BOTTLE_WATER = 1;
    public static final int DATA_COUNT = 5;

    private static final int[] SLOTS_UP = new int[]{INPUT_SLOT};
    private static final int[] SLOTS_DOWN = new int[]{OUTPUT_SLOT};
    private static final int[] SLOTS_SIDES = new int[]{FUEL_SLOT, WATER_SLOT};

    private NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    private int litTime;
    private int litDuration;
    private int distillingProgress;
    private int distillingTotalTime;
    private int water;
    private float storedExperience;

    private final ContainerData distilleryData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> DistilleryBlockEntity.this.litTime;
                case 1 -> DistilleryBlockEntity.this.litDuration;
                case 2 -> DistilleryBlockEntity.this.distillingProgress;
                case 3 -> DistilleryBlockEntity.this.distillingTotalTime;
                case 4 -> DistilleryBlockEntity.this.water;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> DistilleryBlockEntity.this.litTime = value;
                case 1 -> DistilleryBlockEntity.this.litDuration = value;
                case 2 -> DistilleryBlockEntity.this.distillingProgress = value;
                case 3 -> DistilleryBlockEntity.this.distillingTotalTime = value;
                case 4 -> DistilleryBlockEntity.this.water = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public DistilleryBlockEntity(BlockPos pos, BlockState state) {
        super(BnCBlockEntityTypes.DISTILLERY, pos, state);
    }

    public ContainerData getDistilleryData() {
        return this.distilleryData;
    }

    public boolean isLit() {
        return this.litTime > 0;
    }

    public int getWater() {
        return this.water;
    }

    public static void distillingTick(Level level, BlockPos pos, BlockState state, DistilleryBlockEntity distillery) {
        boolean wasLit = distillery.isLit();
        boolean changed = false;

        if (distillery.isLit()) {
            --distillery.litTime;
        }

        changed |= distillery.drainWaterContainer();

        Optional<RecipeHolder<DistillingRecipe>> recipe = distillery.getRecipe();
        boolean canDistill = recipe.isPresent() && distillery.canDistill(recipe.get().value());

        if (!distillery.isLit() && canDistill) {
            ItemStack fuel = distillery.inventory.get(FUEL_SLOT);
            int burnTime = AbstractFurnaceBlockEntity.getFuel().getOrDefault(fuel.getItem(), 0);
            if (burnTime > 0) {
                distillery.litTime = burnTime;
                distillery.litDuration = burnTime;
                ItemStack remainder = BrewinAndChewin.getHelper().getCraftingRemainingItem(fuel);
                if (!remainder.isEmpty()) {
                    distillery.inventory.set(FUEL_SLOT, remainder);
                } else {
                    fuel.shrink(1);
                }
                changed = true;
            }
        }

        if (distillery.isLit() && canDistill) {
            distillery.distillingTotalTime = recipe.get().value().getDistillingTime();
            ++distillery.distillingProgress;
            if (distillery.distillingProgress >= distillery.distillingTotalTime) {
                distillery.distillingProgress = 0;
                distillery.distill(recipe.get().value());
                changed = true;
            }
        } else if (distillery.distillingProgress > 0) {
            distillery.distillingProgress = Math.max(0, distillery.distillingProgress - 2);
        }

        if (wasLit != distillery.isLit()) {
            changed = true;
            DistilleryBlock.setLit(level, pos, state, distillery.isLit());
        }

        if (changed) {
            distillery.setChanged();
            level.sendBlockUpdated(pos, state, level.getBlockState(pos), Block_UPDATE_CLIENTS);
        }
    }

    private static final int Block_UPDATE_ALL = 3;
    private static final int Block_UPDATE_CLIENTS = 2;

    private boolean drainWaterContainer() {
        if (this.water >= MAX_WATER)
            return false;
        ItemStack container = this.inventory.get(WATER_SLOT);
        if (container.isEmpty())
            return false;

        int amount;
        ItemStack remainder;
        if (container.is(Items.WATER_BUCKET)) {
            amount = BUCKET_WATER;
            remainder = new ItemStack(Items.BUCKET);
        } else if (container.is(Items.POTION) && isWaterBottle(container)) {
            amount = BOTTLE_WATER;
            remainder = new ItemStack(Items.GLASS_BOTTLE);
        } else {
            return false;
        }

        if (this.water + amount > MAX_WATER)
            return false;

        ItemStack output = this.inventory.get(OUTPUT_SLOT);
        if (!output.isEmpty() && (!ItemStack.isSameItemSameComponents(output, remainder) || output.getCount() >= output.getMaxStackSize()))
            return false;

        this.water += amount;
        container.shrink(1);
        if (output.isEmpty()) {
            this.inventory.set(OUTPUT_SLOT, remainder);
        } else {
            output.grow(1);
        }
        return true;
    }

    private static boolean isWaterBottle(ItemStack stack) {
        PotionContents contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        return contents != null && contents.is(Potions.WATER);
    }

    private Optional<RecipeHolder<DistillingRecipe>> getRecipe() {
        if (this.level == null || this.inventory.get(INPUT_SLOT).isEmpty())
            return Optional.empty();
        return this.level.getRecipeManager().getRecipeFor(BnCRecipeTypes.DISTILLING,
                new SingleRecipeInput(this.inventory.get(INPUT_SLOT)), this.level);
    }

    private boolean canDistill(DistillingRecipe recipe) {
        if (this.level == null)
            return false;
        if (this.water < recipe.getWaterCost())
            return false;
        ItemStack result = recipe.getResultItem(this.level.registryAccess());
        if (result.isEmpty())
            return false;
        ItemStack output = this.inventory.get(OUTPUT_SLOT);
        if (output.isEmpty())
            return true;
        if (!ItemStack.isSameItemSameComponents(output, result))
            return false;
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void distill(DistillingRecipe recipe) {
        if (this.level == null)
            return;
        ItemStack result = recipe.assemble(new SingleRecipeInput(this.inventory.get(INPUT_SLOT)), this.level.registryAccess());
        ItemStack output = this.inventory.get(OUTPUT_SLOT);
        if (output.isEmpty()) {
            this.inventory.set(OUTPUT_SLOT, result);
        } else {
            output.grow(result.getCount());
        }
        this.inventory.get(INPUT_SLOT).shrink(1);
        this.water -= recipe.getWaterCost();
        this.storedExperience += recipe.getExperience();
    }

    public void awardExperience(Vec3 pos) {
        if (this.level instanceof ServerLevel serverLevel && this.storedExperience > 0.0F) {
            ExperienceOrb.award(serverLevel, pos, (int) this.storedExperience);
            this.storedExperience = 0.0F;
        }
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
        return BnCTextUtils.getTranslation("container.distillery");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new DistilleryMenu(id, playerInventory, this, this.distilleryData);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case FUEL_SLOT -> AbstractFurnaceBlockEntity.isFuel(stack);
            case WATER_SLOT -> stack.is(Items.WATER_BUCKET) || (stack.is(Items.POTION) && isWaterBottle(stack));
            case OUTPUT_SLOT -> false;
            default -> true;
        };
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
        return slot == OUTPUT_SLOT;
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        this.inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, this.inventory, provider);
        this.litTime = nbt.getInt("LitTime");
        this.litDuration = nbt.getInt("LitDuration");
        this.distillingProgress = nbt.getInt("DistillingProgress");
        this.distillingTotalTime = nbt.getInt("DistillingTotalTime");
        this.water = nbt.getInt("Water");
        this.storedExperience = nbt.getFloat("StoredExperience");
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        ContainerHelper.saveAllItems(nbt, this.inventory, provider);
        nbt.putInt("LitTime", this.litTime);
        nbt.putInt("LitDuration", this.litDuration);
        nbt.putInt("DistillingProgress", this.distillingProgress);
        nbt.putInt("DistillingTotalTime", this.distillingTotalTime);
        nbt.putInt("Water", this.water);
        nbt.putFloat("StoredExperience", this.storedExperience);
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
