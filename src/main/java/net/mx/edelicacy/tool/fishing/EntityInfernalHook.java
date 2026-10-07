package net.mx.edelicacy.tool.fishing;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.data.DelicacyData;

/**
 * 熔岩浮漂：照原版 1.12 EntityFishHook 改写成在熔岩里钓鱼。
 * <ul>
 *   <li>浮漂免疫火焰，钩住的实体会被点燃 2 秒；收杆时 2% 钓起一只小岩浆怪</li>
 *   <li>开阔熔岩（{@link InfernalLavaCheck}）才可能出宝藏，战利品见 {@link InfernalFishingLoot}</li>
 *   <li>主人的浮漂 UUID 记在 DelicacyData；浮漂不存档，消失时清掉记录</li>
 * </ul>
 * 客户端与原版一样自行模拟运动（不插值），速度靠追踪器同步。
 */
public class EntityInfernalHook extends Entity implements IEntityAdditionalSpawnData {

    private static final DataParameter<Integer> DATA_HOOKED_ENTITY = EntityDataManager.createKey(EntityInfernalHook.class, DataSerializers.VARINT);

    private enum State {
        FLYING, HOOKED_IN_ENTITY, BOBBING
    }

    @Nullable
    private EntityPlayer angler;
    @Nullable
    private Entity caughtEntity;
    private State currentState = State.FLYING;
    private boolean inGround;
    private int ticksInGround;
    private int ticksInAir;
    private final FishingTimers timers = new FishingTimers();
    private boolean openLava = true;
    private int outOfLavaTime;
    private int luck;

    public EntityInfernalHook(World world) {
        super(world);
        setSize(0.25F, 0.25F);
        this.ignoreFrustumCheck = true;
        this.isImmuneToFire = true;
    }

    public EntityInfernalHook(World world, EntityPlayer angler, int luck, int lureSpeed) {
        this(world);
        this.angler = angler;
        this.luck = Math.max(0, luck);
        this.timers.lureSpeed = Math.max(0, lureSpeed);
        shoot();
        DelicacyData.get(angler).setInfernalHook(getUniqueID());
    }

    /** 服务端：玩家当前的熔岩浮漂；记录失效时顺手清掉 */
    @Nullable
    public static EntityInfernalHook getActiveHook(EntityPlayer player) {
        DelicacyData data = DelicacyData.get(player);
        UUID id = data.getInfernalHook();
        if (id == null || !(player.world instanceof WorldServer)) {
            return null;
        }
        Entity entity = ((WorldServer) player.world).getEntityFromUuid(id);
        if (entity instanceof EntityInfernalHook && !entity.isDead && ((EntityInfernalHook) entity).angler == player) {
            return (EntityInfernalHook) entity;
        }
        data.setInfernalHook(null);
        return null;
    }

    @Nullable
    public EntityPlayer getAngler() {
        return angler;
    }

