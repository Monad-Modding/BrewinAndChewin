package umpaz.brewinandchewin.client.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.SignText;
import umpaz.brewinandchewin.common.registry.BnCEffects;

import java.util.*;
import java.util.stream.Collectors;

public class BnCClientTextUtils {
    //public static final ResourceKey<ChatType> STYLED_CHAT_HACK_CHAT_TYPE = ResourceKey.create(Registries.CHAT_TYPE, ResourceLocation.fromNamespaceAndPath("styled_chat", "generic_hack"));
    public static int tipsyMessageLevel = 0;
    public static boolean generatedRandom = false;
    public static int clearDelayAmount = 0;
    public static long randomSeed = 0L;
    private static PlayerChatMessage nextTipsyMessage;

    public static Component modifyComponents(Component original, long randomSeed) {
        StringBuilder textBuilder = new StringBuilder(original.getContents().visit(Optional::ofNullable).orElse(""));

        if (!textBuilder.isEmpty()) {
            int amplifier = tipsyMessageLevel;
            if (Minecraft.getInstance().player.hasEffect(BnCEffects.TIPSY.value()) && amplifier < Minecraft.getInstance().player.getEffect(BnCEffects.TIPSY.value()).getAmplifier())
                amplifier = Minecraft.getInstance().player.getEffect(BnCEffects.TIPSY.value()).getAmplifier();
            amplifier = amplifier - 3;
            RandomSource random = RandomSource.create(randomSeed);
            int amnt = (int) ((amplifier + 1) * (textBuilder.length() / 6f)) + random.nextInt(amplifier, amplifier + 2) - 1;

            for (int i = 0; i < amnt; i++) {
                List<String> globalWords = Arrays.stream(textBuilder.toString().split(" ")).collect(Collectors.toCollection(ArrayList::new));
                List<String> validWords = globalWords.stream().filter(s -> s.length() > 3).collect(Collectors.toCollection(ArrayList::new));
                if (validWords.isEmpty())
                    break;
                // pick a random word
                int wordIndex = random.nextInt(validWords.size());
                String word = validWords.get(wordIndex);
                int globalWordLength = globalWords.subList(0, globalWords.indexOf(word)).stream().mapToInt(value -> value.length() + 1).sum();
                // pick a random character in the word, excluding the first and last letters
                int index = globalWordLength + random.nextInt(1, word.length() - 2);
                // pick an index within range
                int newIndex = Mth.clamp(index + (random.nextBoolean() ? 1 : -1), globalWordLength + 1,
                        globalWordLength + word.length() - 2);
                // swap the characters
                char temp = textBuilder.charAt(index);
                textBuilder.setCharAt(index, textBuilder.charAt(newIndex));
                textBuilder.setCharAt(newIndex, temp);
            }
            String text = textBuilder.toString();
            MutableComponent component = Component.literal(text).withStyle(original.getStyle());
            for (Component sibling : original.getSiblings()) {
                component.append(modifyComponents(sibling, randomSeed));
            }
            return component;
        }
        MutableComponent component = Component.empty();
        for (Component sibling : original.getSiblings()) {
            component.append(modifyComponents(sibling, randomSeed));
        }
        return component;
    }

    public static void setupChatMessage(PlayerChatMessage chatMessage) {
        if ((tipsyMessageLevel > 0 ||
                Minecraft.getInstance().player.hasEffect(BnCEffects.TIPSY.value()) && Minecraft.getInstance().player.getEffect(BnCEffects.TIPSY.value()).getAmplifier() >= 3)) {

            if (!generatedRandom)
                randomSeed = Minecraft.getInstance().player.getRandom().nextLong();

            Component newMessage = modifyComponents(chatMessage.decoratedContent(), randomSeed);

            if (!chatMessage.decoratedContent().equals(newMessage))  {
                nextTipsyMessage = chatMessage.withUnsignedContent(newMessage);
            }
        }
    }

    public static PlayerChatMessage getTipsyMessage() {
        return nextTipsyMessage;
    }

    public static void clearTipsyMessage() {
        nextTipsyMessage = null;
    }

    /* I will probably have to put this back at some point
    public static MutableComponent getStyledChatPrefix(ChatType.Bound bound, Component message) {
        if (bound.chatType().is(BnCClientTextUtils.STYLED_CHAT_HACK_CHAT_TYPE)) {
            if (message.getContents() instanceof TranslatableContents boundTranslatable &&
                    boundTranslatable.getArgs()[0] instanceof MutableComponent innerMutable) {
                MutableComponent oldComponent = innerMutable.copy();
                if (oldComponent.getContents() instanceof TranslatableContents translatable) {
                    List<Object> args = new ArrayList<>(List.of(translatable.getArgs()));
                    args.removeLast();
                    args.add(Component.empty());
                    return Component.translatable(translatable.getKey(), args.toArray()).withStyle(oldComponent.getStyle());
                }
                return handleSiblings(Component.empty(), innerMutable, true);
            }
        }
        return message.copy();
    } */

    private static MutableComponent handleSiblings(MutableComponent newMessage, Component oldComponent, boolean originalCall) {

        newMessage.append(oldComponent.plainCopy().withStyle(oldComponent.getStyle()));

        if (oldComponent.getSiblings().isEmpty())
            return newMessage;

        for (int i = 0; i < oldComponent.getSiblings().size(); ++i) {
            if (!originalCall || i < oldComponent.getSiblings().size() - 1)
                handleSiblings(newMessage, oldComponent.getSiblings().get(i), false);
        }
        return newMessage;
    }

    public static Component nameTagRenderer(Component original) {
        if (true)
            if (Minecraft.getInstance().player != null) {
                if (Minecraft.getInstance().player.hasEffect(BnCEffects.TIPSY.value()) && Minecraft.getInstance().player.getEffect(BnCEffects.TIPSY.value()).getAmplifier() >= 3) {
                    return modifyComponents(original, 0L);
                }
            }
        return original;
    }

    public static SignText signRenderer(SignText signText) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return signText;
        }
        int minScrambleAmplifier = 3;

        if (player.hasEffect(BnCEffects.TIPSY.value()) && player.getEffect(BnCEffects.TIPSY.value()).getAmplifier() >= minScrambleAmplifier) {
            for (int i = 0; i < 4; i++) {
                signText = signText.setMessage(i, modifyComponents(signText.getMessage(i, false), 0L));
            }
        }
        return signText;
    }

}
