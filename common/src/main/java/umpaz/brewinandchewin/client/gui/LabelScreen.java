package umpaz.brewinandchewin.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.item.component.LabelContents;
import umpaz.brewinandchewin.common.network.serverbound.SetLabelContentsServerboundPacket;
import umpaz.brewinandchewin.common.utility.BnCLabelUtils;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;

public class LabelScreen extends Screen {
    private static final int FIELD_WIDTH = 180;

    private final InteractionHand hand;
    private final LabelContents contents;

    private EditBox textField;
    private Checkbox showAuthorBox;
    private Checkbox showAuthenticityBox;
    private Checkbox hideEffectsBox;

    public LabelScreen(ItemStack stack, InteractionHand hand) {
        super(BnCTextUtils.getTranslation("container.label"));
        this.hand = hand;
        this.contents = BnCLabelUtils.readFromItem(stack);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int top = this.height / 2 - 50;

        this.textField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, top, FIELD_WIDTH, 20,
                BnCTextUtils.getTranslation("container.label.text"));
        this.textField.setMaxLength(LabelContents.MAX_TEXT_LENGTH);
        this.textField.setValue(this.contents.text());
        this.addRenderableWidget(this.textField);
        this.setInitialFocus(this.textField);

        this.showAuthorBox = Checkbox.builder(BnCTextUtils.getTranslation("container.label.show_author"), this.font)
                .pos(centerX - FIELD_WIDTH / 2, top + 28)
                .selected(this.contents.showAuthor())
                .build();
        this.addRenderableWidget(this.showAuthorBox);

        this.showAuthenticityBox = Checkbox.builder(BnCTextUtils.getTranslation("container.label.show_authenticity"), this.font)
                .pos(centerX - FIELD_WIDTH / 2, top + 52)
                .selected(this.contents.showAuthenticity())
                .build();
        this.addRenderableWidget(this.showAuthenticityBox);

        this.hideEffectsBox = Checkbox.builder(BnCTextUtils.getTranslation("container.label.hide_effects"), this.font)
                .pos(centerX - FIELD_WIDTH / 2, top + 76)
                .selected(this.contents.hideEffects())
                .build();
        this.addRenderableWidget(this.hideEffectsBox);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onDone())
                .bounds(centerX - 50, top + 106, 100, 20)
                .build());
    }

    private void onDone() {
        BrewinAndChewin.getHelper().sendServerbound(new SetLabelContentsServerboundPacket(
                this.hand == InteractionHand.OFF_HAND,
                this.textField.getValue(),
                this.showAuthorBox.selected(),
                this.showAuthenticityBox.selected(),
                this.hideEffectsBox.selected()));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 70, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
