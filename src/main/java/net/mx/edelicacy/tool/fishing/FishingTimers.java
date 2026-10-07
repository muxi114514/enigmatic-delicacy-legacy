package net.mx.edelicacy.tool.fishing;

/** 熔岩浮漂的钓鱼计时（服务端），由 {@link InfernalFishingTicker} 推进 */
final class FishingTimers {

    /** 上钩窗口剩余（原版 ticksCatchable，1.21 的 nibble） */
    int catchable;
    /** 鱼游近剩余（原版 ticksCatchableDelay，1.21 的 timeUntilHooked） */
    int catchableDelay;
    /** 等鱼来剩余（原版 ticksCaughtDelay，1.21 的 timeUntilLured） */
    int caughtDelay;
    float approachAngle;
    /** 饵钓等级 */
    int lureSpeed;
}
