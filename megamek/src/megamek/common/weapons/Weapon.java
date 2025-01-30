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
package megamek.common.weapons;

import megamek.common.*;
import megamek.common.actions.WeaponAttackAction;
import megamek.common.annotations.Nullable;
import megamek.common.options.IGameOptions;
import megamek.common.options.OptionsConstants;
import megamek.common.weapons.bayweapons.CapitalLaserBayWeapon;
import megamek.common.weapons.bayweapons.SubCapLaserBayWeapon;
import megamek.server.totalwarfare.TWGameManager;

import java.io.Serializable;

/**
 * A class representing a weapon.
 * @author Andrew Hunter
 * @since May 10, 2004
 */
public abstract class Weapon extends WeaponType implements Serializable {
    private static final long serialVersionUID = -8781224279449654544L;

    public Weapon() {
        this.ammoType = AmmoType.T_NA;
        this.minimumRange = WEAPON_NA;
    }


    // marks any weapon affected by a targeting computer
    public static final WeaponTypeFlag F_DIRECT_FIRE = WeaponTypeFlag.F_DIRECT_FIRE;
    public static final WeaponTypeFlag F_FLAMER = WeaponTypeFlag.F_FLAMER;
    // Glaze armor
    public static final WeaponTypeFlag F_LASER = WeaponTypeFlag.F_LASER;
    public static final WeaponTypeFlag F_PPC = WeaponTypeFlag.F_PPC;
    // for weapons that target Automatically (AMS)
    public static final WeaponTypeFlag F_AUTO_TARGET = WeaponTypeFlag.F_AUTO_TARGET;
    // can not start fires
    public static final WeaponTypeFlag F_NO_FIRES = WeaponTypeFlag.F_NO_FIRES;
    // must be only weapon attacking
    public static final WeaponTypeFlag F_SOLO_ATTACK = WeaponTypeFlag.F_SOLO_ATTACK;
    public static final WeaponTypeFlag F_VGL = WeaponTypeFlag.F_VGL;
    // MGL for rapid fire setup
    public static final WeaponTypeFlag F_MG = WeaponTypeFlag.F_MG;
    // Inferno weapon
    public static final WeaponTypeFlag F_INFERNO = WeaponTypeFlag.F_INFERNO;
    // Infantry caliber weapon, damage based on # of men shooting
    public static final WeaponTypeFlag F_INFANTRY = WeaponTypeFlag.F_INFANTRY;
    // use missile rules for # of hits
    public static final WeaponTypeFlag F_MISSILE_HITS = WeaponTypeFlag.F_MISSILE_HITS;
    public static final WeaponTypeFlag F_ONESHOT = WeaponTypeFlag.F_ONESHOT;
    public static final WeaponTypeFlag F_ARTILLERY = WeaponTypeFlag.F_ARTILLERY;

    // for Gunnery/Ballistic
    public static final WeaponTypeFlag F_BALLISTIC = WeaponTypeFlag.F_BALLISTIC;
    // for Gunnery/Energy
    public static final WeaponTypeFlag F_ENERGY = WeaponTypeFlag.F_ENERGY;
    // for Gunnery/Missile
    public static final WeaponTypeFlag F_MISSILE = WeaponTypeFlag.F_MISSILE;

    // fires
    public static final WeaponTypeFlag F_PLASMA = WeaponTypeFlag.F_PLASMA;
    public static final WeaponTypeFlag F_INCENDIARY_NEEDLES = WeaponTypeFlag.F_INCENDIARY_NEEDLES;

    // War of 3039 prototypes
    public static final WeaponTypeFlag F_PROTOTYPE = WeaponTypeFlag.F_PROTOTYPE;
    // Variable heat, heat is listed in dice, not points
    public static final WeaponTypeFlag F_HEATASDICE = WeaponTypeFlag.F_HEATASDICE;
    // AMS
    public static final WeaponTypeFlag F_AMS = WeaponTypeFlag.F_AMS;

