/*
 * MegaMek - Copyright (C) 2000-2002 Ben Mazur (bmazur@sev.org)
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
package megamek.common.building;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import megamek.common.Compute;
import megamek.common.Coords;
import megamek.common.IBoard;
import megamek.common.IHex;
import megamek.common.ITerrain;
import megamek.common.Report;
import megamek.common.Terrains;
import megamek.common.logging.DefaultMmLogger;

/**
 * Represents a single, possibly multi-hex building on the board.
 *
 * @author Suvarov454@sourceforge.net (James A. Damour )
 */
public class Building implements Serializable {

    private static final long serialVersionUID = -8236017592012683793L;

    /** @deprecated magic values shall be removed */
    @Deprecated public static final int UNKNOWN = -1;

    /** @deprecated use {@link ConstructionType#LIGHT}    instead */ @Deprecated public static final int LIGHT = 1;
    /** @deprecated use {@link ConstructionType#MEDIUM}   instead */ @Deprecated public static final int MEDIUM = 2;
    /** @deprecated use {@link ConstructionType#HEAVY}    instead */ @Deprecated public static final int HEAVY = 3;
    /** @deprecated use {@link ConstructionType#HARDENED} instead */ @Deprecated public static final int HARDENED = 4;
    /** @deprecated use {@link ConstructionType#WALL}     instead */ @Deprecated public static final int WALL = 5;

    /** @deprecated use {@link BuildingClass#STANDARD}        instead */ @Deprecated public static final int STANDARD = 0;
    /** @deprecated use {@link BuildingClass#HANGAR}          instead */ @Deprecated public static final int HANGAR = 1;
    /** @deprecated use {@link BuildingClass#FORTRESS}        instead */ @Deprecated public static final int FORTRESS = 2;
    /** @deprecated use {@link BuildingClass#GUN_EMPLACEMENT} instead */ @Deprecated public static final int GUN_EMPLACEMENT = 3;

    /**
     * Constructs a new building at the given coordinates, fetching info from the given board 
     */
    public static Building newBuildingAt(Coords coords, IBoard board) {
        IHex curHex = board.getHex(coords);
        requirePresent(curHex, Terrains.BUILDING);
        BasementType basementType = BasementType.getType(curHex.terrainLevel(Terrains.BLDG_BASEMENT_TYPE));
        return new Building(buildingIdFromCoordinates(coords), coords, board, Terrains.BUILDING, basementType);
    }

    /**
     * Constructs a new bridge at the given coordinates, fetching info from the given board 
     */
    public static Building newBridgeAt(Coords coords, IBoard board) {
        IHex curHex = board.getHex(coords);
        requirePresent(curHex, Terrains.BRIDGE);
        return new Building(buildingIdFromCoordinates(coords), coords, board, Terrains.BRIDGE, BasementType.NONE);
    }

    /**
     * Constructs a new fuel tank at the given coordinates, fetching info from the given board 
     */
    public static FuelTank newFuelTankAt(Coords coords, IBoard board) {
        IHex curHex = board.getHex(coords);
        requirePresent(curHex, Terrains.FUEL_TANK);
        int magnitude = curHex.getTerrain(Terrains.FUEL_TANK_MAGN).getLevel();
        return new FuelTank(buildingIdFromCoordinates(coords), coords, board, Terrains.FUEL_TANK, magnitude);
    }

