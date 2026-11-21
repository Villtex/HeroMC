package tconstruct.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;
import tconstruct.smeltery.CastingBlockEntity;
import tconstruct.smeltery.ChannelBlockEntity;
import tconstruct.smeltery.DryingRackBlock;
import tconstruct.smeltery.DryingRackBlockEntity;
import tconstruct.smeltery.FaucetBlockEntity;
import tconstruct.smeltery.SmelteryBlockEntity;
import tconstruct.smeltery.SmelteryRecipes;
import tconstruct.smeltery.TankBlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * The block entity renderers of the smeltery (TankRender, SmelteryRender, CastingTableSpecialRenderer,
 * CastingBasinSpecialRender, the faucet stream of SearedRender, BlockRenderCastingChannel, DryingRackSpecialRender).
 * Client check open.
 */
public final class SmelteryRenderers {
    private SmelteryRenderers() {
    }

    /** A box of liquid, saved in the extract step. */
    record Box(Fluid fluid, float x0, float y0, float z0, float x1, float y1, float z1) {
        void submit(PoseStack pose, SubmitNodeCollector collector, int light) {
            LiquidQuads.box(pose, collector, this.fluid, this.x0, this.y0, this.z0, this.x1, this.y1, this.z1, light);
        }
    }

    public static class LiquidState extends BlockEntityRenderState {
        final List<Box> boxes = new ArrayList<>();
        final List<BlockPos> offsets = new ArrayList<>();
        final List<ItemStackRenderState> items = new ArrayList<>();
        final List<float[]> itemPlaces = new ArrayList<>();
        final List<BlockModelRenderState> blocks = new ArrayList<>();
        final List<float[]> blockPlaces = new ArrayList<>();

        void clear() {
            this.boxes.clear();
            this.offsets.clear();
            this.items.clear();
            this.itemPlaces.clear();
            this.blocks.clear();
            this.blockPlaces.clear();
        }

        void box(BlockPos offset, Box box) {
            this.offsets.add(offset);
            this.boxes.add(box);
        }
    }

    /** Shared extract/submit: boxes of liquid per block offset, items (x, y, z, scale, yaw, pitch) and scaled blocks. */
    abstract static class Base<T extends net.minecraft.world.level.block.entity.BlockEntity> implements BlockEntityRenderer<T, LiquidState> {
        final ItemModelResolver items;
        final BlockModelResolver blockModels;

        Base(BlockEntityRendererProvider.Context context) {
            this.items = context.itemModelResolver();
            this.blockModels = context.blockModelResolver();
        }

        @Override
        public LiquidState createRenderState() {
            return new LiquidState();
        }

        @Override
        public void extractRenderState(T entity, LiquidState state, float partialTicks, Vec3 camera, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
            BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, camera, breakProgress);
            state.clear();
            this.extract(entity, state);
        }

        abstract void extract(T entity, LiquidState state);

        void item(T entity, LiquidState state, ItemStack stack, float... place) {
            if (stack.isEmpty()) {
                return;
            }
            ItemStackRenderState item = new ItemStackRenderState();
            this.items.updateForTopItem(item, stack, ItemDisplayContext.FIXED, entity.getLevel(), null, (int) entity.getBlockPos().asLong());
            state.items.add(item);
            state.itemPlaces.add(place);
        }

        void block(LiquidState state, BlockState block, float... place) {
            BlockModelRenderState model = new BlockModelRenderState();
            this.blockModels.update(model, block, BlockDisplayContext.create());
            if (!model.isEmpty()) {
                state.blocks.add(model);
                state.blockPlaces.add(place);
            }
        }

