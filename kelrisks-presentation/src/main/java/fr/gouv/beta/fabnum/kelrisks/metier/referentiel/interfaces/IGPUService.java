package fr.gouv.beta.fabnum.kelrisks.metier.referentiel.interfaces;

import java.util.List;

import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.Assiette;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.Generateur;

public interface IGPUService {
    
    List<Assiette> rechercherAssiettesContenantPolygon(String multiPolygon);
    
    List<Generateur> rechercherGenerateur(String partition, String idgen);

	List<Generateur> rechercherGenerateur(String partition);

	long rechercherCarteGenerateur(String idGaspar);
}
