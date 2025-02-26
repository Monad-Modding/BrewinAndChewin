package umpaz.brewinandchewin.common.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.network.BnCNetworkHandler;
import umpaz.brewinandchewin.common.network.clientbound.SyncNumbedHeartsClientboundPacket;

public class TipsyNumbedHeartsCapability implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("tipsy_numbed_hearts");
    public static final Capability<TipsyNumbedHeartsCapability> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});
    private final LazyOptional<TipsyNumbedHeartsCapability> thisOptional = LazyOptional.of(() -> this);

    private final LivingEntity provider;

    public TipsyNumbedHeartsCapability(LivingEntity provider) {
        this.provider = provider;
    }

    private float numbedHealth = 0.0f;
    private int ticksUntilDamage = 0;

    public float getNumbedHealth() {
        return numbedHealth;
    }

    public void setNumbedHealth(float value) {
        numbedHealth = value;
    }

    public int getTicksUntilDamage() {
        return ticksUntilDamage;
    }

    public void setTicksUntilDamage(int value) {
        ticksUntilDamage = value;
    }

    public void setFrom(TipsyNumbedHeartsCapability cap) {
        if (cap == null)
            return;
        numbedHealth = cap.numbedHealth;
        ticksUntilDamage = cap.ticksUntilDamage;
    }

    public void syncToPlayer(ServerPlayer player) {
        if (provider.level().isClientSide())
            return;
        BnCNetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncNumbedHeartsClientboundPacket(provider.getId(), numbedHealth, ticksUntilDamage));
    }

    public void sync() {
        if (provider.level().isClientSide())
            return;
        BnCNetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> provider), new SyncNumbedHeartsClientboundPacket(provider.getId(), numbedHealth, ticksUntilDamage));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return INSTANCE.orEmpty(cap, thisOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("numbed_health", numbedHealth);
        tag.putInt("ticks_until_damage", ticksUntilDamage);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        numbedHealth = nbt.getFloat("numbed_health");
        ticksUntilDamage = nbt.getInt("ticks_until_damage");
    }
}
