package fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities;

import fr.gouv.beta.fabnum.commun.transverse.entities.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "gaspar")
public class PlanPreventionRisquesGaspar extends AbstractEntity {
    
    static final long serialVersionUID = 1L;
    
    @Column(name = "id_gaspar")
    String idGaspar;
    
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "id_ref_alea")
    Alea alea;
    
    @Column(name = "code_insee", columnDefinition = "DATE")
    String codeINSEE;
    
    @Column(name = "date_prescription", columnDefinition = "DATE")
    Date datePrescription;
    
    @Column(name = "date_application_anticipee", columnDefinition = "DATE")
    Date dateApplicationAnticipee;
    
    @Column(name = "date_deprescription", columnDefinition = "DATE")
    Date dateDeprescription;
    
    @Column(name = "date_approbation", columnDefinition = "DATE")
    Date dateApprobation;
    
    @Column(name = "date_abrogation", columnDefinition = "DATE")
    Date dateAbrogation;
    
    @Column(name = "date_annulation", columnDefinition = "DATE")
    Date dateAnnulation;
    
    @Column(name = "is_georisque")
    boolean existsInGeorisque = false;
    
    @Column(name = "is_gpu")
    boolean existsInGpu = false;
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_gaspar")
    @SequenceGenerator(name = "seq_gaspar", sequenceName = "gaspar_id_seq", allocationSize = 1)
    private Long id;
    
    public Long getId() {
        
        if (this.id == null) { return 0L; }
        return this.id;
    }
}
  
