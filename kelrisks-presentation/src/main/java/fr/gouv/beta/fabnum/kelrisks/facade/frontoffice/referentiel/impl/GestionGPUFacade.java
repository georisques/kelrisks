package fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.gouv.beta.fabnum.commun.facade.AbstractFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionGPUFacade;
import fr.gouv.beta.fabnum.kelrisks.metier.referentiel.interfaces.IGPUService;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.Assiette;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.Generateur;

@Service
public class GestionGPUFacade extends AbstractFacade implements IGestionGPUFacade {

	@Autowired
	IGPUService gpuService;
	
	@Override
	public List<Assiette> rechercherAssiettesContenantPolygon(String multiPolygon) {
		return gpuService.rechercherAssiettesContenantPolygon(multiPolygon);
	}

	@Override
	public List<Generateur> rechercherGenerateur(String partition, String idgen) {
		return gpuService.rechercherGenerateur(partition, idgen);
	}

	@Override
	public List<Generateur> rechercherGenerateur(String partition) {
		return gpuService.rechercherGenerateur(partition);
	}

}
