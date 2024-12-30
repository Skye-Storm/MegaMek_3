/*
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
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

package megamek.client.ui.swing.ai.editor;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class ConsiderationParametersTable extends JTable {

    public ConsiderationParametersTable(
        ParametersTableModel model) {
        super(model);
    }

    public void createUIComponents() {
        //
    }

    @Override
    @SuppressWarnings("unchecked")
    public ParametersTableModel getModel() {
        return (ParametersTableModel) super.getModel();
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if (column == 1) {
            var clazz = getModel().getParameterValueAt(row);
            var value = getModel().getValueAt(row, column);
            if (clazz == null) {
                // Should actually throw an error here...
                return super.getCellEditor(row, column);
            }
            if (clazz.equals(Boolean.class)) {
                return new DefaultCellEditor(new JCheckBox());
            } else if (clazz.equals(Double.class)) {
                return new SpinnerCellEditor(
                    (double) (value == null ? 0d : value),
                    Double.MIN_VALUE,
                    Double.MAX_VALUE,
                    0.1d
                );
            } else if (clazz.equals(Float.class)) {
                return new SpinnerCellEditor(
                    (float) (value == null ? 0f : value),
                    Float.MIN_VALUE,
                    Float.MAX_VALUE,
                    0.1f
                );
            } else if (clazz.equals(Integer.class)) {
                return new SpinnerCellEditor(
                    (int) (value == null ? 0 : value),
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE,
                    1
                );
            } else if (clazz.equals(Long.class)) {
                return new SpinnerCellEditor(
                    (long) (value == null ? 0 : value),
                    Long.MIN_VALUE,
                    Long.MAX_VALUE,
                    1
                );
            } else if (clazz.equals(String.class)) {
                return new DefaultCellEditor(new JTextField());
            } else if (clazz.isEnum()) {
                var cb = new JComboBox<>(
                    clazz.getEnumConstants()
                );
                return new DefaultCellEditor(cb);
            }
        }
        return super.getCellEditor(row, column);
    }

    public static class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JSpinner spinner;

        public SpinnerCellEditor(double defaultValue, double min, double max, double step) {
            spinner = new JSpinner(new SpinnerNumberModel(min, min, max, step));
            spinner.setValue(defaultValue);
            JComponent editor = spinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
                textField.setHorizontalAlignment(JFormattedTextField.LEFT);
            }
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            spinner.setValue(value);
            return spinner;
        }
    }

}
