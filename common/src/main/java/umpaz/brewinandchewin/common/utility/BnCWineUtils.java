package umpaz.brewinandchewin.common.utility;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import umpaz.brewinandchewin.common.item.Distillate;
import umpaz.brewinandchewin.common.item.WineItem;
import umpaz.brewinandchewin.common.item.WineType;
import umpaz.brewinandchewin.common.item.component.FortifiedEffect;
import umpaz.brewinandchewin.common.item.component.WineContents;
import umpaz.brewinandchewin.common.registry.BnCDataComponents;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import umpaz.brewinandchewin.common.tag.BnCTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BnCWineUtils {
    public static final int BASE_DISTILLATE_DURATION = 3600;
    public static final int OLD_WINE_MIN_TIPSY_DURATION = 1200;
    public static final int OLD_WINE_MAX_TIPSY_DURATION = 12000;
    public static final float OLD_WINE_STRONG_EFFECT_CHANCE = 0.2F;

    private static final String[] POTION_STRENGTH_PREFIXES = {"strong_", "long_"};

    public static boolean isDistillate(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        if (stack.getItem() instanceof Distillate)
            return true;
        if (stack.is(BnCTags.Items.DISTILLATES))
            return true;
        return !getPotionEffects(stack).isEmpty();
    }

    public static Holder<Potion> getBasePotion(Holder<Potion> potion) {
        Optional<ResourceKey<Potion>> key = potion.unwrapKey();
        if (key.isEmpty())
            return potion;
        String path = key.get().location().getPath();
        for (String prefix : POTION_STRENGTH_PREFIXES) {
            if (!path.startsWith(prefix))
                continue;
            Optional<Holder.Reference<Potion>> base = BuiltInRegistries.POTION.getHolder(
                    ResourceKey.create(Registries.POTION, key.get().location().withPath(path.substring(prefix.length()))));
            if (base.isPresent())
                return base.get();
        }
        return potion;
    }

    public static List<MobEffectInstance> getPotionEffects(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null)
            return List.of();
        List<MobEffectInstance> effects = new ArrayList<>();
        contents.potion().ifPresent(potion -> {
            for (MobEffectInstance instance : getBasePotion(potion).value().getEffects()) {
                effects.add(new MobEffectInstance(instance));
            }
        });
        for (MobEffectInstance custom : contents.customEffects()) {
            effects.add(new MobEffectInstance(custom));
        }
        return effects;
    }

    public static List<MobEffectInstance> getDistillateEffects(ItemStack stack) {
        List<MobEffectInstance> effects = new ArrayList<>();
        if (stack.getItem() instanceof Distillate distillate) {
            effects.addAll(distillate.getDistillateEffects(stack));
        }
        effects.addAll(getPotionEffects(stack));
        return effects;
    }

    public static int getDistillateTipsyBonus(ItemStack stack) {
        return stack.getItem() instanceof Distillate distillate ? distillate.getTipsyBonus(stack) : 0;
    }

    public static boolean isSickeningDistillate(ItemStack stack) {
        return stack.getItem() instanceof Distillate distillate && distillate.isSickening(stack);
    }

    public static boolean isDeliciousDistillate(ItemStack stack) {
        return stack.getItem() instanceof Distillate distillate && distillate.isDelicious(stack);
    }

    public static WineContents getContents(ItemStack stack) {
        return stack.getOrDefault(BnCDataComponents.WINE_CONTENTS, WineContents.EMPTY);
    }

    public static void setContents(ItemStack stack, WineContents contents) {
        stack.set(BnCDataComponents.WINE_CONTENTS, contents);
        updateRarity(stack, contents);
    }

    public static void updateRarity(ItemStack stack, WineContents contents) {
        if (!(stack.getItem() instanceof WineItem wine))
            return;
        WineType type = wine.getWineType();
        stack.set(DataComponents.RARITY, contents.isFinelyAged(type)
                ? type.getFinelyAgedRarity()
                : stack.getItem().components().getOrDefault(DataComponents.RARITY, Rarity.COMMON));
    }

    public static int modifyDuration(WineType type, int duration) {
        return Math.max(1, Math.round(duration * type.getDurationModifier()));
    }

    public static int getDistillateDuration(WineType type) {
        return modifyDuration(type, BASE_DISTILLATE_DURATION);
    }

    public static WineContents age(WineType type, WineContents contents, List<ItemStack> distillates, RandomSource random) {
        WineContents result = contents.empowered();
        List<FortifiedEffect> effects = new ArrayList<>(result.effects());
        int added = 0;
        int tipsyBonus = result.tipsyBonus();
        boolean sickening = result.sickening();
        boolean delicious = result.delicious();

        for (ItemStack distillate : distillates) {
            if (distillate.isEmpty())
                continue;
            boolean consumed = false;
            for (MobEffectInstance base : getDistillateEffects(distillate)) {
                if (effects.stream().anyMatch(effect -> effect.is(base.getEffect())))
                    continue;
                effects.add(FortifiedEffect.of(scale(type, base)));
                consumed = true;
            }
            int bonus = getDistillateTipsyBonus(distillate);
            if (bonus > 0) {
                tipsyBonus += bonus;
                consumed = true;
            }
            if (isSickeningDistillate(distillate) && !sickening) {
                sickening = true;
                consumed = true;
            }
            if (isDeliciousDistillate(distillate) && !delicious) {
                delicious = true;
                consumed = true;
            }
            if (!consumed)
                continue;
            ++added;
            if (type.spawnsNegativeEffects() && random.nextFloat() < WineType.TWISTED_NEGATIVE_CHANCE) {
                getRandomEffect(BnCTags.Effects.TWISTED_WINE_AFFLICTIONS, random).ifPresent(affliction -> {
                    if (effects.stream().noneMatch(effect -> effect.is(affliction)))
                        effects.add(FortifiedEffect.of(createInstance(affliction, getDistillateDuration(type), 0)));
                });
            }
        }

        return new WineContents(List.copyOf(effects), result.distillates() + added,
                result.agingCycles() + 1, tipsyBonus, sickening, delicious, result.variant());
    }

    public static WineContents createOldWine(RandomSource random) {
        List<FortifiedEffect> effects = new ArrayList<>();
        int duration = getDistillateDuration(WineType.OLD);
        int count = random.nextInt(WineType.OLD.getDistillateCapacity()) + 1;
        for (int i = 0; i < count; ++i) {
            Optional<Holder<MobEffect>> effect = getRandomEffect(BnCTags.Effects.OLD_WINE_EFFECTS, random);
            if (effect.isEmpty())
                break;
            if (effects.stream().anyMatch(instance -> instance.is(effect.get())))
                continue;
            boolean strong = random.nextFloat() < OLD_WINE_STRONG_EFFECT_CHANCE;
            effects.add(new FortifiedEffect(createInstance(effect.get(), duration, strong ? 1 : 0), strong ? 1 : 0));
        }
        effects.add(FortifiedEffect.finished(new MobEffectInstance(BnCEffects.TIPSY,
                random.nextInt(OLD_WINE_MIN_TIPSY_DURATION, OLD_WINE_MAX_TIPSY_DURATION),
                WineType.OLD.getTipsyAmplifier())));
        return new WineContents(List.copyOf(effects), WineType.OLD.getDistillateCapacity(), 1, 0, false, false,
                random.nextInt(WineType.OLD_WINE_VARIANTS));
    }

    public static List<MobEffectInstance> getDrinkEffects(WineType type, WineContents contents) {
        List<MobEffectInstance> effects = new ArrayList<>();
        Optional<MobEffectInstance> storedTipsy = contents.effects().stream()
                .filter(effect -> effect.is(BnCEffects.TIPSY)).map(FortifiedEffect::effect).findFirst();
        int tipsyDuration = storedTipsy.map(MobEffectInstance::getDuration).orElse(type.getTipsyDuration());
        int tipsyAmplifier = storedTipsy.map(MobEffectInstance::getAmplifier).orElse(type.getTipsyAmplifier()) + contents.tipsyBonus();
        effects.add(new MobEffectInstance(BnCEffects.TIPSY, tipsyDuration, Math.min(tipsyAmplifier, 9)));

        for (FortifiedEffect effect : contents.effects()) {
            if (effect.is(BnCEffects.TIPSY))
                continue;
            effects.add(new MobEffectInstance(effect.effect()));
        }

        int extra = extraDistillates(type, contents);
        if (type.addsIntoxicationPerDistillate() && extra > 0) {
            effects.add(new MobEffectInstance(BnCEffects.INTOXICATION,
                    extra * WineType.INTOXICATION_PER_EXTRA_DISTILLATE, 0, false, false));
        }
        if (type.poisonsWhenOverFortified() && contents.distillates() >= WineType.VERRUCA_POISON_THRESHOLD) {
            int poisoning = contents.distillates() - WineType.VERRUCA_POISON_THRESHOLD + 1;
            effects.add(new MobEffectInstance(MobEffects.POISON, poisoning * WineType.POISON_PER_EXTRA_DISTILLATE, 0));
        }
        return effects;
    }

    public static int extraDistillates(WineType type, WineContents contents) {
        if (!type.addsIntoxicationPerDistillate())
            return 0;
        return Math.max(0, contents.distillates() - 1);
    }

    private static MobEffectInstance scale(WineType type, MobEffectInstance base) {
        if (base.getEffect().value().isInstantenous())
            return new MobEffectInstance(base.getEffect(), 1, base.getAmplifier());
        return new MobEffectInstance(base.getEffect(), modifyDuration(type, base.getDuration()), base.getAmplifier(),
                base.isAmbient(), base.isVisible(), base.showIcon());
    }

    private static MobEffectInstance createInstance(Holder<MobEffect> effect, int duration, int amplifier) {
        return new MobEffectInstance(effect, effect.value().isInstantenous() ? 1 : duration, amplifier);
    }

    private static Optional<Holder<MobEffect>> getRandomEffect(TagKey<MobEffect> tag, RandomSource random) {
        Optional<HolderSet.Named<MobEffect>> holders = BuiltInRegistries.MOB_EFFECT.getTag(tag);
        if (holders.isEmpty() || holders.get().size() == 0)
            return Optional.empty();
        return holders.get().getRandomElement(random);
    }
}