    // may only target Infantry
    public static final WeaponTypeFlag F_INFANTRY_ONLY = WeaponTypeFlag.F_INFANTRY_ONLY;

    public static final WeaponTypeFlag F_TAG = WeaponTypeFlag.F_TAG;
    // C3 Master with Target Acquisition gear
    public static final WeaponTypeFlag F_C3M = WeaponTypeFlag.F_C3M;

    // Plasma Rifle
    public static final WeaponTypeFlag F_PLASMA_MFUK = WeaponTypeFlag.F_PLASMA_MFUK;
    // fire Extinguisher
    public static final WeaponTypeFlag F_EXTINGUISHER = WeaponTypeFlag.F_EXTINGUISHER;
    public static final WeaponTypeFlag F_PULSE = WeaponTypeFlag.F_PULSE;
    // Full Damage vs. Infantry
    public static final WeaponTypeFlag F_BURST_FIRE = WeaponTypeFlag.F_BURST_FIRE;
    // Machine Gun Array
    public static final WeaponTypeFlag F_MGA = WeaponTypeFlag.F_MGA;
    public static final WeaponTypeFlag F_NO_AIM = WeaponTypeFlag.F_NO_AIM;
    public static final WeaponTypeFlag F_BOMBAST_LASER = WeaponTypeFlag.F_BOMBAST_LASER;
    public static final WeaponTypeFlag F_CRUISE_MISSILE = WeaponTypeFlag.F_CRUISE_MISSILE;
    public static final WeaponTypeFlag F_B_POD = WeaponTypeFlag.F_B_POD;
    public static final WeaponTypeFlag F_TASER = WeaponTypeFlag.F_TASER;

    // Anti-ship missiles
    public static final WeaponTypeFlag F_ANTI_SHIP = WeaponTypeFlag.F_ANTI_SHIP;
    public static final WeaponTypeFlag F_SPACE_BOMB = WeaponTypeFlag.F_SPACE_BOMB;
    public static final WeaponTypeFlag F_M_POD = WeaponTypeFlag.F_M_POD;
    public static final WeaponTypeFlag F_DIVE_BOMB = WeaponTypeFlag.F_DIVE_BOMB;
    public static final WeaponTypeFlag F_ALT_BOMB = WeaponTypeFlag.F_ALT_BOMB;

    // Currently only used by MML
    public static final WeaponTypeFlag F_BA_WEAPON = WeaponTypeFlag.F_BA_WEAPON;
    public static final WeaponTypeFlag F_MEK_WEAPON = WeaponTypeFlag.F_MEK_WEAPON;
    public static final WeaponTypeFlag F_AERO_WEAPON = WeaponTypeFlag.F_AERO_WEAPON;
    public static final WeaponTypeFlag F_PROTO_WEAPON = WeaponTypeFlag.F_PROTO_WEAPON;
    public static final WeaponTypeFlag F_TANK_WEAPON = WeaponTypeFlag.F_TANK_WEAPON;

    public static final WeaponTypeFlag F_INFANTRY_ATTACK = WeaponTypeFlag.F_INFANTRY_ATTACK;
    public static final WeaponTypeFlag F_INF_BURST = WeaponTypeFlag.F_INF_BURST;
    public static final WeaponTypeFlag F_INF_AA = WeaponTypeFlag.F_INF_AA;
    public static final WeaponTypeFlag F_INF_NONPENETRATING = WeaponTypeFlag.F_INF_NONPENETRATING;
    public static final WeaponTypeFlag F_INF_POINT_BLANK = WeaponTypeFlag.F_INF_POINT_BLANK;
    public static final WeaponTypeFlag F_INF_SUPPORT = WeaponTypeFlag.F_INF_SUPPORT;
    public static final WeaponTypeFlag F_INF_ENCUMBER = WeaponTypeFlag.F_INF_ENCUMBER;
    public static final WeaponTypeFlag F_INF_ARCHAIC = WeaponTypeFlag.F_INF_ARCHAIC;

