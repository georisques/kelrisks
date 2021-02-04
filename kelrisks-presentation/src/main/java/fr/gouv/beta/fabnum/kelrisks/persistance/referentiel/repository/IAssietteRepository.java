package fr.gouv.beta.fabnum.kelrisks.persistance.referentiel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.Assiette;


@Repository("iAssietteRepository")
public interface IAssietteRepository extends JpaRepository<Assiette, Long>, JpaSpecificationExecutor<Assiette> {

    @Query(value = "SELECT ogc_fid, partition, idgen, bbox" +
            " FROM kelrisks.assiette_sup_s" +
            " WHERE st_intersects(geog, ST_GeomFromGeoJSON(:multiPolygon))", nativeQuery = true)
	List<Assiette> rechercherAssiettesContenantPolygon(String multiPolygon);
}
