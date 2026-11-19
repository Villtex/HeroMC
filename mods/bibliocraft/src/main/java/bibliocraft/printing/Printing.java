package bibliocraft.printing;

import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Writing desk ({@link Desks}), typesetting table, printing press, chase, plate and enchanted plate ({@link Presses}).
 *
 * <p>A book is printed in three steps: a signed book is put on the typesetting table and saved to a file
 * ({@link BookFiles}, sneak-click), the book is chosen in the table's list and a redstone signal turns a chase into a
 * plate with the book's name; the plate, ink sacs and blank books in the printing press make copies of the saved book.
 * An enchanted book on the table makes an enchanted plate (costs levels), which prints the book's enchantments three
 * times. The packets of BiblioPacketeer ("BiblioMCBEdit", "BiblioMCBPage", "BiblioType") are {@link PrintingNetwork}.
 */
public final class Printing {
    /** Client-only actions the blocks start (the screens that 1.5.2 opened straight from onBlockActivated). */
    public interface ClientHooks {
        /** GuiScreenBookWriting for the open book on the desk at {@code pos}; {@code editable} for a writable book. */
        void openBook(BlockPos pos, boolean editable);

        /** GuiTypesetting: the list of saved books for the typesetting table at {@code pos}. */
        void openTypesetting(BlockPos pos);
    }

    /** Set by the client (PrintingClient); nothing on a dedicated server. */
    public static ClientHooks client = new ClientHooks() {
        @Override
        public void openBook(BlockPos pos, boolean editable) {
        }

        @Override
        public void openTypesetting(BlockPos pos) {
        }
    };

    private Printing() {
    }

    /** The writing desk comes first in BlockLoader. */
    public static void initDesk(IEventBus modBus) {
        Desks.load();
    }

    public static void init(IEventBus modBus) {
        Presses.load();
        modBus.addListener(RegisterPayloadHandlersEvent.class, PrintingNetwork::register);
    }
}
