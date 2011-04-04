/*
Dynamo CouchDB Connector is a plugin for browsing CouchDB views in LucidDB.
Copyright (C) 2011 Dynamo Business Intelligence Corporation

This program is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the Free
Software Foundation; either version 2 of the License, or (at your option)
any later version approved by Dynamo Business Intelligence Corporation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/

package com.dynamobi.db.conn.couchdb;

import net.sf.farrago.query.*;

import org.eigenbase.rel.*;
import org.eigenbase.relopt.*;
import org.eigenbase.reltype.RelDataType;
import org.eigenbase.rex.RexNode;

/**
 * Overrides the standard FarragoJavaUdxRel, mainly needed to provide
 * a return function of the custom CouchColumnSet.
 * @author Kevin Secretan
 */
public class CouchUdxRel extends FarragoJavaUdxRel {

    protected MedCouchColumnSet table;
    protected FarragoUserDefinedRoutine udx;
    protected String serverMofId;

    public CouchUdxRel(
        RelOptCluster cluster,
        RexNode rexCall,
        RelDataType rowType,
        String serverMofId,
        MedCouchColumnSet couchTable,
        FarragoUserDefinedRoutine udx)
    {
      super(cluster, rexCall, rowType, serverMofId, RelNode.emptyArray);
      this.table = couchTable;
      this.udx = udx;
      this.serverMofId = serverMofId;
    }

    public MedCouchColumnSet getTable() {
      return this.table;
    }

    public FarragoUserDefinedRoutine getUdx() {
      return this.udx;
    }

    public String getServerMofId() {
      return this.serverMofId;
    }
}