    /**
     * Construct a building for the given coordinates from the board's
     * information. If the building covers multiple hexes, every hex will be
     * included in the building.
     *
     * @param coords
     *        the <code>Coords</code> of a hex of the building. If the
     *        building covers multiple hexes, this constructor will include
     *        them all in this building automatically.
     * @param board
     *        the game's <code>Board</code> object.
     *
     * @throws IllegalArgumentException
     *        if the given coordinates do not contain a building, or if the
     *        building covers multiple hexes with different CFs.
     */
    protected Building(int id, Coords coords, IBoard board, int structureType, BasementType basementType) {

        IHex initialHex = board.getHex(coords);

        this.id            = id;
        this.structureType = structureType;
        this.type          = initialHex.terrainLevel(structureType);
        this.bldgClass     = initialHex.getBuildingClass().map(BuildingClass::getId).orElse(ITerrain.LEVEL_NONE); // this is actually optional

        {
            String msg = String.format("Building at: %s, structureType: %s, type: %s, bldgClass: %s,", coords.getBoardNum(), structureType, type, bldgClass); //$NON-NLS-1$
            DefaultMmLogger.getInstance().info(getClass(), "<init>", msg); //$NON-NLS-1$
        }

        // The building occupies the given coords, at least.
        coordinates.add(coords);
        originalHexes++;

        burning.put(coords, false);


        // Insure that we've got a good type (and initialize our CF).
        currentCF.put(coords, ConstructionType.ofRequiredId(type).getDefaultCF());

        // Now read the *real* CF, if the board specifies one.
        if ((structureType == Terrains.BUILDING)
                && initialHex.containsTerrain(Terrains.BLDG_CF)) {
            currentCF.put(coords, initialHex.terrainLevel(Terrains.BLDG_CF));
        }
        if ((structureType == Terrains.BRIDGE)
                && initialHex.containsTerrain(Terrains.BRIDGE_CF)) {
            currentCF.put(coords, initialHex.terrainLevel(Terrains.BRIDGE_CF));
        }
        if ((structureType == Terrains.FUEL_TANK)
                && initialHex.containsTerrain(Terrains.FUEL_TANK_CF)) {
            currentCF.put(coords, initialHex.terrainLevel(Terrains.FUEL_TANK_CF));
        }
        if (initialHex.containsTerrain(Terrains.BLDG_ARMOR)) {
            armor.put(coords, initialHex.terrainLevel(Terrains.BLDG_ARMOR));
        } else {
            armor.put(coords, 0);
        }

        phaseCF.putAll(currentCF);

        basement.put(coords, basementType);
        basementCollapsed.put(coords, initialHex.terrainLevel(Terrains.BLDG_BASE_COLLAPSED) == 1);

        // Walk through the exit directions and
        // identify all hexes in this building.
        for (int dir = 0; dir < 6; dir++) {

            // Does the building exit in this direction?
            if (initialHex.containsTerrainExit(structureType, dir)) {
                include(coords.translated(dir), board);
            }

        }

        // sanity checks temporary logging

        DefaultMmLogger.getInstance().info(getClass(), "<init>", "coords: " + coordinates.stream().map(Coords::getBoardNum).collect(Collectors.joining(", ")));   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

        if (originalHexes != coordinates.size()) {
            String msg = String.format("originalHexes %s, coordinates.size()", originalHexes, coordinates.size()); //$NON-NLS-1$
            DefaultMmLogger.getInstance().info(getClass(), "<init>", msg); //$NON-NLS-1$
        }

        {
            Map<Coords, IHex> spannedHexes = getSpannedHexes(initialHex, board, structureType);
            if (originalHexes != spannedHexes.size()) {
                String msg = String.format("originalHexes %s, spannedHexes.size()", originalHexes, spannedHexes.size()); //$NON-NLS-1$
                DefaultMmLogger.getInstance().info(getClass(), "<init>", msg); //$NON-NLS-1$
            }
    
            for (Coords c : coordinates) {
                if (spannedHexes.remove(c) == null) {
                    String msg = String.format("hex %s missed by spannedHexes", c); //$NON-NLS-1$
                    DefaultMmLogger.getInstance().info(getClass(), "<init>", msg); //$NON-NLS-1$
                }
            }
    
            if (!spannedHexes.isEmpty()) for (Coords c :spannedHexes.keySet()) {
                String msg = String.format("extra hex %s present in spannedHexes", c); //$NON-NLS-1$
                DefaultMmLogger.getInstance().info(getClass(), "<init>", msg); //$NON-NLS-1$
            }
        }

        List<BuildingSection> sections = getSpannedHexes(initialHex, board, structureType).values().stream().map((IHex hex) -> {
            return BuildingSection.at(hex, structureType, basementType);
        }).collect(Collectors.toList());

        if (sections.size() != coordinates.size()) {
            String msg = String.format("XXX sections: %s, coords: %s", sections.size(), coordinates.size()); //$NON-NLS-1$
            DefaultMmLogger.getInstance().info(getClass(), "<init>", msg); //$NON-NLS-1$
        }

        for (BuildingSection bs : sections) {
            if (!coordinates.contains(bs.getCoordinates())) {
                String msg = String.format("section at %s: no coords", bs.getCoordinates()); //$NON-NLS-1$
                DefaultMmLogger.getInstance().info(getClass(), "<init>", msg); //$NON-NLS-1$
            } else {
                Coords c = bs.getCoordinates();
                if (bs.getBasementType() != basement.get(c)) {
                    String msg = String.format("section %s: basement set to %s but should be %s", c, bs.getBasementType(), basement.get(c)); //$NON-NLS-1$
                    DefaultMmLogger.getInstance().info(getClass(), "<init>", msg); //$NON-NLS-1$
                }
                if (bs.getCurrentCF() != currentCF.get(c)) {
                    String msg = String.format("section %s: current CF set to %s but should be %s", c, bs.getCurrentCF(), currentCF.get(c)); //$NON-NLS-1$
                    DefaultMmLogger.getInstance().info(getClass(), "<init>", msg); //$NON-NLS-1$
                }
                if (bs.getPhaseCF() != phaseCF.get(c)) {
                    String msg = String.format("section %s: phase CF set to %s but should be %s", c, bs.getPhaseCF(), phaseCF.get(c)); //$NON-NLS-1$
                    DefaultMmLogger.getInstance().info(getClass(), "<init>", msg); //$NON-NLS-1$
                }
                if (bs.getArmor() != armor.get(c)) {
                    String msg = String.format("section %s: armor set to %s but should be %s", c, bs.getArmor(), armor.get(c)); //$NON-NLS-1$
                    DefaultMmLogger.getInstance().info(getClass(), "<init>", msg); //$NON-NLS-1$
                }
                if (bs.isBurning() != burning.get(c)) {
                    String msg = String.format("section %s: burning set to %s but should be %s", c, bs.isBurning(), burning.get(c)); //$NON-NLS-1$
                    DefaultMmLogger.getInstance().info(getClass(), "<init>", msg); //$NON-NLS-1$
                }
                if (bs.isBasementCollapsed() != basementCollapsed.get(c)) {
                    String msg = String.format("section %s: collapsed set to %s but should be %s", c, bs.isBasementCollapsed(), basementCollapsed.get(c)); //$NON-NLS-1$
                    DefaultMmLogger.getInstance().info(getClass(), "<init>", msg); //$NON-NLS-1$
                }
            }
        }

    }