    // TODO Add game rules IO pg 84
    public static final WeaponTypeFlag F_INF_CLIMBINGCLAWS = WeaponTypeFlag.F_INF_CLIMBINGCLAWS;

    // C3 Master Booster System
    public static final WeaponTypeFlag F_C3MBS = WeaponTypeFlag.F_C3MBS;

    // Naval Mass Drivers
    public static final WeaponTypeFlag F_MASS_DRIVER = WeaponTypeFlag.F_MASS_DRIVER;

    public static final WeaponTypeFlag F_CWS = WeaponTypeFlag.F_CWS;

    public static final WeaponTypeFlag F_MEK_MORTAR = WeaponTypeFlag.F_MEK_MORTAR;

    // Weapon required to make a bomb type function
    public static final WeaponTypeFlag F_BOMB_WEAPON = WeaponTypeFlag.F_BOMB_WEAPON;

    public static final WeaponTypeFlag F_BA_INDIVIDUAL = WeaponTypeFlag.F_BA_INDIVIDUAL;
// Next one's out of order. See F_INF_CLIMBINGCLAWS

    // AMS and Point Defense Bays - Have to work differently from code using the
// F_AMS flag
    public static final WeaponTypeFlag F_PDBAY = WeaponTypeFlag.F_PDBAY;
    public static final WeaponTypeFlag F_AMSBAY = WeaponTypeFlag.F_AMSBAY;

    // Thunderbolt and similar large missiles, for use with AMS resolution
    public static final WeaponTypeFlag F_LARGEMISSILE = WeaponTypeFlag.F_LARGEMISSILE;

    // Hyper-Laser
    public static final WeaponTypeFlag F_HYPER = WeaponTypeFlag.F_HYPER;

    // Fusillade works like a one-shot weapon but has a second round.
    public static final WeaponTypeFlag F_DOUBLE_ONESHOT = WeaponTypeFlag.F_DOUBLE_ONESHOT;
    // ER flamers do half damage in heat mode,
    public static final WeaponTypeFlag F_ER_FLAMER = WeaponTypeFlag.F_ER_FLAMER;
    /** Missile weapon that can be linked to an Artemis fire control system */
    public static final WeaponTypeFlag F_ARTEMIS_COMPATIBLE = WeaponTypeFlag.F_ARTEMIS_COMPATIBLE;

    /**
     * This flag is used by mortar-type weapons that allow indirect fire without a
     * spotter and/or with LOS.
     */
    public static final WeaponTypeFlag F_MORTARTYPE_INDIRECT = WeaponTypeFlag.F_MORTARTYPE_INDIRECT;

    // Used for TSEMP Weapons.
    public static final WeaponTypeFlag F_TSEMP = WeaponTypeFlag.F_TSEMP;
    public static final WeaponTypeFlag F_REPEATING = WeaponTypeFlag.F_REPEATING;


    //Mode text tokens
    public static final String MODE_FLAMER_DAMAGE = "Damage";
    public static final String MODE_FLAMER_HEAT = "Heat";

    public static final String MODE_AMS_ON = "On";
    public static final String MODE_AMS_OFF = "Off";
    public static final String MODE_AMS_MANUAL = "Use as Weapon";

    public static final String MODE_CAP_LASER_AAA = "AAA";

    public static final String MODE_CAPITAL_BRACKET_80 = "Bracket 80%";
    public static final String MODE_CAPITAL_BRACKET_60 = "Bracket 60%";
    public static final String MODE_CAPITAL_BRACKET_40 = "Bracket 40%";

    public static final String MODE_CAP_MISSILE_WAYPOINT_BEARING_EXT = "Waypoint Launch Bearings-Only Extreme Detection Range";
    public static final String MODE_CAP_MISSILE_WAYPOINT_BEARING_LONG = "Waypoint Launch Bearings-Only Long Detection Range";
    public static final String MODE_CAP_MISSILE_WAYPOINT_BEARING_MED = "Waypoint Launch Bearings-Only Medium Detection Range";
    public static final String MODE_CAP_MISSILE_WAYPOINT_BEARING_SHORT = "Waypoint Launch Bearings-Only Short Detection Range";
    public static final String MODE_CAP_MISSILE_WAYPOINT = "Waypoint Launch";

