package chococraft.client;

import chococraft.entity.AbstractChocobo;
import chococraft.entity.Chicobo;
import chococraft.entity.Chocobo;
import chococraft.network.ChocoNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * GuiChocopedia. With a chocobo: its page (name; owner, health, gender and whether it can breed; for the owner the
 * buttons Rename, Name Hidden/Shown, follow/wander/stay, Confirm, change owner on a server and Drop Gear for a saddled
 * or pack-bagged chocobo without rider). Without: the book (title page and the 18 pages of ChocopediaPages, in the
 * language file). The debug lines the original showed its author are left out.
 */
public class ChocopediaScreen extends Screen {
    /** ChocopediaPages: page 0 is the title page, 1 to 18 the text. */
    static final int PAGES = 19;
    private static final int BOOK_WIDTH = 192;
    private static final int BOOK_HEIGHT = 192;

    private final @Nullable AbstractChocobo chocobo;
    private int page;
    private PageButton next;
    private PageButton previous;
    private Button hideName;
    private Button following;

    public ChocopediaScreen(@Nullable AbstractChocobo chocobo) {
        super(Component.translatable("item.chococraft.chocopedia"));
        this.chocobo = chocobo;
    }

    @Override
    protected void init() {
        if (this.chocobo != null) {
            boolean owner = this.minecraft.player != null && this.chocobo.isOwner(this.minecraft.player);
            int x = this.width / 2 - 100;
            int y = 48;
            this.button(x, y, Component.translatable("gui.chococraft.rename"), owner,
                    b -> this.minecraft.gui.setScreen(new NameChocoboScreen(this, this.chocobo)));
            y += 24;
            this.hideName = this.button(x, y, this.hideNameLabel(this.chocobo.isHidename()), owner, b -> {
                this.send(ChocoNetwork.TOGGLE_HIDE_NAME, "");
                b.setMessage(this.hideNameLabel(!this.chocobo.isHidename()));
            });
            y += 24;
            this.following = this.button(x, y, this.followLabel(), owner, b -> this.send(ChocoNetwork.TOGGLE_FOLLOW, ""));
            y += 24;
            this.button(x, y, Component.translatable("gui.chococraft.confirm"), owner, b -> this.onClose());
            if (!this.minecraft.isLocalServer()) {
                y += 30;
                this.button(x, y, Component.translatable("gui.chococraft.change_owner"), owner,
                        b -> this.minecraft.gui.setScreen(new SelectOwnerScreen(this, this.chocobo)));
            }
            if (this.chocobo instanceof Chocobo rideable && !rideable.isVehicle() && (rideable.isSaddled() || rideable.isPackBagged())) {
                y += 24;
                this.button(x, y, Component.translatable("gui.chococraft.drop_gear"), owner, b -> {
                    this.send(ChocoNetwork.DROP_GEAR, "");
                    this.onClose();
                });
            }
        } else {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.onClose())
                    .bounds(this.width / 2 - 100, 4 + BOOK_HEIGHT, 200, 20).build());
            int left = (this.width - BOOK_WIDTH) / 2;
            this.next = this.addRenderableWidget(new PageButton(left + 116, 159, true, b -> this.turn(1), true));
            this.previous = this.addRenderableWidget(new PageButton(left + 43, 159, false, b -> this.turn(-1), true));
            this.updateButtons();
        }
    }

    private Button button(int x, int y, Component label, boolean active, Button.OnPress press) {
        Button button = this.addRenderableWidget(Button.builder(label, press).bounds(x, y, 90, 20).build());
        button.active = active;
        return button;
    }

    void send(int action, String text) {
        if (this.chocobo != null) {
            ClientPacketDistributor.sendToServer(new ChocoNetwork.Action(this.chocobo.getId(), action, text));
        }
    }

    private Component hideNameLabel(boolean hidden) {
        return Component.translatable(hidden ? "gui.chococraft.name_hidden" : "gui.chococraft.name_shown");
    }

    private Component followLabel() {
        AbstractChocobo c = this.chocobo;
        String key = c.isFollowing() && !c.isWander() ? "follow" : !c.isFollowing() && c.isWander() ? "wander"
                : !c.isFollowing() && !c.isWander() ? "stay" : "";
        return key.isEmpty() ? Component.empty() : Component.translatable("gui.chococraft." + key);
    }

    private void turn(int delta) {
        this.page = Mth.clamp(this.page + delta, 0, PAGES - 1);
        this.updateButtons();
    }

    private void updateButtons() {
        this.next.visible = this.page < PAGES - 1;
        this.previous.visible = this.page > 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.chocobo != null) {
            if (this.chocobo.isRemoved()) {
                this.onClose();
                return;
            }
            this.following.setMessage(this.followLabel());
            this.hideName.setMessage(this.hideNameLabel(this.chocobo.isHidename()));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        if (this.chocobo == null) {
            g.blit(RenderPipelines.GUI_TEXTURED, BookViewScreen.BOOK_LOCATION, (this.width - BOOK_WIDTH) / 2, 2, 0.0F, 0.0F,
                    BOOK_WIDTH, BOOK_HEIGHT, 256, 256);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        if (this.chocobo != null) {
            AbstractChocobo c = this.chocobo;
            g.centeredText(this.font, c.getChocoboName(), this.width / 2, this.height / 4 - 40, 0xFFFFFFFF);
            int x = this.width / 2 + 10;
            int color = 0xFFA0A0A0;
            String breed = c.getAge() <= 0 && !(c instanceof Chicobo) ? "gui.chococraft.can_breed" : "gui.chococraft.cannot_breed";
            g.text(this.font, Component.translatable("gui.chococraft.owner", c.getOwnerName()), x, 48, color);
            g.text(this.font, Component.translatable("gui.chococraft.health", Mth.ceil(c.getHealth()), Mth.ceil(c.getMaxHealth())), x, 72, color);
            g.text(this.font, Component.translatable("gui.chococraft.gender_breed",
                    Component.translatable(c.isMale() ? "gui.chococraft.male" : "gui.chococraft.female"), Component.translatable(breed)), x, 96, color);
            return;
        }
        int left = (this.width - BOOK_WIDTH) / 2;
        if (this.page == 0) {
            Component title = Component.translatable("gui.chococraft.book_title").withStyle(ChatFormatting.GOLD);
            Component author = Component.translatable("gui.chococraft.book_author").withStyle(ChatFormatting.DARK_GRAY);
            g.text(this.font, title, left + 36 + (116 - this.font.width(title)) / 2, 50, 0xFF000000, false);
            g.text(this.font, author, left + 36 + (116 - this.font.width(author)) / 2, 60, 0xFF000000, false);
        } else {
            if (this.page > 1) {
                Component indicator = Component.translatable("book.pageIndicator", this.page - 1, PAGES - 2);
                g.text(this.font, indicator, left - this.font.width(indicator) + BOOK_WIDTH - 44, 18, 0xFF000000, false);
            }
            g.textWithWordWrap(this.font, Component.translatable("chococraft.chocopedia.page." + this.page), left + 36, 34, 116,
                    0xFF000000, false);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
