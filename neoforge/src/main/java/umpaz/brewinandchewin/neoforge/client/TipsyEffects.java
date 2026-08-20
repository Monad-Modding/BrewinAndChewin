package umpaz.brewinandchewin.neoforge.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;
import umpaz.brewinandchewin.neoforge.client.integration.IntoxicationAppleSkinCompatNeoForge;

@EventBusSubscriber(modid = BrewinAndChewin.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class TipsyEffects {
    @SubscribeEvent
    public static void whatsYourName(RenderNameTagEvent event) {
//        Component newName = BnCClientTextUtils.nameTagRenderer(event.getContent());
//        if (event.getContent() != newName)
//            event.setContent(newName);
    }

    @SubscribeEvent
    public static void iCanHear(ClientChatReceivedEvent.Player event) {
        BnCClientTextUtils.setupChatMessage(event.getPlayerChatMessage().withUnsignedContent(getChatMessage(event.getMessage())));
        PlayerChatMessage tipsyMessage = BnCClientTextUtils.getTipsyMessage();
        if (tipsyMessage != null && event.getBoundChatType() != null) {
            BnCClientTextUtils.clearTipsyMessage();

            MutableComponent boundChat = BnCClientTextUtils.getStyledChatPrefix(event.getBoundChatType(), event.getBoundChatType().decorate(Component.literal("")).copy());
            MutableComponent newMessage = tipsyMessage.decoratedContent().copy().withStyle(event.getBoundChatType().chatType().value().chat().style());

            event.setMessage(boundChat.append(newMessage));
        }
        BnCClientTextUtils.clearTipsyMessage();

        if (BnCClientTextUtils.clearDelayAmount <= 0) {
            BnCClientTextUtils.tipsyMessageLevel = 0;
            BnCClientTextUtils.randomSeed = 0L;
            BnCClientTextUtils.generatedRandom = false;
        } else {
            --BnCClientTextUtils.clearDelayAmount;
        }
    }

    private static Component getChatMessage(Component component) {
        if (component.getContents() instanceof TranslatableContents translatable) {
            Object[] args = translatable.getArgs();
            if (args.length > 1 && args[1] instanceof Component content)
                return content;
        }
        return component;
    }

    static {
        if (ModList.get().isLoaded("appleskin"))
            IntoxicationAppleSkinCompatNeoForge.init();
    }
}