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
public class InfantryPistolNakjimaLaserPistolWeapon extends InfantryWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -3164871600230559641L;

    public InfantryPistolNakjimaLaserPistolWeapon() {
        super();
        techLevel = TechConstants.T_IS_TW_NON_BOX;
        name = "Nakjima Laser Pistol";
        setInternalName(name);
        addLookupName("InfantryNakjimalaserpistol");
        ammoType = AmmoType.T_NA;
        cost = 750;
        bv = 0.44;
        flags = flags.or(F_NO_FIRES).or(F_DIRECT_FIRE).or(F_ENERGY);
        infantryDamage = 0.18;
        infantryRange = 1;
        introDate = 2880;
        availRating = new int[]{RATING_X,RATING_D,RATING_C};
        techRating = RATING_D;
    }
}
