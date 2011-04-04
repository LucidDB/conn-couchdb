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

import java.util.*;
import java.sql.*;

import net.sf.farrago.namespace.*;
import net.sf.farrago.namespace.impl.*;
import net.sf.farrago.resource.*;
import net.sf.farrago.type.*;
import net.sf.farrago.query.*;

import org.eigenbase.reltype.*;
import org.eigenbase.util.*;

import java.util.logging.*;
import net.sf.farrago.trace.*;

/**
 * This class ties up the relationship between
 * wrapper, server, and foreign table.
 * @author Kevin Secretan
 */
public class MedCouchDataServer extends MedAbstractDataServer {

  // These are options taken by the server and potentially by the foreign table.
  public static final String PROP_USERNAME = "USERNAME";
  
  public static final String PROP_PASSWORD = "PASSWORD";

  public static final String PROP_URL = "URL";
  public static final String DEFAULT_URL = "http://localhost:5984";

  public static final String PROP_VIEW = "VIEW";
  
  public static final String PROP_VIEW_DEF = "VIEW_DEF";
  public static final String DEFAULT_VIEW_DEF = "";

  public static final String PROP_UDX_SPECIFIC_NAME = "UDX_SPECIFIC_NAME";
  public static final String DEFAULT_UDX_SPECIFIC_NAME
    = "LOCALDB.COUCHDB.WRAPPER_UDX"; // must be created

  private MedAbstractDataWrapper wrapper;
  // server uses these values
  private String un;
  private String pw;
  private String url;

  private static final Logger tracer
    = FarragoTrace.getClassTracer(MedCouchDataServer.class);

  public MedCouchDataServer(
      MedAbstractDataWrapper wrapper,
      String serverMofId,
      Properties props)
  {
    super(serverMofId, props);
    this.wrapper = wrapper;
  }

  /**
   * Called on server creation, we can require certain server properties here.
   */
  public void initialize() throws SQLException {
    Properties props = getProperties();
    requireProperty(props, PROP_USERNAME);
    un = props.getProperty(PROP_USERNAME);
    requireProperty(props, PROP_PASSWORD);
    pw = props.getProperty(PROP_PASSWORD);
    url = props.getProperty(PROP_URL, DEFAULT_URL);
  }

  // implment FarragoMedDataServer
  /**
   * Called on creation of a foreign table, tableProps are passed F.T.
   * specific properties and rowType is the expected column types that F.T.
   * expects.
   */
  public FarragoMedColumnSet newColumnSet(
      String[] localName,
      Properties tableProps,
      FarragoTypeFactory typeFactory,
      RelDataType rowType,
      Map<String, Properties> columnPropMap) throws SQLException
  {
    //tracer.info(rowType.toString());
    String udxSpecificName = getProperties().getProperty(
        PROP_UDX_SPECIFIC_NAME,
        DEFAULT_UDX_SPECIFIC_NAME);

    // override any server props:
    String myUn = tableProps.getProperty(PROP_USERNAME, un);
    String myPw = tableProps.getProperty(PROP_PASSWORD, pw);
    String myUrl = tableProps.getProperty(PROP_URL, url);

    requireProperty(tableProps, PROP_VIEW);
    String view = tableProps.getProperty(PROP_VIEW);
    String viewDef=getProperties().getProperty(PROP_VIEW_DEF, DEFAULT_VIEW_DEF);
    if (!viewDef.equals("")) {
      // don't save it in the final foreign table information.
      getProperties().remove(PROP_VIEW_DEF);
    }

    return new MedCouchColumnSet(
        this,
        localName,
        rowType,
        myUn,
        myPw,
        myUrl,
        view,
        viewDef,
        udxSpecificName);
  }

  // someone might want this
  public MedAbstractDataWrapper getWrapper() {
    return wrapper;
  }

}
