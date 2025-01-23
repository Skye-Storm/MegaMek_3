/*
 * Copyright (c) 2025 - The MegaMek Team. All Rights Reserved.
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
 *
 */

package megamek.ai.utility;

import megamek.logging.MMLogger;
import org.apache.logging.log4j.Level;

import java.util.StringJoiner;

public class DebugReporter {
    private static final MMLogger logger = MMLogger.create(DebugReporter.class);
    private StringBuilder stringBuilder;

    public DebugReporter() {
        if (logger.isLevelLessSpecificThan(Level.INFO)) {
            stringBuilder = new StringBuilder();
        }
    }

    public DebugReporter append(String s) {
        if (stringBuilder != null) {
            stringBuilder.append(s);
        }
        return this;
    }

    public DebugReporter appendLine(String s) {
        if (stringBuilder != null) {
            stringBuilder.append(s).append("\n");
        }
        return this;
    }

    public DebugReporter appendLine() {
        if (stringBuilder != null) {
            stringBuilder.append("\n");
        }
        return this;
    }

    public String getReport() {
        if (stringBuilder != null) {
            return stringBuilder.toString();
        }
        return "";
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DebugReporter.class.getSimpleName() + "[", "]")
            .add("stringBuilder=").add((stringBuilder != null) ? stringBuilder : "DEBUG REPORTING DISABLED")
            .toString();
    }
}
