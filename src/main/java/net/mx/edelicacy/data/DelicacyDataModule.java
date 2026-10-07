package net.mx.edelicacy.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.network.DelicacyNetwork;

/** 玩家数据的注册、挂载、死亡复制与同步 */
public final class DelicacyDataModule {

    private static final ResourceLocation KEY = new ResourceLocation(EnigmaticDelicacy.MODID, "delicacy_data");

    private DelicacyDataModule() {
    }

    public static void preInit() {
        CapabilityManager.INSTANCE.register(DelicacyData.class, new Storage(), DelicacyData::new);
        DelicacyNetwork.register(DelicacyDataSyncMessage.Handler.class, DelicacyDataSyncMessage.class, Side.CLIENT);
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    /** 立即把整份数据发给本人（登录、重生、换维度时） */
    public static void sync(EntityPlayerMP player) {
        DelicacyData data = player.getCapability(DelicacyData.CAPABILITY, null);
        if (data != null) {
            data.consumeDirty();
            DelicacyNetwork.CHANNEL.sendTo(new DelicacyDataSyncMessage(data.serialize()), player);
        }
    }

    public static final class Events {

        @SubscribeEvent
        public void onAttach(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof EntityPlayer) {
                event.addCapability(KEY, new Provider());
            }
        }

        /** 死亡后保留全部字段，只清零以太牛排计时（与原版一致）；换维度也会触发 Clone */
        @SubscribeEvent
        public void onClone(PlayerEvent.Clone event) {
            DelicacyData oldData = event.getOriginal().getCapability(DelicacyData.CAPABILITY, null);
            DelicacyData newData = event.getEntityPlayer().getCapability(DelicacyData.CAPABILITY, null);
            if (oldData != null && newData != null) {
                newData.deserialize(oldData.serialize());
                if (event.isWasDeath()) {
                    newData.setEtheriumSteakTick(0);
                }
                newData.markDirty();
            }
        }

        @SubscribeEvent
        public void onLogin(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
            if (event.player instanceof EntityPlayerMP) {
                sync((EntityPlayerMP) event.player);
            }
        }

        @SubscribeEvent
        public void onRespawn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {
            if (event.player instanceof EntityPlayerMP) {
                sync((EntityPlayerMP) event.player);
            }
        }

        @SubscribeEvent
        public void onChangeDimension(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
            if (event.player instanceof EntityPlayerMP) {
                sync((EntityPlayerMP) event.player);
            }
        }

        /** 有改动才同步，一 tick 至多一次 */
        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP)) {
                return;
            }
            DelicacyData data = event.player.getCapability(DelicacyData.CAPABILITY, null);
            if (data != null && data.consumeDirty()) {
                DelicacyNetwork.CHANNEL.sendTo(new DelicacyDataSyncMessage(data.serialize()), (EntityPlayerMP) event.player);
            }
        }
    }

    private static final class Provider implements ICapabilitySerializable<NBTTagCompound> {
        private final DelicacyData data = new DelicacyData();

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == DelicacyData.CAPABILITY;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == DelicacyData.CAPABILITY ? DelicacyData.CAPABILITY.cast(data) : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return data.serialize();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            data.deserialize(nbt);
        }
    }

    private static final class Storage implements Capability.IStorage<DelicacyData> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<DelicacyData> capability, DelicacyData instance, EnumFacing side) {
            return instance.serialize();
        }

        @Override
        public void readNBT(Capability<DelicacyData> capability, DelicacyData instance, EnumFacing side, NBTBase nbt) {
            if (nbt instanceof NBTTagCompound) {
                instance.deserialize((NBTTagCompound) nbt);
            }
        }
    }
}