    public static final String MODE_CAP_MISSILE_BEARING_EXT = "Bearings-Only Extreme Detection Range";
    public static final String MODE_CAP_MISSILE_BEARING_LONG = "Bearings-Only Long Detection Range";
    public static final String MODE_CAP_MISSILE_BEARING_MED = "Bearings-Only Medium Detection Range";
    public static final String MODE_CAP_MISSILE_BEARING_SHORT = "Bearings-Only Short Detection Range";

    public static final String MODE_CAP_MISSILE_TELE_OPERATED = "Tele-Operated";

    public static final String MODE_AC_RAPID = "Rapid";
    public static final String MODE_AC_SINGLE = "Single";
    public static final String MODE_UAC_ULTRA = "Ultra";
    public static final String MODE_RAC_TWO_SHOT = "2-shot";
    public static final String MODE_RAC_THREE_SHOT = "3-shot";
    public static final String MODE_RAC_FOUR_SHOT = "4-shot";
    public static final String MODE_RAC_FIVE_SHOT = "5-shot";
    public static final String MODE_RAC_SIX_SHOT = "6-shot";

    public static final String MODE_GAUSS_POWERED_DOWN = "Powered Down";

    public static final String MODE_MISSILE_INDIRECT = "Indirect";
    public static final String MODE_INDIRECT_HEAT = "Indirect/Heat";

    public static final String MODE_PPC_CHARGE = "Charge";

    public static final String MODE_POINT_DEFENSE = "Point Defense";

    public static final String MODE_NORMAL = "Normal";


    public @Nullable AttackHandler fire(WeaponAttackAction waa, Game game, TWGameManager gameManager) {
        ToHitData toHit = waa.toHit(game);
        // FIXME: SUPER DUPER EVIL HACK: swarm missile handlers must be returned even
        // if the have an impossible to hit, because there might be other targets
        // someone else please please figure out how to do this nice
        AttackHandler ah = getCorrectHandler(toHit, waa, game, gameManager);
        return (ah instanceof LRMSwarmHandler) ? ah
                : (toHit.getValue() == TargetRoll.IMPOSSIBLE) ? null : ah;
    }

    protected AttackHandler getCorrectHandler(ToHitData toHit,
            WeaponAttackAction waa, Game game, TWGameManager gameManager) {
        return new WeaponHandler(toHit, waa, game, gameManager);
    }

