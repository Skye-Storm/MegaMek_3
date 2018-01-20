package megamek.common.weapons.lasers;

/**
 * MegaMek - Copyright (C) 2004 Ben Mazur (bmazur@sev.org)
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
 * Created on Sep 2, 2004
 *
 */

/**
 * @author Andrew Hunter
 */
public class ISLaserMedium extends LaserWeapon {
    /**
     *
     */
    private static final long serialVersionUID = 2178224725694704541L;

    public ISLaserMedium() {
        super();
        this.name = "Medium Laser";
        this.setInternalName(this.name);
        this.addLookupName("IS Medium Laser");
        this.addLookupName("ISMediumLaser");
        this.heat = 3;
        this.damage = 5;
        this.shortRange = 3;
        this.mediumRange = 6;
        this.longRange = 9;
        this.extremeRange = 12;
        this.waterShortRange = 2;
        this.waterMediumRange = 4;
        this.waterLongRange = 6;
        this.waterExtremeRange = 8;
        this.tonnage = 1.0f;
        this.criticals = 1;
        this.bv = 46;
        this.cost = 40000;
        this.shortAV = 5;
        this.maxRange = RANGE_SHORT;
        rulesRefs = "227,TM";
        techAdvancement.setTechBase(TECH_BASE_ALL)
        	.setIntroLevel(true)
        	.setUnofficial(false)
            .setTechRating(RATING_C)
            .setAvailability(RATING_B, RATING_B, RATING_B, RATING_B)
            .setISAdvancement(2290, 2300, 2310, DATE_NONE, DATE_NONE)
            .setISApproximate(false, true, false,false, false)
            .setClanAdvancement(2290, 2300, 2310, 2850, DATE_NONE)
            .setClanApproximate(false, true, false,true, false)
            .setPrototypeFactions(F_TA)
            .setProductionFactions(F_TA);
    }
}
