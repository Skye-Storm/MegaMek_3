/**
 * MegaMek - Copyright (C) 2004,2005 Ben Mazur (bmazur@sev.org)
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
/*
 * Created on Sep 7, 2005
 *
 */
package megamek.common.weapons.infantry;

import megamek.common.AmmoType;
import megamek.common.options.GameOptions;
import megamek.common.options.IGameOptions;
import megamek.common.options.OptionsConstants;

/**
 * @author Sebastian Brocks
 */
public class InfantrySupportLRMWeapon extends InfantryWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -966926675003846938L;

    public InfantrySupportLRMWeapon() {
        super();

        name = "LRM Launcher (Corean Farshot)";
        setInternalName("InfantryLRM");
        addLookupName(name);
        addLookupName("LRM Launcher");
        addLookupName("LRM Launcher (FarShot)");
        ammoType = AmmoType.T_INFANTRY;
        cost = 2000;
        bv = 3.44;
        tonnage = .03;
        flags = flags.or(F_NO_FIRES).or(F_DIRECT_FIRE).or(F_MISSILE).or(F_INF_ENCUMBER).or(F_INF_SUPPORT);
        infantryDamage = 0.48;
        infantryRange = 3;
        ammoWeight = 0.0083;
        ammoCost = 1500;
        shots = 1;
        rulesRefs = "273, TM";
        techAdvancement.setTechBase(TECH_BASE_IS).setISAdvancement(3055, 3057, 3065, DATE_NONE, DATE_NONE)
                .setISApproximate(true, false, false, false, false).setPrototypeFactions(F_FW, F_CC)
                .setProductionFactions(F_FW).setTechRating(RATING_D)
                .setAvailability(RATING_X, RATING_X, RATING_D, RATING_D);
    }

    @Override
    public void adaptToGameOptions(IGameOptions gOp) {
        super.adaptToGameOptions(gOp);

        // Indirect Fire
        if (gOp.booleanOption(OptionsConstants.BASE_INDIRECT_FIRE)) {
            addMode("");
            addMode("Indirect");
        } else {
            removeMode("");
            removeMode("Indirect");
        }
    }
}
