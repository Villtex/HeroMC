package slimeknights.mantle.network;

import slimeknights.mantle.network.NetworkDirection;
import slimeknights.mantle.Mantle;

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
    // PORT-TODO(27b fluid): FluidContainerTransferPacket; PORT-TODO(27c): book packets (OpenLecternBook, UpdateHeldPage,
    // UpdateInventoryPage, UpdateLecternPage, DropLecternBook, OpenNamedBook). SwingArmPacket is gone (vanilla swing).
  }
}
