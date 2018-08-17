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

import java.util.Optional;


public enum BuildingClass {

    STANDARD        (),
    HANGAR          (),
    FORTRESS        (),
    GUN_EMPLACEMENT ();
    // LATER refer back to TacOps and see to add Castle-Brian class buildings

    /**
     * Retrieves the {@linkplain BuildingClass} corresponding to the given
     * integer id, if it's valid (ie: in [0,3]).
     *         
     * @see #getId()
     */
    public static Optional<BuildingClass> ofId(int id) {
        try {
            return Optional.of(BuildingClass.values()[id]);
        } catch (@SuppressWarnings("unused") ArrayIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    /**
     * Same as {@link #ofId(int)}, but throws an exception on invalid ids
     */
    public static BuildingClass ofRequiredId(int id) throws IllegalArgumentException {
        try {
            return BuildingClass.values()[id];
        } catch (@SuppressWarnings("unused") ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(Integer.toString(id));
        }
    }

    private BuildingClass() {
    }


    /**
     * Retrieves the identifier corresponding to this building class.
     * 
     * Values are the same as the "old" constants in {@link Building}:
     * 
     * <pre>
     *    public static final int STANDARD = 0;
     *    public static final int HANGAR = 1;
     *    public static final int FORTRESS = 2;
     *    public static final int GUN_EMPLACEMENT = 3;
     * </pre>
     * 
     * @return the id corresponding to this construction type
     */
    public int getId() {
        return ordinal();
    }

}
