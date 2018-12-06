package fr.gouv.beta.fabnum.kelrisks.presentation.rest;

import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.SiteIndustrielBasiasDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionSiteIndustrielBasiasFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = {"API Basias"}, description = "API permettant les recoupements concernant les Sites Industriels Basias")
public class ApiRestBasias {
    
    private static final String TEXT_PLAIN_UTF8       = MediaType.TEXT_PLAIN + ";charset=UTF-8";
    private static final String APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON + ";charset=UTF-8";
    
    @Autowired
    IGestionSiteIndustrielBasiasFacade gestionSiteIndustrielBasiasFacade;
    
    public ApiRestBasias() {
        // Rien à faire
    }
    
    
    @GetMapping("/api/basias/cadastre/{codeParcelle}")
    @ApiOperation(value = "Requête retournant les sites industiels Basias liés à la Parcelle.", response = String.class)
    public Response basiasInCadastre(@ApiParam(required = true, name = "codeParcelle", value = "Code de la parcelle.")
                                     @PathVariable("codeParcelle") String codeParcelle) {
        
        List<SiteIndustrielBasiasDTO> siteIndustrielBasiasDTOS = gestionSiteIndustrielBasiasFacade.rechercherSitesSurParcelle(codeParcelle);
        
        return Response.ok(siteIndustrielBasiasDTOS).build();
    }
    
    
    @GetMapping("/api/basias/cadastre/{codeParcelle}/{distance}")
    @ApiOperation(value = "Requête retournant les sites industiels Basias dans un certain rayon du centroîde de la Parcelle.", response = String.class)
    public Response basiasWithinCadastreRange(@ApiParam(required = true, name = "codeParcelle", value = "Code de la parcelle.")
                                              @PathVariable("codeParcelle") String codeParcelle,
                                              @ApiParam(required = true, name = "distance", value = "Distance au centroïde de la parcelle (en mètres).", defaultValue = "100")
                                              @PathVariable("distance") String distance) {
        
        Double rayon = distance.equals("") ? 100D : Double.parseDouble(distance);
        
        List<SiteIndustrielBasiasDTO> siteIndustrielBasiasDTOS = gestionSiteIndustrielBasiasFacade.rechercherSiteDansRayonCentroideParcelle(codeParcelle, rayon);
        
        return Response.ok(siteIndustrielBasiasDTOS).build();
    }
}
