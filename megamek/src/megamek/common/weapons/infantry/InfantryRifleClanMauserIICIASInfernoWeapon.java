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
import megamek.common.TechConstants;

/**
 * @author Ben Grills
 */
public class InfantryRifleClanMauserIICIASInfernoWeapon extends InfantryWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -3164871600230559641L;

    public InfantryRifleClanMauserIICIASInfernoWeapon() {
        super();
        techLevel.put(3071,TechConstants.T_CLAN_TW);
        name = "Laser Rifle (Mauser IIC IAS) (Inferno Grenades)";
        setInternalName(name);
        addLookupName("InfantryClanMauserIICIASInferno");
        addLookupName("Infantry Clan Mauser IIC Inferno");
        ammoType = AmmoType.T_NA;
        cost = 18000;
        bv = 6.45;
        flags = flags.or(F_INFERNO).or(F_DIRECT_FIRE).or(F_ENERGY).or(F_INF_ENCUMBER);
        infantryDamage = 0.90;
        infantryRange = 3;
        introDate = 3015;
        techLevel.put(3015,techLevel.get(3071));
        availRating = new int[]{RATING_X,RATING_F,RATING_E};
        techRating = RATING_F;
    }
}
