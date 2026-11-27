package slimeknights.mantle.network;

/**
 * Port: replaces Forge's {@code slimeknights.mantle.network.NetworkDirection} for {@link NetworkWrapper}, so packet
 * registrations keep their original form. Only the play directions exist (NeoForge payloads).
 */
public enum NetworkDirection {
  PLAY_TO_CLIENT,
  PLAY_TO_SERVER
}
