package org.bimrocket.ihub.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xembly.Directives;

/**
 *
 * @author kfiertek-nexus-geographics
 *
 */
public class GeometryUtils
{
  private static final Logger log =
    LoggerFactory.getLogger(GeometryUtils.class);

  private static final ObjectMapper mapper = new ObjectMapper();

  public static boolean validGeojson(JsonNode node)
  {
    JsonNode type = node.get("type");
    if (type == null || type.isNull())
    {
      return false;
    }
    String typeS = type.asText();
    if (!typeS.equals("Point") && !typeS.equals("LineString")
      && !typeS.equals("MultiPolygon") && !typeS.equals("Polygon")
      && !typeS.equals("MultiPoint") && !typeS.equals("MultiLineString")
      && !typeS.equals("GeometryCollection"))
    {
      return false;
    }
    JsonNode coords = node.get("coordinates");
    JsonNode geometries = node.get("geometries");
    if ((coords == null || coords.isNull())
      || ((geometries == null) || geometries.isNull()))
    {
      return false;
    }
    return true;
  }

  public static Directives putGeometryObjInXML(Directives dir, JsonNode geomObj)
  {
    String type = geomObj.get("type").asText();
    if (type.equals("Point"))
    {
      dir.add("Point").attr("xmlns", "http://www.opengis.net/gml");
      dir.add("coordinates").attr("decimal", ".").attr("cs", ",").attr("ts", " ");
      String pointCoordsStr = "";
      ArrayNode coords = mapper.valueToTree(geomObj.get("coordinates"));
      for (int i = 0; i < coords.size(); i++)
      {
        JsonNode pointCoords = coords.get(i);
        pointCoordsStr += ((i == 0) ? pointCoords.asDouble()
          : ("," + pointCoords.asDouble()));
      }
      dir.set(pointCoordsStr);
      dir.up().up();
      log.debug("add point {} of geom key", pointCoordsStr);
    }
    else if (type.equals("LineString"))
    {
      dir.add("LineString");
      dir.add("coordinates").attr("xmlns", "http://www.opengis.net/gml")
        .attr("decimal", ".").attr("cs", ",").attr("ts", " ");
      String coordsStr = "";
      ArrayNode coords = mapper.valueToTree(geomObj.get("coordinates"));
      for (int i = 0; i < coords.size(); i++)
      {
        if (i != 0)
        {
          coordsStr += " ";
        }
        ArrayNode coordsLine = mapper.valueToTree(coords.get(i));
        for (int j = 0; j < coordsLine.size(); j++)
        {
          double coord = coordsLine.get(j).asDouble();
          if (j != 0)
          {
            coordsStr += ",";
          }
          coordsStr += coord;
        }
      }
      dir.set(coordsStr);
      dir.up().up();
      log.debug("add LinesString {} of geom key", coordsStr);

    }
    else if (type.equals("MultiPolygon"))
    {
      dir.add("MultiPolygon").attr("srsName", "http://www.opengis.net/gml/srs/epsg.xml#25831");
      ArrayNode coordsMultiPolygon =
        mapper.valueToTree(geomObj.get("coordinates"));
      for (int i = 0; i < coordsMultiPolygon.size(); i++)
      {
        dir.add("polygonMember");
        dir.add("Polygon");
        ArrayNode coordsPolygon = mapper.valueToTree(coordsMultiPolygon.get(i));
        for (int j = 0; j < coordsPolygon.size(); j++)
        {
          if (j == 0)
          {
            dir.add("outerBoundaryIs");
          }
          else
          {
            dir.add("innerBoundaryIs");
          }
          dir.add("LinearRing");
          dir.add("coordinates").attr("xmlns",
            "http://www.opengis.net/gml").attr("decimal", ".")
            .attr("cs", ",").attr("ts", " ");
          ArrayNode coordsLinearRing = mapper.valueToTree(coordsPolygon.get(j));
          StringBuilder coordsLinear = new StringBuilder();
          for (int c = 0; c < coordsLinearRing.size(); c++)
          {
            if (c != 0)
            {
              coordsLinear.append(" ");
            }
            ArrayNode coordsLinearRingXY = mapper.valueToTree(coordsLinearRing.get(c));
            double x = coordsLinearRingXY.get(0).asDouble();
            double y = coordsLinearRingXY.get(1).asDouble();
            coordsLinear.append(x).append(",").append(y);
          }
          dir.set(coordsLinear.toString());
          dir.up().up().up();
        }
        dir.up().up();
      }
      dir.up();
    }
    else if (type.equals("Polygon"))
    {
      dir.add("Polygon");
      ArrayNode coordsPolygon = mapper.valueToTree(geomObj.get("coordinates"));
      for (int j = 0; j < coordsPolygon.size(); j++)
      {
        if (j == 0)
        {
          dir.add("outerBoundaryIs");
        }
        else
        {
          dir.add("innerBoundaryIs");
        }
        dir.add("LinearRing");
        dir.add("coordinates").attr("xmlns",
          "http://www.opengis.net/gml").attr("decimal", ".").attr("cs", ",").attr("ts", " ");
        ArrayNode coordsLinearRing = mapper.valueToTree(coordsPolygon.get(j));
        StringBuilder coordsLinear = new StringBuilder();
        for (int c = 0; c < coordsLinearRing.size(); c++)
        {
          if (c != 0)
          {
            coordsLinear.append(" ");
          }
          ArrayNode coordsLinearRingXY = mapper.valueToTree(coordsLinearRing.get(c));
          double x = coordsLinearRingXY.get(0).asDouble();
          double y = coordsLinearRingXY.get(1).asDouble();
          coordsLinear.append(x).append(",").append(y);
        }
        dir.set(coordsLinear.toString());
        dir.up().up().up();
      }
      dir.up();
    }
    else if (type.equals("MultiPoint"))
    {
      dir.add("MultiPoint");
      ArrayNode coordsMultiPoint = mapper.valueToTree(geomObj.get("coordinates"));
      for (int i = 0; i < coordsMultiPoint.size(); i++)
      {
        dir.add("pointMember");
        dir.add("Point").attr("xmlns", "http://www.opengis.net/gml");
        dir.add("coordinates").attr("decimal", ".").attr("cs", ",").attr("ts", " ");
        String pointCoordsStr = "";
        ArrayNode coords = mapper.valueToTree(coordsMultiPoint.get(i));
        for (int j = 0; j < coords.size(); j++)
        {
          JsonNode pointCoords = coords.get(j);
          pointCoordsStr += ((j == 0) ? pointCoords.asDouble()
            : ("," + pointCoords.asDouble()));
        }
        dir.set(pointCoordsStr);
        dir.up().up().up();
        log.debug("add point {} of geom key", pointCoordsStr);
      }
      dir.up();
      log.debug("add MultiPoint of geom key");
    }
    else if (type.equals("MultiLineString"))
    {
      dir.add("MultiLineString");
      ArrayNode coordsMultiLineString = mapper.valueToTree(geomObj.get("coordinates"));
      for (int i = 0; i < coordsMultiLineString.size(); i++)
      {
        dir.add("lineStringMember");
        dir.add("LineString");
        dir.add("coordinates").attr("xmlns", "http://www.opengis.net/gml")
          .attr("decimal", ".").attr("cs", ",").attr("ts", " ");
        String coordsStr = "";
        ArrayNode coordsLineString = mapper.valueToTree(coordsMultiLineString.get(i));
        for (int j = 0; j < coordsLineString.size(); j++)
        {
          if (j != 0)
          {
            coordsStr += " ";
          }
          ArrayNode coordsLine = mapper.valueToTree(coordsLineString.get(j));
          for (int k = 0; k < coordsLine.size(); k++)
          {
            double coord = coordsLine.get(k).asDouble();
            if (k != 0)
            {
              coordsStr += ",";
            }
            coordsStr += coord;
          }
        }
        dir.set(coordsStr);
        dir.up().up().up();
        log.debug("add LinesString {} of geom key", coordsStr);
      }
      dir.up();
    }
    else if (type.equals("GeometryCollection"))
    {
      dir.add("GeometryCollection")
        .attr("srsName", "http://www.opengis.net/gml/srs/epsg.xml#2583");
      ArrayNode collection = mapper.valueToTree(geomObj.get("geometries"));
      for (int i = 0; i < collection.size(); i++)
      {
        dir.add("geometryMember");
        dir = putGeometryObjInXML(dir, collection.get(i));
        dir.up();
      }
      dir.up();
    }

    return dir;
  }
}