    private void shoot() {
        EntityPlayer player = angler;
        float pitch = player.rotationPitch;
        float yaw = player.rotationYaw;
        float cosYaw = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float sinYaw = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float cosPitch = -MathHelper.cos(-pitch * 0.017453292F);
        float sinPitch = MathHelper.sin(-pitch * 0.017453292F);
        double x = player.posX - sinYaw * 0.3D;
        double y = player.posY + player.getEyeHeight();
        double z = player.posZ - cosYaw * 0.3D;
        setLocationAndAngles(x, y, z, yaw, pitch);
        motionX = -sinYaw;
        motionY = MathHelper.clamp(-(sinPitch / cosPitch), -5.0F, 5.0F);
        motionZ = -cosYaw;
        float length = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX *= 0.6D / length + 0.5D + rand.nextGaussian() * 0.0045D;
        motionY *= 0.6D / length + 0.5D + rand.nextGaussian() * 0.0045D;
        motionZ *= 0.6D / length + 0.5D + rand.nextGaussian() * 0.0045D;
        float horizontal = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float) (MathHelper.atan2(motionX, motionZ) * 57.29577951308232D);
        rotationPitch = (float) (MathHelper.atan2(motionY, horizontal) * 57.29577951308232D);
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
    }

    @Override
    protected void entityInit() {
        getDataManager().register(DATA_HOOKED_ENTITY, 0);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (DATA_HOOKED_ENTITY.equals(key)) {
            int id = getDataManager().get(DATA_HOOKED_ENTITY);
            caughtEntity = id > 0 ? world.getEntityByID(id - 1) : null;
        }
        super.notifyDataManagerChange(key);
    }

    private void setHookedEntity(@Nullable Entity entity) {
        caughtEntity = entity;
        getDataManager().set(DATA_HOOKED_ENTITY, entity == null ? 0 : entity.getEntityId() + 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return distance < 4096.0D;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int increments, boolean teleport) {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (angler == null) {
            setDead();
            return;
        }
        if (!world.isRemote && shouldStopFishing()) {
            return;
        }
        if (inGround && ++ticksInGround >= 1200) {
            setDead();
            return;
        }
        BlockPos blockpos = new BlockPos(this);
        IBlockState state = world.getBlockState(blockpos);
        boolean inLava = state.getMaterial() == Material.LAVA;
        float height = inLava ? liquidHeight(state, blockpos) : 0.0F;

        if (currentState == State.FLYING) {
            if (caughtEntity != null) {
                motionX = motionY = motionZ = 0.0D;
                currentState = State.HOOKED_IN_ENTITY;
                return;
            }
            if (height > 0.0F) {
                motionX *= 0.3D;
                motionY *= 0.2D;
                motionZ *= 0.3D;
                currentState = State.BOBBING;
                return;
            }
            if (!world.isRemote) {
                checkCollision();
            }
            if (!inGround && !onGround && !collidedHorizontally) {
                ++ticksInAir;
            } else {
                ticksInAir = 0;
                motionX = motionY = motionZ = 0.0D;
            }
        } else if (currentState == State.HOOKED_IN_ENTITY) {
            followHookedEntity();
            return;
        } else {
            bob(blockpos, height);
        }

        if (!inLava) {
            motionY -= 0.03D;
        }
        move(MoverType.SELF, motionX, motionY, motionZ);
        updateRotation();
        motionX *= 0.92D;
        motionY *= 0.92D;
        motionZ *= 0.92D;
        setPosition(posX, posY, posZ);
    }

    private void followHookedEntity() {
        if (caughtEntity == null) {
            return;
        }
        if (caughtEntity.isDead || caughtEntity.dimension != dimension) {
            if (!world.isRemote) {
                setHookedEntity(null);
            }
            caughtEntity = null;
            currentState = State.FLYING;
            return;
        }
        setPosition(caughtEntity.posX, caughtEntity.getEntityBoundingBox().minY + caughtEntity.height * 0.8D, caughtEntity.posZ);
        if (!world.isRemote && !caughtEntity.isBurning()) {
            caughtEntity.setFire(2);
        }
    }

    private void bob(BlockPos blockpos, float height) {
        motionX *= 0.9D;
        motionZ *= 0.9D;
        double offset = posY + motionY - blockpos.getY() - height;
        if (Math.abs(offset) < 0.01D) {
            offset += Math.signum(offset) * 0.1D;
        }
        motionY -= offset * rand.nextFloat() * 0.2D;
        if (world.isRemote) {
            return;
        }
        if (timers.catchable <= 0 && timers.catchableDelay <= 0) {
            openLava = true;
        } else {
            openLava = openLava && outOfLavaTime < 10 && InfernalLavaCheck.isOpenLava(world, blockpos);
        }
        if (height > 0.0F) {
            outOfLavaTime = Math.max(0, outOfLavaTime - 1);
            InfernalFishingTicker.tick(this, (WorldServer) world, blockpos);
        } else {
            outOfLavaTime = Math.min(10, outOfLavaTime + 1);
        }
    }

    /** 熔岩表面高度（0~1）；非原版熔岩按 Forge 流体的填充比例 */
    private float liquidHeight(IBlockState state, BlockPos pos) {
        if (state.getBlock() instanceof BlockLiquid) {
            return BlockLiquid.getBlockLiquidHeight(state, world, pos);
        }
        if (state.getBlock() instanceof IFluidBlock) {
            return Math.abs(((IFluidBlock) state.getBlock()).getFilledPercentage(world, pos));
        }
        return 0.9F;
    }

    private boolean shouldStopFishing() {
        EntityPlayer player = angler;
        boolean holding = player.getHeldItemMainhand().getItem() instanceof ItemInfernalFishingRod
                || player.getHeldItemOffhand().getItem() instanceof ItemInfernalFishingRod;
        boolean current = getUniqueID().equals(DelicacyData.get(player).getInfernalHook());
        if (!player.isDead && player.isEntityAlive() && holding && current && player.world == world && getDistanceSq(player) <= 1024.0D) {
            return false;
        }
        setDead();
        return true;
    }

    private void updateRotation() {
        float horizontal = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float) (MathHelper.atan2(motionX, motionZ) * 57.29577951308232D);
        rotationPitch = (float) (MathHelper.atan2(motionY, horizontal) * 57.29577951308232D);
        while (rotationPitch - prevRotationPitch < -180.0F) {
            prevRotationPitch -= 360.0F;
        }
        while (rotationPitch - prevRotationPitch >= 180.0F) {
            prevRotationPitch += 360.0F;
        }
        while (rotationYaw - prevRotationYaw < -180.0F) {
            prevRotationYaw -= 360.0F;
        }
        while (rotationYaw - prevRotationYaw >= 180.0F) {
            prevRotationYaw += 360.0F;
        }
        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
    }

    private void checkCollision() {
        Vec3d start = new Vec3d(posX, posY, posZ);
        Vec3d end = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
        RayTraceResult result = world.rayTraceBlocks(start, end, false, true, false);
        if (result != null) {
            end = new Vec3d(result.hitVec.x, result.hitVec.y, result.hitVec.z);
        }
        Entity hit = null;
        double best = 0.0D;
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this,
                getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(1.0D));
        for (Entity candidate : list) {
            if (!canBeHooked(candidate) || (candidate == angler && ticksInAir < 5)) {
                continue;
            }
            AxisAlignedBB box = candidate.getEntityBoundingBox().grow(0.30000001192092896D);
            RayTraceResult intercept = box.calculateIntercept(start, end);
            if (intercept != null) {
                double distance = start.squareDistanceTo(intercept.hitVec);
                if (distance < best || best == 0.0D) {
                    hit = candidate;
                    best = distance;
                }
            }
        }
        if (hit != null) {
            setHookedEntity(hit);
        } else if (result != null && result.typeOfHit != RayTraceResult.Type.MISS) {
            inGround = true;
        }
    }

    private boolean canBeHooked(Entity entity) {
        return entity.canBeCollidedWith() || entity instanceof EntityItem;
    }

    /** 收杆，返回钓竿损耗 */
    public int retrieve() {
        if (world.isRemote || angler == null) {
            return 0;
        }
        int damage = 0;
        if (caughtEntity != null) {
            bringInHookedEntity();
            world.setEntityState(this, (byte) 31);
            damage = caughtEntity instanceof EntityItem ? 3 : 5;
        } else if (timers.catchable > 0) {
            damage = InfernalFishingTicker.reelIn(this, (WorldServer) world, angler, luck);
        }
        if (inGround) {
            damage = 2;
        }
        setDead();
        return damage;
    }

    boolean isOpenLava() {
        return openLava;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 31 && world.isRemote && caughtEntity instanceof EntityPlayer && ((EntityPlayer) caughtEntity).isUser()) {
            bringInHookedEntity();
        }
        super.handleStatusUpdate(id);
    }

    private void bringInHookedEntity() {
        if (angler != null && caughtEntity != null) {
            double dx = angler.posX - posX;
            double dy = angler.posY - posY + getDistance(angler) * 0.02D;
            double dz = angler.posZ - posZ;
            caughtEntity.motionX += dx * 0.2D;
            caughtEntity.motionY += dy * 0.2D;
            caughtEntity.motionZ += dz * 0.2D;
            caughtEntity.velocityChanged = true;
        }
    }

    FishingTimers timers() {
        return timers;
    }

    java.util.Random random() {
        return rand;
    }

    // ---------------- 生命周期 ----------------

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    /** 不进传送门 */
    @Override
    public void setPortal(BlockPos pos) {
    }

    @Override
    public void setDead() {
        super.setDead();
        if (angler == null) {
            return;
        }
        if (world.isRemote) {
            ClientHookTracker.remove(angler, this);
            return;
        }
        DelicacyData data = DelicacyData.get(angler);
        if (getUniqueID().equals(data.getInfernalHook())) {
            data.setInfernalHook(null);
        }
    }

    /** 客户端：该玩家有没有在用的熔岩浮漂（钓竿模型用） */
    static boolean clientHasHook(EntityPlayer player) {
        return ClientHookTracker.hasHook(player);
    }

    /** 不写入区块存档（主人引用无法恢复） */
    @Override
    public boolean writeToNBTOptional(NBTTagCompound compound) {
        return false;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeInt(angler == null ? -1 : angler.getEntityId());
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        Entity owner = world.getEntityByID(buffer.readInt());
        angler = owner instanceof EntityPlayer ? (EntityPlayer) owner : null;
        if (angler != null) {
            ClientHookTracker.put(angler, this);
        }
    }
}
