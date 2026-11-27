package slimeknights.mantle.block.entity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;

/**
 * Extension of tile entity to make it namable.
 * Port: the custom name is also the {@link DataComponents#CUSTOM_NAME} component of the item (as in Mantle 1.21),
 * saved with the component codec like vanilla containers.
 */
public abstract class NameableBlockEntity extends MantleBlockEntity implements INameableMenuProvider {
	private static final String TAG_CUSTOM_NAME = "CustomName";

	/** Default title for this tile entity */
	@Getter
	private final Component defaultName;
	/** Title set to this tile entity */
	@Getter @Setter @Nullable
	private Component customName;

	public NameableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Component defaultTitle) {
		super(type, pos, state);
		this.defaultName = defaultTitle;
	}

	/* Item stack components */

	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		super.applyImplicitComponents(components);
		this.customName = components.get(DataComponents.CUSTOM_NAME);
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		components.set(DataComponents.CUSTOM_NAME, this.customName);
	}

	@Override
	public void removeComponentsFromTag(ValueOutput output) {
		// custom name is passed to the stack directly, don't keep it in BE data
		super.removeComponentsFromTag(output);
		output.discard(TAG_CUSTOM_NAME);
	}

	/* NBT */

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.customName = BlockEntity.parseCustomNameSafe(input, TAG_CUSTOM_NAME);
	}

	@Override
	protected void saveSynced(ValueOutput output) {
		super.saveSynced(output);
		output.storeNullable(TAG_CUSTOM_NAME, ComponentSerialization.CODEC, this.customName);
	}
}
