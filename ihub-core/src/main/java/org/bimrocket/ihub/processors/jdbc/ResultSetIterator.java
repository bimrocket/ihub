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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;

/**
 *
 * @author realor
 */
public class ResultSetIterator implements Iterator<JsonNode>
{
  private ResultSet resultSet;
  private final ResultSetMetaData metaData;
  private ObjectNode bufferedNode;
  private final ObjectMapper mapper;

  public ResultSetIterator(ResultSet resultSet) throws SQLException
  {
    this.resultSet = resultSet;
    metaData = resultSet.getMetaData();
    mapper = new ObjectMapper();
  }

  @Override
  public boolean hasNext()
  {
    try
    {
      if (bufferedNode == null)
      {
        bufferedNode = readNode();
      }
      return bufferedNode != null;
    }
    catch (SQLException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public JsonNode next()
  {
    try
    {
      if (bufferedNode == null)
      {
        return readNode();
      }
      else
      {
        ObjectNode returnedNode = bufferedNode;
        bufferedNode = null;

        return returnedNode;
      }
    }
    catch (SQLException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private ObjectNode readNode() throws SQLException
  {
    if (resultSet == null)
    {
      return null;
    }
    else if (resultSet.next())
    {
      ObjectNode node = mapper.createObjectNode();
      int columnCount = metaData.getColumnCount();
      for (int col = 1; col <= columnCount; col++)
      {
        String columnName = metaData.getColumnName(col);
        int columnType = metaData.getColumnType(col);
        switch (columnType)
        {
          case Types.VARCHAR:
          case Types.CHAR:
            node.put(columnName, resultSet.getString(col));
            break;
          case Types.INTEGER:
            node.put(columnName, resultSet.getInt(col));
            break;
          case Types.BIGINT:
          case Types.DECIMAL:
            node.put(columnName, resultSet.getBigDecimal(col));
            break;
          case Types.FLOAT:
            node.put(columnName, resultSet.getFloat(col));
            break;
          case Types.REAL:
          case Types.DOUBLE:
          case Types.NUMERIC:
            node.put(columnName, resultSet.getDouble(col));
            break;
          case Types.DATE:
            node.put(columnName, resultSet.getTime(col).toString());
            break;
          case Types.TIMESTAMP:
            node.put(columnName, resultSet.getTimestamp(col).toString());
            break;
          case Types.BOOLEAN:
            node.put(columnName, resultSet.getBoolean(col));
            break;
        }
      }
      return node;
    }
    else
    {
      resultSet.close();
      resultSet = null;
    }
    return null;
  }
}
