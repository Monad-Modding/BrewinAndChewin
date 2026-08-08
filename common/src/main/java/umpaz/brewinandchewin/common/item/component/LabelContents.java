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

public record LabelContents(String text, Optional<String> author, int generation, boolean showAuthor, boolean showAuthenticity, Optional<Integer> color) {
    public static final int MAX_TEXT_LENGTH = 48;
    public static final int MAX_GENERATION = 3;

    public static final LabelContents EMPTY = new LabelContents("", Optional.empty(), 0, true, true, Optional.empty());

    public static final Codec<LabelContents> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.string(0, MAX_TEXT_LENGTH).optionalFieldOf("text", "").forGetter(LabelContents::text),
            Codec.STRING.optionalFieldOf("author").forGetter(LabelContents::author),
            Codec.intRange(0, MAX_GENERATION).optionalFieldOf("generation", 0).forGetter(LabelContents::generation),
            Codec.BOOL.optionalFieldOf("show_author", true).forGetter(LabelContents::showAuthor),
            Codec.BOOL.optionalFieldOf("show_authenticity", true).forGetter(LabelContents::showAuthenticity),
            Codec.INT.optionalFieldOf("color").forGetter(LabelContents::color)
    ).apply(inst, LabelContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LabelContents> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_TEXT_LENGTH), LabelContents::text,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), LabelContents::author,
            ByteBufCodecs.VAR_INT, LabelContents::generation,
            ByteBufCodecs.BOOL, LabelContents::showAuthor,
            ByteBufCodecs.BOOL, LabelContents::showAuthenticity,
            ByteBufCodecs.optional(ByteBufCodecs.INT), LabelContents::color,
            LabelContents::new);

    public boolean isBlank() {
        return this.text.isBlank() && this.author.isEmpty();
    }

    public LabelContents withText(String newText) {
        return new LabelContents(newText, this.author, this.generation, this.showAuthor, this.showAuthenticity, this.color);
    }

    public LabelContents withAuthor(String newAuthor) {
        return new LabelContents(this.text, Optional.of(newAuthor), this.generation, this.showAuthor, this.showAuthenticity, this.color);
    }

    public LabelContents withColor(int newColor) {
        return new LabelContents(this.text, this.author, this.generation, this.showAuthor, this.showAuthenticity, Optional.of(newColor));
    }

    public LabelContents withShowAuthor(boolean newShowAuthor) {
        return new LabelContents(this.text, this.author, this.generation, newShowAuthor, this.showAuthenticity, this.color);
    }

    public LabelContents withShowAuthenticity(boolean newShowAuthenticity) {
        return new LabelContents(this.text, this.author, this.generation, this.showAuthor, newShowAuthenticity, this.color);
    }

    public Optional<LabelContents> copied() {
        if (this.generation >= MAX_GENERATION - 1)
            return Optional.empty();
        return Optional.of(new LabelContents(this.text, this.author, this.generation + 1, this.showAuthor, this.showAuthenticity, this.color));
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