    private final int id;

    private final int structureType;
    /** @deprecated this is being refactored out and  the int replaced with ConstructionType */
    @Deprecated private final int type;
    /** @deprecated this is being refactored out and  the int replaced with BuildingClass */
    @Deprecated private final int bldgClass;

    private int collapsedHexes = 0;
    private int originalHexes = 0;
    private List<DemolitionCharge> demolitionCharges = new ArrayList<>();

    private List<Coords> coordinates = new ArrayList<>();
    private final Map<Coords,BasementType> basement = new HashMap<>();
    private Map<Coords, Integer> currentCF = new HashMap<>(); // any damage immediately updates this value
    private Map<Coords, Integer> phaseCF = new HashMap<>(); // cf at start of phase - damage is applied at the end of the phase it was received in
    private Map<Coords, Integer> armor = new HashMap<>();
    private Map<Coords, Boolean> basementCollapsed = new HashMap<>();
    private Map<Coords, Boolean> burning = new HashMap<>();


    // TODO: leaving out Castles Brian until issues with damage scaling are
    // resolved
    // public static final int CASTLE_BRIAN = 3;

    /**
     * Determine if the building occupies given coordinates. Multi-hex buildings
     * will occupy multiple coordinates. Only one building per hex.
     *
     * @param coords
     *            - the <code>Coords</code> being examined.
     * @return <code>true</code> if the building occupies the coordinates.
     *         <code>false</code> otherwise.
     */
    public boolean isIn(Coords coords) {
        return coordinates.contains(coords);
    }

