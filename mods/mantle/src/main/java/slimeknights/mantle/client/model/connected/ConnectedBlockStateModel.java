package slimeknights.mantle.client.model.connected;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.joml.Matrix4fc;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.block.IMultipartConnectedBlock;
import slimeknights.mantle.client.model.util.QuadRetexture;
import slimeknights.mantle.client.model.util.QuadRetexture.StatedElement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Port (Teil 27c): world aware part of {@link ConnectedModel}. 26.3 only gives block state models access to the
 * neighbours, so a block state variant {@code {"type": "mantle:connected", "model": ...}} renders connected textures.
 * Faces using a connected texture are baked on their own while loading; at runtime their sprite is swapped for the one
 * of the current connections (the original rebaked the elements per connection, which needs a model baker).
 */
public class ConnectedBlockStateModel implements DynamicBlockStateModel {
  public static final Identifier ID = Mantle.getResource("connected");

  /** Face whose texture follows the connections */
  private record ConnectedFace(QuadCollection quads, String texture, Function<Direction,Direction> transform, Map<String,TextureAtlasSprite> sprites) {}

  private final ConnectedModel model;
  private final BlockStateModelPart plain;
  private final QuadCollection staticQuads;
  private final List<ConnectedFace> faces;
  private final Matrix4fc rotation;
  /** Model part per connection set (6 bits, one per direction) */
  private final BlockStateModelPart[] cache = new BlockStateModelPart[64];

  private ConnectedBlockStateModel(ConnectedModel model, BlockStateModelPart plain, QuadCollection staticQuads, List<ConnectedFace> faces, Matrix4fc rotation) {
    this.model = model;
    this.plain = plain;
    this.staticQuads = staticQuads;
    this.faces = faces;
    this.rotation = rotation;
  }

  /** Gets the model part for the given connections */
  private BlockStateModelPart getPart(int connections) {
    BlockStateModelPart part = cache[connections];
    if (part == null) {
      QuadCollection.Builder builder = new QuadCollection.Builder().addAll(staticQuads);
      for (ConnectedFace face : faces) {
        String suffix = model.getTextureSuffix(face.texture, connections, face.transform);
        TextureAtlasSprite sprite = face.sprites.get(suffix);
        if (sprite == null) {
          builder.addAll(face.quads);
        } else {
          QuadRetexture.addSwapped(builder, face.quads, old -> sprite);
        }
      }
      part = new SimpleModelWrapper(builder.build(), plain.useAmbientOcclusion(), plain.particleMaterial());
      cache[connections] = part;
    }
    return part;
  }

  /** Direction of the world for a direction of the model */
  private Direction worldDirection(Direction dir) {
    return Direction.rotate(rotation, dir);
  }

  /** Connections from the neighbours in the world */
  private int getConnections(BlockAndTintGetter level, BlockPos pos, BlockState state) {
    int connections = 0;
    for (Direction dir : Direction.values()) {
      if (model.sides.contains(dir) && model.connectionPredicate.test(state, level.getBlockState(pos.relative(worldDirection(dir))))) {
        connections |= 1 << dir.get3DDataValue();
      }
    }
    return connections;
  }

  /** Connections from the block state properties of {@link IMultipartConnectedBlock} (no world, e.g. items) */
  private int getStateConnections(BlockState state) {
    int connections = 0;
    for (Direction dir : Direction.values()) {
      if (model.sides.contains(dir)) {
        BooleanProperty prop = IMultipartConnectedBlock.CONNECTED_DIRECTIONS.get(worldDirection(dir));
        if (state.hasProperty(prop) && state.getValue(prop)) {
          connections |= 1 << dir.get3DDataValue();
        }
      }
    }
    return connections;
  }

