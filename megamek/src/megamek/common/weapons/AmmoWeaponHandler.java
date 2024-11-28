/*
 * MegaMek - Copyright (C) 2004, 2005 Ben Mazur (bmazur@sev.org)
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

import java.io.Serial;
import java.util.List;
import java.util.Vector;

import megamek.common.Compute;
import megamek.common.CriticalSlot;
import megamek.common.Game;
import megamek.common.HitData;
import megamek.common.IBomber;
import megamek.common.Mounted;
import megamek.common.Report;
import megamek.common.Roll;
import megamek.common.ToHitData;
import megamek.common.actions.WeaponAttackAction;
import megamek.common.modifiers.EquipmentModifier;
import megamek.common.modifiers.WeaponHeatModifier;
import megamek.common.modifiers.WeaponJamModifier;
import megamek.common.options.OptionsConstants;
import megamek.logging.MMLogger;
import megamek.server.totalwarfare.TWGameManager;

/**
 * @author Andrew Hunter
 */
public class AmmoWeaponHandler extends WeaponHandler {
    @Serial
    private static final long serialVersionUID = -4934490646657484486L;

    private static final MMLogger logger = MMLogger.create(AmmoWeaponHandler.class);

    Mounted<?> ammo;

    public AmmoWeaponHandler(ToHitData t, WeaponAttackAction w, Game g, TWGameManager m) {
        super(t, w, g, m);
        generalDamageType = HitData.DAMAGE_BALLISTIC;
    }

    @Override
    protected void useAmmo() {
        checkAmmo();
        if (ammo == null) {
            // Can't happen. w/o legal ammo, the weapon *shouldn't* fire.
            logger.error("Handler can't find any ammo! Oh no!", new Exception());
            return;
        }

        if (ammo.getUsableShotsLeft() <= 0) {
            ae.loadWeaponWithSameAmmo(weapon);
            ammo = weapon.getLinked();
        }
        ammo.setShotsLeft(ammo.getBaseShotsLeft() - 1);

        if (weapon.isInternalBomb()) {
            ((IBomber) ae).increaseUsedInternalBombs(1);
        }

        super.useAmmo();
    }

    protected void checkAmmo() {
        ammo = weapon.getLinked();
        if (ammo == null) {
            ae.loadWeapon(weapon);
            ammo = weapon.getLinked();
        }
    }

    /**
     * For ammo weapons, this number can be less than the full number if the amount of ammo is not high enough.
     *
     * @return the number of weapons of this type firing (for squadron weapon groups)
     */
    @Override
    protected int getNumberWeapons() {
        if (ammo == null) {
            // shouldn't happen
            return weapon.getNWeapons();
        }
        int totalShots = ae.getTotalAmmoOfType(ammo.getType());
        return Math.min(weapon.getNWeapons(), (int) Math.floor((double) totalShots / (double) weapon.getCurrentShots()));
    }

    @Override
    protected boolean doChecks(Vector<Report> vPhaseReport) {
        processJamFromEquipmentModifier(vPhaseReport);
        return doAmmoFeedProblemCheck(vPhaseReport);
    }

    /**
     * Jams the weapon if it is has a jamming equipment modifier (e.g. salvage quality) which is triggered by the to-hit roll and
     * adds the appropriate report.
     *
     * @param phaseReport The current report
     */
    protected void processJamFromEquipmentModifier(List<Report> phaseReport) {
        for (EquipmentModifier modifier : weapon.getModifiers()) {
            if (modifier instanceof WeaponJamModifier jamModifier && jamModifier.isJammed(roll.getIntValue())) {
                phaseReport.add(new Report(3161).subject(subjectId));
                weapon.setJammed(true);
                isJammed = true;
                // CO p.215 doesn't say if the jam prevents the current shot, assuming no
            }
        }
    }

    /**
     * Carry out an 'ammo feed problems' check on the weapon. Return true if it blew up.
     */
    @Override
    protected boolean doAmmoFeedProblemCheck(Vector<Report> vPhaseReport) {
        // don't have neg ammo feed problem quirk
        if (!weapon.hasQuirk(OptionsConstants.QUIRK_WEAP_NEG_AMMO_FEED_PROBLEMS)) {
            return false;
        } else if ((roll.getIntValue() <= 2) && !ae.isConventionalInfantry()) {
            // attack roll was a 2, may explode
            Roll diceRoll = Compute.rollD6(2);

            Report r = new Report(3173);
            r.subject = subjectId;
            r.newlines = 0;
            r.add(diceRoll);
            vPhaseReport.addElement(r);

            if (diceRoll.getIntValue() == 12) {
                // round explodes in weapon
                r = new Report(3163);
                r.subject = subjectId;
                vPhaseReport.addElement(r);

                explodeRoundInBarrel(vPhaseReport);
            } else if (diceRoll.getIntValue() >= 10) {
                // plain old weapon jam
                r = new Report(3161);
                r.subject = subjectId;
                vPhaseReport.addElement(r);
                weapon.setJammed(true);
            } else {
                // nothing bad happens
                r = new Report(5041);
                r.subject = subjectId;
                vPhaseReport.addElement(r);
                return false;
            }
        } else {
            // attack roll was not 2, won't explode
            return false;
        }

        return true;
    }

    /**
     * Worker function that explodes a round in the barrel of the attack's weapon
     */
    protected void explodeRoundInBarrel(Vector<Report> vPhaseReport) {
        weapon.setJammed(true);
        weapon.setHit(true);

        int wloc = weapon.getLocation();
        for (int i = 0; i < ae.getNumberOfCriticals(wloc); i++) {
            CriticalSlot slot1 = ae.getCritical(wloc, i);
            if ((slot1 == null) || (slot1.getType() == CriticalSlot.TYPE_SYSTEM)) {
                continue;
            }
            Mounted<?> mounted = slot1.getMount();
            if (mounted.equals(weapon)) {
                ae.hitAllCriticals(wloc, i);
                break;
            }
        }

        // if we're here, the weapon is going to explode whether it's flagged as explosive or not
        vPhaseReport.addAll(gameManager.explodeEquipment(ae, wloc, weapon, true));
    }
}