    /**
     * Determins if the coord exist in the currentCF has.
     *
     * @param coords
     *            - the <code>Coords</code> being examined.
     * @return <code>true</code> if the building has CF at the coordinates.
     *         <code>false</code> otherwise.
     */
    public boolean hasCFIn(Coords coords) {
        return currentCF.containsKey(coords);

    }

    /** @deprecated use {@link #iterateCoords()} instead */
    @Deprecated public Enumeration<Coords> getCoords() {
        return Collections.enumeration(coordinates);
    }

    public Iterator<Coords> iterateCoords() {
        return Collections.unmodifiableList(coordinates).iterator();
    }

    /**
     * @return the structure type of this building
     *         ({@linkplain Terrains#BUILDING},
     *         {@linkplain Terrains#FUEL_TANK} or
     *         {@linkplain Terrains#BRIDGE})
     */
    public int getStructureType() {
        return structureType;
    }

    public Optional<ConstructionType> getConstructionType() {
        return ConstructionType.ofId(getType());
    } 

    /** @deprecated use {@link #getConstructionType()} instead */
    @Deprecated public int getType() { return type; }

    public Optional<BuildingClass> getBuildingClass() {
        return BuildingClass.ofId(getBldgClass());
    } 

    /** @deprecated use {@link #getBuildingClass()} instead */
    @Deprecated public int getBldgClass() { return bldgClass; }

    /**
     * Get the building basement, per TacOps rules.
     *
     * @return the <code>int</code> code of the buildingbasement type.
     */
    public boolean getBasementCollapsed(Coords coords) {
        return basementCollapsed.get(coords);
    }

    public void collapseBasement(Coords coords, IBoard board, List<Report> vPhaseReport) {
        if ((basement.get(coords) == BasementType.NONE) || (basement.get(coords) == BasementType.ONE_DEEP_NORMALINFONLY)) {
            System.err.println("hex has no basement to collapse"); //$NON-NLS-1$
            return;
        }
        if (basementCollapsed.get(coords)) {
            System.err.println("hex has basement that already collapsed"); //$NON-NLS-1$
            return;
        }
        Report r = new Report(2112, Report.PUBLIC);
        r.add(getName());
        r.add(coords.getBoardNum());
        vPhaseReport.add(r);
        System.err.println("basement " + basement + "is collapsing, hex:" //$NON-NLS-1$ //$NON-NLS-2$
                + coords.toString() + " set terrain!"); //$NON-NLS-1$
        board.getHex(coords).addTerrain(Terrains.getTerrainFactory().createTerrain(
                Terrains.BLDG_BASE_COLLAPSED, 1));
        basementCollapsed.put(coords, true);

    }

    /**
     * Roll what kind of basement this building has
     * @param coords the <code>Coords</code> of theb building to roll for
     * @param vPhaseReport the <code>Vector<Report></code> containing the phasereport
     * @return a <code>boolean</code> indicating wether the hex and building was changed or not
     */
    public boolean rollBasement(Coords coords, IBoard board, List<Report> vPhaseReport) {
        if (basement.get(coords) == BasementType.UNKNOWN) {
            IHex hex = board.getHex(coords);
            Report r = new Report(2111, Report.PUBLIC);
            r.add(getName());
            r.add(coords.getBoardNum());
            int basementRoll = Compute.d6(2);
            r.add(basementRoll);
            if (basementRoll == 2) {
                basement.put(coords, BasementType.TWO_DEEP_FEET);
                hex.addTerrain(Terrains.getTerrainFactory().createTerrain(
                        Terrains.BLDG_BASEMENT_TYPE, basement.get(coords).getValue()));
            } else if (basementRoll == 3) {
                basement.put(coords, BasementType.ONE_DEEP_FEET);
                hex.addTerrain(Terrains.getTerrainFactory().createTerrain(
                        Terrains.BLDG_BASEMENT_TYPE, basement.get(coords).getValue()));
            } else if (basementRoll == 4) {
                basement.put(coords, BasementType.ONE_DEEP_NORMAL);
                hex.addTerrain(Terrains.getTerrainFactory().createTerrain(
                        Terrains.BLDG_BASEMENT_TYPE, basement.get(coords).getValue()));
            } else if (basementRoll == 10) {
                basement.put(coords, BasementType.ONE_DEEP_NORMAL);
                hex.addTerrain(Terrains.getTerrainFactory().createTerrain(
                        Terrains.BLDG_BASEMENT_TYPE, basement.get(coords).getValue()));
            } else if (basementRoll == 11) {
                basement.put(coords, BasementType.ONE_DEEP_HEAD);
                hex.addTerrain(Terrains.getTerrainFactory().createTerrain(
                        Terrains.BLDG_BASEMENT_TYPE, basement.get(coords).getValue()));
            } else if (basementRoll == 12) {
                basement.put(coords, BasementType.TWO_DEEP_HEAD);
                hex.addTerrain(Terrains.getTerrainFactory().createTerrain(
                        Terrains.BLDG_BASEMENT_TYPE, basement.get(coords).getValue()));
            } else {
                basement.put(coords, BasementType.NONE);
                hex.addTerrain(Terrains.getTerrainFactory().createTerrain(
                        Terrains.BLDG_BASEMENT_TYPE, basement.get(coords).getValue()));
            }
            r.add(BasementType.getType(hex.terrainLevel(Terrains.BLDG_BASEMENT_TYPE)).getDesc());
            vPhaseReport.add(r);
            return true;
        }
        return false;
    }

