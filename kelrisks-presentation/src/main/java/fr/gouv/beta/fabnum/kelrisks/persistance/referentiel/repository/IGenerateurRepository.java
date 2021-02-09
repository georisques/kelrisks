package fr.gouv.beta.fabnum.kelrisks.persistance.referentiel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.Generateur;

@Repository("iGenerateurRepository")
public interface IGenerateurRepository extends JpaRepository<Generateur, Long>, JpaSpecificationExecutor<Generateur> {

	List<Generateur> findByPartitionAndIdgen(String partition, String idgen);

	List<Generateur> findByPartition(String partition);
	
	long countByIdGaspar(String idGaspar);
}
