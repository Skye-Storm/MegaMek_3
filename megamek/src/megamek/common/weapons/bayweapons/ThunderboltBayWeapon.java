/*
 * MegaMek - Copyright (C) 2004, 2005 Ben Mazur (bmazur@sev.org)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package megamek.common.weapons.bayweapons;

import megamek.common.EquipmentTypeLookup;
import megamek.common.Game;
import megamek.common.ToHitData;
import megamek.common.actions.WeaponAttackAction;
import megamek.common.weapons.AttackHandler;
import megamek.common.weapons.MissileBayWeaponHandler;
import megamek.server.gameManager.*;

/**
 * @author Jay Lawson
 * @since Sep 25, 2004
 */
public class ThunderboltBayWeapon extends AmmoBayWeapon {
    private static final long serialVersionUID = 8756042527483383101L;

    public ThunderboltBayWeapon() {
        super();
        // tech levels are a little tricky
        this.name = "Thunderbolt Bay";
        this.setInternalName(EquipmentTypeLookup.THUNDERBOLT_BAY);
        this.heat = 0;
        this.damage = DAMAGE_VARIABLE;
        this.shortRange = 6;
        this.mediumRange = 12;
        this.longRange = 20;
        this.extremeRange = 25;
        this.tonnage = 0.0;
        this.bv = 0;
        this.cost = 0;
        this.flags = flags.or(F_MISSILE).or(F_LARGEMISSILE);
        this.atClass = CLASS_THUNDERBOLT;
    }
    
    @Override
    public int getBattleForceClass() {
        return BFCLASS_LRM;
    }
    
    @Override
    protected AttackHandler getCorrectHandler(ToHitData toHit,
            WeaponAttackAction waa, Game game, GameManager manager) {
        return new MissileBayWeaponHandler(toHit, waa, game, manager);
    }
}
