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

import megamek.ai.utility.*;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

public class CurvePane extends JPanel {
    private JComboBox<DefaultCurve> curveTypeComboBox;
    private JSpinner bParamSpinner;
    private JSpinner mParamSpinner;
    private JSpinner kParamSpinner;
    private JSpinner cParamSpinner;
    private JPanel curveGraph;
    private JPanel basePane;
    private HoverStateModel hoverStateModel;

    public CurvePane() {
        $$$setupUI$$$();
        setLayout(new BorderLayout());
        add(basePane, BorderLayout.CENTER);
    }

    public void setHoverStateModel(HoverStateModel model) {
        ((CurveGraph) this.curveGraph).setHoverStateModel(model);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        basePane = new JPanel();
        basePane.setLayout(new GridBagLayout());
        basePane.setBackground(new Color(-13947600));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        basePane.add(curveTypeComboBox, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        basePane.add(label1, gbc);
        curveGraph.setBackground(new Color(-13947600));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 11;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        basePane.add(curveGraph, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        basePane.add(bParamSpinner, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        basePane.add(mParamSpinner, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        basePane.add(kParamSpinner, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        basePane.add(cParamSpinner, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("b");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        basePane.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("m");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        basePane.add(label3, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("k");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        basePane.add(label4, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("c");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        basePane.add(label5, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return basePane;
    }

    private void createUIComponents() {
        curveTypeComboBox = new JComboBox<>(DefaultCurve.values());
        AtomicReference<Curve> selectedCurve = new AtomicReference<>();
        curveTypeComboBox.addActionListener(e1 -> {
            if (curveTypeComboBox.getSelectedItem() != null) {
                var curve = ((DefaultCurve) curveTypeComboBox.getSelectedItem()).getCurve();
                selectedCurve.set(curve);
                curveGraph.repaint();

                if (curve instanceof LinearCurve) {
                    bParamSpinner.setValue(((LinearCurve) curve).getB());
                    mParamSpinner.setValue(((LinearCurve) curve).getM());
                    kParamSpinner.setEnabled(false);
                    cParamSpinner.setEnabled(false);
                } else if (curve instanceof ParabolicCurve) {
                    bParamSpinner.setValue(((ParabolicCurve) curve).getB());
                    mParamSpinner.setValue(((ParabolicCurve) curve).getM());
                    kParamSpinner.setEnabled(true);
                    kParamSpinner.setValue(((ParabolicCurve) curve).getK());
                    cParamSpinner.setEnabled(false);
                } else if (curve instanceof LogitCurve) {
                    bParamSpinner.setValue(((LogitCurve) curve).getB());
                    mParamSpinner.setValue(((LogitCurve) curve).getM());
                    kParamSpinner.setEnabled(true);
                    kParamSpinner.setValue(((LogitCurve) curve).getK());
                    cParamSpinner.setEnabled(true);
                    cParamSpinner.setValue(((LogitCurve) curve).getC());
                } else if (curve instanceof LogisticCurve) {
                    bParamSpinner.setValue(((LogisticCurve) curve).getB());
                    mParamSpinner.setValue(((LogisticCurve) curve).getM());
                    kParamSpinner.setEnabled(true);
                    kParamSpinner.setValue(((LogisticCurve) curve).getK());
                    cParamSpinner.setEnabled(true);
                    cParamSpinner.setValue(((LogisticCurve) curve).getC());
                }
            }
        });

        curveGraph = new CurveGraph(selectedCurve);

        curveGraph.setPreferredSize(new Dimension(800, 600));
        bParamSpinner = new JSpinner(new SpinnerNumberModel(0d, -100d, 100d, 0.01d));
        mParamSpinner = new JSpinner(new SpinnerNumberModel(0d, -100d, 100d, 0.01d));
        kParamSpinner = new JSpinner(new SpinnerNumberModel(0d, -100d, 100d, 0.01d));
        cParamSpinner = new JSpinner(new SpinnerNumberModel(0d, -100d, 100d, 0.01d));

        mParamSpinner.addChangeListener(e -> {
            if (selectedCurve.get() != null) {
                selectedCurve.get().setM((Double) mParamSpinner.getValue());
            }
            curveGraph.repaint();
        });
        bParamSpinner.addChangeListener(e -> {
            if (selectedCurve.get() != null) {
                selectedCurve.get().setB((Double) bParamSpinner.getValue());
            }
            curveGraph.repaint();
        });
        kParamSpinner.addChangeListener(e -> {
            if (selectedCurve.get() != null) {
                selectedCurve.get().setK((Double) kParamSpinner.getValue());
            }
            curveGraph.repaint();
        });
        cParamSpinner.addChangeListener(e -> {
            if (selectedCurve.get() != null) {
                selectedCurve.get().setC((Double) cParamSpinner.getValue());
            }
            curveGraph.repaint();
        });
    }
}
