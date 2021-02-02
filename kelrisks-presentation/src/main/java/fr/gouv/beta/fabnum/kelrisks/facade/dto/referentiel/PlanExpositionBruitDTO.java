package fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import org.geolatte.geom.Geometry;

@Data
public class PlanExpositionBruitDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String      zone;
    private Date        dateArret;
    private Geometry<?> multiPolygon;
}
