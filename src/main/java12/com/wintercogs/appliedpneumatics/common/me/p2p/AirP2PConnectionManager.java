package com.wintercogs.appliedpneumatics.common.me.p2p;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Tracks the explicit PNC 1.12 air connections that bridge one input handler to the current P2P outputs.
 */
final class AirP2PConnectionManager {
    private IAirHandler input;
    private final IdentityHashMap<IAirHandler, Boolean> outputs = new IdentityHashMap<>();

    void synchronize(IAirHandler newInput, List<IAirHandler> newOutputs) {
        Objects.requireNonNull(newInput, "newInput");
        Objects.requireNonNull(newOutputs, "newOutputs");

        if (this.input != newInput) {
            clear();
            this.input = newInput;
        }

        IdentityHashMap<IAirHandler, Boolean> requested = new IdentityHashMap<>();
        for (IAirHandler output : newOutputs) {
            if (output != null && output != newInput) {
                requested.put(output, Boolean.TRUE);
            }
        }

        removeMissingConnections(requested);
        addNewConnections(requested);
    }

    void clear() {
        if (this.input != null) {
            for (IAirHandler output : this.outputs.keySet()) {
                this.input.removeConnection(output);
            }
        }
        this.outputs.clear();
        this.input = null;
    }

    private void removeMissingConnections(IdentityHashMap<IAirHandler, Boolean> requested) {
        if (this.input == null) {
            return;
        }

        var iterator = this.outputs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<IAirHandler, Boolean> entry = iterator.next();
            IAirHandler output = entry.getKey();
            if (!requested.containsKey(output)) {
                this.input.removeConnection(output);
                iterator.remove();
            }
        }
    }

    private void addNewConnections(IdentityHashMap<IAirHandler, Boolean> requested) {
        if (this.input == null) {
            return;
        }

        for (IAirHandler output : requested.keySet()) {
            if (!this.outputs.containsKey(output)) {
                this.input.createConnection(output);
                this.outputs.put(output, Boolean.TRUE);
            }
        }
    }
}
