package projectzulu.client.entity;

import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Per-entity animation state on the client. The 1.5.2 models eased their parts through EntityModelRotation fields
 * stored on the entity; with 26.3 render states the renderer computes the easing in extractRenderState and copies
 * the values into the render state. This cache keeps one easing holder per live entity.
 */
public final class EasingCache<T> {
    private final Map<Entity, T> states = new WeakHashMap<>();
    private final Function<Entity, T> factory;

    public EasingCache(Function<Entity, T> factory) {
        this.factory = factory;
    }

    public T get(Entity entity) {
        return this.states.computeIfAbsent(entity, this.factory);
    }
}
