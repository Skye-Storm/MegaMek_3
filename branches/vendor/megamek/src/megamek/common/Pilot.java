/**
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

package megamek.common;

import java.io.*;

public class Pilot
	implements Serializable
{
    private String      name;
    private int         gunnery;
    private int         piloting;
    private int         hits; // hits taken
      
    private boolean     unconcious;
    private boolean     dead;
    
    // these are only used on the server:
    private transient int rollsNeeded; // how many KO rolls needed this turn
    private transient boolean koThisRound; // did I go KO this game round?
	
	
	public Pilot() {
        name = "Unnamed";
        gunnery = 4;
        piloting = 5;
        hits = 0;
        unconcious = false;
        dead = false;
        rollsNeeded = 0;
        koThisRound = false;
	}
  
    public String getName() {
        return name;
    }
  
    public int getGunnery() {
        return gunnery;
    }
  
    public int getPiloting() {
        return piloting;
    }
  
    public int getHits() {
        return hits;
    }
  
    public void setHits(int hits) {
        this.hits = hits;
    }
  
    public boolean isUnconcious() {
        return unconcious;
    }
	
    public void setUnconcious(boolean unconcious) {
        this.unconcious = unconcious;
    }
	
    public boolean isDead() {
        return dead;
    }
	
    public void setDead(boolean dead) {
        this.dead = dead;
    }
	
    public boolean isActive() {
        return !unconcious && !dead;
    }
    
    public int getRollsNeeded() {
        return rollsNeeded;
    }
    
    public void setRollsNeeded(int rollsNeeded) {
        this.rollsNeeded = rollsNeeded;
    }
	
    public boolean isKoThisRound() {
        return koThisRound;
    }
    
    public void setKoThisRound(boolean koThisRound) {
        this.koThisRound = koThisRound;
    }
	
	/**
	 * Returns a digest string describing the pilot
	 */
	public String getDesc() {
		String s = new String(name);
		if (hits > 0) {
			s += " (" + hits + " hits taken";
            if (isUnconcious()) {
		        s += " [ko]";
            } else if (isDead()) {
		        s += " [dead]";
            }
			s += ")";
		}
		return s;
	}
	
	/**
	 * Returns the conciousness roll number
	 * 
	 * TODO: maybe this should be in Rules?
	 */
	public int getConciousnessNumber() {
		switch(hits) {
		case 0:
			return 2;
		case 1:
			return 3;
		case 2:
			return 5;
		case 3:
			return 7;
		case 4:
			return 10;
		case 5:
			return 11;
		default:
			return Integer.MAX_VALUE;
		}
	}
}
