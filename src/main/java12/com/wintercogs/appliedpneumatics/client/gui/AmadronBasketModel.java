package com.wintercogs.appliedpneumatics.client.gui;

import me.desht.pneumaticcraft.common.recipes.AmadronOffer;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class AmadronBasketModel {
    private final Map<AmadronOffer, Integer> basketUnits = new HashMap<>();

    boolean isEmpty() {
        return this.basketUnits.isEmpty();
    }

    int getUnits(AmadronOffer offer) {
        return this.basketUnits.getOrDefault(offer, 0);
    }

    void adjustUnits(AmadronOffer offer, int delta) {
        if (offer == null || delta == 0) {
            return;
        }
        long next = (long) this.basketUnits.getOrDefault(offer, 0) + delta;
        if (next < 0) {
            next = 0;
        }
        int stock = offer.getStock();
        if (stock >= 0) {
            next = Math.min(next, Math.max(0, stock));
        }
        if (next <= 0) {
            this.basketUnits.remove(offer);
        } else {
            this.basketUnits.put(offer, (int) Math.min(Integer.MAX_VALUE, next));
        }
    }

    void removeUnavailableOffers(List<AmadronOffer> visibleOffers) {
        this.basketUnits.keySet().removeIf(offer -> !visibleOffers.contains(offer));
    }

    List<Map.Entry<AmadronOffer, Integer>> buildOrders() {
        List<Map.Entry<AmadronOffer, Integer>> orders = new ArrayList<>();
        for (Map.Entry<AmadronOffer, Integer> entry : this.basketUnits.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                orders.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue()));
            }
        }
        return orders;
    }

    int getUnitTotal() {
        long total = 0;
        for (Integer units : this.basketUnits.values()) {
            if (units != null && units > 0) {
                total += units;
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, total);
    }
}
