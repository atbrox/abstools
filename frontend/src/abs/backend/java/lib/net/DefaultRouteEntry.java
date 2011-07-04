/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.backend.java.lib.net;

public class DefaultRouteEntry implements RouteEntry {
    public static final int DEFAULT_HOPS = 1;
    private final NetNode nextNode;
    private final int hops;
        
    public DefaultRouteEntry(NetNode nextNode) {
	this.nextNode = nextNode;
	hops = DEFAULT_HOPS;
    }

    public DefaultRouteEntry(NetNode nextNode, int hops) {
	if (hops < 0) {
	    throw new IllegalArgumentException("hops " + hops + " less than 0");
	}
	this.nextNode = nextNode;	
	this.hops = hops;
    }

    @Override
    public int getHops() {
	return hops;
    }
        
    @Override
    public NetNode getNextNode() {
	return nextNode;
    }

    @Override
    public int compareTo(RouteEntry entry) {
	return hops - entry.getHops();
    }

    @Override 
    public boolean equals(Object o) {
	if (o == this) {
	    return true;
	} else if (!(o instanceof RouteEntry)) {
	    return false;
	} else {
	    RouteEntry entry = (RouteEntry) o;
	    return nextNode == entry.getNextNode() && hops == entry.getHops();
	}
    }
    
    @Override
    public int hashCode() {
	int result = 17;
	result = 31 * result + hops;
	result = 31 * result + nextNode.getId();
	return result;
    }
}
