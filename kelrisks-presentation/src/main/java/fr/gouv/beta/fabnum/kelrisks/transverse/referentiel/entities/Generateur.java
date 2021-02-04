package fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "generateur_sup_s")
public class Generateur {
    static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ogc_fid", updatable = false, nullable = false)
    private Long id;
	
    private String partition;
    
    private String idgen;
    
    @Column(name = "id_gaspar")
    private String idGaspar;
    
}
