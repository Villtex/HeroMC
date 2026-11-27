package slimeknights.mantle.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.DelegateUnbakedModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;

import java.util.List;

/**
 * Item model with a texture chosen by an NBT key ({@code "loader": "mantle:nbt_key"}, {@code "nbt_key": "variant"},
 * one texture per value plus {@code "default"}).
 * Port (Teil 27c): 26.3 item models pick variants in the item model definition ({@code assets/<ns>/items/<id>.json},
 * {@code minecraft:select} on a component). This model therefore bakes the {@code default} texture as a flat item; the
 * variant textures stay available in the model for the item definitions of the Tinkers items (27d/27h).
 */
public class NBTKeyModel extends DelegateUnbakedModel {
  public static final UnbakedModelLoader<NBTKeyModel> LOADER = NBTKeyModel::deserialize;

  /** Key read from the stack in 1.20.1, kept for the item definitions */
  private final String key;
  private final TextureSlots.Data textures;

  protected NBTKeyModel(CuboidModel delegate, String key) {
    super(delegate);
    this.key = key;
    // layer0 shows the default texture
    TextureSlots.Data.Builder builder = new TextureSlots.Data.Builder();
    delegate.textureSlots().values().forEach((name, contents) -> {
      if (contents instanceof TextureSlots.Value value) {
        builder.addTexture(name, value.material());
      } else if (contents instanceof TextureSlots.Reference reference) {
        builder.addReference(name, reference.target());
      }
    });
    if (!delegate.textureSlots().values().containsKey("layer0")) {
      builder.addReference("layer0", "default");
    }
    this.textures = builder.build();
  }

  /** NBT key of the original model */
  public String getKey() {
    return key;
  }

  @Override
  public TextureSlots.Data textureSlots() {
    return textures;
  }

  @Override
  public UnbakedGeometry geometry() {
    return new MantleItemLayerModel.Geometry(List.of());
  }

  public static NBTKeyModel deserialize(JsonObject json, JsonDeserializationContext context) {
    return new NBTKeyModel(ColoredBlockModel.readVanilla(json, context), GsonHelper.getAsString(json, "nbt_key"));
  }
}
