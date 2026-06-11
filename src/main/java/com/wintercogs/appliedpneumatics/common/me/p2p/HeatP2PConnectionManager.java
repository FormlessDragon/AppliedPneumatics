package com.wintercogs.appliedpneumatics.common.me.p2p;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Tracks explicit PNC heat logic links from one input exchanger to the current P2P outputs.
 */
final class HeatP2PConnectionManager {
    private IHeatExchangerLogic input;
    private final IdentityHashMap<IHeatExchangerLogic, Boolean> outputs = new IdentityHashMap<>();

    void synchronize(IHeatExchangerLogic newInput, List<IHeatExchangerLogic> newOutputs) {
        Objects.requireNonNull(newInput, "newInput");
        Objects.requireNonNull(newOutputs, "newOutputs");

        if (this.input != newInput) {
            clear();
            this.input = newInput;
        }

        IdentityHashMap<IHeatExchangerLogic, Boolean> requested = new IdentityHashMap<>();
        for (IHeatExchangerLogic output : newOutputs) {
            if (output != null && output != newInput) {
                requested.put(output, Boolean.TRUE);
            }
        }

        removeMissingConnections(requested);
        addNewConnections(requested);
    }

    void clear() {
        if (this.input != null) {
            for (IHeatExchangerLogic output : this.outputs.keySet()) {
                this.input.removeConnectedExchanger(output);
            }
        }
        this.outputs.clear();
        this.input = null;
    }

    private void removeMissingConnections(IdentityHashMap<IHeatExchangerLogic, Boolean> requested) {
        if (this.input == null) {
            return;
        }

        var iterator = this.outputs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<IHeatExchangerLogic, Boolean> entry = iterator.next();
            IHeatExchangerLogic output = entry.getKey();
            if (!requested.containsKey(output)) {
                this.input.removeConnectedExchanger(output);
                iterator.remove();
            }
        }
    }

    private void addNewConnections(IdentityHashMap<IHeatExchangerLogic, Boolean> requested) {
        if (this.input == null) {
            return;
        }

        for (IHeatExchangerLogic output : requested.keySet()) {
            if (!this.outputs.containsKey(output)) {
                this.input.addConnectedExchanger(output);
                this.outputs.put(output, Boolean.TRUE);
            }
        }
    }
}
