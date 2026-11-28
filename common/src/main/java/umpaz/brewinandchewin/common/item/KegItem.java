package umpaz.brewinandchewin.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import umpaz.brewinandchewin.client.gui.KegTooltip;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

import java.util.Optional;

public class KegItem extends BlockItem {
   private static final int BAR_COLOR = 0x6666FF;

   public KegItem(Block block, Properties properties) {
      super(block, properties);
   }

   @Override
   public boolean isBarVisible(ItemStack stack) {
      return getServingCount(stack) > 0;
   }

   @Override
   public int getBarWidth(ItemStack stack) {
      return (int) Math.min(1 + getServingCount(stack) / 77, 13);
   }

   @Override
   public int getBarColor(ItemStack stack) {
      return BAR_COLOR;
   }

   @Override
   public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
      if (Minecraft.getInstance().level == null)
         return Optional.empty();
      AbstractedFluidStack mealStack = KegBlockEntity.getMealFromItem(stack,
            Minecraft.getInstance().level.registryAccess());
      return Optional.of(new KegTooltip.KegTooltipComponent(mealStack));
   }

   private static long getServingCount(ItemStack stack) {
      TypedEntityData<BlockEntityType<?>> nbt = stack.get(DataComponents.BLOCK_ENTITY_DATA);
      if (nbt == null || Minecraft.getInstance().level == null) {
         return 0;
      } else {
         AbstractedFluidStack mealStack = KegBlockEntity.getMealFromItem(stack,
               Minecraft.getInstance().level.registryAccess());
         return mealStack.amount();
      }
   }
}