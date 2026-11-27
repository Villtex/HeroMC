/**
 * Port: Forge 1.20.1 item handler API ({@code IItemHandler}, {@code IItemHandlerModifiable}, {@code ItemStackHandler},
 * {@code ItemHandlerHelper}, {@code InvWrapper}, {@code SidedInvWrapper}, {@code EmptyHandler}, {@code SlotItemHandler})
 * on top of NeoForge 26.3's transfer API, see {@link slimeknights.mantle.inventory.compat.ItemHandlers} for the adapters
 * and capability lookups. Mantle and Tinkers' Construct 3 keep their slot based inventory logic this way.
 */
@ParametersAreNonnullByDefault
package slimeknights.mantle.inventory.compat;

import javax.annotation.ParametersAreNonnullByDefault;
