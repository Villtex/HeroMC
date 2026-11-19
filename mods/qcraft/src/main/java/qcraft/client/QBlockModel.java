package qcraft.client;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import org.jspecify.annotations.Nullable;
import qcraft.block.QBlock;
import qcraft.block.QBlockEntity;

import java.util.List;

/**
 * QBlockRenderingHandler.renderWorldBlock / BlockQBlock.getBlockTexture: a qBlock showing a block draws the model of its
 * impostor (nothing for type 0, the original's transparent texture), swirling or fuzzy it draws a cube of the animated
 * unobserved or fuzz texture. The appearance comes from the block entity's model data; without block entity (a falling
 * qBlock) it is fuzz, as {@code getAppearance} returned without tile entity.
 */
public class QBlockModel implements DynamicBlockStateModel {
    private final @Nullable BlockStateModel impostor;
    private final BlockState impostorState;
    private final BlockStateModelPart swirl;
    private final BlockStateModelPart fuzz;

    public QBlockModel(@Nullable BlockStateModel impostor, BlockState impostorState, BlockStateModelPart swirl, BlockStateModelPart fuzz) {
        this.impostor = impostor;
        this.impostorState = impostorState;
        this.swirl = swirl;
        this.fuzz = fuzz;
    }

    private static QBlockEntity.Appearance appearance(BlockAndTintGetter level, BlockPos pos) {
        QBlockEntity.Appearance appearance = level.getModelData(pos).get(QBlockEntity.APPEARANCE);
        return appearance != null ? appearance : QBlockEntity.Appearance.FUZZ;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        switch (appearance(level, pos)) {
            case SWIRL -> parts.add(this.swirl);
            case FUZZ -> parts.add(this.fuzz);
            case BLOCK -> {
                if (this.impostor != null && QBlock.impostor(state) != null) {
                    this.impostor.collectParts(level, pos, this.impostorState, random, parts);
                }
            }
        }
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return switch (appearance(level, pos)) {
            case SWIRL -> this.swirl.particleMaterial();
            case FUZZ -> this.fuzz.particleMaterial();
            case BLOCK -> this.impostor != null && QBlock.impostor(state) != null
                    ? this.impostor.particleMaterial(level, pos, this.impostorState) : this.swirl.particleMaterial();
        };
    }

    @Override
    @Deprecated
    public Material.Baked particleMaterial() {
        return this.swirl.particleMaterial();
    }

    @Override
    @Deprecated
    public int materialFlags() {
        int flags = this.swirl.materialFlags() | this.fuzz.materialFlags();
        return this.impostor != null ? flags | this.impostor.materialFlags() : flags;
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return this.materialFlags();
    }
}
