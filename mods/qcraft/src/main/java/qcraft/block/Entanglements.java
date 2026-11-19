package qcraft.block;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * QuantumCraftProxyCommon.ForgeHandlers onWorldLoad/onWorldUnload/onWorldSave: the next unused frequencies of qBlocks and
 * quantum computers in {@code <world>/quantum/entanglements.bin} (gzip NBT, tags "qblocks" and "qcomputers" with
 * "nextUnusedFrequency"). The original did this for every dimension with the same file; here for the overworld, which
 * loads first and saves with the others.
 */
public final class Entanglements {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Entanglements() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(LevelEvent.Load.class, e -> {
            if (e.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
                load(level.getServer());
            }
        });
        NeoForge.EVENT_BUS.addListener(LevelEvent.Unload.class, e -> {
            if (e.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
                QBlockEntity.REGISTRY.reset();
                QuantumComputerBlockEntity.REGISTRY.reset();
            }
        });
        NeoForge.EVENT_BUS.addListener(LevelEvent.Save.class, e -> {
            if (e.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
                save(level.getServer());
            }
        });
    }

    private static Path file(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("quantum").resolve("entanglements.bin");
    }

    private static void load(MinecraftServer server) {
        QBlockEntity.REGISTRY.reset();
        QuantumComputerBlockEntity.REGISTRY.reset();
        Path file = file(server);
        if (!Files.exists(file)) {
            return;
        }
        try {
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            root.getCompound("qblocks").ifPresent(t -> QBlockEntity.REGISTRY.setNextUnusedFrequency(t.getIntOr("nextUnusedFrequency", 0)));
            root.getCompound("qcomputers").ifPresent(t -> QuantumComputerBlockEntity.REGISTRY.setNextUnusedFrequency(t.getIntOr("nextUnusedFrequency", 0)));
        } catch (IOException e) {
            LOGGER.warn("Warning: failed to load qCraft entanglement info", e);
        }
    }

    private static void save(MinecraftServer server) {
        CompoundTag root = new CompoundTag();
        CompoundTag qblocks = new CompoundTag();
        qblocks.putInt("nextUnusedFrequency", QBlockEntity.REGISTRY.getNextUnusedFrequency());
        root.put("qblocks", qblocks);
        CompoundTag qcomputers = new CompoundTag();
        qcomputers.putInt("nextUnusedFrequency", QuantumComputerBlockEntity.REGISTRY.getNextUnusedFrequency());
        root.put("qcomputers", qcomputers);
        try {
            Path file = file(server);
            Files.createDirectories(file.getParent());
            NbtIo.writeCompressed(root, file);
        } catch (IOException e) {
            LOGGER.warn("Warning: failed to save qCraft entanglement info", e);
        }
    }
}
