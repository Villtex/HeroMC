package slimeknights.mantle.mixin;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Decoder;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.mantle.recipe.helper.RecipeIdContext;

/**
 * Port: recipes (and loot tables) are registry elements in 26.3, decoded by codecs that do not know the element ID.
 * Mantle and Tinkers recipes take their ID while loading (as in 1.20.1), so the ID of the element being decoded is kept
 * in {@link RecipeIdContext} for the duration of the decode.
 */
@Mixin(targets = "net.minecraft.resources.RegistryLoadTask$PendingRegistration")
public abstract class PendingRegistrationMixin {
  @Inject(method = "loadFromResource", at = @At("HEAD"))
  private static void mantle$startResource(Decoder<?> decoder, RegistryOps<JsonElement> ops, ResourceKey<?> elementKey, Resource thunk, CallbackInfoReturnable<Either<?, Exception>> cir) {
    RecipeIdContext.start(elementKey);
  }

  @Inject(method = "loadFromResource", at = @At("RETURN"))
  private static void mantle$endResource(Decoder<?> decoder, RegistryOps<JsonElement> ops, ResourceKey<?> elementKey, Resource thunk, CallbackInfoReturnable<Either<?, Exception>> cir) {
    RecipeIdContext.end();
  }

  @Inject(method = "loadFromNetwork", at = @At("HEAD"))
  private static void mantle$startNetwork(Decoder<?> decoder, RegistryOps<Tag> ops, ResourceKey<?> elementKey, Tag contents, CallbackInfoReturnable<Either<?, Exception>> cir) {
    RecipeIdContext.start(elementKey);
  }

  @Inject(method = "loadFromNetwork", at = @At("RETURN"))
  private static void mantle$endNetwork(Decoder<?> decoder, RegistryOps<Tag> ops, ResourceKey<?> elementKey, Tag contents, CallbackInfoReturnable<Either<?, Exception>> cir) {
    RecipeIdContext.end();
  }
}
