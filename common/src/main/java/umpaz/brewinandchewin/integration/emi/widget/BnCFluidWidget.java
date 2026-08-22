package umpaz.brewinandchewin.integration.emi.widget;

import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.integration.emi.recipe.FermentingEmiRecipe;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

// Please ignore that I reimplemented the GeneratedSlotWidget class...
public class BnCFluidWidget extends SlotWidget {
    private final Function<Random, EmiStack> fluidGenerator;

    private final int unique;
    private long lastFluidGenerate = 0L;
    private EmiIngredient fluidIngredient = null;

    private boolean invalidateItemStack = false;
    private EmiIngredient itemIngredient = null;

    public BnCFluidWidget(EmiIngredient fluid, int unique, int x, int y) {
        super(EmiStack.EMPTY, x, y);
        fluidGenerator = random -> {
            List<EmiStack> stacks = fluid.getEmiStacks();
            return stacks.get(random.nextInt(stacks.size()));
        };
        this.unique = unique;
        custom = true;
        customWidth = 28;
        customHeight = 32;
        output = true;
    }

    public void drawBackground(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        Bounds bounds = this.getBounds();

        AbstractedFluidStack fluidStack = new AbstractedFluidStack((Fluid) getStack().getEmiStacks().getFirst().getKey(), getStack().getEmiStacks().getFirst().getAmount(), PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, getStack().getEmiStacks().getFirst().getComponentChanges()), FluidUnit.getLoaderUnit());

        if (BnCConfiguration.client().renderFluidInKeg())
            BrewinAndChewinClient.getHelper().renderFluidInKeg(fluidStack, draw, bounds.x() + 2, bounds.y() + 2, 1.0F);
    }

    @Override
    public void drawStack(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        Bounds bounds = this.getBounds();

        int xOff = (bounds.width() - 16) / 2;
        int yOff = (bounds.height() - 16) / 2 - 4;
        getItemStack().render(draw, bounds.x() + xOff, bounds.y() + yOff, delta);
    }

    @Override
    public void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        Bounds bounds = this.getBounds();
        draw.blit(FermentingEmiRecipe.BACKGROUND, bounds.x() + 1, bounds.y() + 1, 170, 45, bounds.width(), bounds.height() - 2);
        super.drawOverlay(draw, mouseX, mouseY, delta);
    }

    @Override
    public EmiIngredient getStack() {
        long time = System.currentTimeMillis() / 1000L;
        if (fluidIngredient == null || time > lastFluidGenerate) {
            lastFluidGenerate = time;
            fluidIngredient = fluidGenerator.apply(getRandom(time));
            invalidateItemStack = true;
        }

        return fluidIngredient;
    }

    public EmiIngredient getItemStack() {
        if (invalidateItemStack) {
            AbstractedFluidStack fluidStack = new AbstractedFluidStack((Fluid) getStack().getEmiStacks().getFirst().getKey(), getStack().getEmiStacks().getFirst().getAmount(), PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, getStack().getEmiStacks().getFirst().getComponentChanges()), FluidUnit.getLoaderUnit());
            ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(Minecraft.getInstance().level.registryAccess(), fluidStack).copy();
            Optional<KegPouringRecipe> pouringRecipe = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().map(RecipeHolder::value).sorted(Comparator.comparing(KegPouringRecipe::isStrict)).filter(kegPouringRecipe -> {
                if (kegPouringRecipe.isStrict())
                    return ItemStack.isSameItemSameComponents(itemDisplay, kegPouringRecipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
                return ItemStack.isSameItem(itemDisplay, kegPouringRecipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
            }).findFirst();
            int pourCount = pouringRecipe.map(kegPouringRecipe -> (int) (Math.min(KegBlockEntity.localizedCapacity(), fluidStack.amount()) / kegPouringRecipe.getLoaderAmount())).orElse(1);
            itemDisplay.setCount(pourCount);
            itemIngredient = EmiStack.of(itemDisplay);
            invalidateItemStack = false;
        }
        return itemIngredient;
    }

    private Random getRandom(long time) {
        return new Random((new Random(time ^ (long)this.unique)).nextInt());
    }
}
