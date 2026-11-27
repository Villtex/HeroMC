package slimeknights.mantle.client.model.connected;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.math.Quadrant;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import slimeknights.mantle.client.model.util.ColoredBlockModel;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Model that handles generating variants for connected textures ({@code "loader": "mantle:connected"}).
 * Port (Teil 27c): the model file only holds the connection data and bakes like a normal (coloured) model; the world
 * aware part is the block state model {@link ConnectedBlockStateModel} ({@code "type": "mantle:connected"} in the
 * block state, added by the asset import of the build).
 */
public class ConnectedModel extends ColoredBlockModel {
  /** Loader instance */
  public static final UnbakedModelLoader<ConnectedModel> LOADER = ConnectedModel::deserialize;

  /** Map of texture name to index of suffixes (indexed as 0bENWS) */
  final Map<String,String[]> connectedTextures;
  /** Function to run to check if this block connects to another */
  final BiPredicate<BlockState,BlockState> connectionPredicate;
  /** List of sides to check when getting block directions */
  final Set<Direction> sides;

  protected ConnectedModel(CuboidModel delegate, ColoredGeometry geometry, Map<String,String[]> connectedTextures, BiPredicate<BlockState,BlockState> connectionPredicate, Set<Direction> sides) {
    super(delegate, geometry);
    this.connectedTextures = connectedTextures;
    this.connectionPredicate = connectionPredicate;
    this.sides = sides;
  }

  /** Finds the connected model in the parent chain of a resolved model */
  @Nullable
  public static ConnectedModel find(@Nullable ResolvedModel model) {
    for (int i = 0; model != null && i < 64; i++, model = model.parent()) {
      if (model.wrapped() instanceof ConnectedModel connected) {
        return connected;
      }
    }
    return null;
  }

  /* Connection logic of the original, unchanged */

  /**
   * Gets the direction rotated
   * @param direction  Original direction to rotate
   * @param rotation   Rotation origin, aka the face of the block we are looking at. As a result, UP is identity
   * @return  Rotated direction
   */
  static Direction rotateDirection(Direction direction, Direction rotation) {
    if (rotation == Direction.UP) {
      return direction;
    }
    if (rotation == Direction.DOWN) {
      // Z is backwards on the bottom
      if (direction.getAxis() == Axis.Z) {
        return direction.getOpposite();
      }
      // X is normal
      return direction;
    }
    // sides all just have the next side for left and right, and consistent up and down
    return switch (direction) {
      case NORTH -> Direction.UP;
      case SOUTH -> Direction.DOWN;
      case EAST -> rotation.getCounterClockWise();
      case WEST -> rotation.getClockWise();
      default -> throw new IllegalArgumentException("Direction must be horizontal axis");
    };
  }

  /**
   * Gets a transform function based on the block part UV and block face
   * @param face   Block face in question
   * @param uv     Block UV data, null if the face uses the default UVs
   * @param uvRotation  Rotation of the face texture
   * @return  Direction transform function
   */
  static Function<Direction,Direction> getTransform(Direction face, @Nullable CuboidFace.UVs uv, Quadrant uvRotation) {
    // final transform switches from face (NSWE) to world direction, the rest are composed in to apply first
    Function<Direction,Direction> transform = (d) -> rotateDirection(d, face);
    // flipping
    if (uv != null) {
      boolean flipV = uv.minV() > uv.maxV();
      if (uv.minU() > uv.maxU()) {
        // flip both
        if (flipV) {
          transform = transform.compose(Direction::getOpposite);
        } else {
          // flip U
          transform = transform.compose((d) -> {
            if (d.getAxis() == Axis.X) {
              return d.getOpposite();
            }
            return d;
          });
        }
      } else if (flipV) {
        transform = transform.compose((d) -> {
          if (d.getAxis() == Axis.Z) {
            return d.getOpposite();
          }
          return d;
        });
      }
    }
    // rotation
    return switch (uvRotation) {
      // 90 degrees
      case R90 -> transform.compose(Direction::getClockWise);
      case R180 -> transform.compose(Direction::getOpposite);
      case R270 -> transform.compose(Direction::getCounterClockWise);
      default -> transform;
    };
  }

  /**
   * Gets the texture suffix
   * @param texture      Texture name, must be a connected texture
   * @param connections  Connections byte
   * @param transform    Rotations to apply to faces
   * @return  Suffix including the leading underscore, empty for the base texture
   */
  String getTextureSuffix(String texture, int connections, Function<Direction,Direction> transform) {
    int key = 0;
    for (Direction dir : Plane.HORIZONTAL) {
      int flag = 1 << transform.apply(dir).get3DDataValue();
      if ((connections & flag) == flag) {
        key |= 1 << dir.get2DDataValue();
      }
    }
    // if empty, do not prefix
    String[] suffixes = connectedTextures.get(texture);
    assert suffixes != null;
    String suffix = suffixes[key];
    if (suffix.isEmpty()) {
      return suffix;
    }
    return "_" + suffix;
  }

  /** Loader class containing singleton instance */
  public static ConnectedModel deserialize(JsonObject json, JsonDeserializationContext context) {
    CuboidModel model = readVanilla(json, context);
    ColoredGeometry geometry = readGeometry(model, json);
    // root object for all model data
    JsonObject data = GsonHelper.getAsJsonObject(json, "connection");
    // need at least one connected texture
    JsonObject connected = GsonHelper.getAsJsonObject(data, "textures");
    if (connected.size() == 0) {
      throw new JsonSyntaxException("Must have at least one texture in connected");
    }
    // build texture list
    Map<String,String[]> connectedTextures = new HashMap<>(connected.size());
    for (Entry<String,JsonElement> entry : connected.entrySet()) {
      // don't validate texture as it may be contained in a child model that is not yet loaded
      // get type, put in map
      String name = entry.getKey();
      connectedTextures.put(name, ConnectedModelRegistry.deserializeType(entry.getValue(), "textures[" + name + "]"));
    }
    // get a list of sides to pay attention to
    Set<Direction> sides;
    if (data.has("sides")) {
      JsonArray array = GsonHelper.getAsJsonArray(data, "sides");
      sides = EnumSet.noneOf(Direction.class);
      for (int i = 0; i < array.size(); i++) {
        String side = GsonHelper.convertToString(array.get(i), "sides[" + i + "]");
        Direction dir = Direction.byName(side);
        if (dir == null) {
          throw new JsonParseException("Invalid side " + side);
        }
        sides.add(dir);
      }
    } else {
      sides = EnumSet.allOf(Direction.class);
    }
    // other data
    BiPredicate<BlockState,BlockState> predicate = ConnectedModelRegistry.deserializePredicate(data, "predicate");
    // final model instance
    return new ConnectedModel(model, geometry, Map.copyOf(connectedTextures), predicate, sides);
  }
}
