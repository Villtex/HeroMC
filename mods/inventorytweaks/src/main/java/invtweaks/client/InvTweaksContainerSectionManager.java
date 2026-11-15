package invtweaks.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** InvTweaksContainerSectionManager: a container manager bound to one section. */
public final class InvTweaksContainerSectionManager {
    private final InvTweaksContainerManager containerMgr;
    private final ContainerSection section;

    public InvTweaksContainerSectionManager(Minecraft mc, ContainerSection section) throws Exception {
        this(new InvTweaksContainerManager(mc), section);
    }

    public InvTweaksContainerSectionManager(InvTweaksContainerManager containerMgr, ContainerSection section) throws Exception {
        this.containerMgr = containerMgr;
        this.section = section;
        if (!this.containerMgr.hasSection(section)) {
            throw new Exception("Section not available");
        }
    }

    public boolean move(int from, int to) {
        return this.containerMgr.move(this.section, from, this.section, to);
    }

    public boolean moveSome(int from, int to, int amount) {
        return this.containerMgr.moveSome(this.section, from, this.section, to, amount);
    }

    public boolean drop(int index) {
        return this.containerMgr.drop(this.section, index);
    }

    public boolean dropSome(int index, int amount) {
        return this.containerMgr.dropSome(this.section, index, amount);
    }

    public boolean putHoldItemDown(int index) {
        return this.containerMgr.putHoldItemDown(this.section, index);
    }

    public void leftClick(int index) {
        this.containerMgr.leftClick(this.section, index);
    }

    public void rightClick(int index) {
        this.containerMgr.rightClick(this.section, index);
    }

    public void click(int index, boolean rightClick) {
        this.containerMgr.click(this.section, index, rightClick);
    }

    public List<Slot> getSlots() {
        return this.containerMgr.getSlots(this.section);
    }

    public int getSize() {
        return this.containerMgr.getSize(this.section);
    }

    public int getFirstEmptyIndex() {
        return this.containerMgr.getFirstEmptyIndex(this.section);
    }

    public boolean isSlotEmpty(int index) {
        return this.containerMgr.isSlotEmpty(this.section, index);
    }

    public Slot getSlot(int index) {
        return this.containerMgr.getSlot(this.section, index);
    }

    public int getSlotIndex(int slotNumber) {
        return this.isSlotInSection(slotNumber) ? this.containerMgr.getSlotIndex(slotNumber) : -1;
    }

    public boolean isSlotInSection(int slotNumber) {
        return this.containerMgr.getSlotSection(slotNumber) == this.section;
    }

    public ItemStack getItemStack(int index) {
        return this.containerMgr.getItemStack(this.section, index);
    }

    public AbstractContainerMenu getContainer() {
        return this.containerMgr.getContainer();
    }

    public InvTweaksContainerManager getContainerManager() {
        return this.containerMgr;
    }
}
