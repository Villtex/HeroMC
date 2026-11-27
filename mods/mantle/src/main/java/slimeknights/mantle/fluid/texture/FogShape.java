package slimeknights.mantle.fluid.texture;

/**
 * Port (Teil 27c): fog shape of a fluid texture. 26.3 has no fog shapes any more (Blaze3D {@code FogShape} is gone); the
 * value is still read from fluid texture JSON so the files stay valid, the start and end distances are applied.
 */
public enum FogShape {
  SPHERE, CYLINDER
}
