package slimeknights.mantle.network;

import slimeknights.mantle.network.NetworkDirection;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferPacket;

public class MantleNetwork {
  /**
   * Network instance
   * 1: 1.11.101 and before
   * 2: 1.11.102 - New predicate types, enum loadable nullable field optimization
   * 3: 1.11.108 - New export book command
   */
  public static final NetworkWrapper INSTANCE = new NetworkWrapper(Mantle.getResource("network"), "3");

  /**
   * Registers packets into this network
   */
  public static void registerPackets() {
    INSTANCE.registerPacket(FluidContainerTransferPacket.class, FluidContainerTransferPacket::new, NetworkDirection.PLAY_TO_CLIENT);
    // PORT-TODO(27c): book packets (OpenLecternBook, UpdateHeldPage,
    // UpdateInventoryPage, UpdateLecternPage, DropLecternBook, OpenNamedBook). SwingArmPacket is gone (vanilla swing).
  }
}
