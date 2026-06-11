package com.wintercogs.appliedpneumatics.common.gui;

import ae2.api.config.Actionable;
import ae2.api.inventories.InternalInventory;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.GenericStack;
import ae2.api.storage.MEStorage;
import ae2.container.GuiIds;
import ae2.container.SlotSemantics;
import ae2.container.me.common.ContainerMEStorage;
import ae2.container.slot.RestrictedInputSlot;
import com.wintercogs.appliedpneumatics.common.init.APItems;
import com.wintercogs.appliedpneumatics.common.item.AmadronWirelessTerminalItem;
import com.wintercogs.appliedpneumatics.common.me.amadron.AmadronPatternDetails;
import com.wintercogs.appliedpneumatics.common.me.amadron.Pnc112AmadronBridge;
import com.wintercogs.appliedpneumatics.common.tile.MEAmadronProcessStationTile;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ContainerAmadronWirelessTerminal extends ContainerMEStorage {
    static final String ACTION_SAVE_PATTERN = "save_amadron_pattern";
    static final String ACTION_SUBMIT_ORDER = "submit_amadron_order";
    private static final String ORDER_FAIL_ME_DISCONNECTED = "amadron.appliedpneumatics.order_fail.me_disconnected";
    private static final String ORDER_FAIL_CANT_FIND_STATION =
        "amadron.appliedpneumatics.order_fail.cant_find_process_station";
    private static final String ORDER_FAIL_STATION_INACTIVE =
        "amadron.appliedpneumatics.order_fail.process_station_not_active";
    private static final String ORDER_FAIL_BASKET_EMPTY = "amadron.appliedpneumatics.order_fail.basket_empty";
    private static final String ORDER_FAIL_INVALID_ORDER = "amadron.appliedpneumatics.order_fail.order_invaild";
    private static final String ORDER_FAIL_NO_VALID_ORDER = "amadron.appliedpneumatics.order_fail.have_not_vaild_order";
    private static final String ORDER_FAIL_MISSING_MATERIALS =
        "amadron.appliedpneumatics.order_fail.missing_materials";
    private static final String ORDER_FAIL_FOR_DETAILS = "amadron.appliedpneumatics.order_fail.for_details";
    private static final String ORDER_FAIL_TOO_MANY = "amadron.appliedpneumatics.order_fail.order_too_much";
    private static final String ORDER_SUCCESS = "amadron.appliedpneumatics.order_success";

    private final AmadronWirelessTerminalGuiHost host;

    public ContainerAmadronWirelessTerminal(InventoryPlayer playerInventory, AmadronWirelessTerminalGuiHost host) {
        super(GuiIds.GuiKey.WIRELESS_TERMINAL, playerInventory, host, false);
        this.host = host;
        registerClientAction(ACTION_SAVE_PATTERN, String.class, this::onSavePatternAction);
        registerClientAction(ACTION_SUBMIT_ORDER, AmadronOrderRequest.class, this::onSubmitOrderAction);
        addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.BLANK_PATTERN,
            host.getPatternInventory(), 0), SlotSemantics.BLANK_PATTERN);
        addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN,
            host.getPatternInventory(), 1), SlotSemantics.ENCODED_PATTERN);
        addPlayerInventorySlots(8, 185);
    }

    @Override
    public AmadronWirelessTerminalGuiHost getHost() {
        return this.host;
    }

    public void sendSavePatternActionToServer(AmadronOffer offer) {
        sendClientAction(ACTION_SAVE_PATTERN, serializeOfferForSaveAction(offer));
    }

    public void sendSubmitOrderActionToServer(AmadronOffer offer, int units) {
        sendSubmitOrdersActionToServer(Collections.singletonList(
            new AbstractMap.SimpleImmutableEntry<>(offer, units)));
    }

    public void sendSubmitOrdersActionToServer(List<? extends Map.Entry<AmadronOffer, Integer>> orders) {
        sendClientAction(ACTION_SUBMIT_ORDER, AmadronOrderRequest.of(orders));
    }

    private void onSavePatternAction(String offerPayload) {
        encodePatternForOffer(this.host.getPatternInventory(), deserializeOfferForSaveAction(offerPayload));
    }

    private void onSubmitOrderAction(AmadronOrderRequest request) {
        SubmissionResult preflightResult = validateWirelessSubmitPrerequisites(request != null, true, true, true);
        if (preflightResult != null) {
            sendSubmitMessage(preflightResult);
            return;
        }

        preflightResult = validateWirelessSubmitPrerequisites(true, this.host.getLinkStatus().connected(), true, true);
        if (preflightResult != null) {
            sendSubmitMessage(preflightResult);
            return;
        }

        AmadronWirelessTerminalItem.AmadronStationLink link =
            this.host.getAmadronTerminalItem().getLinkedAmadronStation(this.host.getItemStack());
        MEAmadronProcessStationTile station = getLinkedStation(link);
        preflightResult = validateWirelessSubmitPrerequisites(true, true,
            station != null, station == null || station.getMainNode().isActive());
        if (preflightResult != null) {
            sendSubmitMessage(preflightResult);
            return;
        }

        List<Map.Entry<AmadronOffer, Integer>> orders;
        try {
            orders = request.toOfferEntries();
        } catch (IllegalArgumentException ignored) {
            sendSubmitMessage(SubmissionResult.fail(ORDER_FAIL_NO_VALID_ORDER));
            return;
        }

        sendSubmitMessage(submitPrepaidOrdersDetailed(this.host.getInventory(), station, orders,
            AmadronOfferManager.getInstance()::get, getActionSource()));
    }

    static SubmissionResult validateWirelessSubmitPrerequisites(boolean hasRequest, boolean connected,
                                                                boolean hasStation, boolean stationActive) {
        if (!hasRequest) {
            return SubmissionResult.fail(ORDER_FAIL_BASKET_EMPTY);
        }
        if (!connected) {
            return SubmissionResult.fail(ORDER_FAIL_ME_DISCONNECTED);
        }
        if (!hasStation) {
            return SubmissionResult.fail(ORDER_FAIL_CANT_FIND_STATION);
        }
        if (!stationActive) {
            return SubmissionResult.fail(ORDER_FAIL_STATION_INACTIVE);
        }
        return null;
    }

    private void sendSubmitMessage(SubmissionResult result) {
        if (result != null && result.message() != null) {
            getPlayer().sendStatusMessage(result.message(), false);
        }
    }

    static boolean encodePatternForOffer(InternalInventory patternInventory, AmadronOffer offer) {
        if (patternInventory == null || offer == null || patternInventory.size() < 2
            || !patternInventory.getStackInSlot(1).isEmpty()) {
            return false;
        }

        ItemStack extracted = patternInventory.extractItem(0, 1, false);
        if (extracted.isEmpty()) {
            return false;
        }

        ItemStack encodedPattern = new ItemStack(APItems.AMADRON_PATTERN);
        AmadronPatternDetails.encode(encodedPattern, offer);
        ItemStack remainder = patternInventory.insertItem(1, encodedPattern, false);
        if (remainder.isEmpty()) {
            return true;
        }

        patternInventory.insertItem(0, extracted, false);
        return false;
    }

    static boolean submitPrepaidOrder(MEStorage storage, MEAmadronProcessStationTile station, AmadronOffer offer,
                                      int units) {
        return submitPrepaidOrder(storage, station, offer, units, AmadronOfferManager.getInstance()::get);
    }

    static boolean submitPrepaidOrder(MEStorage storage, MEAmadronProcessStationTile station, AmadronOffer offer,
                                      int units, Function<AmadronOffer, AmadronOffer> liveOfferResolver) {
        return submitPrepaidOrder(storage, station, offer, units, liveOfferResolver, IActionSource.empty());
    }

    static boolean submitPrepaidOrder(MEStorage storage, MEAmadronProcessStationTile station, AmadronOffer offer,
                                      int units, Function<AmadronOffer, AmadronOffer> liveOfferResolver,
                                      IActionSource source) {
        if (storage == null || station == null || offer == null || units <= 0) {
            return false;
        }
        AmadronOffer liveOffer = liveOfferResolver == null ? offer : liveOfferResolver.apply(offer);
        if (liveOffer == null) {
            return false;
        }

        GenericStack unitInput = toGenericStack(liveOffer.getInput());
        if (unitInput == null || unitInput.amount() <= 0) {
            return false;
        }

        long totalAmount;
        try {
            totalAmount = Math.multiplyExact(unitInput.amount(), (long) units);
        } catch (ArithmeticException e) {
            return false;
        }
        GenericStack payment = new GenericStack(unitInput.what(), totalAmount);
        if (source == null) {
            source = IActionSource.empty();
        }
        if (storage.extract(payment.what(), payment.amount(), Actionable.SIMULATE, source) < payment.amount()) {
            return false;
        }
        long extracted = storage.extract(payment.what(), payment.amount(), Actionable.MODULATE, source);
        if (extracted < payment.amount()) {
            if (extracted > 0) {
                storage.insert(payment.what(), extracted, Actionable.MODULATE, source);
            }
            return false;
        }
        if (station.enqueuePrepaidOrder(liveOffer, payment, units)) {
            return true;
        }

        storage.insert(payment.what(), payment.amount(), Actionable.MODULATE, source);
        return false;
    }

    static boolean submitPrepaidOrders(MEStorage storage, MEAmadronProcessStationTile station,
                                       List<? extends Map.Entry<AmadronOffer, Integer>> orders,
                                       Function<AmadronOffer, AmadronOffer> liveOfferResolver) {
        return submitPrepaidOrders(storage, station, orders, liveOfferResolver, IActionSource.empty());
    }

    static boolean submitPrepaidOrders(MEStorage storage, MEAmadronProcessStationTile station,
                                       List<? extends Map.Entry<AmadronOffer, Integer>> orders,
                                       Function<AmadronOffer, AmadronOffer> liveOfferResolver,
                                       IActionSource source) {
        return submitPrepaidOrdersDetailed(storage, station, orders, liveOfferResolver, source).success();
    }

    static SubmissionResult submitPrepaidOrdersDetailed(MEStorage storage, MEAmadronProcessStationTile station,
                                                        List<? extends Map.Entry<AmadronOffer, Integer>> orders,
                                                        Function<AmadronOffer, AmadronOffer> liveOfferResolver,
                                                        IActionSource source) {
        if (storage == null || station == null || orders == null || orders.isEmpty()) {
            return SubmissionResult.fail(storage == null ? ORDER_FAIL_ME_DISCONNECTED
                : station == null ? ORDER_FAIL_CANT_FIND_STATION : ORDER_FAIL_BASKET_EMPTY);
        }
        if (source == null) {
            source = IActionSource.empty();
        }

        List<MEAmadronProcessStationTile.PrepaidOrder> preparedOrders = new ArrayList<>();
        Map<AEKey, Long> totalPayments = new LinkedHashMap<>();
        Map<AmadronOffer, Long> totalUnitsByOffer = new LinkedHashMap<>();
        int totalUnits = 0;
        for (Map.Entry<AmadronOffer, Integer> order : orders) {
            if (order == null || order.getKey() == null || order.getValue() == null || order.getValue() <= 0) {
                return SubmissionResult.fail(ORDER_FAIL_NO_VALID_ORDER);
            }

            AmadronOffer liveOffer = liveOfferResolver == null ? order.getKey() : liveOfferResolver.apply(order.getKey());
            if (liveOffer == null) {
                return SubmissionResult.fail(ORDER_FAIL_INVALID_ORDER, describeOffer(order.getKey()));
            }

            GenericStack unitInput = toGenericStack(liveOffer.getInput());
            if (unitInput == null || unitInput.amount() <= 0) {
                return SubmissionResult.fail(ORDER_FAIL_NO_VALID_ORDER);
            }

            long paymentAmount;
            try {
                paymentAmount = Math.multiplyExact(unitInput.amount(), order.getValue().longValue());
                totalPayments.merge(unitInput.what(), paymentAmount, Math::addExact);
                totalUnitsByOffer.merge(liveOffer, order.getValue().longValue(), Math::addExact);
                totalUnits = Math.addExact(totalUnits, order.getValue());
            } catch (ArithmeticException e) {
                return SubmissionResult.fail(ORDER_FAIL_NO_VALID_ORDER);
            }

            preparedOrders.add(new MEAmadronProcessStationTile.PrepaidOrder(liveOffer,
                new GenericStack(unitInput.what(), paymentAmount), order.getValue()));
        }

        if (preparedOrders.isEmpty() || station.getJobAmount() + preparedOrders.size() > 512) {
            return SubmissionResult.fail(preparedOrders.isEmpty() ? ORDER_FAIL_NO_VALID_ORDER : ORDER_FAIL_TOO_MANY);
        }
        for (Map.Entry<AmadronOffer, Long> offerUnits : totalUnitsByOffer.entrySet()) {
            long units = offerUnits.getValue();
            if (units > Integer.MAX_VALUE || !Pnc112AmadronBridge.hasEnoughStock(offerUnits.getKey(), (int) units)) {
                return SubmissionResult.fail(ORDER_FAIL_INVALID_ORDER, describeOffer(offerUnits.getKey()));
            }
        }

        for (Map.Entry<AEKey, Long> payment : totalPayments.entrySet()) {
            if (storage.extract(payment.getKey(), payment.getValue(), Actionable.SIMULATE, source)
                < payment.getValue()) {
                return SubmissionResult.fail(ORDER_FAIL_MISSING_MATERIALS, payment.getValue(),
                    payment.getKey().getDisplayName());
            }
        }

        List<GenericStack> extractedPayments = new ArrayList<>();
        try {
            for (Map.Entry<AEKey, Long> payment : totalPayments.entrySet()) {
                long extracted = storage.extract(payment.getKey(), payment.getValue(), Actionable.MODULATE, source);
                if (extracted < payment.getValue()) {
                    if (extracted > 0) {
                        extractedPayments.add(new GenericStack(payment.getKey(), extracted));
                    }
                    rollbackExtractedPayments(storage, extractedPayments, source);
                    return SubmissionResult.fail(ORDER_FAIL_FOR_DETAILS);
                }
                extractedPayments.add(new GenericStack(payment.getKey(), payment.getValue()));
            }

            if (station.enqueuePrepaidOrders(preparedOrders)) {
                return SubmissionResult.success(totalUnits);
            }
        } catch (RuntimeException e) {
            rollbackExtractedPayments(storage, extractedPayments, source);
            return SubmissionResult.fail(ORDER_FAIL_FOR_DETAILS);
        }

        rollbackExtractedPayments(storage, extractedPayments, source);
        return SubmissionResult.fail(station.getJobAmount() + preparedOrders.size() > 512
            ? ORDER_FAIL_TOO_MANY : ORDER_FAIL_FOR_DETAILS);
    }

    private static void rollbackExtractedPayments(MEStorage storage, List<GenericStack> extractedPayments,
                                                  IActionSource source) {
        for (GenericStack payment : extractedPayments) {
            storage.insert(payment.what(), payment.amount(), Actionable.MODULATE, source);
        }
    }

    static String serializeOfferForSaveAction(AmadronOffer offer) {
        if (offer == null) {
            throw new IllegalArgumentException("Cannot serialize a null Amadron offer");
        }

        try {
            NBTTagCompound tag = Pnc112AmadronBridge.writeOfferKey(offer);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(tag, output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not serialize Amadron offer for wireless terminal action", e);
        }
    }

    static AmadronOffer deserializeOfferForSaveAction(String payload) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("Cannot deserialize an empty Amadron offer payload");
        }

        try {
            byte[] compressed = Base64.getDecoder().decode(payload);
            NBTTagCompound tag = CompressedStreamTools.readCompressed(new ByteArrayInputStream(compressed));
            return Pnc112AmadronBridge.readOfferKey(tag);
        } catch (IllegalArgumentException | IOException e) {
            throw new IllegalArgumentException("Could not deserialize Amadron offer for wireless terminal action", e);
        }
    }

    private MEAmadronProcessStationTile getLinkedStation(AmadronWirelessTerminalItem.AmadronStationLink link) {
        MinecraftServer server = getPlayer().getServer();
        if (link == null || server == null) {
            return null;
        }

        WorldServer world = server.getWorld(link.dimension());
        if (world == null) {
            return null;
        }

        var tile = world.getTileEntity(link.pos());
        return tile instanceof MEAmadronProcessStationTile ? (MEAmadronProcessStationTile) tile : null;
    }

    private static GenericStack toGenericStack(Object resource) {
        if (resource instanceof ItemStack) {
            return GenericStack.fromItemStack((ItemStack) resource);
        }
        if (resource instanceof FluidStack) {
            return GenericStack.fromFluidStack((FluidStack) resource);
        }
        return null;
    }

    private static String describeOffer(AmadronOffer offer) {
        return offer == null ? "" : offer.toString();
    }

    static final class SubmissionResult {
        private final boolean success;
        private final ITextComponent message;

        private SubmissionResult(boolean success, ITextComponent message) {
            this.success = success;
            this.message = message;
        }

        static SubmissionResult success(int totalUnits) {
            return new SubmissionResult(true, new TextComponentTranslation(ORDER_SUCCESS, totalUnits));
        }

        static SubmissionResult fail(String translationKey, Object... args) {
            return new SubmissionResult(false, new TextComponentTranslation(translationKey, args));
        }

        boolean success() {
            return this.success;
        }

        ITextComponent message() {
            return this.message;
        }
    }

    static final class AmadronOrderRequest {
        private List<AmadronOrderLine> orders = new ArrayList<>();

        private AmadronOrderRequest(String offerPayload, int units) {
            this.orders.add(new AmadronOrderLine(offerPayload, units));
        }

        private static AmadronOrderRequest of(List<? extends Map.Entry<AmadronOffer, Integer>> orders) {
            AmadronOrderRequest request = new AmadronOrderRequest();
            if (orders != null) {
                for (Map.Entry<AmadronOffer, Integer> order : orders) {
                    if (order != null && order.getKey() != null && order.getValue() != null) {
                        request.orders.add(new AmadronOrderLine(serializeOfferForSaveAction(order.getKey()),
                            order.getValue()));
                    }
                }
            }
            return request;
        }

        private AmadronOrderRequest() {
        }

        private List<Map.Entry<AmadronOffer, Integer>> toOfferEntries() {
            List<Map.Entry<AmadronOffer, Integer>> entries = new ArrayList<>();
            if (this.orders == null) {
                return entries;
            }

            for (AmadronOrderLine order : this.orders) {
                if (order != null) {
                    entries.add(new AbstractMap.SimpleImmutableEntry<>(
                        deserializeOfferForSaveAction(order.offerPayload), order.units));
                }
            }
            return entries;
        }
    }

    private static final class AmadronOrderLine {
        private String offerPayload;
        private int units;

        private AmadronOrderLine(String offerPayload, int units) {
            this.offerPayload = offerPayload;
            this.units = units;
        }
    }
}

