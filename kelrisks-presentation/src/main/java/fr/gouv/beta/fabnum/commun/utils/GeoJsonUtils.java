package fr.gouv.beta.fabnum.commun.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geolatte.geom.Feature;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.json.GeoJsonFeature;
import org.geolatte.geom.json.GeolatteGeomModule;
import org.geolatte.geom.jts.JTS;
import org.locationtech.jts.geom.GeometryFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class GeoJsonUtils {
    
    // https://theartofdev.com/2014/07/20/jackson-objectmapper-performance-pitfall/
    // https://stackoverflow.com/questions/57670466/objectmapper-best-practice-for-thread-safety-and-performance
    private static final ObjectMapper  mapper = new ObjectMapper();
    private static final GeometryFactory jtsFactory = new GeometryFactory();
    
    static {
        mapper.registerModule(new GeolatteGeomModule());
    }
	
    public static String toGeoJson(Geometry<?> geom) {
        
        return toGeoJson(geom, null, null);
    }
    
    public static String toGeoJson(List<Geometry<?>> geometries) {
        
        if (geometries == null) { return ""; }
        
        List<Polygon<?>> polygons = new ArrayList<>();
        
        geometries.forEach(geometry -> {
            if (geometry instanceof MultiPolygon) {
                MultiPolygon<?> multiPolygon = (MultiPolygon<?>) geometry;
                
                for (int index = 0; index < multiPolygon.getNumGeometries(); index++) {
                    polygons.add(multiPolygon.getGeometryN(index));
                }
            }
            else { polygons.add((Polygon<?>) geometry); }
        });
        
        MultiPolygon<?> multipolygon = new MultiPolygon(polygons.toArray(new Polygon[0]));
        
        return toGeoJson(multipolygon, null, null);
    }
    
    public static String toGeoJson(Geometry<?> geom, String id) {
        
        return toGeoJson(geom, id, null);
    }
    
    public static String toGeoJson(Geometry<?> geom, Map<String, Object> properties) {
        
        return toGeoJson(geom, null, properties);
    }
    
    public static String toGeoJson(Geometry<?> geom, String id, Map<String, Object> properties) {
    
        if (geom == null) { return ""; }
    
        if (properties == null) {
            properties = new HashMap<>();
            properties.put("code", geom.hashCode());
            properties.put("nom", "MyGeom");
        }
    
        String geoJsonOutput = "";
    
        try {
            Feature<?, ?> f      = new GeoJsonFeature<>(geom, (id == null ? "1" : id), properties);
            geoJsonOutput = mapper.writeValueAsString(f);
        }
        catch (JsonProcessingException e) {
            // TODO : Catcher cette exception correctement !
            log.error("Fail to create GeoJson",e);
        }
    
        return geoJsonOutput;
    }
    
    public static String getGeometryFromGeoJson(String featureGeoJson) {
    
        String simpleGeoJson;
        String fixedGeoJson = simpleGeoJson = featureGeoJson.replace("\\", "");
        if (fixedGeoJson.contains("geom") && fixedGeoJson.contains("coordinates")) {
            String geoJsonType        = fixedGeoJson.replaceAll(".*geom.*?(\"type\"[\\s\\S]*?),.*", "$1");
            String geoJsonCoordinates = fixedGeoJson.replaceAll(".*?(\"coordinates\"[\\s\\S]*\\]).*", "$1");
            simpleGeoJson = "{" + geoJsonType + "," + geoJsonCoordinates + "}";
        }
    
        return simpleGeoJson;
    }
    
    /**
     * Get bbox from a geometry
     * @param geom
     * @return
     */
    public static Geometry<?> convertToBBOX(Geometry<?> geom) {
    	return JTS.from(jtsFactory.toGeometry(JTS.to(geom.getEnvelope())));
    }
    
    public static Geometry<?> fromGeoJson(String geoJson) {
        
        if (geoJson == null) {
            return null;
        }
        Geometry<?> geom = null;
        try {
            String geometry = getGeometryFromGeoJson(geoJson);
            geom = mapper.readValue(geometry, Geometry.class);
        }
        catch (IOException e) {
            // TODO : Catcher cette exception correctement !
            log.error("Fail to create geom from GeoJson",e);
        }
        
        return geom;
    }
}
