package fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel;

import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.SiteIndustrielBasiasDTO;

import java.util.List;

import org.geolatte.geom.Geometry;

public interface IGestionSiteIndustrielBasiasFacade {
    
    List<SiteIndustrielBasiasDTO> rechercherSitesSurParcelle(String codeParcelle);
    
    List<SiteIndustrielBasiasDTO> rechercherSiteDansRayonCentroideParcelle(String codeParcelle, double distance);
    
    List<SiteIndustrielBasiasDTO> rechercherSitesParRaisonSociale(String nomProprietaire);
    
    List<SiteIndustrielBasiasDTO> rechercherSitesDansPolygon(Geometry multiPolygon);
}