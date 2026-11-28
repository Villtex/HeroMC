package slimeknights.tconstruct.library.utils;

import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * Helper for use with our extensions of resource location for some type safety in IDs.
 * Note we left {@link Identifier#withPath(String)} and alike as returning {@link Identifier} as there is not much use extending an ID.
 * @see IdParser
 */
public abstract class ResourceId implements Comparable<ResourceId> {
  /*
   * 26.3: Identifier is final, so the Tinkers IDs (MaterialId, ModifierId, ToolStatId, MaterialStatsId) wrap one
   * instead of extending it. They keep the Identifier methods Tinkers calls; {@link #location()} gives the Identifier
   * for registries, codecs and maps keyed by Identifier. Equality is by namespace and path among ResourceIds (as in
   * 1.20.1, where all of them were Identifiers).
   */
  private final Identifier location;

  protected ResourceId(String namespace, String path, @Nullable Void pDummy) {
    this.location = Identifier.fromNamespaceAndPath(namespace, path);
  }

  public ResourceId(Identifier location) {
    this.location = location;
  }

  public ResourceId(String namespace, String path) {
    this.location = Identifier.fromNamespaceAndPath(namespace, path);
  }

  public ResourceId(String location) {
    this.location = Identifier.parse(location);
  }

  /** The identifier of this ID */
  public Identifier location() {
    return location;
  }

  public String getNamespace() {
    return location.getNamespace();
  }

  public String getPath() {
    return location.getPath();
  }

  public String toLanguageKey() {
    return location.toLanguageKey();
  }

  public String toLanguageKey(String type) {
    return location.toLanguageKey(type);
  }

  public String toLanguageKey(String type, String suffix) {
    return location.toLanguageKey(type, suffix);
  }

  public String toShortLanguageKey() {
    return location.toShortLanguageKey();
  }

  public Identifier withPath(String path) {
    return location.withPath(path);
  }

  public Identifier withPrefix(String prefix) {
    return location.withPrefix(prefix);
  }

  public Identifier withSuffix(String suffix) {
    return location.withSuffix(suffix);
  }

  @Override
  public String toString() {
    return location.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof ResourceId other && location.equals(other.location);
  }

  @Override
  public int hashCode() {
    return location.hashCode();
  }

  @Override
  public int compareTo(ResourceId other) {
    return location.compareTo(other.location);
  }


  /* Helpers for static constructors */

  /**
   * Creates a new ID from the given string
   * @param string  String
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T extends ResourceId> T tryParse(String string, BiFunction<String,String,T> constructor) {
    int index = string.indexOf(':');
    String namespace = Identifier.DEFAULT_NAMESPACE;
    String path = string;
    if (index >= 0) {
      path = string.substring(index + 1);
      if (index >= 1) {
        namespace = string.substring(0, index);
      }
    }
    return tryBuild(namespace, path, constructor);
  }

  /**
   * Creates a new ID from the given namespace and path
   * @param namespace  Namespace
   * @param path       Path
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T extends ResourceId> T tryBuild(String namespace, String path, BiFunction<String,String,T> constructor) {
    if (Identifier.isValidNamespace(namespace) && Identifier.isValidPath(path)) {
      return constructor.apply(namespace, path);
    }
    return null;
  }
}
