package org.bimrocket.ihub.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kfiertek-nexus-geographics
 *
 */
public class GeometryWorkshop
{
  private static final Logger log =
    LoggerFactory.getLogger(GeometryWorkshop.class);

  private static final ObjectMapper mapper = new ObjectMapper();

  public GeometryWorkshop()
  {
  }

  public static JsonNode toPointCentroid(JsonNode geometry)
  {
    String type = geometry.get("type").asText();

    if (type.equals("GeometryCollection"))
    {
      ArrayNode collection = mapper.valueToTree(geometry.get("geometries"));
      ArrayNode newCollection = mapper.createArrayNode();
      for (int i = 0; i < collection.size(); i++)
      {
        newCollection.add(toPointCentroid(collection.get(i)));
      }
      geometry = setNode(type, newCollection, true);
    }
    else
    {
      Geometry geo = null;
      geo = nodeToGeometry(geometry);
      Point p = geo.getCentroid();
      ArrayNode arrGeo = mapper.createArrayNode();
      arrGeo.add(p.getX()).add(p.getY());
      geometry = setNode("Point", arrGeo, false);
    }
    return geometry;
  }

  public static JsonNode toMultiPointCentroid(JsonNode geometry)
  {
    String type = geometry.get("type").asText();

    if (type.equals("GeometryCollection"))
    {
      ArrayNode collection = mapper.valueToTree(geometry.get("geometries"));
      ArrayNode newCollection = mapper.createArrayNode();
      for (int i = 0; i < collection.size(); i++)
      {
        newCollection.add(toMultiPointCentroid(collection.get(i)));
      }
      geometry = setNode(type, newCollection, true);
    }
    else
    {
      Geometry geo = null;
      if (!type.contains("Multi"))
      {
        geo = nodeToGeometry(geometry);
        Point p = geo.getCentroid();
        ArrayNode multiPointNode = mapper.createArrayNode();
        ArrayNode arrGeo = mapper.createArrayNode();
        multiPointNode.add(arrGeo);
        arrGeo.add(p.getX()).add(p.getY());
        geometry = setNode("MultiPoint", multiPointNode, false);
      }
      else
      {
        JsonNode aux;
        ArrayNode arMultiPoint = mapper.createArrayNode();
        for (int i = 0; i < geometry.get("coordinates").size(); i++)
        {
          aux = setNode(type,
            mapper.valueToTree(geometry.get("coordinates").get(i)), false);
          geo = nodeToGeometry(aux);
          if (geo != null)
          {
            Point p = geo.getCentroid();

            ArrayNode arPoint = mapper.createArrayNode();
            arPoint.add(p.getX()).add(p.getY());
            arMultiPoint.add(arPoint);
          }

        }
        geometry = setNode("MultiPoint", arMultiPoint, false);
      }
    }
    return geometry;
  }

  public static JsonNode toSingleGeometry(JsonNode geometry)
  {
    String type = geometry.get("type").asText();

    if (type.equals("GeometryCollection"))
    {
      ArrayNode collection = mapper.valueToTree(geometry.get("geometries"));
      ArrayNode newCollection = mapper.createArrayNode();
      for (int i = 0; i < collection.size(); i++)
      {
        newCollection.add(toSingleGeometry(collection.get(i)));
      }
      geometry = setNode(type, newCollection, true);
    }
    else
    {
      if (type.contains("Multi"))
      {
        geometry = setNode(type.substring(5),
          mapper.valueToTree(geometry.get("coordinates").get(0)), false);
      }
    }

    return geometry;
  }

  public static JsonNode toMultiGeometry(JsonNode geometry)
  {
    String type = geometry.get("type").asText();

    if (type.equals("GeometryCollection"))
    {
      ArrayNode collection = mapper.valueToTree(geometry.get("geometries"));
      ArrayNode newCollection = mapper.createArrayNode();
      for (int i = 0; i < collection.size(); i++)
      {
        newCollection.add(toMultiGeometry(collection.get(i)));
      }
      geometry = setNode(type, newCollection, true);
    }
    else
    {
      if (!type.contains("Multi"))
      {
        ArrayNode arr = mapper.createArrayNode();
        arr.add(geometry.get("coordinates"));
        geometry = setNode("Multi" + type, arr, false);
      }
    }
    return geometry;
  }