    /**
     * Get the current construction factor of the building hex at the passed
     * coords. Any damage immediately updates this value.
     *
     * @param coords
     *            - the <code>Coords> of the hex in question
     *
     * @return the <code>int</code> value of the building hex's current
     *         construction factor. This value will be greater than or equal to
     *         zero.
     */
    public int getCurrentCF(Coords coords) {
        return currentCF.get(coords);
    }

    /**
     * Get the construction factor of the building hex at the passed coords at
     * the start of the current phase. Damage that is received during the phase
     * is applied at the end of the phase.
     *
     * @param coords
     *            - the <code>Coords> of the hex in question
     * @return the <code>int</code> value of the building's construction factor
     *         at the start of this phase. This value will be greater than or
     *         equal to zero.
     */
    public int getPhaseCF(Coords coords) {
        return phaseCF.get(coords);
    }

    public int getArmor(Coords coords) {
        return armor.get(coords);
    }

    /**
     * Set the current construction factor of the building hex. Call this method
     * immediately when the building sustains any damage.
     *
     * @param coords
     *        the <code>Coords> of the hex in question
     * @param cf
     *        the <code>int</code> value of the building hex's current
     *        construction factor. This value must be greater than or equal
     *        to zero.
     * @throws IllegalArgumentException
     *         if the passed value is less than zero, an
     *          <code>IllegalArgumentException</code> is thrown.
     */
    public void setCurrentCF(int cf, Coords coords) {
        if (cf < 0) {
            throw new IllegalArgumentException(
                    "Invalid value for Construction Factor: " + cf); //$NON-NLS-1$
        }

        currentCF.put(coords, cf);
    }

    /**
     * Set the construction factor of the building hex for the start of the next
     * phase. Call this method at the end of the phase to apply damage sustained
     * by the building during the phase.
     *
     * @param coords
     *        the <code>Coords> of the hex in question
     * @param cf
     *        the <code>int</code> value of the building's current
     *        construction factor. This value must be greater than or equal
     *        to zero.
     *
     * @throws IllegalArgumentException
     *         if the passed value is less than zero, an
     *         <code>IllegalArgumentException</code> is thrown.
     */
    public void setPhaseCF(int cf, Coords coords) {
        if (cf < 0) {
            throw new IllegalArgumentException(
                    "Invalid value for Construction Factor: " + cf); //$NON-NLS-1$
        }

        phaseCF.put(coords, cf);
    }

    public void setArmor(int a, Coords coords) {
        if (a < 0) {
            throw new IllegalArgumentException("Invalid value for armor: " + a); //$NON-NLS-1$
        }

        armor.put(coords, a);
    }

