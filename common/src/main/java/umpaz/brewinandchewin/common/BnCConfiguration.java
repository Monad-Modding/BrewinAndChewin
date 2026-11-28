package umpaz.brewinandchewin.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.platform.BnCPlatform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BnCConfiguration {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Common commonInstance = Common.DEFAULT;
    private static Client clientInstance = Client.DEFAULT;

    public static Common common() {
        return commonInstance;
    }

    public static Client client() {
        return clientInstance;
    }

    public static void init() {
        commonInstance = loadOrCreate("brewinandchewin-common.json", Common.CODEC, Common.DEFAULT);
        clientInstance = loadOrCreate("brewinandchewin-client.json", Client.CODEC, Client.DEFAULT);
    }

    private static <T> T loadOrCreate(String fileName, Codec<T> codec, T def) {
        try {
            Path configPath = FabricLoader.getInstance().getConfigDir().resolve(fileName);
            if (Files.exists(configPath)) {
                JsonElement json = JsonParser.parseString(Files.readString(configPath));
                return codec.parse(JsonOps.INSTANCE, json).resultOrPartial(err ->
                        BrewinAndChewin.LOG.warn("Failed to parse config {}: {}", fileName, err)
                ).orElse(def);
            } else {
                Files.createDirectories(configPath.getParent());
                codec.encodeStart(JsonOps.INSTANCE, def).result().ifPresent(el -> {
                    try {
                        Files.writeString(configPath, GSON.toJson(el));
                    } catch (IOException e) {
                        BrewinAndChewin.LOG.warn("Failed to write default config {}", fileName, e);
                    }
                });
            }
        } catch (Exception e) {
            BrewinAndChewin.LOG.warn("Error loading config {}, using defaults", fileName, e);
        }
        return def;
    }

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
                    Codec.intRange(1, 10).optionalFieldOf("levelChatScramble", DEFAULT.levelChatScramble()).forGetter(Root::levelChatScramble),
                    Codec.intRange(1, 10).optionalFieldOf("levelSignScramble", DEFAULT.levelSignScramble()).forGetter(Root::levelSignScramble),
                    Codec.intRange(1, 10).optionalFieldOf("levelNameScramble", DEFAULT.levelNameScramble()).forGetter(Root::levelNameScramble)
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
            public static final Keg DEFAULT = new Keg(2, 1, 1, 2, true, true);

            public static final Codec<Keg> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("kegCold", DEFAULT.cold()).forGetter(Keg::cold),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("kegChilly", DEFAULT.chilly()).forGetter(Keg::chilly),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("kegWarm", DEFAULT.warm()).forGetter(Keg::warm),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("kegHot", DEFAULT.hot()).forGetter(Keg::hot),
                    Codec.BOOL.optionalFieldOf("kegBiomeTemp", DEFAULT.biomeTemp()).forGetter(Keg::biomeTemp),
                    Codec.BOOL.optionalFieldOf("kegDimTemp", DEFAULT.dimTemp()).forGetter(Keg::dimTemp)
            ).apply(inst, Keg::new));
            public static final StreamCodec<ByteBuf, Keg> STREAM_CODEC = StreamCodec.of(Keg::encode, Keg::new);

            public Keg(ByteBuf buf) {
                this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readBoolean());
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
                    Codec.BOOL.optionalFieldOf("enableRecipeBookKeg", DEFAULT.enabled()).forGetter(RecipeBook::enabled)
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
        private static final Codec<Client> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                FluidUnit.CODEC.optionalFieldOf("fluidDisplayUnit", DEFAULT.displayUnit()).forGetter(Client::displayUnit),
                DisplaySettings.CODEC.optionalFieldOf("oppositeFluidDisplay", DEFAULT.oppositeFluidDisplay()).forGetter(Client::oppositeFluidDisplay),
                Codec.BOOL.optionalFieldOf("numbedHeartFlickering", DEFAULT.numbedHeartFlickering()).forGetter(Client::numbedHeartFlickering),
                Codec.BOOL.optionalFieldOf("intoxicationFoodOverlay", DEFAULT.intoxicationFoodOverlay()).forGetter(Client::intoxicationFoodOverlay),
                Codec.BOOL.optionalFieldOf("scrambleChat", DEFAULT.scrambleChat()).forGetter(Client::scrambleChat),
                Codec.BOOL.optionalFieldOf("scrambleName", DEFAULT.scrambleName()).forGetter(Client::scrambleName),
                Codec.BOOL.optionalFieldOf("scrambleSign", DEFAULT.scrambleSign()).forGetter(Client::scrambleSign),
                Codec.BOOL.optionalFieldOf("renderFluidInKeg", DEFAULT.renderFluidInKeg()).forGetter(Client::renderFluidInKeg)
        ).apply(inst, Client::new));

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
        return BrewinAndChewin.getHelper() != null && BrewinAndChewin.getHelper().getPlatform() == BnCPlatform.NEOFORGE ? neoForge : fabric;
    }
}
