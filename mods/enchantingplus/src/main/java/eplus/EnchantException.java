package eplus;

import net.minecraft.network.chat.Component;

/** The exceptions of canPurchase / enchant; the GUI shows the message, the server logs it. */
public class EnchantException extends Exception {
    private final Component component;

    public EnchantException(Component component) {
        super(component.getString());
        this.component = component;
    }

    public Component component() {
        return this.component;
    }
}