    public String getName() {
        StringBuffer buffer = new StringBuffer();
        if (structureType == Terrains.FUEL_TANK) {
            buffer.append("Fuel Tank #");
        } else if (getType() == Building.WALL) {
            buffer.append("Wall #");
        } else if (structureType == Terrains.BUILDING) {
            buffer.append("Building #");
        } else if (structureType == Terrains.BRIDGE) {
            buffer.append("Bridge #");
        } else {
            buffer.append("Structure #");
        }
        buffer.append(id);
        return buffer.toString();
    }

    /**
     * Get the default construction factor for the given type of building.
     *
     * @param type
     *            - the <code>int</code> construction type of the building.
     * @return the <code>int</code> default construction factor for that type of
     *         building. If a bad type value is passed, the constant
     *         <code>Building.UNKNOWN</code> will be returned instead.
     *
     * @deprecated use {@link ConstructionType} instead
     */
    @Deprecated public static int getDefaultCF(int type) {
        return ConstructionType.ofId(type).map(ConstructionType::getDefaultCF)
                                          .orElse(Building.UNKNOWN);
    }

    /**
     * Determine if this building is on fire.
     *
     * @return <code>true</code> if the building is on fire.
     */
    public boolean isBurning(Coords coords) {
        return burning.get(coords);
    }

    /**
     * Set the flag that indicates that this building is on fire.
     *
     * @param onFire
     *            - a <code>boolean</code> value that indicates whether this
     *            building is on fire.
     */
    public void setBurning(boolean onFire, Coords coords) {
        burning.put(coords, onFire);
    }

    public void addDemolitionCharge(int playerId, int damage, Coords pos) {
        DemolitionCharge charge = new DemolitionCharge(playerId, damage, pos);
        demolitionCharges.add(charge);
    }

    public void removeDemolitionCharge(DemolitionCharge charge) {
        demolitionCharges.remove(charge);
    }

    public List<DemolitionCharge> getDemolitionCharges() {
        return demolitionCharges;
    }

    public void setDemolitionCharges(List<DemolitionCharge> charges) {
        demolitionCharges = charges;
    }

    /**
     * Remove one building hex from the building
     *
     * @param coords
     *            - the <code>Coords</code> of the hex to be removed
     */
    public void removeHex(Coords coords) {
        coordinates.remove(coords);
        currentCF.remove(coords);
        phaseCF.remove(coords);
        collapsedHexes++;
    }

    public int getOriginalHexCount() {
        return originalHexes;
    }

    public int getCollapsedHexCount() {
        return collapsedHexes;
    }

    /**
     * @return the damage scale multiplier for units passing through this
     *         building
     *
     * @deprecated use {@link #getBldgClass()} and {@link BuildingClass#getDamageFromScaleMultiplier()} instead
     */
    @Deprecated public double getDamageFromScale() {
        return getBuildingClass().map(BuildingClass::getDamageFromScaleMultiplier)
                                 .orElse(1.0);
    }

    /**
     * @return the damage scale multiplier for damage applied to this building
     *         (and occupants)
     *
     * @deprecated use {@link #getBldgClass()} and {@link BuildingClass#getDamageToScaleMultiplier()} instead
     */
    @Deprecated public double getDamageToScale() {
        return getBuildingClass().map(BuildingClass::getDamageToScaleMultiplier)
                .orElse(1.0);
    }

    /**
     *
     * @return the amount of damage the building absorbs
     */
    public int getAbsorbtion(Coords pos) {
        // if(getBldgClass() == Building.CASTLE_BRIAN) {
        // return (int) Math.ceil(getPhaseCF(pos));
        // }
        return (int) Math.ceil(getPhaseCF(pos) / 10.0);
    }

    /**
     * Returns the percentage of damage done to the building for attacks against
     * infantry in the building from other units within the building.  TW pg175.
     *
     * @deprecated use {@link #getConstructionType()} and {@link ConstructionType#getDamageReductionFromInside()} instead
     */
    @Deprecated public double getInfDmgFromInside() {
        return getConstructionType().map(ConstructionType::getDamageReductionFromInside)
                                    .orElse(0f);
    }

