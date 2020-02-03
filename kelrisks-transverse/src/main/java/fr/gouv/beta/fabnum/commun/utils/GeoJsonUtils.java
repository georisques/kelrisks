package fr.gouv.beta.fabnum.commun.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geolatte.geom.Feature;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.json.GeoJsonFeature;
import org.geolatte.geom.json.GeolatteGeomModule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeoJsonUtils {
    
    public static String toGeoJson(Geometry<?> geom) {
        
        return toGeoJson(geom, null, null);
    }
    
    public static String toGeoJson(List<Geometry<?>> polygons) {
        
        MultiPolygon multipolygon = new MultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
        
        return toGeoJson(multipolygon, null, null);
    }
    
    public static String toGeoJson(Geometry<?> geom, Object id) {
        
        return toGeoJson(geom, id, null);
    }
    
    public static String toGeoJson(Geometry<?> geom, Map<String, Object> properties) {
        
        return toGeoJson(geom, null, properties);
    }
    
    public static String toGeoJson(Geometry<?> geom, Object id, Map<String, Object> properties) {
        
        if (properties == null) {
            properties = new HashMap<>();
            properties.put("code", geom.hashCode());
            properties.put("nom", "MyGeom");
        }
        
        String geoJsonOutput = "";
        
        try {
            Feature<?, ?> f      = new GeoJsonFeature<>(geom, (id == null ? "1" : id), properties);
            ObjectMapper  mapper = new ObjectMapper();
            mapper.registerModule(new GeolatteGeomModule());
            geoJsonOutput = mapper.writeValueAsString(f);
        }
        catch (JsonProcessingException e) {
            // TODO : Catcher cette exception correctement !
            e.printStackTrace();
        }
        
        return geoJsonOutput;
    }
    
    public static Geometry<?> fromGeoJson(String geoJson) {
        
        if (geoJson == null) {
            return null;
        }
        Geometry<?> geometry = null;
        try {
            geoJson = geoJson.replace("\\", "");
            String geoJsonType        = geoJson.replaceFirst(".*?(\"type\"[\\s\\S]*?),.*", "$1");
            String geoJsonCoordinates = geoJson.replaceAll(".*?(\"coordinates\"[\\s\\S]*\\]).*", "$1");
    
            geoJson = "{" + geoJsonType + "," + geoJsonCoordinates + "}";
    
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new GeolatteGeomModule());
            geometry = mapper.readValue(geoJson, Geometry.class);
        }
        catch (IOException e) {
            // TODO : Catcher cette exception correctement !
            e.printStackTrace();
        }
        
        return geometry;
    }
}