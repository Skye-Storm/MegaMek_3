/**
 * MegaMek - Copyright (C) 2005 Ben Mazur (bmazur@sev.org)
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
package megamek.common.weapons;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import megamek.common.Aero;
import megamek.common.AmmoType;
import megamek.common.BattleArmor;
import megamek.common.Building;
import megamek.common.Compute;
import megamek.common.Entity;
import megamek.common.EquipmentType;
import megamek.common.HitData;
import megamek.common.IGame;
import megamek.common.Infantry;
import megamek.common.Mech;
import megamek.common.MiscType;
import megamek.common.Mounted;
import megamek.common.RangeType;
import megamek.common.Report;
import megamek.common.Tank;
import megamek.common.TargetRoll;
import megamek.common.Targetable;
import megamek.common.ToHitData;
import megamek.common.WeaponType;
import megamek.common.actions.WeaponAttackAction;
import megamek.server.Server;

/**
 * @author Sebastian Brocks
 */
public class MissileWeaponHandler extends AmmoWeaponHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -4801130911083653548L;
    String sSalvoType = " missile(s) ";
    boolean amsEnganged = false;
    int nSalvoBonus = 0;

    /**
     * @param t
     * @param w
     * @param g
     * @param s
     */
    public MissileWeaponHandler(ToHitData t, WeaponAttackAction w, IGame g,
            Server s) {
        super(t, w, g, s);
        generalDamageType = HitData.DAMAGE_MISSILE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see megamek.common.weapons.WeaponHandler#calcHits(java.util.Vector)
     */
    protected int calcHits(Vector<Report> vPhaseReport) {
        // conventional infantry gets hit in one lump
        // BAs do one lump of damage per BA suit
        if (target instanceof Infantry && !(target instanceof BattleArmor)) {
            if (ae instanceof BattleArmor) {
                bSalvo = true;
                r = new Report(3325);
                r.subject = subjectId;
                r.add(wtype.getRackSize()
                        * ((BattleArmor) ae).getShootingStrength());
                r.add(sSalvoType);
                r.add(toHit.getTableDesc());
                r.newlines = 0;
                vPhaseReport.add(r);
                return ((BattleArmor) ae).getShootingStrength();
            }
            r = new Report(3325);
            r.subject = subjectId;
            r.add(wtype.getRackSize());
            r.add(sSalvoType);
            r.add(toHit.getTableDesc());
            r.newlines = 0;
            vPhaseReport.add(r);
            return 1;
        }
        Entity entityTarget = (target.getTargetType() == Targetable.TYPE_ENTITY) ? (Entity) target
                : null;
        int missilesHit;
        int nMissilesModifier = nSalvoBonus;
        boolean bWeather = false;
        boolean maxtechmissiles = game.getOptions().booleanOption(
                "maxtech_mslhitpen");
        if (maxtechmissiles) {
            if (nRange <= 1) {
                nMissilesModifier += 1;
            } else if (nRange <= wtype.getShortRange()) {
                nMissilesModifier += 0;
            } else if (nRange <= wtype.getMediumRange()) {
                nMissilesModifier -= 1;
            } else {
                nMissilesModifier -= 2;
            }
        }
        boolean bMekStealthActive = false;
        if (ae instanceof Mech) {
            bMekStealthActive = ae.isStealthActive();
        }
        Mounted mLinker = weapon.getLinkedBy();
        AmmoType atype = (AmmoType) ammo.getType();
        // is any hex in the flight path of the missile ECM affected?
        boolean bECMAffected = false;
        // if the attacker is affected by ECM or the target is protected by ECM
        // then
        // act as if effected.
        if (Compute.isAffectedByECM(ae, ae.getPosition(), target.getPosition())) {
            bECMAffected = true;
        }
        
        if ((mLinker != null && mLinker.getType() instanceof MiscType
                && !mLinker.isDestroyed() && !mLinker.isMissing()
                && !mLinker.isBreached() && mLinker.getType().hasFlag(
                MiscType.F_ARTEMIS))
                && atype.getMunitionType() == AmmoType.M_ARTEMIS_CAPABLE) {
            if (bECMAffected) {
                // ECM prevents bonus
                r = new Report(3330);
                r.subject = subjectId;
                r.newlines = 0;
                vPhaseReport.addElement(r);
            } else if (bMekStealthActive) {
                // stealth prevents bonus
                r = new Report(3335);
                r.subject = subjectId;
                r.newlines = 0;
                vPhaseReport.addElement(r);
            } else
                nMissilesModifier += 2;
        } else if (atype.getAmmoType() == AmmoType.T_ATM) {
            if (bECMAffected) {
                // ECM prevents bonus
                r = new Report(3330);
                r.subject = subjectId;
                r.newlines = 0;
                vPhaseReport.addElement(r);
            } else if (bMekStealthActive) {
                // stealth prevents bonus
                r = new Report(3335);
                r.subject = subjectId;
                r.newlines = 0;
                vPhaseReport.addElement(r);
            } else
                nMissilesModifier += 2;
        } else if (entityTarget != null
                && (entityTarget.isNarcedBy(ae.getOwner().getTeam()) || entityTarget
                        .isINarcedBy(ae.getOwner().getTeam()))) {
            // only apply Narc bonus if we're not suffering ECM effect
            // and we are using narc ammo, and we're not firing indirectly.
            // narc capable missiles are only affected if the narc pod, which 
            // sits on the target, is ECM affected
            boolean bTargetECMAffected = false;
            bTargetECMAffected = Compute.isAffectedByECM(ae, 
                    target.getPosition(), target.getPosition());
            if (((atype.getAmmoType() == AmmoType.T_LRM) ||
                 (atype.getAmmoType() == AmmoType.T_SRM)) ||
                 (atype.getAmmoType() == AmmoType.T_MML)
                    && atype.getMunitionType() == AmmoType.M_NARC_CAPABLE
                    && (weapon.curMode() == null || !weapon.curMode().equals(
                            "Indirect"))) {
                if (bTargetECMAffected) {
                    // ECM prevents bonus
                    r = new Report(3330);
                    r.subject = subjectId;
                    r.newlines = 0;
                    vPhaseReport.addElement(r);
                } else
                    nMissilesModifier += 2;
            }
        }
        if (bGlancing) {
            nMissilesModifier -= 4;
        }

        // weather checks
        if (game.getOptions().booleanOption("blizzard")
                && wtype.hasFlag(WeaponType.F_MISSILE)) {
            nMissilesModifier -= 4;
            bWeather = true;
        }

        if (game.getOptions().booleanOption("moderate_winds")
                && wtype.hasFlag(WeaponType.F_MISSILE)) {
            nMissilesModifier -= 2;
            bWeather = true;
        }

        if (game.getOptions().booleanOption("high_winds")
                && wtype.hasFlag(WeaponType.F_MISSILE)) {
            nMissilesModifier -= 4;
            bWeather = true;
        }

        // add AMS mods
        nMissilesModifier += getAMSHitsMod(vPhaseReport);

        if (allShotsHit())
            missilesHit = wtype.getRackSize();
        else {
            if (ae instanceof BattleArmor)
                missilesHit = Compute.missilesHit(wtype.getRackSize()
                        * ((BattleArmor) ae).getShootingStrength(),
                        nMissilesModifier, bWeather || bGlancing
                                || maxtechmissiles, weapon.isHotLoaded());
            else
                missilesHit = Compute.missilesHit(wtype.getRackSize(),
                        nMissilesModifier, bWeather || bGlancing
                                || maxtechmissiles, weapon.isHotLoaded());
        }

        if (missilesHit > 0) {
            r = new Report(3325);
            r.subject = subjectId;
            r.add(missilesHit);
            r.add(sSalvoType);
            r.add(toHit.getTableDesc());
            r.newlines = 0;
            vPhaseReport.addElement(r);
            if (nMissilesModifier != 0) {
                if (nMissilesModifier > 0)
                    r = new Report(3340);
                else
                    r = new Report(3341);
                r.subject = subjectId;
                r.add(nMissilesModifier);
                r.newlines = 0;
                vPhaseReport.addElement(r);
            }
        }
        r = new Report(3345);
        r.subject = subjectId;
        r.newlines = 0;
        vPhaseReport.addElement(r);
        bSalvo = true;
        return missilesHit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see megamek.common.weapons.WeaponHandler#calcnCluster()
     */
    protected int calcnCluster() {
        return 5;
    }

    /*
     * (non-Javadoc)
     * 
     * @see megamek.common.weapons.WeaponHandler#calcDamagePerHit()
     */
    protected int calcDamagePerHit() {
        double toReturn;
        if (target instanceof Infantry && !(target instanceof BattleArmor)) {
            toReturn = wtype.getRackSize();
            toReturn /= 5;
            toReturn = Math.ceil(toReturn);
            if (bGlancing)
                toReturn = (int) Math.floor(toReturn / 2.0);
            return (int)toReturn;
        }
        return 1;
    }

    /**
     * Calculate the attack value based on range
     * 
     * @return an <code>int</code> representing the attack value at that range.
     */
    protected int calcAttackValue() {
        int distance = ae.getPosition().distance(target.getPosition());
        int av = 0;
        int range = RangeType.rangeBracket(distance, wtype.getATRanges(), true);
        if(range == WeaponType.RANGE_SHORT) {
            av = wtype.getRoundShortAV();
        } else if(range == WeaponType.RANGE_MED) {
            av = wtype.getRoundMedAV();
        } else if (range == WeaponType.RANGE_LONG) {
            av = wtype.getRoundLongAV();
        } else if (range == WeaponType.RANGE_EXT) {
            av = wtype.getRoundExtAV();
        }
        Mounted mLinker = weapon.getLinkedBy();
        AmmoType atype = (AmmoType) ammo.getType();
        int bonus = 0;
        if ((mLinker != null && mLinker.getType() instanceof MiscType
                && !mLinker.isDestroyed() && !mLinker.isMissing()
                && !mLinker.isBreached() && mLinker.getType().hasFlag(
                MiscType.F_ARTEMIS))
                && atype.getMunitionType() == AmmoType.M_ARTEMIS_CAPABLE) {
            bonus = (int)Math.ceil(atype.getRackSize() / 5.0);
            if(atype.getAmmoType() == AmmoType.T_SRM) {
                bonus = 2;
            }
        }
        av = av + bonus;
        if (atype.getAmmoType() == AmmoType.T_MML && !atype.hasFlag(AmmoType.F_MML_LRM)) {
            av = av * 2;
        }
        return (av);
    }

    
    /*
     * (non-Javadoc)
     * 
     * @see megamek.common.weapons.WeaponHandler#handleSpecialMiss(megamek.common.Entity,
     *      boolean, megamek.common.Building)
     */
    protected boolean handleSpecialMiss(Entity entityTarget,
            boolean targetInBuilding, Building bldg, Vector<Report> vPhaseReport) {
        // Shots that miss an entity can set fires.
        // Buildings can't be accidentally ignited,
        // and some weapons can't ignite fires.
        if (entityTarget != null
                && (bldg == null && wtype.getFireTN() != TargetRoll.IMPOSSIBLE)) {
            server.tryIgniteHex(target.getPosition(), subjectId, false, 11,
                    vPhaseReport);
        }

        // Report any AMS action.
        if (amsEnganged) {
            r = new Report(3230);
            r.indent();
            r.subject = subjectId;
            vPhaseReport.addElement(r);
        }

        // BMRr, pg. 51: "All shots that were aimed at a target inside
        // a building and miss do full damage to the building instead."
        if (!targetInBuilding || toHit.getValue() == TargetRoll.AUTOMATIC_FAIL) {
            return false;
        }
        return true;
    }

    protected int getAMSHitsMod(Vector<Report> vPhaseReport) {
        Entity entityTarget = (target.getTargetType() == Targetable.TYPE_ENTITY) ? (Entity) target
                : null;
        if (entityTarget != null) {
            // any AMS attacks by the target?
            ArrayList<Mounted> lCounters = waa.getCounterEquipment();
            if (null != lCounters) {
                // resolve AMS counter-fire
                for (int x = 0; x < lCounters.size(); x++) {
                    Mounted counter = lCounters.get(x);
                    if (counter.getType().hasFlag(WeaponType.F_AMS)
                            && !amsEnganged) {

                        Mounted mAmmo = counter.getLinked();
                        Entity ae = waa.getEntity(game);
                        if (!(counter.getType() instanceof WeaponType)
                                || !counter.isReady() || counter.isMissing()
                                // no AMS when a shield in the AMS location
                                || entityTarget.hasShield()
                                && entityTarget.hasActiveShield(counter.getLocation(),
                                        false)
                                // shutdown means no AMS
                                || entityTarget.isShutDown()
                                // AMS only fires against attacks coming into
                                // the
                                // arc the ams is covering
                                || !Compute.isInArc(game, entityTarget.getId(),
                                        entityTarget.getEquipmentNum(counter),
                                        ae)) {
                            continue;
                        }

                        // build up some heat (assume target is ams owner)
                        if (counter.getType().hasFlag(WeaponType.F_HEATASDICE))
                            entityTarget.heatBuildup += Compute
                                    .d6(((WeaponType) counter.getType())
                                            .getHeat());
                        else
                            entityTarget.heatBuildup += ((WeaponType) counter
                                    .getType()).getHeat();

                        // decrement the ammo
                        if (mAmmo != null)
                            mAmmo.setShotsLeft(Math.max(0,
                                    mAmmo.getShotsLeft() - 1));

                        // set the ams as having fired
                        counter.setUsedThisRound(true);
                        this.amsEnganged = true;
                        r = new Report(3350);
                        r.newlines = 0;
                        vPhaseReport.add(r);
                        return -4;
                    }
                }
            }
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see megamek.common.weapons.AttackHandler#handle(int, java.util.Vector)
     */
    public boolean handle(IGame.Phase phase, Vector<Report> vPhaseReport) {
        if (!this.cares(phase)) {
            return true;
        }
        Entity entityTarget = (target.getTargetType() == Targetable.TYPE_ENTITY) ? (Entity) target
                : null;
        final boolean targetInBuilding = Compute.isInBuilding(game,
                entityTarget);
        boolean bNemesisConfusable = isNemesisConfusable();

        if (entityTarget != null)
            ae.setLastTarget(entityTarget.getId());

        // Which building takes the damage?
        Building bldg = game.getBoard().getBuildingAt(target.getPosition());

        // Report weapon attack and its to-hit value.
        r = new Report(3115);
        r.indent();
        r.newlines = 0;
        r.subject = subjectId;
        r.add(wtype.getName());
        if (entityTarget != null) {
            r.addDesc(entityTarget);
        } else {
            r.messageId = 3120;
            r.add(target.getDisplayName(), true);
        }
        vPhaseReport.addElement(r);
        // check for nemesis
        boolean shotAtNemesisTarget = false;
        if (bNemesisConfusable && !waa.isNemesisConfused()) {
            // loop through nemesis targets
            for (Enumeration<Entity> e = game.getNemesisTargets(ae, target
                    .getPosition()); e.hasMoreElements();) {
                Entity entity = e.nextElement();
                // friendly unit with attached iNarc Nemesis pod standing in the
                // way
                r = new Report(3125);
                r.subject = subjectId;
                vPhaseReport.addElement(r);
                weapon.setUsedThisRound(false);
                WeaponAttackAction newWaa = new WeaponAttackAction(ae.getId(),
                        entity.getTargetId(), waa.getWeaponId());
                newWaa.setNemesisConfused(true);
                Entity ae = game.getEntity(waa.getEntityId());
                Mounted m = ae.getEquipment(waa.getWeaponId());
                Weapon w = (Weapon) m.getType();
                AttackHandler ah = w.fire(newWaa, game, server);
                // increase ammo by one, becaues we just incorrectly used one up
                weapon.getLinked().setShotsLeft(weapon.getLinked().getShotsLeft()+1);
                WeaponHandler wh = (WeaponHandler) ah;
                // attack the new target, and if we hit it, return;
                wh.handle(phase, vPhaseReport);
                // if the new attack hit, we are finished.
                if (!wh.bMissed)
                    return false;
                shotAtNemesisTarget = true;
            }
            if (shotAtNemesisTarget) {
                // back to original target
                r = new Report(3130);
                r.subject = subjectId;
                r.newlines = 0;
                r.indent();
                vPhaseReport.addElement(r);
            }
        }
        if (toHit.getValue() == ToHitData.IMPOSSIBLE) {
            r = new Report(3135);
            r.subject = subjectId;
            r.add(toHit.getDesc());
            vPhaseReport.addElement(r);
            return false;
        } else if (toHit.getValue() == ToHitData.AUTOMATIC_FAIL) {
            r = new Report(3140);
            r.newlines = 0;
            r.subject = subjectId;
            r.add(toHit.getDesc());
            vPhaseReport.addElement(r);
        } else if (toHit.getValue() == ToHitData.AUTOMATIC_SUCCESS) {
            r = new Report(3145);
            r.newlines = 0;
            r.subject = subjectId;
            r.add(toHit.getDesc());
            vPhaseReport.addElement(r);
        } else {
            // roll to hit
            r = new Report(3150);
            r.newlines = 0;
            r.subject = subjectId;
            r.add(toHit.getValue());
            vPhaseReport.addElement(r);
        }

        // dice have been rolled, thanks
        r = new Report(3155);
        r.newlines = 0;
        r.subject = subjectId;
        r.add(roll);
        vPhaseReport.addElement(r);

        // do we hit?
        bMissed = roll < toHit.getValue();

        // are we a glancing hit?
        if (game.getOptions().booleanOption("maxtech_glancing_blows")) {
            if (roll == toHit.getValue()) {
                bGlancing = true;
                r = new Report(3186);
                r.subject = subjectId;
                r.newlines = 0;
                vPhaseReport.addElement(r);
            } else {
                bGlancing = false;
            }
        } else {
            bGlancing = false;
        }

        // Do this stuff first, because some weapon's miss report reference the
        // amount of shots fired and stuff.
        if (!shotAtNemesisTarget) {
            addHeat();
        }
        // Any necessary PSRs, jam checks, etc.
        // If this boolean is true, don't report
        // the miss later, as we already reported
        // it in doChecks
        boolean missReported = doChecks(vPhaseReport);

        nDamPerHit = calcDamagePerHit();

        // Do we need some sort of special resolution (minefields, artillery,
        if (specialResolution(vPhaseReport, entityTarget, bMissed)) {
            return false;
        }

        if (bMissed && !missReported) {
            reportMiss(vPhaseReport);

            // Works out fire setting, AMS shots, and whether continuation is
            // necessary.
            if (!handleSpecialMiss(entityTarget, targetInBuilding, bldg,
                    vPhaseReport)) {
                return false;
            }
        }

        // yeech. handle damage. . different weapons do this in very different
        // ways
        int hits = calcHits(vPhaseReport), nCluster = calcnCluster();

        //Now I need to adjust this for air-to-air attacks because they
        //use attack value
        if(ae instanceof Aero && target instanceof Aero) {
            if(hits == 1 && nCluster == 1) {
                nDamPerHit = attackValue;
            } else {
                nDamPerHit = 1;
                hits = attackValue;
                nCluster = 5;
            }
            
        }
        
        // We've calculated how many hits. At this point, any missed
        // shots damage the building instead of the target.
        if (bMissed) {
            if (targetInBuilding && bldg != null) {
                handleAccidentalBuildingDamage(vPhaseReport, bldg, hits,
                        nDamPerHit);
            } // End missed-target-in-building
            return false;

        } // End missed-target

        // The building shields all units from a certain amount of damage.
        // The amount is based upon the building's CF at the phase's start.
        int bldgAbsorbs = 0;
        if (targetInBuilding && bldg != null) {
            bldgAbsorbs = (int) Math.ceil(bldg.getPhaseCF() / 10.0);
        }

        // Make sure the player knows when his attack causes no damage.
        if (hits == 0) {
            r = new Report(3365);
            r.subject = subjectId;
            vPhaseReport.addElement(r);
        }

        // for each cluster of hits, do a chunk of damage
        while (hits > 0) {
            int nDamage;
            // targeting a hex for igniting
            if (target.getTargetType() == Targetable.TYPE_HEX_IGNITE
                    || target.getTargetType() == Targetable.TYPE_BLDG_IGNITE) {
                handleIgnitionDamage(vPhaseReport, bldg, bSalvo, hits);
                return false;
            }
            // targeting a hex for clearing
            if (target.getTargetType() == Targetable.TYPE_HEX_CLEAR) {
                nDamage = nDamPerHit * hits;
                handleClearDamage(vPhaseReport, bldg, nDamage, bSalvo);
                return false;
            }
            // Targeting a building.
            if (target.getTargetType() == Targetable.TYPE_BUILDING) {
                // The building takes the full brunt of the attack.
                nDamage = nDamPerHit * hits;
                handleBuildingDamage(vPhaseReport, bldg, nDamage, bSalvo);
                // And we're done!
                return false;
            }
            if (entityTarget != null) {
                handleEntityDamage(entityTarget, vPhaseReport, bldg, hits,
                        nCluster, nDamPerHit, bldgAbsorbs);
                server.creditKill(entityTarget, ae);
                hits -= nCluster;
            }
        } // Handle the next cluster.
        Report.addNewline(vPhaseReport);
        return false;
    }

    protected boolean isNemesisConfusable() {
        // Are we iNarc Nemesis Confusable?
        boolean isNemesisConfusable = false;
        AmmoType atype = (AmmoType) weapon.getLinked().getType();
        Mounted mLinker = weapon.getLinkedBy();
        if (wtype.getAmmoType() == AmmoType.T_ATM
                || (mLinker != null && mLinker.getType() instanceof MiscType
                        && !mLinker.isDestroyed() && !mLinker.isMissing()
                        && !mLinker.isBreached() && mLinker.getType().hasFlag(
                        MiscType.F_ARTEMIS))) {
            if ((!weapon.getType().hasModes() || !weapon.curMode().equals(
                    "Indirect"))
                    && (((atype.getAmmoType() == AmmoType.T_ATM) && (atype
                            .getMunitionType() == AmmoType.M_STANDARD
                            || atype.getMunitionType() == AmmoType.M_EXTENDED_RANGE || atype
                            .getMunitionType() == AmmoType.M_HIGH_EXPLOSIVE)) || ((atype
                            .getAmmoType() == AmmoType.T_LRM || atype
                            .getAmmoType() == AmmoType.T_SRM) && atype
                            .getMunitionType() == AmmoType.M_ARTEMIS_CAPABLE))) {
                isNemesisConfusable = true;
            }
        } else if (wtype.getAmmoType() == AmmoType.T_LRM
                || wtype.getAmmoType() == AmmoType.T_SRM) {
            if (atype.getMunitionType() == AmmoType.M_NARC_CAPABLE
                    || atype.getMunitionType() == AmmoType.M_LISTEN_KILL) {
                isNemesisConfusable = true;
            }
        }
        return isNemesisConfusable;
    }
    
    protected boolean usesClusterTable() {
        return true;
    }
}
