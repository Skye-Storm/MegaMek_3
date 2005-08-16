/*
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

package megamek.client.ui.AWT;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Vector;
import megamek.client.ui.AWT.widget.AdvancedLabel;
import megamek.common.Entity;
import megamek.common.Mounted;
import megamek.common.MiscType;
import megamek.common.actions.LayMinefieldAction;

/**
 * A dialog displayed to the player when they have an opportunity to
 * trigger an Anti-Personell Pod on one of their units.
 */
public class MineLayingDialog
    extends Dialog implements ActionListener
{
    private Button butOkay = new Button(Messages.getString("Okay")); //$NON-NLS-1$
    private Button butCancel = new Button(Messages.getString("Cancel")); //$NON-NLS-1$
    private AdvancedLabel labMessage;
    private boolean okay = true;

    /** The <code>int</code> ID of the entity that lays the mine. */
    private Entity entity = null;
    private Choice chMines = new Choice();
    private Vector vMines = new Vector();

    /**
     * Display a dialog that shows the AP Pods on the entity, and allows
     * the player to fire any active pods.
     *
     * @param   parent the <code>Frame</code> parent of this dialog
     * @param   entity the <code>Entity</code> that can fire AP Pods.
     */
    public MineLayingDialog( Frame parent, Entity entity ) {
        super(parent, Messages.getString("MineLayingDialog.title"), true); //$NON-NLS-1$
        this.entity = entity;

        labMessage = new AdvancedLabel(Messages.getString("MineLayingDialog.selectMineToLay",new Object[]{entity.getDisplayName()})); //$NON-NLS-1$ 

        // Walk through the entity's misc equipment, looking for mines.
        Enumeration equip = entity.getMisc();
        while ( equip.hasMoreElements() ) {
            Mounted mount = (Mounted) equip.nextElement();

            // Is this a Mine that can be layed?
            if ( mount.getType().hasFlag(MiscType.F_MINE) && mount.canFire() ) {

                StringBuffer message = new StringBuffer();
                message.append( entity.getLocationName(mount.getLocation()) )
                    .append( " " ) //$NON-NLS-1$
                    .append( mount.getName() );
                chMines.add(message.toString());
                vMines.addElement(entity.getEquipmentNum(mount));
                
            } // End found-mine

        } // Look at the next piece of equipment.

        // buttons
        butOkay.addActionListener(this);
        butCancel.addActionListener(this);
        
        // layout
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);
            
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(10, 10, 10, 10);
        c.weightx = 1.0;    c.weighty = 0.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(labMessage, c);
        add(labMessage);
            
        gridbag.setConstraints(chMines, c);
        add(chMines);

        // Allow the player to confirm or abort the choice.
        add(butOkay);
        add(butCancel);
        butOkay.requestFocus();
        
        addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) { setVisible(false); }
    });
        
        pack();
        Dimension size = getSize();
        boolean updateSize = false;
        if ( size.width < GUIPreferences.getInstance().getMinimumSizeWidth()) {
            size.width = GUIPreferences.getInstance().getMinimumSizeWidth();
        }
        if ( size.height < GUIPreferences.getInstance().getMinimumSizeHeight() ) {
            size.height = GUIPreferences.getInstance().getMinimumSizeHeight();
        }
        if ( updateSize ) {
            setSize( size );
            size = getSize();
        }
        setResizable(false);
        setLocation(parent.getLocation().x + parent.getSize().width/2 - size.width/2,
                    parent.getLocation().y + parent.getSize().height/2 - size.height/2);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == butCancel) {
            okay = false;
        }
        this.setVisible(false);
    }
    public boolean getAnswer() {
        return (okay);
    }

    /**
     * Get the trigger actions that the user selected.
     *
     * @return  the <code>int</code> id of the mine to lay,
     *  or <code>-1</code>, if no mine is selected
     */
    public int getMine() {
        Integer equipnr = (Integer)vMines.elementAt(chMines.getSelectedIndex());
        Mounted mine = entity.getEquipment(equipnr);
        return entity.getEquipmentNum(mine);
    }
}