  public static JsonNode toMultiPointInterior(JsonNode geometry)
  {
    String type = geometry.get("type").asText();

    if (type.equals("GeometryCollection"))
    {
      ArrayNode collection = mapper.valueToTree(geometry.get("geometries"));
      ArrayNode newCollection = mapper.createArrayNode();
      for (int i = 0; i < collection.size(); i++)
      {
        newCollection.add(toMultiPointInterior(collection.get(i)));
      }
      geometry = setNode(type, newCollection, true);
    }
    else
    {
      Geometry geo = null;
      JsonNode aux;
      ArrayNode arMultiPoint = mapper.createArrayNode();
      for (int i = 0; i < geometry.get("coordinates").size(); i++)
      {
        aux = setNode(type, mapper.valueToTree(
          geometry.get("coordinates").get(i)), false);
        geo = nodeToGeometry(aux);
        if (geo != null)
        {
          Point p = geo.getInteriorPoint();

          ArrayNode arPoint = mapper.createArrayNode();
          arPoint.add(p.getX()).add(p.getY());
          arMultiPoint.add(arPoint);
        }
      }
      geometry = setNode("MultiPoint", arMultiPoint, false);
    }
    return geometry;
  }

  public static JsonNode toPointInterior(JsonNode geometry)
  {
    String type = geometry.get("type").asText();

    if (type.equals("GeometryCollection"))
    {
      ArrayNode collection = mapper.valueToTree(geometry.get("geometries"));
      ArrayNode newCollection = mapper.createArrayNode();
      for (int i = 0; i < collection.size(); i++)
      {
        newCollection.add(toPointInterior(collection.get(i)));
      }
      geometry = setNode(type, newCollection, true);
    }
    else
    {
      Geometry geo = null;
      geo = nodeToGeometry(geometry);
      if (geo != null)
      {
        Point p = geo.getInteriorPoint();
        ArrayNode arrGeo = mapper.createArrayNode();
        arrGeo.add(p.getX()).add(p.getY());
        geometry = setNode("Point", arrGeo, false);
      }
    }
    return geometry;
  }

  public static JsonNode toLineString(JsonNode geometry)
  {
    String type = geometry.get("type").asText();
    if (type.equals("GeometryCollection"))
    {
      ArrayNode collection = mapper.valueToTree(geometry.get("geometries"));
      ArrayNode newCollection = mapper.createArrayNode();
      for (int i = 0; i < collection.size(); i++)
      {
        newCollection.add(toLineString(collection.get(i)));
      }
      geometry = setNode(type, newCollection, true);
    }
    else
    {
      if (!type.equals("LineString") && !type.equals("MultiLineString"))
      {
        if (type.equals("Point"))
        {
          String error = String.format(
            "Incorrect transformation of type '%s' to LineString", type);
          log.error(error);
          throw new UnsupportedOperationException(error);
        }
        else
        {
          if (type.equals("MultiPolygon"))
          {
            ArrayNode arrPolygon = mapper.createArrayNode();
            for (int i = 0; i < geometry.get("coordinates").size(); i++)
            {
              for (int j = 0; j < geometry.get("coordinates").get(i).size(); j++)
              {
                arrPolygon.add(geometry.get("coordinates").get(i).get(j));
              }
            }
            geometry = setNode("MultiLineString", arrPolygon, false);
          }
          else
          {
            ArrayNode arrLineString = mapper.createArrayNode();
            if (type.equals("MultiPoint"))
            {
              arrLineString = mapper.valueToTree(geometry.get("coordinates"));
            }
            else if (type.equals("Polygon"))
            {
              for (int i = 0; i < geometry.get("coordinates").size(); i++)
              {
                for (int j = 0; j < geometry.get("coordinates").get(i).size(); j++)
                {
                  arrLineString.add(geometry.get("coordinates").get(i).get(j));
                }
              }
            }
            geometry = setNode("LineString", arrLineString, false);
          }
        }
      }
    }
    return geometry;
  }

