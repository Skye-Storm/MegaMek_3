/*
 * Copyright (c) 2018 The MegaMek Team. All rights reserved.
 *
 * This file is part of MegaMek.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */
package megamek.common.building;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import megamek.common.Coords;

/**
 * Represents one hex worth of building
 */
public class BuildingSection implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@linkplain BuildingSection}
     */
    public static BuildingSection of( Coords coordinates,
                                      BasementType basementType,
                                      int currentCF,
                                      int phaseCF,
                                      int armor,
                                      boolean basementCollapsed,
                                      boolean burning ) {
        return new BuildingSection( Objects.requireNonNull(coordinates),
                                    Objects.requireNonNull(basementType),
                                    requireValidCF(currentCF),
                                    requireValidCF(phaseCF),
                                    requireValidArmor(armor),
                                    basementCollapsed,
                                    burning );
    }

    private BuildingSection( Coords coordinates,
                             BasementType basementType,
                             int currentCF,
                             int phaseCF,
                             int armor,
                             boolean basementCollapsed,
                             boolean burning ) {
        this.coordinates  = coordinates;
        this.basementType = basementType;
        this.currentCF    = currentCF;
        this.phaseCF      = phaseCF;
        this.armor        = armor;
        this.basementCollapsed = basementCollapsed;
        this.burning           = burning;
    }

    private final Coords coordinates; // != null
    private final Set<DemolitionCharge> demolitionCharges = new LinkedHashSet<>();

    private BasementType basementType; // != null
    private int currentCF; // >= 0 // any damage immediately updates this value
    private int phaseCF;   // >= 0 // cf at start of phase - damage is applied at the end of the phase it was received in
    private int armor;     // >= 0
    private boolean basementCollapsed;
    private boolean burning;

    /**
     * @return the coordinates of this building section
     */
    public Coords getCoordinates() {
        return coordinates;
    }

   /**
    * @return the amount of damage the building absorbs
    */
   public int getAbsorbtion() {
       return (int) Math.ceil(phaseCF / 10.0);
   }

   /**
    * @return the basement type of this building section
    */
    public BasementType getBasementType() {
        return basementType;
    }

    /**
     * Sets this building section's basement type to the given value
     * @throws NullPointerException if called with {@code null}
     */
    public void setBasementType(BasementType basementType) {
        this.basementType = Objects.requireNonNull(basementType);
    }

    /**
     * @return the current CF of this building (ie: with any damage applied immediately)
     */
    public int getCurrentCF() {
        return currentCF;
    }

    /**
     * Sets the current CF of this building to the given value
     * @throws IllegalArgumentException if called with a negative value
     */
    public void setCurrentCF(int currentCF) {
        this.currentCF = requireValidCF(currentCF);
    }

    /**
     * @return the CF this building had at the start of the last phase
     */
    public int getPhaseCF() {
        return phaseCF;
    }

    /**
     * Sets the CF this building had at the start of the last phase to to the given value
     * @throws IllegalArgumentException if called with a negative value
     */
    public void setPhaseCF(int phaseCF) {
        this.phaseCF = requireValidCF(phaseCF);
    }

    /**
     * @return the armor of this building
     */
    public int getArmor() {
        return armor;
    }

    /**
     * Sets the armor of this building to the given value
     * @throws IllegalArgumentException if called with a negative value
     */
    public void setArmor(int armor) {
        this.armor = requireValidArmor(armor);
    }

    /**
     * @return whether the basement of this building section is collapsed
     */
    public boolean isBasementCollapsed() {
        return basementCollapsed;
    }

    /**
     * Sets whether the basement of this building section is collapsed
     */
    public void setBasementCollapsed(boolean basementCollapsed) {
        this.basementCollapsed = basementCollapsed;
    }

    /**
     * @return whether this building section is burning
     */
    public boolean isBurning() {
        return burning;
    }

    /**
     * Sets whether this building section is burning
     */
    public void setBurning(boolean burning) {
        this.burning = burning;
    }

    /**
     * @return a stream of the demolition charges planted in this building section
     */
    public Stream<DemolitionCharge> streamDemolitionCharges() {
        return demolitionCharges.stream();
    }

    /**
     * Replaces the charges planted in this section with the given ones 
     * 
     * @throws IllegalArgumentException
     *         if any of the new charges' positions don't match this building section's
     */
    public void setDemolitionCharges(Collection<DemolitionCharge> charges) {
        for (DemolitionCharge charge : charges) {
            if (!charge.getPos().equals(getCoordinates())) {
                throw new IllegalArgumentException(String.format("Charge coordinates %s don't match building seciton coordinates %s", charge.getPos(), coordinates)); //$NON-NLS-1$
            }
        }
        demolitionCharges.clear();
        demolitionCharges.addAll(charges);
    }

    /**
     * Adds a demolition charge belonging to the given player and of the given potency
     */
    public void addDemolitionCharge(int playerId, int damage) {
        demolitionCharges.add(new DemolitionCharge(playerId, damage, getCoordinates()));
    }

    /**
     * Makes sure the given demolition charge is not planted in this building section
     * 
     * @return whether the charge was actually planted
     */
    public boolean removeDemolitionCharge(DemolitionCharge charge) {
        return demolitionCharges.remove(charge);
    }

    @Override
    public int hashCode() {
        return Objects.hash( coordinates,
                             basementType,
                             currentCF,
                             phaseCF,
                             armor,
                             basementCollapsed,
                             burning );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BuildingSection other = (BuildingSection) obj;
        return (coordinates.equals(other.coordinates))
            && (armor == other.armor)
            && (currentCF == other.currentCF)
            && (phaseCF == other.phaseCF)
            && (burning == other.burning)
            && (basementCollapsed == other.basementCollapsed);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(String.format("[hex:%s", coordinates.getBoardNum())); //$NON-NLS-1$
        sb.append(String.format(",CF:%s", currentCF)); //$NON-NLS-1$

        if (phaseCF != currentCF) {
            sb.append(String.format(",phaseCF:%s", phaseCF)); //$NON-NLS-1$
        }
        if (armor > 0) {
            sb.append(String.format(",armor:%s", armor)); //$NON-NLS-1$
        }
        if (!demolitionCharges.isEmpty()) {
            sb.append(String.format(",#charges:%s", demolitionCharges.size())); //$NON-NLS-1$
        }
        if (basementCollapsed) {
            sb.append(",basementCollapsed"); //$NON-NLS-1$
        }
        if (burning) {
            sb.append(",burning"); //$NON-NLS-1$
        }

        sb.append("]"); //$NON-NLS-1$
        return sb.toString();
    }

    private static int requireValidCF(int cf) {
        if (cf < 0) {
            throw new IllegalArgumentException("Invalid CF value: " + cf); //$NON-NLS-1$
        }
        return cf;
    }

    private static int requireValidArmor(int armor) {
        if (armor < 0) {
            throw new IllegalArgumentException("Invalid armor value: " + armor); //$NON-NLS-1$
        }
        return armor;
    }

}
