package fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities;

import fr.gouv.beta.fabnum.commun.transverse.entities.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.geolatte.geom.Geometry;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "commune")
public class Commune extends AbstractEntity {
    
    @Column(name = "code_insee")
    String codeINSEE;
    
    @Column(name = "code_postal")
    String codePostal;
    
    @Column(name = "nom_commune")
    String nomCommune;
    
    @Column(name = "bbox", columnDefinition = "geometry")
    private Geometry<?> multiPolygon;
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_commune")
    @SequenceGenerator(name = "seq_commune", sequenceName = "adresse_commune_id_seq", allocationSize = 1)
    private Long id;
    
    public Long getId() {
        
        if (this.id == null) { return 0L; }
        return this.id;
    }
}
