package fr.gouv.beta.fabnum.kelrisks.presentation.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.crs.CoordinateSystemAxis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.gouv.beta.fabnum.kelrisks.facade.avis.AvisDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.ParcelleDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionParcelleFacade;
import fr.gouv.beta.fabnum.kelrisks.presentation.rest.AbstractBasicApi;
import lombok.Data;

@RestController
public class CadastreController extends AbstractBasicApi {
    
    @Autowired
    IGestionParcelleFacade gestionParcelleFacade;
    
    @GetMapping("/api/cadastre/{x}/{y}")
    @ResponseBody
    public ResponseEntity<List<String>> parcelleDansRayon(@PathVariable(value = "x", required = true) Double x, @PathVariable(value = "y", required = true) Double y) {
        
        List<ParcelleDTO> parcelleDTOs = gestionParcelleFacade.rechercherParcellesDansRayon(x, y, 0.006);
        
        List<String> ewkts = parcelleDTOs.stream().map(ParcelleDTO::getEwkt).collect(Collectors.toList());
        
        return ResponseEntity.ok(ewkts);
    }
    
    @GetMapping("/api/cadastre/proximite/{x}/{y}")
    public ResponseEntity<ParcelleDTO> parcelleLaPlusProche(@PathVariable(value = "x", required = true) Double x, @PathVariable(value = "y", required = true) Double y) {
        
        ParcelleDTO parcelleDTO = gestionParcelleFacade.rechercherParcelleAvecCoordonnees(x, y);
        
        return ResponseEntity.ok(parcelleDTO);
    }
    
    @GetMapping("/api/cadastre/match/{insee}/{code}")
    public ResponseEntity<ParcelleWithCentroidDTO> parcelleMatch(@PathVariable("insee") @NotBlank @Size(max = 5) String insee, @PathVariable("code") @NotBlank @Size(max = 255) String code) {
    
        if (StringUtils.isEmpty(insee) || StringUtils.isEmpty(code)) { return ResponseEntity.ok(new ParcelleWithCentroidDTO()); }
    
        List<ParcelleDTO> parcelles = getParcelles(insee, code);
        if (parcelles != null && !parcelles.isEmpty()) {
            ParcelleWithCentroidDTO parcelleWithCentroid = new ParcelleWithCentroidDTO();
            parcelleWithCentroid.setParcelle(parcelles.get(0));
            Geometry<?> centroid = gestionParcelleFacade.rechercherCentroidParcelle(parcelleWithCentroid.getParcelle().getMultiPolygon());
            AvisDTO.Leaflet.Point point = new AvisDTO.Leaflet.Point(Double.toString(centroid.getPositionN(0).getCoordinate(CoordinateSystemAxis.mkLonAxis())),
                                                                    Double.toString(centroid.getPositionN(0).getCoordinate(CoordinateSystemAxis.mkLatAxis())));
            parcelleWithCentroid.setCentroid(point);
        
            return ResponseEntity.ok(parcelleWithCentroid);
        }
        
        return ResponseEntity.ok(new ParcelleWithCentroidDTO());
    }
    
    @Data
    public static class ParcelleWithCentroidDTO {
        
        private ParcelleDTO           parcelle;
        private AvisDTO.Leaflet.Point centroid = new AvisDTO.Leaflet.Point();
    }
}