  @Override
  public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
    int connections = level == BlockAndTintGetter.EMPTY ? getStateConnections(state) : getConnections(level, pos, state);
    parts.add(getPart(connections));
  }

  @Nullable
  @Override
  public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
    return new GeometryKey(this, level == BlockAndTintGetter.EMPTY ? getStateConnections(state) : getConnections(level, pos, state));
  }

  private record GeometryKey(ConnectedBlockStateModel model, int connections) {}

  @Override
  public Material.Baked particleMaterial() {
    return plain.particleMaterial();
  }

  @Override
  public int materialFlags() {
    return plain.materialFlags();
  }

  /** Block state model {@code "type": "mantle:connected"}, same fields as a vanilla variant */
  public record Unbaked(Variant variant) implements CustomUnbakedBlockStateModel {
    public static final MapCodec<Unbaked> MAP_CODEC = Variant.MAP_CODEC.xmap(Unbaked::new, Unbaked::variant);

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
      return MAP_CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
      variant.resolveDependencies(resolver);
    }

    @Override
    public BlockStateModel bake(ModelBaker baker) {
      BlockStateModelPart plain = variant.bake(baker);
      ResolvedModel resolved = baker.getModel(variant.modelLocation());
      ConnectedModel connected = ConnectedModel.find(resolved);
      ModelState state = variant.modelState().asModelState();
      List<StatedElement> elements = connected == null ? null : QuadRetexture.elements(resolved, state);
      if (connected == null || elements == null) {
        return new SingleVariant(plain);
      }
      TextureSlots slots = resolved.getTopTextureSlots();
      List<TextureSlots.Data> chain = QuadRetexture.textureChain(resolved);
      QuadCollection.Builder staticQuads = new QuadCollection.Builder();
      List<ConnectedFace> faces = new ArrayList<>();
      Map<String,Map<String,TextureAtlasSprite>> spriteCache = new HashMap<>();
      for (StatedElement stated : elements) {
        CuboidModelElement element = stated.element();
        Map<Direction,CuboidFace> plainFaces = new EnumMap<>(Direction.class);
        for (Map.Entry<Direction,CuboidFace> entry : element.faces().entrySet()) {
          CuboidFace face = entry.getValue();
          String texture = QuadRetexture.findTextureName(face.texture(), chain, connected.connectedTextures::containsKey);
          if (texture.isEmpty() || slots.getMaterial(texture) == null) {
            plainFaces.put(entry.getKey(), face);
          } else {
            QuadRetexture.BakedFace baked = QuadRetexture.bakeFace(element, entry.getKey(), face, slots, baker, stated.state(), resolved);
            Map<String,TextureAtlasSprite> sprites = spriteCache.computeIfAbsent(texture, name -> suffixSprites(connected, name, slots, baker, resolved));
            faces.add(new ConnectedFace(baked.quads(), texture, ConnectedModel.getTransform(entry.getKey(), face.uvs(), face.rotation()), sprites));
          }
        }
        if (!plainFaces.isEmpty()) {
          staticQuads.addAll(QuadRetexture.bake(List.of(QuadRetexture.withFaces(element, plainFaces)), slots, baker, stated.state(), resolved));
        }
      }
      return new ConnectedBlockStateModel(connected, plain, staticQuads.build(), faces, state.transformation().getMatrix());
    }

    /** Sprites of all suffixes of a connected texture, keyed by suffix with the leading underscore */
    private static Map<String,TextureAtlasSprite> suffixSprites(ConnectedModel connected, String name, TextureSlots slots, ModelBaker baker, ResolvedModel resolved) {
      Material base = slots.getMaterial(name);
      Map<String,TextureAtlasSprite> sprites = new HashMap<>();
      for (String suffix : connected.connectedTextures.get(name)) {
        if (suffix.isEmpty() || sprites.containsKey("_" + suffix)) {
          continue;
        }
        // allow overriding a specific texture, else the texture path with the suffix as folder entry
        Material material = slots.getMaterial(name + "_" + suffix);
        if (material == null) {
          Identifier sprite = base.sprite();
          material = new Material(sprite.withPath(sprite.getPath() + "/" + suffix), base.forceTranslucent());
        }
        sprites.put("_" + suffix, baker.materials().get(material, resolved).sprite());
      }
      return sprites;
    }
  }
}
