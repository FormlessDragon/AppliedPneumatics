package com.wintercogs.appliedpneumatics.common.me.p2p;

import ae2.api.parts.IPartItem;
import ae2.api.parts.IPartModel;
import ae2.items.parts.PartModels;
import ae2.parts.p2p.P2PModels;
import ae2.parts.p2p.P2PTunnelPart;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class AirP2PTunnelPart extends P2PTunnelPart<AirP2PTunnelPart> {
    private static final P2PModels MODELS = new P2PModels(
        AppliedPneumatics.makeId("part/p2p/p2p_tunnel_air"));

    private final AirP2PConnectionManager connectionManager = new AirP2PConnectionManager();

    public AirP2PTunnelPart(IPartItem<?> partItem) {
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
        refreshPneumaticConnections();
    }

    @Override
    public void onTunnelConfigChange() {
        refreshPneumaticConnections();
    }

    @Override
    public void onNeighborChanged(IBlockAccess level, BlockPos pos, BlockPos neighbor) {
        refreshPneumaticConnections();
    }

    @Override
    public void onUpdateShape(EnumFacing side) {
        if (side == getSide()) {
            refreshPneumaticConnections();
        }
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        refreshPneumaticConnections();
    }

    @Override
    public void removeFromWorld() {
        AirP2PTunnelPart input = isOutput() ? getInput() : null;
        this.connectionManager.clear();
        super.removeFromWorld();
        if (input != null) {
            input.refreshPneumaticConnections();
        }
    }

    private void refreshPneumaticConnections() {
        if (isOutput()) {
            this.connectionManager.clear();
            AirP2PTunnelPart input = getInput();
            if (input != null) {
                input.refreshPneumaticConnections();
            }
            return;
        }

        IAirHandler inputHandler = resolveAdjacentAirHandler(this);
        if (inputHandler == null) {
            this.connectionManager.clear();
            return;
        }

        List<IAirHandler> outputHandlers = new ArrayList<>();
        for (AirP2PTunnelPart output : getOutputs()) {
            IAirHandler outputHandler = resolveAdjacentAirHandler(output);
            if (outputHandler != null) {
                outputHandlers.add(outputHandler);
            }
        }
        this.connectionManager.synchronize(inputHandler, outputHandlers);
    }

    private static IAirHandler resolveAdjacentAirHandler(AirP2PTunnelPart part) {
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

        TileEntity adjacent = level.getTileEntity(adjacentPos);
        IPneumaticMachine machine = IPneumaticMachine.getMachine(adjacent);
        if (machine == null) {
            return null;
        }

        return machine.getAirHandler(side.getOpposite());
    }
}

