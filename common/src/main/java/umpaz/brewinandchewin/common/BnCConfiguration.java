package umpaz.brewinandchewin.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.api.OrderCorrectedRecordCodec;
import house.greenhouse.greenhouseconfig.api.util.DefaultFieldUtil;
import house.greenhouse.greenhouseconfig.toml.TomlLang;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.platform.BnCPlatform;

public class BnCConfiguration {

    public static final GreenhouseConfigHolder<Common> COMMON_CONFIG = GreenhouseConfigHolder.common("brewinandchewin-common",
                    BnCConfiguration.Common.CODEC,
                    BnCConfiguration.Common.DEFAULT,
                    TomlLang.INSTANCE)
            .networkSynchronized(BnCConfiguration.Common.STREAM_CODEC.cast())
            .build();

    public static final GreenhouseConfigHolder<BnCConfiguration.Client> CLIENT_CONFIG = GreenhouseConfigHolder.client("brewinandchewin-client",
                    BnCConfiguration.Client.CODEC,
                    BnCConfiguration.Client.DEFAULT,
                    TomlLang.INSTANCE)
            .build();

    public static Common common() {
        Common config = COMMON_CONFIG.get();
        return config != null ? config : Common.DEFAULT;
    }

    public static Client client() {
        Client config = CLIENT_CONFIG.get();
        return config != null ? config : Client.DEFAULT;
    }

    public static void init() {}

