package net.mx.edelicacy.enigmatic.api;

/**
 * 谜之食物：吃下后永久获得对应颜色谜之护符一半的加成（见 EnigmaticFoodBonuses）。
 * <p>附属模组的食物实现本接口并在食用完成时调用 {@code EnigmaticFoodEffects.markEaten} 即可接入。
 */
public interface IEnigmaticFood {

    EnigmaticColor getEnigmaticColor();
}