    /**
     * Per page 172 of Total Warfare, this is the fraction of a weapon's damage that
     * passes through to infantry inside the building.
     * @return Damage fraction.
     * 
     * @deprecated use {@link #getConstructionType()} and {@link ConstructionType#getDamageReductionFromOutside()} instead
     */
    @Deprecated public float getDamageReductionFromOutside() {
        return getConstructionType().map(ConstructionType::getDamageReductionFromOutside)
                                    .orElse(0f);
    }

    public BasementType getBasement(Coords coords) {
        return basement.get(coords);
    }

    public void setBasement(Coords coords, BasementType basement) {
        this.basement.put(coords, basement);
    }

    public void setBasementCollapsed(Coords coords, boolean collapsed) {
        basementCollapsed.put(coords, collapsed);
    }

    /**
     * Update this building to include the new hex (and all hexes off the new
     * hex, which aren't already included).
     *
     * @param coords
     *            - the <code>Coords</code> of the new hex.
     * @param board
     *            - the game's <code>IBoard</code> object.
     * @exception an
     *                <code>IllegalArgumentException</code> will be thrown if
     *                the given coordinates do not contain a building, or if the
     *                building covers multiple hexes with different CF.
     */
    private void include(Coords coords, IBoard board) {

        // If the hex is already in the building, we've covered it before.
        if (isIn(coords)) {
            return;
        }

        // Get the nextHex hex.
        IHex nextHex = board.getHex(coords);
        if ((null == nextHex) || !(nextHex.containsTerrain(structureType))) {
            return;
        }

        if (structureType == Terrains.BUILDING) {

            Optional<ConstructionType> ct = nextHex.getConstructionType(structureType);
            if (!ct.isPresent() || ct.get().getId() != type) {
                String msg = String.format( "Unexpected construction type at %s: expected %s (%s), got %s (%s)", //$NON-NLS-1$
                                            coords.getBoardNum(),
                                            getConstructionType().map(ConstructionType::name).orElse("null"), //$NON-NLS-1$
                                            type,
                                            ct.map(ConstructionType::name).orElse("null"), //$NON-NLS-1$
                                            ct.map(v -> Integer.toString(v.getId())).orElse("?")); //$NON-NLS-1$
                throw new IllegalArgumentException(msg);
            }

            Optional<BuildingClass> bc = nextHex.getBuildingClass();
            if (bc.map(BuildingClass::getId).orElse(ITerrain.LEVEL_NONE) != bldgClass) {
                String msg = String.format( "Unexpected building class at %s: expected %s (%s), got %s (%s)", //$NON-NLS-1$
                                            coords.getBoardNum(),
                                            getBuildingClass().map(BuildingClass::name).orElse("null"), //$NON-NLS-1$
                                            bldgClass,
                                            bc.map(BuildingClass::name).orElse("null"), //$NON-NLS-1$
                                            bc.map(v -> Integer.toString(v.getId())).orElse("?")); //$NON-NLS-1$
                throw new IllegalArgumentException(msg);
            }

        }
        // We passed our tests, add the next hex to this building.
        coordinates.add(coords);
        originalHexes++;
        currentCF.put(coords, nextHex.terrainLevel(Terrains.BLDG_CF));
        phaseCF.put(coords, nextHex.terrainLevel(Terrains.BLDG_CF));

        // Note this really only applies to buildings, as they are the only
        // structure that can span multiple hexes.
        // All in all, buildings get whatever basement type is passed into the
        // constructor for the first hex and this basement type in other hexes;
        // while bridges and tanks get only the ctor basemet type.
        // For buildings, the ctor basement type is 
        // fistHex.terrainLevel(Terrains.BLDG_BASEMENT_TYPE), for tanks and
        // bridges it's hardcoded to NONE
        basement.put(coords, BasementType.getType(nextHex.terrainLevel(Terrains.BLDG_BASEMENT_TYPE)));

        basementCollapsed.put(coords, nextHex.terrainLevel(Terrains.BLDG_BASE_COLLAPSED) == 1);
        if (structureType == Terrains.BRIDGE) {
            currentCF.put(coords, nextHex.terrainLevel(Terrains.BRIDGE_CF));
            phaseCF.put(coords, nextHex.terrainLevel(Terrains.BRIDGE_CF));
        }
        if (structureType == Terrains.FUEL_TANK) {
            currentCF.put(coords, nextHex.terrainLevel(Terrains.FUEL_TANK_CF));
            phaseCF.put(coords, nextHex.terrainLevel(Terrains.FUEL_TANK_CF));
        }
        if (nextHex.containsTerrain(Terrains.BLDG_ARMOR)) {
            armor.put(coords, nextHex.terrainLevel(Terrains.BLDG_ARMOR));
        } else {
            armor.put(coords, 0);
        }

        burning.put(coords, false);

        // Walk through the exit directions and
        // identify all hexes in this building.
        for (int dir = 0; dir < 6; dir++) {

            // Does the building exit in this direction?
            if (nextHex.containsTerrainExit(structureType, dir)) {
                include(coords.translated(dir), board);
            }

        }

    }

