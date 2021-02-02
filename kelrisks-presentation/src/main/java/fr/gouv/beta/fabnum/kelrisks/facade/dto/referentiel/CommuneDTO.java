package fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

import org.geolatte.geom.Geometry;

@Data
public class CommuneDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String           codeINSEE;
    private String           codePostal;
    private String           nomCommune;
    private Geometry<?>      multiPolygon;
    private List<CommuneDTO> communesLimitrophes;
    private String           codeZoneSismicite;
    private String           classePotentielRadon;
}