    public record Common(Root root, Keg keg, RecipeBook recipeBook) {
        public static final Common DEFAULT = new Common(Root.DEFAULT, Keg.DEFAULT, RecipeBook.DEFAULT);
        private static final Codec<Common> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Root.CODEC.forGetter(Common::root),
                Keg.CODEC.fieldOf("keg").forGetter(Common::keg),
                RecipeBook.CODEC.fieldOf("recipe_book").forGetter(Common::recipeBook)
        ).apply(inst, Common::new));
        public static final StreamCodec<ByteBuf, Common> STREAM_CODEC = StreamCodec.composite(
                Root.STREAM_CODEC, Common::root,
                Keg.STREAM_CODEC, Common::keg,
                RecipeBook.STREAM_CODEC, Common::recipeBook,
                Common::new
        );

        public record Root(int levelChatScramble, int levelSignScramble, int levelNameScramble) {
            public static final Root DEFAULT = new Root(3, 3, 3);

            public static final MapCodec<Root> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    DefaultFieldUtil.codecWithComments(
                            Codec.intRange(1, 10),
                            "levelChatScramble",
                            DEFAULT.levelChatScramble(),
                            "At what amplifier of Tipsy should the chat scramble?",
                            "Default: " + DEFAULT.levelChatScramble()
                    ).forGetter(Root::levelChatScramble),
                    DefaultFieldUtil.codecWithComments(
                            Codec.intRange(1, 10),
                            "levelSignScramble",
                            DEFAULT.levelSignScramble(),
                            "At what amplifier of Tipsy should signs scramble?",
                            "Default: " + DEFAULT.levelSignScramble()
                    ).forGetter(Root::levelSignScramble),
                    DefaultFieldUtil.codecWithComments(
                            Codec.intRange(1, 10),
                            "levelNameScramble",
                            DEFAULT.levelNameScramble(),
                            "At what amplifier of Tipsy should nametags scramble?",
                            "Default: " + DEFAULT.levelNameScramble()
                    ).forGetter(Root::levelNameScramble)
            ).apply(inst, Root::new));

            public static final StreamCodec<ByteBuf, Root> STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.INT, Root::levelChatScramble,
                    ByteBufCodecs.INT, Root::levelSignScramble,
                    ByteBufCodecs.INT, Root::levelNameScramble,
                    Root::new
            );
        }

        public record Keg(int cold, int chilly, int warm, int hot,
                          boolean biomeTemp, boolean dimTemp) {
            public static final Keg DEFAULT = new Keg(
                    2, 1, 1, 2,
                    true, true
            );

            public static final Codec<Keg> CODEC = OrderCorrectedRecordCodec.wrap(RecordCodecBuilder.create(inst -> inst.group(
                    DefaultFieldUtil.codecWithComments(
                            ExtraCodecs.POSITIVE_INT,
                            "kegCold",
                            DEFAULT.cold(),
                            "How many cold blocks are required for a cold temperature in the Keg?",
                            "Default: " + DEFAULT.cold()
                    ).forGetter(Keg::cold),
                    DefaultFieldUtil.codecWithComments(
                            ExtraCodecs.POSITIVE_INT,
                            "kegChilly",
                            DEFAULT.chilly(),
                            "How many cold blocks are required for a chilly temperature in the Keg?",
                            "Default: " + DEFAULT.chilly()
                    ).forGetter(Keg::chilly),
                    DefaultFieldUtil.codecWithComments(
                            ExtraCodecs.POSITIVE_INT,
                            "kegWarm",
                            DEFAULT.warm(),
                            "How many hot blocks are required for a warm temperature in the Keg?",
                            "Default: " + DEFAULT.warm()
                    ).forGetter(Keg::warm),
                    DefaultFieldUtil.codecWithComments(
                            ExtraCodecs.POSITIVE_INT,
                            "kegHot",
                            DEFAULT.hot(),
                            "How many hot blocks are required for a hot temperature in the Keg?",
                            "Default: " + DEFAULT.hot()
                    ).forGetter(Keg::hot),
                    DefaultFieldUtil.codecWithComments(
                            Codec.BOOL,
                            "kegBiomeTemp",
                            DEFAULT.biomeTemp(),
                            "Should the biome temperature influence the temperature in the Keg?",
                            "Default: " + DEFAULT.biomeTemp()
                    ).forGetter(Keg::biomeTemp),
                    DefaultFieldUtil.codecWithComments(
                            Codec.BOOL,
                            "kegDimTemp",
                            DEFAULT.dimTemp(),
                            "Should the dimension temperature influence the temperature in the Keg?",
                            "Default: " + DEFAULT.dimTemp()
                    ).forGetter(Keg::dimTemp)
            ).apply(inst, Keg::new)));
            public static final StreamCodec<ByteBuf, Keg> STREAM_CODEC = StreamCodec.of(Keg::encode, Keg::new);

            public Keg(ByteBuf buf) {
                this(
                        buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                        buf.readBoolean(), buf.readBoolean()
                );
            }

            public static void encode(ByteBuf buf, Keg keg) {
                buf.writeInt(keg.cold);
                buf.writeInt(keg.chilly);
                buf.writeInt(keg.warm);
                buf.writeInt(keg.hot);
                buf.writeBoolean(keg.biomeTemp);
                buf.writeBoolean(keg.dimTemp);
            }
        }

        public record RecipeBook(boolean enabled) {
            public static final RecipeBook DEFAULT = new RecipeBook(true);
            public static final Codec<RecipeBook> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    DefaultFieldUtil.codecWithComments(
                            Codec.BOOL,
                            "enableRecipeBookKeg",
                            DEFAULT.enabled(),
                            "Should the Keg have a Recipe Book available on its interface?",
                            "Default: " + DEFAULT.enabled()
                    ).forGetter(RecipeBook::enabled)
            ).apply(inst, RecipeBook::new));
            public static final StreamCodec<ByteBuf, RecipeBook> STREAM_CODEC = ByteBufCodecs.BOOL.map(RecipeBook::new, RecipeBook::enabled);
        }
    }


    public record Client(FluidUnit displayUnit, DisplaySettings oppositeFluidDisplay,
                         boolean numbedHeartFlickering, boolean intoxicationFoodOverlay,
                         boolean scrambleChat, boolean scrambleName, boolean scrambleSign,
                         boolean renderFluidInKeg) {
        public static final Client DEFAULT = new Client(
                platformSpecificValue(FluidUnit.MILLIBUCKET, FluidUnit.LITER),
                platformSpecificValue(DisplaySettings.NEVER, DisplaySettings.ADVANCED_TOOLTIPS),
                true, true,
                true, true, true,
                true
        );
        private static final Codec<Client> CODEC = OrderCorrectedRecordCodec.wrap(RecordCodecBuilder.create(inst -> inst.group(
                 DefaultFieldUtil.codecWithComments(
                         FluidUnit.CODEC,
                         "fluidDisplayUnit",
                         DEFAULT.displayUnit(),
                         "Which unit the fluid display in the keg should use.",
                         "Should be 'liters', 'millibuckets' or 'droplets'",
                         "1 L = 1 mB = 81 d",
                         "Default: " + DEFAULT.displayUnit().getSerializedName()
                 ).forGetter(Client::displayUnit),
                DefaultFieldUtil.codecWithComments(
                        DisplaySettings.CODEC,
                        "oppositeFluidDisplay",
                        DEFAULT.oppositeFluidDisplay(),
                        "When the opposite fluid display unit should be shown.",
                        "Should be one of 'never', 'advanced_tooltips' or 'always'",
                        "Default: " + DEFAULT.oppositeFluidDisplay().getSerializedName()
                ).forGetter(Client::oppositeFluidDisplay),
                DefaultFieldUtil.codecWithComments(
                        Codec.BOOL,
                        "numbedHeartFlickering",
                        DEFAULT.numbedHeartFlickering(),
                        "Should the numbed hearts obtained from being damaged when Tipsy flicker when you are about to take damage?",
                        "Default: " + DEFAULT.numbedHeartFlickering()
                ).forGetter(Client::numbedHeartFlickering),
                DefaultFieldUtil.codecWithComments(
                        Codec.BOOL,
                        "intoxicationFoodOverlay",
                        DEFAULT.intoxicationFoodOverlay(),
                        "Should the food bar have a yellow overlay when the player has the Intoxication effect?",
                        "Default: " + DEFAULT.intoxicationFoodOverlay()
                ).forGetter(Client::intoxicationFoodOverlay),
                DefaultFieldUtil.codecWithComments(
                        Codec.BOOL,
                        "scrambleChat",
                        DEFAULT.scrambleChat(),
                        "Should the chat scramble when the player has the Tipsy effect?",
                        "Default: " + DEFAULT.scrambleChat()
                ).forGetter(Client::scrambleChat),
                DefaultFieldUtil.codecWithComments(
                        Codec.BOOL,
                        "scrambleName",
                        DEFAULT.scrambleName(),
                        "Should other player's nametags scramble when the player has the Tipsy effect?",
                        "Default: " + DEFAULT.scrambleName()
                ).forGetter(Client::scrambleName),
                DefaultFieldUtil.codecWithComments(
                        Codec.BOOL,
                        "scrambleSign",
                        DEFAULT.scrambleSign(),
                        "Should signs scramble when the player has the Tipsy effect?",
                        "Default: " + DEFAULT.scrambleSign()
                ).forGetter(Client::scrambleSign),
                DefaultFieldUtil.codecWithComments(
                        Codec.BOOL,
                        "renderFluidInKeg",
                        DEFAULT.renderFluidInKeg(),
                        "Should kegs render the fluid texture in the background of the fluid slot?",
                        "Default: " + DEFAULT.renderFluidInKeg()
                ).forGetter(Client::renderFluidInKeg)
        ).apply(inst, Client::new)));

        public enum DisplaySettings implements StringRepresentable {
            NEVER("never"),
            ADVANCED_TOOLTIPS("advanced_tooltips"),
            ALWAYS("always");

            public static final Codec<DisplaySettings> CODEC = StringRepresentable.fromEnum(DisplaySettings::values);

            final String name;

            DisplaySettings(String name) {
                this.name = name;
            }

            @Override
            public @NotNull String getSerializedName() {
                return name;
            }
        }
    }

    private static <T> T platformSpecificValue(T neoForge, T fabric) {
        return BrewinAndChewin.getHelper().getPlatform() == BnCPlatform.NEOFORGE ? neoForge : fabric;
    }
}
