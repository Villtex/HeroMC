package bibliocraft.decor;

import bibliocraft.BiblioCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * TapeMeasure: the first click on a block starts a measurement, the second one prints the distance into the chat;
 * right click into the air switches between the absolute distance (mode 0) and the distances along the axes (mode 1).
 *
 * <p>As in the original everything happens on the client only (onItemUse was client-only, onItemRightClick checked
 * isRemote): the start point and the mode are fields of the item, shared by all tape measures of the player, and
 * the sounds play only for him. A start block at x = 0 is not remembered (firstMeasurex != 0 is the "started" test).
 * The debug print of the clicked block is left out. A click on a table goes to the table (it turns the item on it).
 */
public class TapeMeasureItem extends Item {
    private int firstMeasurex;
    private int firstMeasurey;
    private int firstMeasurez;
    private int mode;

    public TapeMeasureItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide() && player != null) {
            BlockPos pos = context.getClickedPos();
            int face = context.getClickedFace().get3DDataValue();
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            if (this.firstMeasurex != 0) {
                int xdist = Math.abs(this.firstMeasurex - i);
                int ydist = Math.abs(this.firstMeasurey - j);
                int zdist = Math.abs(this.firstMeasurez - k);
                if (xdist > 0) {
                    xdist++;
                }
                if (zdist > 0) {
                    zdist++;
                }
                if (face != 0 && face != 1) {
                    ydist++;
                }
                int measurmentxz = (int) Math.sqrt(xdist * xdist + zdist * zdist);
                int measurmentxy = (int) Math.sqrt(xdist * xdist + ydist * ydist);
                int measurmentyz = (int) Math.sqrt(ydist * ydist + zdist * zdist);
                if (this.mode == 1) {
                    if (zdist != 0) {
                        chat(player, "Measurement North/South = " + zdist);
                    }
                    if (xdist != 0) {
                        chat(player, "Measurement East/West = " + xdist);
                    }
                    if (ydist != 0) {
                        chat(player, "Measurement Height = " + ydist);
                    }
                    if (xdist == 0 && zdist == 0 && ydist == 0) {
                        chat(player, "No Measurement");
                    }
                }
                if (this.mode == 0) {
                    if (ydist == 0) {
                        chat(player, "Measurement = " + measurmentxz);
                    } else if (xdist == 0) {
                        chat(player, "Measurement = " + measurmentyz);
                    } else if (zdist == 0) {
                        chat(player, "Measurement = " + measurmentxy);
                    } else {
                        int thedistance = (int) Math.sqrt(measurmentxz * measurmentxz + measurmentxy * measurmentxy) - 1;
                        chat(player, "Measurement = " + thedistance);
                    }
                }
                sound(level, player, BiblioCraft.TAPE_CLOSE.get());
                this.firstMeasurex = 0;
                this.firstMeasurey = 0;
                this.firstMeasurez = 0;
            } else {
                sound(level, player, BiblioCraft.TAPE_OPEN.get());
                chat(player, "Starting Measurement.");
                this.firstMeasurex = i;
                this.firstMeasurey = j;
                this.firstMeasurez = k;
            }
            return InteractionResult.SUCCESS;
        }
        // the server half of onItemUse did not exist (Item.onItemUse: false)
        return InteractionResult.PASS;
    }

    /** onItemRightClick: switch the mode. */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            if (this.mode == 0) {
                chat(player, "Switching mode to North/South, East/West, and height.");
                this.mode = 1;
            } else if (this.mode == 1) {
                chat(player, "Switching mode to absolute measurment.");
                this.mode = 0;
            }
        }
        return InteractionResult.PASS;
    }

    /** addChatMessage on the client: only into the own chat. */
    private static void chat(Player player, String text) {
        player.sendSystemMessage(Component.literal(text));
    }

    /** player.playSound on the client: heard only by this player, at his position. */
    private static void sound(Level level, Player player, SoundEvent sound) {
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F, false);
    }
}
