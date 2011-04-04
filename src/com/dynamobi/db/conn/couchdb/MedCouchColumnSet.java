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

import net.sf.farrago.catalog.*;
import net.sf.farrago.namespace.impl.*;
import net.sf.farrago.query.*;
import net.sf.farrago.resource.*;

import org.eigenbase.rel.*;
import org.eigenbase.relopt.*;
import org.eigenbase.reltype.*;
import org.eigenbase.resource.*;
import org.eigenbase.rex.*;
import org.eigenbase.sql.*;
import org.eigenbase.sql.parser.*;

import java.util.*;

/**
 * This class is called when a query is made. It communicates with the Udx
 * to bring down the data.
 * @author Kevin Secretan
 */
public class MedCouchColumnSet extends MedAbstractColumnSet {

  private final MedCouchDataServer server;

  // foreign table options passed in:
  private final String username;
  private final String password;
  private final String url;
  private final String view;

  private final String udxSpecificName;

  public MedCouchColumnSet(
      MedCouchDataServer server,
      String[] localName,
      RelDataType rowType,
      String username,
      String password,
      String url,
      String view,
      String viewDef,
      String udxSpecificName) {
    super(localName, null, rowType, null, null);
    this.server = server;
    this.udxSpecificName = udxSpecificName;
    
    this.username = username;
    this.password = password;
    this.url = url;
    this.view = view;

    if (!viewDef.equals("")) {
      // need to post this view def up to couchdb, make the call directly
      // to the UDX class.
    }

    setAllowedAccess(SqlAccessType.READ_ONLY);
  }

  // implement RelOptTable
  // (defines how we get from a F.T. call to the Udx class)
  public RelNode toRel(RelOptCluster cluster, RelOptConnection connection) {
    RexBuilder builder = cluster.getRexBuilder();
    RexNode rnUn = builder.makeLiteral(username);
    RexNode rnPw = builder.makeLiteral(password);
    RexNode rnUrl = builder.makeLiteral(url);
    RexNode rnView = builder.makeLiteral(view);

    return toUdxRel(cluster, connection, udxSpecificName,
        server.getServerMofId(), new RexNode[] {rnUn, rnPw, rnUrl, rnView});
  }

  /**
   * standard helper function from other farrago versions,
   * main purpose is to use our UdxRel.
   */
  protected RelNode toUdxRel(
      RelOptCluster cluster,
      RelOptConnection connection,
      String udxSpecificName,
      String serverMofId,
      RexNode [] args)
  {
    // Parse the specific name of the UDX.
    SqlIdentifier udxId;
    try {
      SqlParser parser = new SqlParser(udxSpecificName);
      SqlNode parsedId = parser.parseExpression();
      udxId = (SqlIdentifier) parsedId;
    } catch (Exception ex) {
      /*throw FarragoResource.instance().MedInvalidUdxId.ex(
          udxSpecificName,
          ex);*/
      return null;
    }

    // Look up the UDX in the catalog.
    List list =
      getPreparingStmt().getSqlOperatorTable().lookupOperatorOverloads(
          udxId,
          SqlFunctionCategory.UserDefinedSpecificFunction,
          SqlSyntax.Function);
    FarragoUserDefinedRoutine udx = null;
    if (list.size() == 1) {
      Object obj = list.iterator().next();
      if (obj instanceof FarragoUserDefinedRoutine) {
        udx = (FarragoUserDefinedRoutine) obj;
        if (!FarragoCatalogUtil.isTableFunction(udx.getFemRoutine())) {
          // Not a UDX.
          udx = null;
        }
      }
    }
    if (udx == null) {
      return null;
      //throw FarragoResource.instance().MedUnknownUdx.ex(udxId.toString());
    }

    // UDX wants all types nullable, so construct a corresponding
    // type descriptor for the result of the call.
    RexBuilder rexBuilder = cluster.getRexBuilder();
    RelDataTypeFactory typeFactory = rexBuilder.getTypeFactory();
    RelDataType resultType =
      typeFactory.createTypeWithNullability(
          this.rowType,
          true);

    // Create a relational algebra expression for invoking the UDX.
    RexNode rexCall = rexBuilder.makeCall(udx, args);

    RelNode udxRel =
      new CouchUdxRel(
          cluster,
          rexCall,
          resultType,
          serverMofId,
          this,
          udx);

    // Optimizer wants us to preserve original types,
    // so cast back for the final result.
    return RelOptUtil.createCastRel(udxRel, this.rowType, true);
  }
}
