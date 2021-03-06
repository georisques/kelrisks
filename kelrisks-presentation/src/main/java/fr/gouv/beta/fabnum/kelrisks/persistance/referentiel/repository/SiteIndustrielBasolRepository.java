package fr.gouv.beta.fabnum.kelrisks.persistance.referentiel.repository;

import fr.gouv.beta.fabnum.commun.persistance.IAbstractRepository;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.SiteIndustrielBasol;

import java.util.List;

import org.geolatte.geom.Geometry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Un repository pour SiteIndustrielBasol
 * Cette classe est utilisé par Spring data, elle facilite la réalisation des DAO en diminuant le code à écrire.
 * Chaque DAO fait appel au repository associé pour executer ses requêtes JPA (Hibernate)
 */
public interface SiteIndustrielBasolRepository extends IAbstractRepository<SiteIndustrielBasol> {
    
    @Query(value = "SELECT * " +
                   " FROM kelrisks.basol si, " +
                   "      kelrisks.cadastre p " +
                   " WHERE p.code = :codeParcelle " +
                   "   AND (st_intersects(si.point, p.geog) " +
                   "     OR st_intersects(si.geog, p.geog)) ", nativeQuery = true)
    List<SiteIndustrielBasol> rechercherSiteSurParcelle(@Param("codeParcelle") String codeParcelle);
    
    @Query(value = "SELECT si " +
                   " FROM kelrisks.basol AS si " +
                   " WHERE st_dwithin(si.geog, " +
                   "                         st_centroid((SELECT p.geog " +
                   "                                       FROM kelrisks.cadastre AS p " +
                   "                                       WHERE p.code = :codeParcelle)), " +
                   "                         :distance)", nativeQuery = true)
    List<SiteIndustrielBasol> rechercherSiteDansRayonCentroideParcelle(@Param("codeParcelle") String codeParcelle,
                                                                       @Param("distance") double distance);
    
    @Query(value = "SELECT * " +
                   " FROM kelrisks.basol sib " +
                   " WHERE st_intersects(sib.geog, :multiPolygon) " +
                   "    OR st_intersects(sib.geog, :multiPolygon)", nativeQuery = true)
    List<SiteIndustrielBasol> rechercherSitesDansPolygon(Geometry<?> multiPolygon);
    
    @Query(value = "SELECT * " +
                   " FROM kelrisks.basol sib " +
                   " WHERE st_intersects(sib.geog, st_union(:multiPolygon)) " +
                   "    OR st_intersects(sib.geog, st_union(:multiPolygon))", nativeQuery = true)
    List<SiteIndustrielBasol> rechercherSitesDansPolygons(List<Geometry<?>> multiPolygon);
    
    @Query(value = "SELECT * " +
                   " FROM kelrisks.basol si, " +
                   "      kelrisks.cadastre p " +
                   " WHERE p.code IN :codes" +
                   "   AND st_intersects(si.point, p.geog)", nativeQuery = true)
    List<SiteIndustrielBasol> rechercherSitesSurParcelles(List<String> codes);
}

