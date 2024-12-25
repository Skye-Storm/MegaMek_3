/*
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 */
package megamek.ai.utility;

import java.util.*;


public abstract class DecisionContext<IN_GAME_OBJECT, TARGETABLE> {

    private final Agent<IN_GAME_OBJECT, TARGETABLE> agent;
    private final World<IN_GAME_OBJECT, TARGETABLE> world;
    private final IN_GAME_OBJECT currentUnit;
    private final List<TARGETABLE> targetUnits;
    private final Map<String, Double> damageCache;
    private final static int DAMAGE_CACHE_SIZE = 10_000;

    public DecisionContext(Agent<IN_GAME_OBJECT, TARGETABLE> agent, World<IN_GAME_OBJECT, TARGETABLE> world) {
        this(agent, world, null, Collections.emptyList());
    }

    public DecisionContext(Agent<IN_GAME_OBJECT, TARGETABLE> agent, World<IN_GAME_OBJECT, TARGETABLE> world, IN_GAME_OBJECT currentUnit) {
        this(agent, world, currentUnit, Collections.emptyList());
    }

    public DecisionContext(Agent<IN_GAME_OBJECT, TARGETABLE> agent, World<IN_GAME_OBJECT, TARGETABLE> world, IN_GAME_OBJECT currentUnit, List<TARGETABLE> targetUnits) {
        this.agent = agent;
        this.world = world;
        this.currentUnit = currentUnit;
        this.targetUnits = targetUnits;
        this.damageCache = new LinkedHashMap<>(32, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Double> eldest) {
                return size() > DAMAGE_CACHE_SIZE;
            }
        };
    }

    public World<IN_GAME_OBJECT, TARGETABLE> getWorld() {
        return world;
    }

    public Agent<IN_GAME_OBJECT, TARGETABLE> getAgent() {
        return agent;
    }

    public List<TARGETABLE> getTargets() {
        return targetUnits;
    }

    public Optional<IN_GAME_OBJECT> getCurrentUnit() {
        return Optional.ofNullable(currentUnit);
    }

    public List<TARGETABLE> getEnemyUnits() {
        return world.getEnemyUnits();
    }

    public double getUnitMaxDamageAtRange(TARGETABLE unit, int enemyRange) {
        String cacheKey = unit.hashCode() + "-" + enemyRange;
        if (damageCache.containsKey(cacheKey)) {
            return damageCache.get(cacheKey);
        }

        double maxDamage = calculateUnitMaxDamageAtRange(unit, enemyRange);
        damageCache.put(cacheKey, maxDamage);
        return maxDamage;
    }

    public abstract double calculateUnitMaxDamageAtRange(TARGETABLE unit, int enemyRange);

    public void clearCaches() {
        damageCache.clear();
    }

    public abstract double getBonusFactor(DecisionContext<IN_GAME_OBJECT, TARGETABLE> lastContext);
}
