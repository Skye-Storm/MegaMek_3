/*
 * MegaMek - Copyright (C) 2005 Ben Mazur (bmazur@sev.org)
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package megamek.common.weapons;

import megamek.common.*;
import megamek.common.actions.WeaponAttackAction;
import megamek.common.options.OptionsConstants;
import megamek.server.totalwarfare.TWGameManager;

import java.io.Serial;

/**
 * @author Jason Tighe
 */
public class ACWeaponHandler extends AmmoWeaponHandler {
    @Serial
    private static final long serialVersionUID = 7326881584091651519L;

    public ACWeaponHandler(ToHitData t, WeaponAttackAction w, Game g, TWGameManager m) {
        super(t, w, g, m);
    }

    @Override
    protected int calcDamagePerHit() {
        double toReturn = weaponType.getDamage();
        // during a swarm, all damage gets applied as one block to one location
        if ((attackerEntity instanceof BattleArmor)
            && (weapon.getLocation() == BattleArmor.LOC_SQUAD)
            && !(weapon.isSquadSupportWeapon())
            && (attackerEntity.getSwarmTargetId() == target.getId())) {
            toReturn *= ((BattleArmor) attackerEntity).getShootingStrength();
        }
        // we default to direct fire weapons for anti-infantry damage
        if (target.isConventionalInfantry()) {
            toReturn = Compute.directBlowInfantryDamage(toReturn,
                    bDirect ? toHit.getMoS() / 3 : 0,
                    weaponType.getInfantryDamageClass(),
                    ((Infantry) target).isMechanized(),
                    toHit.getThruBldg() != null,
                    attackerEntity.getId(), calcDmgPerHitReport);
        } else if (bDirect) {
            toReturn = Math.min(toReturn + (toHit.getMoS() / 3), toReturn * 2);
        }
        toReturn = applyGlancingBlowModifier(toReturn, target.isConventionalInfantry());
        if (game.getOptions().booleanOption(OptionsConstants.ADVCOMBAT_TACOPS_RANGE)
            && nRange > weaponType.getRanges(weapon)[RangeType.RANGE_LONG]) {
            toReturn = (int) Math.floor(toReturn * .75);
        }
        if (game.getOptions().booleanOption(OptionsConstants.ADVCOMBAT_TACOPS_LOS_RANGE)
                && (nRange > weaponType.getRanges(weapon)[RangeType.RANGE_EXTREME])) {
            toReturn = (int) Math.floor(toReturn * .5);
        }

        return (int) toReturn;
    }
}
