package umpaz.brewinandchewin.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
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
   public int getHeight() {
      return mealStack.isEmpty() ? textSpacing : textSpacing + ITEM_SIZE;
   }

   @Override
   public int getWidth( Font font ) {
      if ( !mealStack.isEmpty() ) {
         MutableComponent textServingsOf = mealStack.amount() == FluidUnit.MILLIBUCKET.convertToLoader(250)
                 ? TextUtils.tooltip("cooking_pot.single_serving")
                 : TextUtils.tooltip("cooking_pot.many_servings", mealStack.amount() / FluidUnit.MILLIBUCKET.convertToLoader(250));
         return Math.max(font.width(textServingsOf), font.width(BrewinAndChewin.getHelper().getFluidDisplayName(mealStack)) + 20);
      }
      else {
         return font.width(TextUtils.tooltip("cooking_pot.empty"));
      }
   }

   @Override
   public void renderImage( Font font, int mouseX, int mouseY, GuiGraphics gui ) {
      if (mealStack.isEmpty()) return;

      ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(Minecraft.getInstance().level.registryAccess(), mealStack);
      gui.renderItem(itemDisplay, mouseX, mouseY + 9);
   }

   @Override
   public void renderText( Font font, int x, int y, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource ) {
      Integer color = ChatFormatting.GRAY.getColor();
      int gray = color == null ? -1 : color;

      if (!mealStack.isEmpty()) {
         MutableComponent textServingsOf = mealStack.amount() == FluidUnit.MILLIBUCKET.convertToLoader(250)
                 ? TextUtils.tooltip("cooking_pot.single_serving")
                 : TextUtils.tooltip("cooking_pot.many_servings", mealStack.amount() / FluidUnit.MILLIBUCKET.convertToLoader(250));

         font.drawInBatch(textServingsOf, (float) x, (float) y, gray, true, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
         font.drawInBatch(BrewinAndChewin.getHelper().getFluidDisplayName(mealStack), x + ITEM_SIZE + MARGIN, y + textSpacing + MARGIN, -1, true, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
      }
      else {
         MutableComponent textEmpty = TextUtils.tooltip("cooking_pot.empty");
         font.drawInBatch(textEmpty, x, y, gray, true, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
      }
   }

   public record KegTooltipComponent(AbstractedFluidStack mealStack) implements TooltipComponent {}
}