/*
 * BIMROCKET
 *
 * Copyright (C) 2022, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.bimrocket.ihub.processors.jdbc;

import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bimrocket.ihub.processors.FullScanLoader;
import org.bimrocket.ihub.util.ConfigProperty;

/**
 *
 * @author realor
 */
public class JdbcFullScanLoader extends FullScanLoader
{
  @ConfigProperty(name="driverClassName")
  String driverClassName;

  @ConfigProperty(name="connectionUrl")
  String connectionUrl;

  @ConfigProperty(name="username")
  String username;

  @ConfigProperty(name="password", secret=true)
  String password;

  @ConfigProperty(name="query", contentType="text/x-sql")
  String query;

  @ConfigProperty(name="idFields")
  List<String> idFields = new ArrayList<>();

  Connection connection;
  PreparedStatement statement;
  ResultSet resultSet;

  @Override
  public void init() throws Exception
  {
    super.init();

    Class.forName(driverClassName);

    connection =
      DriverManager.getConnection(connectionUrl, username, password);

    statement = connection.prepareStatement(query);
  }

  @Override
  public void end()
  {
    super.end();
    closeQuietly();
  }

  @Override
  protected Iterator<JsonNode> fullScan()
  {
    try
    {
      resultSet = statement.executeQuery();
      return new ResultSetIterator(resultSet);
    }
    catch (SQLException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  protected String getLocalId(JsonNode localObject)
  {
    List<String> ids = new ArrayList<>(idFields.size());
    for (String idField : idFields)
    {
      ids.add(String.valueOf(localObject.get(idField)));
    }
    return String.join(";", ids);
  }

  private void closeQuietly()
  {
    try
    {
      if (resultSet != null) resultSet.close();
    }
    catch (SQLException ex)
    {
    }
    finally
    {
      resultSet = null;
    }

    try
    {
      if (statement != null) statement.close();
    }
    catch (SQLException ex)
    {
    }
    finally
    {
      statement = null;
    }

    try
    {
      if (connection != null) connection.close();
    }
    catch (SQLException ex)
    {
    }
    finally
    {
      connection = null;
    }
  }
}
