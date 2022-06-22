/*
 * MechSelectorDialog.java - Copyright (C) 2009 Jay Lawson
 * Copyright © 2013 Edward Cullen (eddy@obsessedcomputers.co.uk)
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
package megamek.client.ui.swing.util;

import megamek.common.*;
import megamek.common.alphaStrike.ASUnitType;
import megamek.common.alphaStrike.AlphaStrikeElement;
import megamek.common.util.fileUtils.MegaMekFile;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Looks for a fluff image for an entity based on model and chassis.
 *
 * Heavily based on code from MegaMekLab's ImageHelper.
 * @author Jay Lawson
 */
public class FluffImageHelper {
    public static final String DIR_NAME_MECH = "mech";
    public static final String DIR_NAME_AERO = "aero";
    public static final String DIR_NAME_BA = "BattleArmor";
    public static final String DIR_NAME_VEHICLE = "vehicle";
    public static final String[] EXTENSIONS_FLUFF_IMAGE_FORMATS = { ".png", ".jpg", ".gif", ".PNG", ".JPG", ".GIF" };

    /**
     * Get the fluff image for the specified unit, if available.
     * 
     * @param unit The unit.
     * @return An image file, if one is available, else {@code null}.
     */
    public static Image getFluffImage(final Entity unit) {
        Image fluff = loadFluffImage(unit);

        if (fluff == null) {
            fluff = loadFluffImageHeuristic(unit);
        }

        return fluff;
    }

    /**
     * Attempt to load the fluff image specified in the Entity data.
     * 
     * @param unit
     *            The unit.
     * @return An image or {@code null}.
     */
    protected static Image loadFluffImage(final Entity unit) {
        Image fluff = null;
        String path = unit.getFluff().getMMLImagePath();
        if (new File(path).isFile()) {
            fluff = new ImageIcon(path).getImage();
        }
        return fluff;
    }

    /**
     * Attempt to load a fluff image by combining elements of type and name.
     * 
     * @param unit
     *            The unit.
     * @return An image or {@code null}.
     */
    protected static Image loadFluffImageHeuristic(final Entity unit) {
        Image fluff = null;

        String dir = DIR_NAME_MECH;
        if (unit instanceof Aero) {
            dir = DIR_NAME_AERO;
        } else if (unit instanceof BattleArmor) {
            dir = DIR_NAME_BA;
        } else if (unit instanceof Tank) {
            dir = DIR_NAME_VEHICLE;
        }

        File fluff_image_file = findFluffImage(
                new MegaMekFile(Configuration.fluffImagesDir(), dir).getFile(), unit);
        if (fluff_image_file != null) {
            fluff = new ImageIcon(fluff_image_file.toString()).getImage();
        }

        return fluff;
    }

    /**
     * Attempt to load a fluff image by combining elements of type and name.
     *
     * @param element The AlphaStrike element
     * @return An image or null
     */
    public static Image loadFluffImageHeuristic(final AlphaStrikeElement element) {
        Image fluff = null;

        String dir = DIR_NAME_MECH;
        if (element.isAerospace()) {
            dir = DIR_NAME_AERO;
        } else if (element.isType(ASUnitType.BA)) {
            dir = DIR_NAME_BA;
        } else if (element.isType(ASUnitType.CV)) {
            dir = DIR_NAME_VEHICLE;
        }

        File fluff_image_file = findFluffImage(
                new MegaMekFile(Configuration.fluffImagesDir(), dir).getFile(), element);
        if (fluff_image_file != null) {
            fluff = new ImageIcon(fluff_image_file.toString()).getImage();
        }

        return fluff;
    }

    /**
     * Find a fluff image file for the given AlphaStrike element
     *
     * @param directory Directory to search
     * @param element The AlphaStrike element
     * @return Path to an appropriate file or {@code null} if none is found
     */
    protected static File findFluffImage(final File directory, final AlphaStrikeElement element) {
        return findFluffImage(directory, element.getModel(), element.getChassis());
    }

    /**
     * Find a fluff image file for the unit
     *
     * @param directory Directory to search
     * @param unit The unit
     * @return Path to an appropriate file or {@code null} if none is found
     */
    protected static File findFluffImage(final File directory, final Entity unit) {
        return findFluffImage(directory, unit.getModel(), unit.getChassis());
    }

    /**
     * Find a fluff image file for the unit.
     * 
     * @param directory Directory to search.
     * @param origModel The model name of the unit
     * @param origChassis The chassis name of the unit
     * @return Path to an appropriate file or {@code null} if none is found
     */
    protected static File findFluffImage(final File directory, String origModel, String origChassis) {
        // Search for a file in the specified directory.
        // Searches for each supported extension on each of the following
        // combinations:
        // Chassis + model
        // Model only
        // Chassis only
        // Model needs .replace("\"", "") because Windows disallows double quote
        // in the filename.
        File fluff_file = null;
        // Remove characters that will cause path problems
        String sanitizedChassis = origChassis.replace("\"", "").replace("/", "");
        String sanitizedModel = origModel.replace("\"", "").replace("/", "");
        String[] basenames = {
                new MegaMekFile(directory, sanitizedChassis + " " + sanitizedModel).toString(),
                new MegaMekFile(directory, sanitizedModel).toString(),
                new MegaMekFile(directory, sanitizedChassis).toString(), };

        for (String basename : basenames) {
            for (String extension : EXTENSIONS_FLUFF_IMAGE_FORMATS) {
                File filepath = new File(basename + extension);
                if (filepath.isFile()) {
                    fluff_file = filepath;
                    break;
                }
            }
            if (fluff_file != null) {
                break;
            }
        }
        final String model = origModel.replace("\"", "");
        final String chassisModel = origChassis + " " + model;

        // If the previous checks failed, we're going to try to discount the
        //  CSO author name, which will make the file look like:
        //   Chassis + model + [ <author> ] + extension
        if (fluff_file == null) {
            File[] files = directory.listFiles((direc, name) -> {
                boolean extMatch = false;
                for (String ext : EXTENSIONS_FLUFF_IMAGE_FORMATS) {
                    extMatch |= name.endsWith(ext);
                }
                return name.contains(chassisModel) && extMatch;
            });

            if ((files != null) && (files.length > 0)) {
                fluff_file = files[0];
            }
        }

        // If we still haven't found a file, see if ignoring the model helps
        if (fluff_file == null) {
            File[] files = directory.listFiles((direc, name) -> {
                boolean extMatch = false;
                for (String ext : EXTENSIONS_FLUFF_IMAGE_FORMATS) {
                    extMatch |= name.endsWith(ext);
                }
                String chassis = name.split("\\[")[0].trim();
                return chassis.equalsIgnoreCase(origChassis) && extMatch;
            });

            if ((files != null) && (files.length > 0)) {
                fluff_file = files[0];
            }
        }

        return fluff_file;
    }
}
