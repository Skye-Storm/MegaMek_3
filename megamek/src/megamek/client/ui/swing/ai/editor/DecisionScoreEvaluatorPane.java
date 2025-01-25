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

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import megamek.ai.utility.DecisionScoreEvaluator;
import megamek.client.bot.queen.ai.utility.tw.decision.TWDecisionScoreEvaluator;
import megamek.client.ui.Messages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DecisionScoreEvaluatorPane extends JPanel {
    private JTextField nameField;
    private JTextField descriptionField;
    private JTextField notesField;
    private JPanel decisionScoreEvaluatorPane;
    private JPanel considerationsPane;
    private JToolBar considerationsToolbar;
    private final HoverStateModel hoverStateModel;
    private final List<ConsiderationPane> considerationPaneList = new ArrayList<>();

    public DecisionScoreEvaluatorPane() {
        $$$setupUI$$$();
        add(decisionScoreEvaluatorPane, BorderLayout.WEST);
        hoverStateModel = new HoverStateModel();

        // Considerations Toolbar
        var newConsiderationBtn = new JButton(Messages.getString("aiEditor.new.consideration"));
        var copyConsiderationBtn = new JButton(Messages.getString("aiEditor.copy.consideration"));
        var editConsiderationBtn = new JButton(Messages.getString("aiEditor.edit.consideration"));
        var deleteConsiderationBtn = new JButton(Messages.getString("aiEditor.delete.consideration"));

        considerationsToolbar.add(newConsiderationBtn);
        considerationsToolbar.add(copyConsiderationBtn);
        considerationsToolbar.add(editConsiderationBtn);
        considerationsToolbar.add(deleteConsiderationBtn);

        // Add a MouseWheelListener to forward the event to the parent JScrollPane
        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (getParent() instanceof JViewport viewport) {
                    if (viewport.getParent() instanceof JScrollPane scrollPane) {
                        scrollPane.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, scrollPane));
                    }
                }
            }
        });
    }

    public TWDecisionScoreEvaluator getDecisionScoreEvaluator() {
        var dse = new TWDecisionScoreEvaluator();
        dse.setName(nameField.getText());
        dse.setDescription(descriptionField.getText());
        dse.setNotes(notesField.getText());
        for (var considerationPane : considerationPaneList) {
            dse.addConsideration(considerationPane.getConsideration());
        }
        return dse;
    }

    public void addEmptyConsideration() {
        considerationsPane.removeAll();
        considerationsPane.setLayout(new GridLayoutManager((considerationPaneList.size() + 1) * 2, 1, new Insets(0, 0, 0, 0), -1, -1));
        int row = 0;
        var emptyConsideration = new ConsiderationPane();
        emptyConsideration.setEmptyConsideration();
        emptyConsideration.setHoverStateModel(hoverStateModel);
        considerationsPane.add(emptyConsideration, new GridConstraints(row++, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        considerationsPane.add(new JSeparator(), new GridConstraints(row++, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        for (var c : considerationPaneList) {
            considerationsPane.add(c, new GridConstraints(row++, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
            considerationsPane.add(new JSeparator(), new GridConstraints(row++, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        }
        considerationPaneList.add(0, emptyConsideration);
    }

    public void reset() {
        nameField.setText("");
        descriptionField.setText("");
        notesField.setText("");
        considerationsPane.removeAll();
        considerationPaneList.clear();
    }

    public void setDecisionScoreEvaluator(DecisionScoreEvaluator<?, ?> dse) {
        nameField.setText(dse.getName());
        descriptionField.setText(dse.getDescription());
        notesField.setText(dse.getNotes());
        considerationsPane.removeAll();

        var considerations = dse.getConsiderations();
        considerationsPane.setLayout(new GridLayoutManager(considerations.size() * 2, 1, new Insets(0, 0, 0, 0), -1, -1));
        considerationPaneList.clear();

        int row = 0;
        for (var consideration : considerations) {
            var c = new ConsiderationPane();
            c.setConsideration(consideration);
            c.setHoverStateModel(hoverStateModel);
            considerationPaneList.add(c);
            considerationsPane.add(c, new GridConstraints(row++, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
            considerationsPane.add(new JSeparator(), new GridConstraints(row++, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        }
        this.updateUI();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     *
     */
    private void $$$setupUI$$$() {
        decisionScoreEvaluatorPane = new JPanel();
        decisionScoreEvaluatorPane.setLayout(new GridLayoutManager(10, 1, new Insets(0, 0, 0, 0), -1, -1));
        nameField = new JTextField();
        decisionScoreEvaluatorPane.add(nameField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        descriptionField = new JTextField();
        decisionScoreEvaluatorPane.add(descriptionField, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        notesField = new JTextField();
        decisionScoreEvaluatorPane.add(notesField, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(31);
        scrollPane1.setMaximumSize(new Dimension(800, 32767));
        scrollPane1.setMinimumSize(new Dimension(800, 600));
        scrollPane1.setWheelScrollingEnabled(true);
        decisionScoreEvaluatorPane.add(scrollPane1, new GridConstraints(8, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        considerationsPane = new JPanel();
        considerationsPane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        considerationsPane.setMaximumSize(new Dimension(800, 2147483647));
        considerationsPane.setMinimumSize(new Dimension(800, 600));
        scrollPane1.setViewportView(considerationsPane);
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("megamek/common/options/messages", "aiEditor.considerations"));
        decisionScoreEvaluatorPane.add(label1, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, this.$$$getMessageFromBundle$$$("megamek/common/options/messages", "aiEditor.notes"));
        decisionScoreEvaluatorPane.add(label2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, this.$$$getMessageFromBundle$$$("megamek/common/options/messages", "aiEditor.description"));
        decisionScoreEvaluatorPane.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$(label4, this.$$$getMessageFromBundle$$$("megamek/common/options/messages", "aiEditor.name"));
        decisionScoreEvaluatorPane.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        considerationsToolbar = new JToolBar();
        decisionScoreEvaluatorPane.add(considerationsToolbar, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        label1.setLabelFor(scrollPane1);
        label2.setLabelFor(notesField);
        label3.setLabelFor(descriptionField);
        label4.setLabelFor(nameField);
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    public JComponent $$$getRootComponent$$$() {
        return decisionScoreEvaluatorPane;
    }

}
