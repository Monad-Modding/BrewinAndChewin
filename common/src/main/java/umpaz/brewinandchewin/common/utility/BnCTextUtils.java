package umpaz.brewinandchewin.common.utility;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCEffects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BnCTextUtils {
    public static PlayerChatMessage setupChatMessageServer(PlayerChatMessage chatMessage, ServerPlayer sender, long randomSeed) {
        if (sender.hasEffect(BnCEffects.TIPSY.value()) && sender.getEffect(BnCEffects.TIPSY.value()).getAmplifier() >= 3) {
            int amplifier = sender.getEffect(BnCEffects.TIPSY.value()).getAmplifier();
            amplifier = amplifier - 3;
            RandomSource random = RandomSource.create(randomSeed);

            StringBuilder textBuilder = new StringBuilder(chatMessage.decoratedContent().getString());

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
            if (!chatMessage.signedContent().equals(text))  {
                Style style = chatMessage.decoratedContent().getStyle();
                return chatMessage.withUnsignedContent(Component.literal(text).withStyle(style));
            }
        }
        return chatMessage;
    }

    public static MutableComponent getTranslation(String key, Object... args) {
        return Component.translatable(BrewinAndChewin.MODID + "." + key, args);
    }
}