    /**
     * Adapt the weapon type to the Game Options such as
     * PPC Field Inhibitors or Dial Down Damage, usually
     * adding or removing modes. <B><I>When overriding this in a
     * weapon subclass, call super()!</I></B>
     *
     * @param gameOptions The GameOptions (game.getOptions())
     * @author Simon (Juliez)
     */
    public void adaptToGameOptions(IGameOptions gameOptions) {
        // Flamers are spread out over all sorts of weapon types not limited to FlamerWeapon.
        // So modes are handled here.
        if (hasFlag(WeaponTypeFlag.F_FLAMER)) {
            if (!gameOptions.booleanOption(OptionsConstants.BASE_FLAMER_HEAT)) {
                addMode(MODE_FLAMER_DAMAGE);
                addMode(MODE_FLAMER_HEAT);
            } else {
                removeMode(MODE_FLAMER_DAMAGE);
                removeMode(MODE_FLAMER_HEAT);
            }
        }

        // Capital weapons are spread out over all sorts of weapons.
        if (isCapital()) {
            if ((getAtClass() != WeaponType.CLASS_CAPITAL_MISSILE)
                    && (getAtClass() != WeaponType.CLASS_TELE_MISSILE)
                    && (getAtClass() != WeaponType.CLASS_AR10)) {

                if ((this instanceof CapitalLaserBayWeapon)
                        || (this instanceof SubCapLaserBayWeapon)) {
                    if (gameOptions.booleanOption(OptionsConstants.ADVAERORULES_STRATOPS_AAA_LASER)) {
                        addMode("");
                        addMode("AAA");
                        addEndTurnMode("AAA");
                    } else {
                        removeMode("AAA");
                    }
                }
                if (gameOptions.booleanOption(OptionsConstants.ADVAERORULES_STRATOPS_BRACKET_FIRE)) {
                    addMode("");
                    addMode("Bracket 80%");
                    addMode("Bracket 60%");
                    addMode("Bracket 40%");
                } else {
                    removeMode("Bracket 80%");
                    removeMode("Bracket 60%");
                    removeMode("Bracket 40%");
                }
                // If only the standard mode "" is left, remove that as well
                if (getModesCount() == 1) {
                    clearModes();
                }

            } else {
                if (getAtClass() == WeaponType.CLASS_TELE_MISSILE) {
                    setInstantModeSwitch(true);
                    addMode(MODE_NORMAL);
                    addMode(MODE_CAP_MISSILE_TELE_OPERATED);
                }

                if (gameOptions.booleanOption(OptionsConstants.ADVAERORULES_STRATOPS_WAYPOINT_LAUNCH)) {
                    setInstantModeSwitch(true);
                    addMode(MODE_NORMAL);
                    addMode(MODE_CAP_MISSILE_WAYPOINT);
                    if (gameOptions.booleanOption(OptionsConstants.ADVAERORULES_STRATOPS_BEARINGS_ONLY_LAUNCH)) {
                        addMode(MODE_CAP_MISSILE_WAYPOINT_BEARING_EXT);
                        addMode(MODE_CAP_MISSILE_WAYPOINT_BEARING_LONG);
                        addMode(MODE_CAP_MISSILE_WAYPOINT_BEARING_MED);
                        addMode(MODE_CAP_MISSILE_WAYPOINT_BEARING_SHORT);
                    } else {
                        removeMode(MODE_CAP_MISSILE_WAYPOINT_BEARING_EXT);
                        removeMode(MODE_CAP_MISSILE_WAYPOINT_BEARING_LONG);
                        removeMode(MODE_CAP_MISSILE_WAYPOINT_BEARING_MED);
                        removeMode(MODE_CAP_MISSILE_WAYPOINT_BEARING_SHORT);
                    }
                } else {
                    removeMode(MODE_CAP_MISSILE_WAYPOINT);
                }

                if (gameOptions.booleanOption(OptionsConstants.ADVAERORULES_STRATOPS_BEARINGS_ONLY_LAUNCH)) {
                    setInstantModeSwitch(true);
                    addMode(MODE_NORMAL);
                    addMode(MODE_CAP_MISSILE_BEARING_EXT);
                    addMode(MODE_CAP_MISSILE_BEARING_LONG);
                    addMode(MODE_CAP_MISSILE_BEARING_MED);
                    addMode(MODE_CAP_MISSILE_BEARING_SHORT);
                } else {
                    removeMode(MODE_CAP_MISSILE_BEARING_EXT);
                    removeMode(MODE_CAP_MISSILE_BEARING_LONG);
                    removeMode(MODE_CAP_MISSILE_BEARING_MED);
                    removeMode(MODE_CAP_MISSILE_BEARING_SHORT);
                }
            }
        }

        if (hasFlag(WeaponTypeFlag.F_AMS)) {
            if (gameOptions.booleanOption(OptionsConstants.ADVCOMBAT_TACOPS_MANUAL_AMS)) {
                addMode(Weapon.MODE_AMS_MANUAL);
            }
            if (gameOptions.booleanOption(OptionsConstants.BASE_AUTO_AMS)) {
                removeMode("Automatic");
            } else {
                addMode("Automatic");
            }
        }
    }
}
