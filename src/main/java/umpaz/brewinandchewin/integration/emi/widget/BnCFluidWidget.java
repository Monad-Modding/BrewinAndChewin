package umpaz.brewinandchewin.integration.emi.widget;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
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

        FluidStack fluidStack = new FluidStack((Fluid) getStack().getEmiStacks().get(0).getKey(), (int) getStack().getEmiStacks().get(0).getAmount(), getStack().getEmiStacks().get(0).getNbt());

        if (BnCConfiguration.RENDER_FLUID_IN_KEG.get()) {
            IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
            ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
            if (stillTexture != null) {
                TextureAtlasSprite sprite =
                        Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
                int tintColor = fluidTypeExtensions.getTintColor(fluidStack);

                float alpha = ((tintColor >> 24) & 0xFF) / 255f;
                float red = ((tintColor >> 16) & 0xFF) / 255f;
                float green = ((tintColor >> 8) & 0xFF) / 255f;
                float blue = (tintColor & 0xFF) / 255f;

                float capacity = Math.min(BnCConfiguration.KEG_CAPACITY.get(), fluidStack.getAmount()) / (float) BnCConfiguration.KEG_CAPACITY.get();
                if (capacity > 0.57) {
                    int y1 = bounds.y() + 2 + (int) (12 * (1 - ((capacity - 0.57F) / .43F)));
                    int y2 = bounds.y() + 2 + 12;
                    float topCapacity = (capacity - 0.57F) / 0.43F;
                    float vDistance = sprite.getV1() - sprite.getV0();
                    float v0 = sprite.getV0() + (0.25F * vDistance) + (0.75F * vDistance * (1 - topCapacity));
                    draw.innerBlit(sprite.atlasLocation(), bounds.x() + 2, bounds.x() + 2 + 16, y1, y2, 0, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), red, green, blue, alpha);
                    draw.innerBlit(sprite.atlasLocation(), bounds.x() + 2 + 16, bounds.x() + 2 + 16 + 8, y1, y2, 0, sprite.getU0(), sprite.getU0() + 0.5F * (sprite.getU1() - sprite.getU0()), v0, sprite.getV1(), red, green, blue, alpha);

                }
                int y1 = bounds.y() + 2 + 12 + (int) (16 * (1 - Math.min(1, (capacity / .57F))));
                int y2 = bounds.y() + 2 + 12 + 16;
                float vDistance = sprite.getV1() - sprite.getV0();
                float v0 = sprite.getV0() + (vDistance * (1 - Math.min(1, (capacity / .57F))));
                draw.innerBlit(sprite.atlasLocation(), bounds.x() + 2, bounds.x() + 2 + 16, y1, y2, 0, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), red, green, blue, alpha);
                draw.innerBlit(sprite.atlasLocation(), bounds.x() + 2 + 16, bounds.x() + 2 + 16 + 8, y1, y2, 0, sprite.getU0(), sprite.getU0() + 0.5F * (sprite.getU1() - sprite.getU0()), v0, sprite.getV1(), red, green, blue, alpha);

            }
        }
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
            FluidStack fluidStack = new FluidStack((Fluid) getStack().getEmiStacks().get(0).getKey(), (int) getStack().getEmiStacks().get(0).getAmount(), getStack().getEmiStacks().get(0).getNbt());
            ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(Minecraft.getInstance().level.registryAccess(), fluidStack).copy();
            Optional<KegPouringRecipe> pouringRecipe = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING.get()).stream().sorted(Comparator.comparing(KegPouringRecipe::isStrict)).filter(kegPouringRecipe -> {
                if (kegPouringRecipe.isStrict())
                    return ItemStack.isSameItemSameTags(itemDisplay, kegPouringRecipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
                return ItemStack.isSameItem(itemDisplay, kegPouringRecipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
            }).findFirst();
            int pourCount = pouringRecipe.map(kegPouringRecipe -> (int) (Math.min(BnCConfiguration.KEG_CAPACITY.get(), getStack().getAmount()) / kegPouringRecipe.getAmount())).orElse(1);
            itemDisplay.setCount(pourCount);
            itemIngredient = EmiStack.of(itemDisplay);
        }
        return itemIngredient;
    }

    private Random getRandom(long time) {
        return new Random((new Random(time ^ (long)this.unique)).nextInt());
    }
}
