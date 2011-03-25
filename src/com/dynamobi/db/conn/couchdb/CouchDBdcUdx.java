/**
 * Copyright DynamoBI Corporation
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

import sun.misc.BASE64Encoder;

import java.sql.SQLException;
import java.sql.PreparedStatement;


public class CouchDBdcUdx {

  /**
   * @param resultInserter - assumed to have the necessary column
   * names in the order we get them.
   */
  public static void execute(
      String user,
      String pw,
      String view,
      String url,
      PreparedStatement resultInserter) {
    JSONArray lst = getViewRows(user, pw, view, url);
    try {
      for (JSONObject obj : lst) {
        JSONObject cols = obj.get("value");
        int c = 0;
        for (Iterator iter = cols.keySet().iterator(); iter.hasNext(); ) {
          String key = (String) iter.next();
          String value = (String) cols.get(key);
          resultInserter.setString(++c, value);
        }
        resultInserter.executeUpdate();
      }
    } catch (SQLException e) {
      throw ApplibResource.instance().DatabaseAccessError.ex(
          e.toString(),
          e);
    }
  }
  
  private static void getViewRows(
      String user, String pw, String view, String url) {
    try {
      URL u = new URL(url + "/" + view);
      HttpURLConnection uc = (HttpURLConnection) u.openConnection();
      uc.setRequestProperty(
          "Authorization", "Basic " + buildAuthHeader(user, pw));
      uc.connect();
      String s = readStringFromConnection(uc);

      JSONParser parser = new JSONParser();
      // View Object
      JSONObject o = (JSONObject) parser.parse(s);
      JSONArray a = (JSONArray) o.get("rows");
      return a;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  private static String buildAuthHeader (String username, String password) {
    String login = username + ":" + password;
    String encodedLogin = new BASE64Encoder().encode(login.getBytes());
    return encodedLogin;
    
  }
  
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
  
  public static void main(String [ ] args) {
    
    getStaticView();
    
    
  }

  
}
