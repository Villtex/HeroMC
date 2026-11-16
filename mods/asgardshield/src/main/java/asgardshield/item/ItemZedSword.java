package asgardshield.item;

/**
 * ItemZedSword: the giant sword, a heavy sword that guards like a shield (without perks, see
 * {@link asgardshield.guard.ASGiantSwordPerks}).
 */
public class ItemZedSword extends AbstractGuardItem {
    public ItemZedSword(Properties properties, GuardSound sound, float damageMultiplier, Special special) {
        super(properties, sound, damageMultiplier, -0.1F, special);
    }
}
