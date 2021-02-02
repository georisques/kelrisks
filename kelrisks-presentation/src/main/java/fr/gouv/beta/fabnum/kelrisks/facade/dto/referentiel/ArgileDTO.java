package fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel;

import lombok.Data;

import java.io.Serializable;

import org.geolatte.geom.Geometry;

@Data
public class ArgileDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
	
    private Geometry<?> multiPolygon;
    private String      numeroDepartement;
    private int         niveauAlea;
}