    // LATER fix equals/hashCode
    //
    // Basing equality on id equality does not make sense on a mutable class.
    // This will need to be addressed, but to do so one must check all places
    // where equality is used (eg: calls to equals() and use in collections).
    //
    // Also note the comment "True until we're talking about more than one
    // Board per Game" below, which seems to imply that building ids are not
    // necessarily unique in multi-board setups.

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((null == obj) || (getClass() != obj.getClass())) {
            return false;
        }
        // True until we're talking about more than one Board per Game.
        final Building other = (Building) obj;
        return (id == other.id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        getConstructionType().ifPresent(ct -> {
            switch (ct) {
            case LIGHT:    buf.append("Light ");    break;
            case MEDIUM:   buf.append("Medium ");   break;
            case HEAVY:    buf.append("Heavy ");    break;
            case HARDENED: buf.append("Hardened "); break;
            case WALL:     // fall-through
            default:       // do nothing
            }
        });

        getBuildingClass().ifPresent(bc -> {
            switch (bc) {
            case HANGAR:          buf.append("Hangar "); break;
            case FORTRESS:        buf.append("Fortress "); break;
            case GUN_EMPLACEMENT: buf.append("Gun Emplacement"); break;
            case STANDARD:        // fall-through
            default:              buf.append("Standard ");
            }
        });

        buf.append(getName());

        return buf.toString();
    }

    public static Map<Coords,IHex> getSpannedHexes(IHex hex , IBoard board, int structureType) {

        if (!(hex.containsTerrain(structureType))) {
            String msg = String.format("Hex %s does not contain structure %s", hex.getCoords(), structureType); //$NON-NLS-1$
            throw new IllegalArgumentException(msg);
        }

        Map<Coords,IHex> receptacle = new HashMap<>();
        getSpannedHexesRecurse(hex, board, structureType, receptacle);
        return receptacle;

    }

    private static void getSpannedHexesRecurse(IHex hex , IBoard board, int structureType, Map<Coords,IHex> receptacle) {

        receptacle.put(hex.getCoords(), hex);

        for (int dir = 0; dir < 6; dir++) {
            if (hex.containsTerrainExit(structureType, dir)) {
                Coords nextCoords = hex.getCoords().translated(dir);
                if (!receptacle.containsKey(nextCoords)) {
                    IHex nextHex = board.getHex(nextCoords);
                    if (nextHex.containsTerrain(structureType)) {
                        getSpannedHexesRecurse(nextHex, board, structureType, receptacle);
                    }
                }
            }
        }

    }

    /** @deprecated this will be removed in a future refactoring */
    @Deprecated protected static int buildingIdFromCoordinates(Coords coordinates) {
        // FIXME This is an unlucky idea, especially considering that id is used
        //       as the only factor to check for equality and that (apparently?)
        //       coords can repeat in multi-map setups
        return coordinates.hashCode();
    }

    private static void requirePresent(IHex hex, int structureType) {
        if (!hex.containsTerrain(structureType)) {
            String msg = String.format("Structure type %s expected at %s", structureType, hex.getCoords().getBoardNum()); //$NON-NLS-1$
            throw new IllegalArgumentException(msg);
        }
    }

}
