package fr.gouv.beta.fabnum.kelrisks.metier.referentiel;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.gouv.beta.fabnum.kelrisks.metier.referentiel.interfaces.IGPUService;
import fr.gouv.beta.fabnum.kelrisks.persistance.referentiel.repository.IAssietteRepository;
import fr.gouv.beta.fabnum.kelrisks.persistance.referentiel.repository.IGenerateurRepository;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.Assiette;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.Generateur;

/**
 * Documentation du GPU : TODO
 * http://www.geoinformations.developpement-durable.gouv.fr/fichier/pdf/20190211_nomenclature_ordre_alphabetique_cle7abf9e.pdf?arg=177835582&cle=0673a76b5f0f4aff7e397dcaf7be486b73aa6e55&file=pdf%2F20190211_nomenclature_ordre_alphabetique_cle7abf9e.pdf
 */
@Service("GPUService")
public class GPUService implements IGPUService {

	@Autowired
	private IAssietteRepository repoAssiette;

	@Autowired
	private IGenerateurRepository repoGenerateur;
	
	@Override
	public List<Assiette> rechercherAssiettesContenantPolygon(String multiPolygon) {
		return repoAssiette.rechercherAssiettesContenantPolygon(multiPolygon);
	}

	@Override
	public List<Generateur> rechercherGenerateur(String partition, String idgen) {
		return repoGenerateur.findByPartitionAndIdgen(partition, idgen);
	}

	@Override
	public List<Generateur> rechercherGenerateur(String partition) {
		return repoGenerateur.findByPartition(partition);
	}

	@Override
	public long rechercherCarteGenerateur(String idGaspar) {
		return repoGenerateur.countByIdGaspar(idGaspar);
	}
}
