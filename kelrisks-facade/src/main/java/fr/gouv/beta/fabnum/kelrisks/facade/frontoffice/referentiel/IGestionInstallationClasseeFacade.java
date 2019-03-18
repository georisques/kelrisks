package fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel;

import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.InstallationClasseeDTO;

import java.util.List;

import org.geolatte.geom.Geometry;

public interface IGestionInstallationClasseeFacade {
    
    List<InstallationClasseeDTO> rechercherInstallationsSurParcelle(String codeParcelle);
    
    List<InstallationClasseeDTO> rechercherInstallationsDansRayonCentroideParcelle(String codeParcelle, double distance);
    
    List<InstallationClasseeDTO> rechercherInstallationsAuCentroideCommune(String codePostal);
    
    List<InstallationClasseeDTO> rechercherInstallationsDansPolygon(Geometry codeParcelle);
    
    List<InstallationClasseeDTO> rechercherInstallationsSurParcelles(List<String> codes);
}