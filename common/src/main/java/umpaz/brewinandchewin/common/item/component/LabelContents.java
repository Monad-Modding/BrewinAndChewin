package umpaz.brewinandchewin.common.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;

import java.util.Optional;

public record LabelContents(String text, Optional<String> author, int generation, boolean showAuthor, boolean showAuthenticity, Optional<Integer> color, boolean hideEffects) {
    public static final int MAX_TEXT_LENGTH = 48;
    public static final int MAX_GENERATION = 3;

    public static final LabelContents EMPTY = new LabelContents("", Optional.empty(), 0, true, true, Optional.empty(), true);

    public static final Codec<LabelContents> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.string(0, MAX_TEXT_LENGTH).optionalFieldOf("text", "").forGetter(LabelContents::text),
            Codec.STRING.optionalFieldOf("author").forGetter(LabelContents::author),
            Codec.intRange(0, MAX_GENERATION).optionalFieldOf("generation", 0).forGetter(LabelContents::generation),
            Codec.BOOL.optionalFieldOf("show_author", true).forGetter(LabelContents::showAuthor),
            Codec.BOOL.optionalFieldOf("show_authenticity", true).forGetter(LabelContents::showAuthenticity),
            Codec.INT.optionalFieldOf("color").forGetter(LabelContents::color),
            Codec.BOOL.optionalFieldOf("hide_effects", true).forGetter(LabelContents::hideEffects)
    ).apply(inst, LabelContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LabelContents> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                ByteBufCodecs.stringUtf8(MAX_TEXT_LENGTH).encode(buf, value.text());
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buf, value.author());
                ByteBufCodecs.VAR_INT.encode(buf, value.generation());
                buf.writeBoolean(value.showAuthor());
                buf.writeBoolean(value.showAuthenticity());
                ByteBufCodecs.optional(ByteBufCodecs.INT).encode(buf, value.color());
                buf.writeBoolean(value.hideEffects());
            },
            buf -> new LabelContents(
                    ByteBufCodecs.stringUtf8(MAX_TEXT_LENGTH).decode(buf),
                    ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    ByteBufCodecs.optional(ByteBufCodecs.INT).decode(buf),
                    buf.readBoolean()));

    public boolean isBlank() {
        return this.text.isBlank() && this.author.isEmpty();
    }

    public LabelContents withText(String newText) {
        return new LabelContents(newText, this.author, this.generation, this.showAuthor, this.showAuthenticity, this.color, this.hideEffects);
    }

    public LabelContents withAuthor(String newAuthor) {
        return new LabelContents(this.text, Optional.of(newAuthor), this.generation, this.showAuthor, this.showAuthenticity, this.color, this.hideEffects);
    }

    public LabelContents withColor(int newColor) {
        return new LabelContents(this.text, this.author, this.generation, this.showAuthor, this.showAuthenticity, Optional.of(newColor), this.hideEffects);
    }

    public LabelContents withShowAuthor(boolean newShowAuthor) {
        return new LabelContents(this.text, this.author, this.generation, newShowAuthor, this.showAuthenticity, this.color, this.hideEffects);
    }

    public LabelContents withShowAuthenticity(boolean newShowAuthenticity) {
        return new LabelContents(this.text, this.author, this.generation, this.showAuthor, newShowAuthenticity, this.color, this.hideEffects);
    }

    public Optional<LabelContents> copied() {
        if (this.generation >= MAX_GENERATION - 1)
            return Optional.empty();
        return Optional.of(new LabelContents(this.text, this.author, this.generation + 1, this.showAuthor, this.showAuthenticity, this.color, this.hideEffects));
    }

    public LabelContents withHideEffects(boolean newHideEffects) {
        return new LabelContents(this.text, this.author, this.generation, this.showAuthor, this.showAuthenticity, this.color, newHideEffects);
    }

    public MutableComponent getDisplayName() {
        MutableComponent name = this.showAuthor && this.author.isPresent()
                ? BnCTextUtils.getTranslation("label.by", this.text, this.author.get())
                : Component.literal(this.text);
        return this.color.map(value -> name.withColor(value)).orElseGet(() -> name.withStyle(ChatFormatting.GRAY));
    }

    public Component getGenerationName() {
        return BnCTextUtils.getTranslation("label.generation." + this.generation).withStyle(ChatFormatting.DARK_GRAY);
    }
}
