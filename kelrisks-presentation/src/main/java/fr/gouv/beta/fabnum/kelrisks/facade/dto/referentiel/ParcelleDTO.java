package fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel;

import lombok.Data;

import java.io.Serializable;

import org.geolatte.geom.Geometry;

@Data
public class ParcelleDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long        id;
    private String      type;
    private String      code;
    private String      type_geom;
    private String      commune;
    private Geometry<?> multiPolygon;
    private String      prefixe;
    private String      section;
    private String      numero;
    private String      ewkt;
}
  