        @Override
        public void submit(LiquidState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            for (int i = 0; i < state.boxes.size(); i++) {
                BlockPos offset = state.offsets.get(i);
                pose.pushPose();
                pose.translate(offset.getX(), offset.getY(), offset.getZ());
                state.boxes.get(i).submit(pose, collector, state.lightCoords);
                pose.popPose();
            }
            for (int i = 0; i < state.items.size(); i++) {
                float[] p = state.itemPlaces.get(i);
                pose.pushPose();
                pose.translate(p[0], p[1], p[2]);
                pose.rotateDegrees(Axis.YP, p[4]);
                pose.rotateDegrees(Axis.XP, p[5]);
                pose.scale(p[3], p[3], p[3]);
                state.items.get(i).submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                pose.popPose();
            }
            for (int i = 0; i < state.blocks.size(); i++) {
                float[] p = state.blockPlaces.get(i);
                pose.pushPose();
                pose.translate(p[0], p[1], p[2]);
                pose.scale(1.0F, p[3], 1.0F);
                state.blocks.get(i).submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                pose.popPose();
            }
        }
    }

    /** TankRender: the liquid fills the tank from the bottom. */
    public static class Tank extends Base<TankBlockEntity> {
        public Tank(BlockEntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        void extract(TankBlockEntity tank, LiquidState state) {
            FluidStack liquid = tank.liquid();
            if (!liquid.isEmpty()) {
                float height = liquid.getAmount() / (tank.capacity() * 1.01F);
                state.box(BlockPos.ZERO, new Box(liquid.getFluid(), 0.001F, 0.001F, 0.001F, 0.999F, height, 0.999F));
            }
        }
    }

    /** CastingTableSpecialRenderer / CastingBasinSpecialRender and the liquid of SearedRender. */
    public static class Casting extends Base<CastingBlockEntity> {
        public Casting(BlockEntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        void extract(CastingBlockEntity cast, LiquidState state) {
            FluidStack liquid = cast.liquid;
            if (!liquid.isEmpty() && cast.capacity() > 0) {
                float fill = Math.max(0, liquid.getAmount() - cast.renderOffset) / (float) cast.capacity();
                if (cast.basin) {
                    state.box(BlockPos.ZERO, new Box(liquid.getFluid(), 0.125F, 0.25F, 0.125F, 0.875F, 0.25F + 0.6875F * fill, 0.875F));
                } else {
                    state.box(BlockPos.ZERO, new Box(liquid.getFluid(), 0.0625F, 0.9375F, 0.0625F, 0.9375F, 0.9375F + 0.0625F * fill, 0.9375F));
                }
            }
            for (int slot = 0; slot < 2; slot++) {
                ItemStack stack = cast.getItem(slot);
                if (cast.basin) {
                    this.item(cast, state, stack, 0.5F, 0.6F, 0.5F, 1.5F, 0, 0);
                } else {
                    this.item(cast, state, stack, 0.5F, 0.94F + slot * 0.01F, 0.5F, 0.875F, 0, 90);
                }
            }
        }
    }

    /** The stream of a running faucet down into the block below. */
    public static class Faucet extends Base<FaucetBlockEntity> {
        public Faucet(BlockEntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        void extract(FaucetBlockEntity faucet, LiquidState state) {
            FluidStack liquid = faucet.liquid;
            if (liquid.isEmpty()) {
                return;
            }
            Direction face = faucet.facing();
            float cx = 0.5F - face.getStepX() * 0.125F, cz = 0.5F - face.getStepZ() * 0.125F;
            if (face.getAxis() == Direction.Axis.Z) {
                cz = 0.5F + (face == Direction.NORTH ? 0.1875F : -0.1875F);
            } else {
                cx = 0.5F + (face == Direction.WEST ? 0.1875F : -0.1875F);
            }
            float bottom = faucet.getLevel() != null && faucet.getLevel().getBlockEntity(faucet.getBlockPos().below()) instanceof CastingBlockEntity c
                    && c.basin ? 0.25F : 0.9375F;
            state.box(BlockPos.ZERO, new Box(liquid.getFluid(), cx - 0.125F, 0.0F, cz - 0.125F, cx + 0.125F, 0.375F, cz + 0.125F));
            state.box(BlockPos.ZERO.below(), new Box(liquid.getFluid(), cx - 0.125F, bottom, cz - 0.125F, cx + 0.125F, 1.0F, cz + 0.125F));
        }
    }

    /** BlockRenderCastingChannel: the liquid in the gutter and its arms. */
    public static class Channel extends Base<ChannelBlockEntity> {
        public Channel(BlockEntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        void extract(ChannelBlockEntity channel, LiquidState state) {
            FluidStack liquid = channel.liquid;
            if (liquid.isEmpty()) {
                return;
            }
            float top = 7 / 16F + 3 / 16F * Math.min(1.0F, liquid.getAmount() / (float) ChannelBlockEntity.FILL_MAX);
            Fluid fluid = liquid.getFluid();
            state.box(BlockPos.ZERO, new Box(fluid, 6 / 16F, 7 / 16F, 6 / 16F, 10 / 16F, top, 10 / 16F));
            BlockState block = channel.getBlockState();
            if (block.getValue(tconstruct.smeltery.CastingChannelBlock.NORTH)) {
                state.box(BlockPos.ZERO, new Box(fluid, 6 / 16F, 7 / 16F, 0, 10 / 16F, top, 6 / 16F));
            }
            if (block.getValue(tconstruct.smeltery.CastingChannelBlock.SOUTH)) {
                state.box(BlockPos.ZERO, new Box(fluid, 6 / 16F, 7 / 16F, 10 / 16F, 10 / 16F, top, 1));
            }
            if (block.getValue(tconstruct.smeltery.CastingChannelBlock.WEST)) {
                state.box(BlockPos.ZERO, new Box(fluid, 0, 7 / 16F, 6 / 16F, 6 / 16F, top, 10 / 16F));
            }
            if (block.getValue(tconstruct.smeltery.CastingChannelBlock.EAST)) {
                state.box(BlockPos.ZERO, new Box(fluid, 10 / 16F, 7 / 16F, 6 / 16F, 1, top, 10 / 16F));
            }
        }
    }

    /** DryingRackSpecialRender: the item hangs from the bar. */
    public static class DryingRack extends Base<DryingRackBlockEntity> {
        public DryingRack(BlockEntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        void extract(DryingRackBlockEntity rack, LiquidState state) {
            int meta = rack.getBlockState().getValue(DryingRackBlock.META);
            float[][] places = {{0.5F, 0.4F, 0.5F, 0}, {0.5F, 0.4F, 0.5F, 90}, {0.5F, 0.5F, 0.875F, 0}, {0.5F, 0.5F, 0.125F, 0},
                    {0.875F, 0.5F, 0.5F, 90}, {0.125F, 0.5F, 0.5F, 90}};
            float[] p = places[meta];
            this.item(rack, state, rack.getItem(0), p[0], p[1], p[2], 0.75F, p[3], 0);
        }
    }

    /** SmelteryRender: the items heating inside as their blocks (an ingot a quarter high) and the molten metal, 20000 mB per layer. */
    public static class Smeltery extends Base<SmelteryBlockEntity> {
        public Smeltery(BlockEntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public boolean shouldRenderOffScreen() {
            return true;
        }

        @Override
        public AABB getRenderBoundingBox(SmelteryBlockEntity smeltery) {
            BlockPos center = smeltery.centerPos == null ? smeltery.getBlockPos() : smeltery.centerPos;
            return new AABB(center).inflate(2, 0, 2).expandTowards(0, Math.max(1, smeltery.layers), 0).minmax(new AABB(smeltery.getBlockPos()));
        }

        @Override
        void extract(SmelteryBlockEntity smeltery, LiquidState state) {
            if (!smeltery.validStructure || smeltery.centerPos == null) {
                return;
            }
            BlockPos corner = smeltery.centerPos.offset(-1, 0, -1).subtract(smeltery.getBlockPos());
            for (int i = 0; i < smeltery.getContainerSize(); i++) {
                ItemStack input = smeltery.getItem(i);
                if (input.isEmpty() || i >= smeltery.activeTemps.length || smeltery.activeTemps[i] <= 20) {
                    continue;
                }
                SmelteryRecipes.Melting melting = SmelteryRecipes.melting(input);
                if (melting != null) {
                    int layer = i / 9, cell = i % 9;
                    float height = Math.clamp(1.0F / melting.renderSize(), 0.01F, 1.0F);
                    this.block(state, melting.renderState(input), corner.getX() + cell % 3, corner.getY() + layer, corner.getZ() + cell / 3, height);
                }
            }
            float base = 0;
            int yBase = 0;
            int liquidBase = 0;
            for (FluidStack liquid : smeltery.moltenMetal) {
                int liquidSize = liquid.getAmount();
                while (liquidSize > 0) {
                    int room = 20000 - liquidBase;
                    int countSize = Math.min(liquidSize, room);
                    liquidSize -= countSize;
                    float height = countSize / 20000.0F;
                    for (int i = 0; i < 9; i++) {
                        float minX = i % 3 == 0 ? -0.001F : 0.0F, minZ = i / 3 == 0 ? -0.001F : 0.0F;
                        float maxX = i % 3 == 2 ? 1.001F : 1.0F, maxZ = i / 3 == 2 ? 1.001F : 1.0F;
                        state.box(corner.offset(i % 3, yBase, i / 3), new Box(liquid.getFluid(), minX, base, minZ, maxX, base + height, maxZ));
                    }
                    base += height;
                    liquidBase += countSize;
                    if (countSize == room) {
                        base = 0;
                        yBase++;
                        liquidBase = 0;
                    }
                }
            }
        }
    }
}
