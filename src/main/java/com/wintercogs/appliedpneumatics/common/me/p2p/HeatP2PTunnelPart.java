package com.wintercogs.appliedpneumatics.common.me.p2p;

import ae2.api.parts.IPartItem;
import ae2.api.parts.IPartModel;
import ae2.items.parts.PartModels;
import ae2.parts.p2p.P2PModels;
import ae2.parts.p2p.P2PTunnelPart;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class HeatP2PTunnelPart extends P2PTunnelPart<HeatP2PTunnelPart> {
    private static final P2PModels MODELS = new P2PModels(
        AppliedPneumatics.makeId("part/p2p/p2p_tunnel_heat"));

    private final HeatP2PConnectionManager connectionManager = new HeatP2PConnectionManager();

    public HeatP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(isPowered(), isActive());
    }

    @Override
    public void onTunnelNetworkChange() {
        refreshHeatConnections();
    }

    @Override
    public void onTunnelConfigChange() {
        refreshHeatConnections();
    }

    @Override
    public void onNeighborChanged(IBlockAccess level, BlockPos pos, BlockPos neighbor) {
        refreshHeatConnections();
    }

    @Override
    public void onUpdateShape(EnumFacing side) {
        if (side == getSide()) {
            refreshHeatConnections();
        }
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        refreshHeatConnections();
    }

    @Override
    public void removeFromWorld() {
        HeatP2PTunnelPart input = isOutput() ? getInput() : null;
        this.connectionManager.clear();
        super.removeFromWorld();
        if (input != null) {
            input.refreshHeatConnections();
        }
    }

    private void refreshHeatConnections() {
        if (isOutput()) {
            this.connectionManager.clear();
            HeatP2PTunnelPart input = getInput();
            if (input != null) {
                input.refreshHeatConnections();
            }
            return;
        }

        IHeatExchangerLogic inputLogic = resolveAdjacentHeatLogic(this);
        if (inputLogic == null) {
            this.connectionManager.clear();
            return;
        }

        List<IHeatExchangerLogic> outputLogics = new ArrayList<>();
        for (HeatP2PTunnelPart output : getOutputs()) {
            IHeatExchangerLogic outputLogic = resolveAdjacentHeatLogic(output);
            if (outputLogic != null) {
                outputLogics.add(outputLogic);
            }
        }
        this.connectionManager.synchronize(inputLogic, outputLogics);
    }

    private static IHeatExchangerLogic resolveAdjacentHeatLogic(HeatP2PTunnelPart part) {
        TileEntity host = part.getTileEntity();
        EnumFacing side = part.getSide();
        if (host == null || side == null) {
            return null;
        }

        World level = host.getWorld();
        if (level == null) {
            return null;
        }

        BlockPos adjacentPos = host.getPos().offset(side);
        if (!level.isBlockLoaded(adjacentPos)) {
            return null;
        }

        return HeatExchangerManager.getInstance().getLogic(level, adjacentPos, side.getOpposite());
    }
}
