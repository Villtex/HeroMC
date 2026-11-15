package bspkrs.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * A block specification, the 26.3 counterpart of bspkrsCore's BlockID (block ID + metadata, -1 = any metadata).
 * Written as {@code namespace:block}, {@code namespace:block[property=value,...]} (only the named properties must
 * match, like a metadata value) or {@code #namespace:tag}.
 *
 * <p>The block is looked up lazily: a specification for a block of a mod that is not installed matches nothing, as the
 * old ID of a missing mod did. That lets tree definitions name blocks of mods that are ported later.
 */
public final class BlockID {
    public static final Codec<BlockID> CODEC = Codec.STRING.comapFlatMap(BlockID::read, BlockID::toString);

    private final Identifier id;
    private final boolean tag;
    private final Map<String, String> properties;
    private final TagKey<Block> tagKey;

    private BlockID(Identifier id, boolean tag, Map<String, String> properties) {
        this.id = id;
        this.tag = tag;
        this.properties = Map.copyOf(properties);
        this.tagKey = tag ? TagKey.create(Registries.BLOCK, id) : null;
    }

    public static BlockID of(Block block) {
        return new BlockID(BuiltInRegistries.BLOCK.getKey(block), false, Map.of());
    }

    private static DataResult<BlockID> read(String s) {
        try {
            return DataResult.success(parse(s));
        } catch (IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
    }

    /** Parses {@code ns:block}, {@code ns:block[a=b,c=d]} or {@code #ns:tag}. */
    public static BlockID parse(String s) {
        String text = s.trim();
        boolean tag = text.startsWith("#");
        if (tag) {
            text = text.substring(1);
        }
        Map<String, String> props = new LinkedHashMap<>();
        int open = text.indexOf('[');
        if (open >= 0) {
            if (tag || !text.endsWith("]")) {
                throw new IllegalArgumentException("Bad block specification: " + s);
            }
            for (String pair : text.substring(open + 1, text.length() - 1).split(",")) {
                if (pair.isBlank()) {
                    continue;
                }
                String[] kv = pair.split("=", 2);
                if (kv.length != 2) {
                    throw new IllegalArgumentException("Bad block property in " + s + ": " + pair);
                }
                props.put(kv[0].trim(), kv[1].trim());
            }
            text = text.substring(0, open);
        }
        Identifier id = Identifier.tryParse(text.trim());
        if (id == null) {
            throw new IllegalArgumentException("Bad block id: " + s);
        }
        return new BlockID(id, tag, props);
    }

    /** True if the state is this block (or in this tag) and has every named property value. */
    public boolean matches(BlockState state) {
        if (tag) {
            return state.is(tagKey);
        }
        if (!BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(id)) {
            return false;
        }
        for (Map.Entry<String, String> e : properties.entrySet()) {
            Property<?> property = state.getBlock().getStateDefinition().getProperty(e.getKey());
            if (property == null || !valueName(state, property).equals(e.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    public Identifier id() {
        return id;
    }

    public boolean isTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BlockID b && b.id.equals(id) && b.tag == tag && b.properties.equals(properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tag, properties);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (tag) {
            sb.append('#');
        }
        sb.append(id);
        if (!properties.isEmpty()) {
            sb.append('[');
            properties.forEach((k, v) -> sb.append(sb.charAt(sb.length() - 1) == '[' ? "" : ",").append(k).append('=').append(v));
            sb.append(']');
        }
        return sb.toString();
    }
}
