package fr.gouv.beta.fabnum.kelrisks.persistance.referentiel.repository;

import fr.gouv.beta.fabnum.commun.persistance.IAbstractRepository;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.Argile;

import java.util.List;

import org.geolatte.geom.Geometry;
import org.springframework.data.jpa.repository.Query;

/**
 * Un repository pour Argile
 * Cette classe est utilisé par Spring data, elle facilite la réalisation des DAO en diminuant le code à écrire.
 * Chaque DAO fait appel au repository associé pour executer ses requêtes JPA (Hibernate)
 */
public interface ArgileRepository extends IAbstractRepository<Argile> {
    // TODO :
	// http://blog.cleverelephant.ca/2010/08/stintersects-and-stbuffer-no.html
	// ST_DWithin(geog, :multiPolygon, :distance) => serveur bdd sature CPU plus vite = plus d'erreur cote API errial
	// avec st_intersects CPU sature aussi mais j'ai pas d'erreur coté API
	
    @Query(value = "SELECT max(niveau_alea) " +
                   " FROM kelrisks.argile" +
                   " WHERE st_intersects(geog, st_buffer(:multiPolygon, :distance))", nativeQuery = true)
    Integer rechercherNiveauMaximumArgileDansPolygonEtendu(Geometry<?> multiPolygon, double distance);
    
    @Query(value = "SELECT id, bbox, departement, niveau_alea, version " +
                   " FROM kelrisks.argile" +
                   " WHERE st_intersects(geog, st_buffer(:multiPolygon, :distance))", nativeQuery = true)
    List<Argile> rechercherLentillesDansPolygonEtendu(Geometry<?> multiPolygon, double distance);
    
    @Query(value = "SELECT id, bbox, departement, niveau_alea, version  " +
                   " FROM kelrisks.argile" +
                   " WHERE st_intersects(geog, st_buffer((SELECT st_union(:multiPolygons)), :distance))", nativeQuery = true)
    List<Argile> rechercherLentillesDansPolygonsEtendu(List<Geometry<?>> multiPolygons, double distance);
}