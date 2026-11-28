package umpaz.brewinandchewin.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import vectorwing.farmersdelight.common.utility.TextUtils;

public class KegTooltip implements ClientTooltipComponent {
   private static final int ITEM_SIZE = 16;
   private static final int MARGIN = 4;

   private final int textSpacing = Minecraft.getInstance().font.lineHeight + 1;
   private final AbstractedFluidStack mealStack;

   public KegTooltip(KegTooltipComponent tooltip) {
      this.mealStack = tooltip.mealStack;
   }

   @Override
   public int getHeight(Font font) {
      return mealStack.isEmpty() ? textSpacing : textSpacing + ITEM_SIZE;
   }

   @Override
   public int getWidth(Font font) {
      if (!mealStack.isEmpty()) {
         MutableComponent textServingsOf = mealStack.amount() == FluidUnit.MILLIBUCKET.convertToLoader(250)
                 ? TextUtils.tooltip("cooking_pot.single_serving")
                 : TextUtils.tooltip("cooking_pot.many_servings", mealStack.amount() / FluidUnit.MILLIBUCKET.convertToLoader(250));
         return Math.max(font.width(textServingsOf), font.width(BrewinAndChewin.getHelper().getFluidDisplayName(mealStack)) + 20);
      } else {
         return font.width(TextUtils.tooltip("cooking_pot.empty"));
      }
   }

   @Override
   public void extractImage(Font font, int mouseX, int mouseY, int width, int height, GuiGraphicsExtractor gui) {
      if (mealStack.isEmpty() || Minecraft.getInstance().level == null) return;

      ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(Minecraft.getInstance().level.registryAccess(), mealStack);
      gui.item(itemDisplay, mouseX, mouseY + 9);
   }

   @Override
   public void extractText(GuiGraphicsExtractor gui, Font font, int x, int y) {
      int gray = 0xFFAAAAAA;

      if (!mealStack.isEmpty()) {
         MutableComponent textServingsOf = mealStack.amount() == FluidUnit.MILLIBUCKET.convertToLoader(250)
                 ? TextUtils.tooltip("cooking_pot.single_serving")
                 : TextUtils.tooltip("cooking_pot.many_servings", mealStack.amount() / FluidUnit.MILLIBUCKET.convertToLoader(250));

         gui.text(font, textServingsOf, x, y, gray, true);
         gui.text(font, BrewinAndChewin.getHelper().getFluidDisplayName(mealStack), x + ITEM_SIZE + MARGIN, y + textSpacing + MARGIN, -1, true);
      } else {
         MutableComponent textEmpty = TextUtils.tooltip("cooking_pot.empty");
         gui.text(font, textEmpty, x, y, gray, true);
      }
   }

   public record KegTooltipComponent(AbstractedFluidStack mealStack) implements TooltipComponent {}
}