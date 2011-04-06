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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.commons.codec.binary.Base64;

import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * Generic Udx class to build any custom functions to browse CouchDB views.
 * @author Kevin Secretan
 */
public class CouchUdx {

  /**
   * Called by a custom LucidDB function for each view.
   * @param userName - CouchDB user name
   * @param pw - CouchDB password
   * @param url - CouchDB REST URL
   * @param view - CouchDB REST view -- concatenated on the end of URL with
   *               a slash prefix.
   * @param resultInserter - Table for inserting results. Assumed to have the
   * necessary column names in the order we get them.
   */
  public static void query(
      String userName,
      String pw,
      String url,
      String view,
      PreparedStatement resultInserter) {

    JSONArray lst = getViewRows(userName, pw, url, view);
    if (lst == null)
      return;

    try {
      Iterator iter = lst.iterator();
      while (iter.hasNext()) {
        JSONObject obj = (JSONObject) iter.next();
        JSONObject cols = (JSONObject) obj.get("value");
        int c = 0;
        for (Iterator iter2 = cols.keySet().iterator(); iter2.hasNext(); ) {
          // TODO: de-stringify the value.
          Object k = iter2.next();
          if (k == null) continue;
          Object v = cols.get(k.toString());
          String value  = (v != null) ? v.toString() : null;
          resultInserter.setString(++c, value);
        }
        resultInserter.executeUpdate();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Helper function gets the rows from the foreign CouchDB server and returns
   * them as a JSONArray.
   */
  private static JSONArray getViewRows(
      String user, String pw, String url, String view) {
    try {
      URL u = new URL(url + "/" + view);
      HttpURLConnection uc = (HttpURLConnection) u.openConnection();
      if ( user != null && user.length() > 0 ) { 
    	  uc.setRequestProperty(
          "Authorization", "Basic " + buildAuthHeader(user, pw));
      }
      uc.connect();
      String s = readStringFromConnection(uc);

      JSONParser parser = new JSONParser();
      // View Object
      JSONObject o = (JSONObject) parser.parse(s);
      JSONArray a = (JSONArray) o.get("rows");
      return a;
    } 
    // REVIEW NAG 05-APR-2011
    // Add actual SQL exception processing and eliminate 
    // stacktraces.
    catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  /**
   * Builds a standard basic authentication http header with the user and pass.
   */
  private static String buildAuthHeader (String username, String password) {
    String login = username + ":" + password;
    String encodedLogin = new String(Base64.encodeBase64(login.getBytes()));
    return encodedLogin;
  }
  
  /**
   * Reads the data from a http call into a string.
   */
  private static String readStringFromConnection (HttpURLConnection uc) throws IOException {
    InputStreamReader in = new InputStreamReader(uc.getInputStream());
    BufferedReader buff = new BufferedReader(in);
    StringBuffer sb = new StringBuffer();
    String line = null;
    do {
      line = buff.readLine();
      if ( line != null ) sb.append(line);
    } while ( line != null );
    
    return sb.toString();
  }
  
}
