package slimeknights.mantle;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.registration.MantleData;
import slimeknights.mantle.util.OffhandCooldownTracker;

/**
 * Mantle
 *
 * Central mod object for Mantle (port of Mantle 1.11.117 for Forge 1.20.1 to NeoForge 26.3, Teil 27b).
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
@Mod(Mantle.modId)
public class Mantle {
  public static final String modId = "mantle";
  public static final Logger logger = LoggerFactory.getLogger("Mantle");
  /** Namespace for common tags: "c" on NeoForge 26.3 (was "forge" on 1.20.1) */
  public static final String COMMON = "c";

  /* Instance of this mod, used for grabbing prototype fields */
  public static Mantle instance;

  public Mantle(IEventBus bus) {
    instance = this;
    MantleData.init(bus);
    OffhandCooldownTracker.register(bus);
    // NeoForge registers payloads in RegisterPayloadHandlersEvent, before common setup
    MantleNetwork.registerPackets();
  }

  /**
   * Gets a resource location for Mantle
   * @param name  Name
   * @return  Resource location instance
   */
  public static Identifier getResource(String name) {
    return Identifier.fromNamespaceAndPath(modId, name);
  }

  /**
   * Gets a resource location for the common namespace, which is "c" on NeoForge.
   * @param name  Name
   * @return  Resource location instance
   */
  public static Identifier commonResource(String name) {
    return Identifier.fromNamespaceAndPath(COMMON, name);
  }

  /**
   * Makes a translation key for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static String makeDescriptionId(String base, String name) {
    return Util.makeDescriptionId(base, getResource(name));
  }

  /**
   * Makes a translation text component for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static MutableComponent makeComponent(String base, String name) {
    return Component.translatable(makeDescriptionId(base, name));
  }

  /**
   * Makes a translation text component for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @param args  Additional arguments to format strings
   * @return  Translation key
   */
  public static MutableComponent makeComponent(String base, String name, Object... args) {
    return Component.translatable(makeDescriptionId(base, name), args);
  }
}