  public static JsonNode toPolygon(JsonNode geometry)
    throws JsonProcessingException
  {
    String type = geometry.get("type").asText();
    if (type.equals("GeometryCollection"))
    {
      ArrayNode collection = mapper.valueToTree(geometry.get("geometries"));
      ArrayNode newCollection = mapper.createArrayNode();
      for (int i = 0; i < collection.size(); i++)
      {
        newCollection.add(toPolygon(collection.get(i)));
      }
      geometry = setNode(type, newCollection, true);
    }
    else
    {
      if (!type.equals("Polygon") && !type.equals("MultiPolygon"))
      {
        if (type.equals("Point"))
        {
          String error = String.format(
            "Incorrect transformation of type '%s' to Polygon", type);
          log.error(error);
          throw new UnsupportedOperationException(error);
        }
        else
        {
          if (type.equals("LineString") || type.equals("MultiPoint"))
          {
            if (isPolygon(geometry.get("coordinates")))
            {
              ArrayNode arrPolygon = mapper.createArrayNode();
              arrPolygon.add(mapper.valueToTree(geometry.get("coordinates")));
              geometry = setNode("Polygon", arrPolygon, false);
            }
            else
            {
              String error = String.format(
                "Incorrect transformation of type '%s' to Polygon, does not meet the conditions",
                type);
              log.error(error);
              throw new UnsupportedOperationException(error);
            }
          }
          else if (type.equals("MultiLineString"))
          {
            ArrayNode arrPolygon = mapper.createArrayNode();
            for (int i = 0; i < geometry.get("coordinates").size(); i++)
            {
              if (isPolygon(geometry.get("coordinates").get(i)))
              {
                ArrayNode arrLinearRing = mapper.createArrayNode();
                arrLinearRing.add(geometry.get("coordinates").get(i));
                arrPolygon.add(arrLinearRing);
              }
              else
              {
                String error = String.format(
                  "Incorrect transformation of type '%s' to Polygon, does not meet the conditions",
                  type);
                log.error(error);
                throw new UnsupportedOperationException(error);
              }
            }
            geometry = setNode("MultiPolygon", arrPolygon, false);
          }
        }
      }
    }
    return geometry;
  }

  public static JsonNode bufferPoint(JsonNode geometry, double radius)
  {
    String type = geometry.get("type").asText();
    if (type.equals("GeometryCollection"))
    {
      ArrayNode collection = mapper.valueToTree(geometry.get("geometries"));
      ArrayNode newCollection = mapper.createArrayNode();
      for (int i = 0; i < collection.size(); i++)
      {
        newCollection.add(bufferPoint(collection.get(i), radius));
      }
      geometry = setNode(type, newCollection, true);
    }
    else
    {
      Geometry geo = nodeToGeometry(geometry);
      if (geo != null)
      {
        geo = geo.buffer(radius);

        String polygon = new GeometryJSON().toString(geo);
        try
        {
          geometry = mapper.readTree(polygon);
        }
        catch (JsonProcessingException e)
        {
          log.error(geometry.toPrettyString(), e);
        }
      }
    }
    return geometry;
  }

  private static Boolean isPolygon(JsonNode ar)
  {
    ArrayNode first = mapper.valueToTree(ar.get(0));
    ArrayNode last = mapper.valueToTree(ar.get(ar.size() - 1));
    Boolean polygon = false;
    if (first.get(0).asDouble() == last.get(0).asDouble() &&
      first.get(1).asDouble() == last.get(1).asDouble())
    {
      polygon = true;
    }
    return polygon;
  }

  private static JsonNode setNode(String type, ArrayNode array,
    Boolean collection)
  {
    JsonNode node = null;
    ObjectNode ob = mapper.createObjectNode();
    ob.put("type", type);
    if (collection)
    {
      ob.set("geometries", array);
    }
    else
    {
      ob.set("coordinates", array);
    }

    try
    {
      node = mapper.readTree(mapper.writeValueAsString(ob));
    }
    catch (JsonProcessingException e)
    {
      e.printStackTrace();
    }
    return node;
  }

  private static Geometry nodeToGeometry(JsonNode geometry)
  {
    GeometryJSON gjson = new GeometryJSON();
    Reader reader = null;
    Geometry geo = null;
    try
    {
      reader = new StringReader(mapper.writeValueAsString(geometry));
      geo = gjson.read(reader);
    }
    catch (IOException e)
    {
      log.error("can't read following geojson :: {}", geometry.toPrettyString(),
        e);
    }
    return geo;
  }

  public static JsonNode crsTransform(JsonNode geojson,
    String source, String dest)
  {
    Geometry sourceGeometry = nodeToGeometry(geojson);
    try
    {
      CoordinateReferenceSystem sourceCRS = CRS.decode(source);
      CoordinateReferenceSystem targetCRS = CRS.decode(dest);
      MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
      Geometry targetGeometry = JTS.transform(sourceGeometry, transform);

      String st = new GeometryJSON().toString(targetGeometry);
      geojson = mapper.readTree(st);

    }
    catch (FactoryException e)
    {
      log.error("can't decode a Coordinate Reference System");
    }
    catch (TransformException e)
    {
      log.error("can't transform a Coordinate Reference System");
    }
    catch (IOException e)
    {
      log.error("can't read geometry to Json Node");
    }

    return geojson;
  }
}
