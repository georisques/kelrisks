package fr.gouv.beta.fabnum.kelrisks.metier.referentiel;

import fr.gouv.beta.fabnum.commun.metier.impl.AbstractCRUDService;
import fr.gouv.beta.fabnum.kelrisks.metier.referentiel.interfaces.ISiteSolPolueService;
import fr.gouv.beta.fabnum.kelrisks.persistance.referentiel.ISiteSolPolueDAO;
import fr.gouv.beta.fabnum.kelrisks.persistance.referentiel.impl.SiteSolPolueDAO;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.SiteSolPolue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Service métier de gestion de l'entité SiteSolPolue
 */

@Service("siteSolPolueService")
public class SiteSolPolueService extends AbstractCRUDService<SiteSolPolue> implements ISiteSolPolueService {
    
    private static final long serialVersionUID = 1L;
    
    /*
     * Le dao spécifique à ce service
     */
    private ISiteSolPolueDAO dao;
    
    @Autowired
    public SiteSolPolueService(@Qualifier("siteSolPolueDAO") final SiteSolPolueDAO fdao) {
        
        this.setFdao(fdao);
        dao = fdao;
    }
    
    @Override
    public SiteSolPolue rechercherZoneContenantParcelle(String codeParcelle) {
        
        return dao.rechercherZoneContenantParcelle(codeParcelle);
    }
}
  