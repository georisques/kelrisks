package fr.gouv.beta.fabnum.kelrisks.metier.referentiel.interfaces;

import java.util.List;

import fr.gouv.beta.fabnum.commun.metier.IAbstractCRUDService;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.Commune;

/**
 * Interface du Service qui gère les entités Commune
 */
public interface ICommuneService extends IAbstractCRUDService<Commune> {
    
    List<Commune> rechercherCommunesLimitrophes(String codeINSEE);

	Commune rechercherCommuneAvecCodeINSEE(String codeINSEE);
}
  