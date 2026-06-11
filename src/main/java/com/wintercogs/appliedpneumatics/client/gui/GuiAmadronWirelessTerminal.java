package com.wintercogs.appliedpneumatics.client.gui;

import ae2.client.gui.Icon;
import ae2.client.gui.me.common.GuiMEStorage;
import ae2.client.gui.style.GuiStyle;
import ae2.client.gui.widgets.AE2Button;
import ae2.client.gui.widgets.IconButton;
import com.wintercogs.appliedpneumatics.common.gui.ContainerAmadronWirelessTerminal;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class GuiAmadronWirelessTerminal extends GuiMEStorage<ContainerAmadronWirelessTerminal> {
    private final List<AmadronOffer> visibleOffers = new ArrayList<>();
    private final AmadronOfferListModel offerList = new AmadronOfferListModel();
    private final AmadronBasketModel basket = new AmadronBasketModel();
    private final AE2Button submitButton;
    private final IconButton savePatternButton;
    private AmadronOffer selectedOffer;

    public GuiAmadronWirelessTerminal(ContainerAmadronWirelessTerminal container, InventoryPlayer playerInventory,
                                      ITextComponent title, GuiStyle style) {
        super(container, playerInventory, title, style);

        this.submitButton = widgets.addButton("submit_button",
            new TextComponentTranslation("menu.appliedpneumatics.button.submit"),
            this::submitSelectedOffer);
        this.savePatternButton = new IconButton(this::saveSelectedOfferPattern) {
            @Override
            protected Icon getIcon() {
                return Icon.ARROW_RIGHT;
            }
        };
        this.savePatternButton.setMessage(new TextComponentTranslation("menu.appliedpneumatics.button.save_pattern"));
        widgets.add("save_pattern_button", this.savePatternButton);

        refreshOffers();
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        refreshOffers();
        this.submitButton.enabled = !this.basket.isEmpty();
        this.savePatternButton.enabled = this.selectedOffer != null;
        this.submitButton.setMessage(new TextComponentTranslation("menu.appliedpneumatics.button.submit_units",
            getBasketUnitTotal()));
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(offsetX, offsetY, mouseX, mouseY);

        this.fontRenderer.drawString("Amadron offers: " + this.visibleOffers.size(),
            AmadronOfferListModel.OFFER_LIST_LEFT, AmadronOfferListModel.OFFER_LIST_TOP - 10, 0x404040);
        if (this.visibleOffers.isEmpty()) {
            this.fontRenderer.drawString("No offers", AmadronOfferListModel.OFFER_LIST_LEFT,
                AmadronOfferListModel.OFFER_LIST_TOP, 0x404040);
            return;
        }

        int rows = Math.min(AmadronOfferListModel.MAX_VISIBLE_OFFERS, this.visibleOffers.size());
        for (int i = 0; i < rows; i++) {
            AmadronOffer offer = this.visibleOffers.get(this.offerList.getOfferScrollOffset() + i);
            boolean selected = offer.equals(this.selectedOffer);
            int wantedUnits = this.basket.getUnits(offer);
            String line = (selected ? "> " : "  ") + (wantedUnits > 0 ? "[" + wantedUnits + "] " : "")
                + describeOffer(offer);
            line = this.fontRenderer.trimStringToWidth(line, AmadronOfferListModel.OFFER_LIST_WIDTH);
            this.fontRenderer.drawString(line, AmadronOfferListModel.OFFER_LIST_LEFT,
                AmadronOfferListModel.OFFER_LIST_TOP + i * AmadronOfferListModel.OFFER_ROW_HEIGHT,
                selected ? 0xFFFFFF : 0x404040);
        }

        this.fontRenderer.drawString("Basket units: " + getBasketUnitTotal(),
            AmadronOfferListModel.UNITS_LEFT, AmadronOfferListModel.UNITS_TOP, 0x404040);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        refreshOffers();

        int localX = mouseX - this.guiLeft;
        int localY = mouseY - this.guiTop;
        if (handleOfferListClick(localX, localY, mouseButton)
            || handleUnitsClick(localX, localY, mouseButton)) {
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        int delta = Mouse.getEventDWheel();
        if (delta != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            int localX = mouseX - this.guiLeft;
            int localY = mouseY - this.guiTop;
            if (this.offerList.isOverOfferList(localX, localY)
                && this.offerList.scrollOffers(this.visibleOffers.size(), delta, isShiftKeyDown())) {
                return;
            }
        }

        super.handleMouseInput();
    }

    private void submitSelectedOffer() {
        List<Map.Entry<AmadronOffer, Integer>> orders = this.basket.buildOrders();
        if (!orders.isEmpty()) {
            getContainer().sendSubmitOrdersActionToServer(orders);
        }
    }

    private void saveSelectedOfferPattern() {
        if (this.selectedOffer != null) {
            getContainer().sendSavePatternActionToServer(this.selectedOffer);
        }
    }

    private void refreshOffers() {
        this.visibleOffers.clear();
        this.visibleOffers.addAll(AmadronOfferManager.getInstance().getAllOffers());

        if (this.selectedOffer == null || !this.visibleOffers.contains(this.selectedOffer)) {
            this.selectedOffer = this.visibleOffers.isEmpty() ? null : this.visibleOffers.get(0);
            this.offerList.resetScroll();
        }
        this.basket.removeUnavailableOffers(this.visibleOffers);
        this.offerList.clampOfferScrollOffset(this.visibleOffers.size());
    }

    private boolean handleOfferListClick(int localX, int localY, int mouseButton) {
        int offerIndex = this.offerList.offerIndexAt(localX, localY, this.visibleOffers.size());
        if (offerIndex < 0) {
            return false;
        }

        this.selectedOffer = this.visibleOffers.get(offerIndex);
        if (mouseButton == 1) {
            adjustBasketUnits(this.selectedOffer, -(isShiftKeyDown() ? 10 : 1));
        } else if (mouseButton == 2) {
            adjustBasketUnits(this.selectedOffer, -this.basket.getUnits(this.selectedOffer));
        } else {
            adjustBasketUnits(this.selectedOffer, isShiftKeyDown() ? 10 : 1);
        }
        return true;
    }

    private boolean handleUnitsClick(int localX, int localY, int mouseButton) {
        if (!this.offerList.isOverUnits(localX, localY)) {
            return false;
        }

        int delta = isShiftKeyDown() ? 10 : 1;
        adjustBasketUnits(this.selectedOffer, mouseButton == 1 ? -delta : delta);
        return true;
    }

    private void adjustBasketUnits(AmadronOffer offer, int delta) {
        this.basket.adjustUnits(offer, delta);
    }

    private int getBasketUnitTotal() {
        return this.basket.getUnitTotal();
    }

    private static String describeOffer(AmadronOffer offer) {
        return describeResource(offer.getInput()) + " -> " + describeResource(offer.getOutput());
    }

    private static String describeResource(Object resource) {
        if (resource instanceof ItemStack) {
            ItemStack stack = (ItemStack) resource;
            if (stack.isEmpty()) {
                return "empty";
            }
            String name = stack.getDisplayName();
            return stack.getCount() > 1 ? name + " x" + stack.getCount() : name;
        }
        if (resource instanceof FluidStack) {
            FluidStack stack = (FluidStack) resource;
            return stack.getLocalizedName() + " " + stack.amount + " mB";
        }
        return String.valueOf(resource);
    }
}

