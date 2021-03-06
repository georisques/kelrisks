package fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities_write;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "short_url")
public class ShortUrl {
    
    static final long serialVersionUID = 1L;
    
    String url;
    String code;
    @Column(name = "date_maj", columnDefinition = "DATE")
    Date dateMaj = new Date();
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_url")
    @SequenceGenerator(name = "seq_url", sequenceName = "short_url_id_seq", allocationSize = 1)
    private Long id;
    
//    public Long getId() {
//        
//        if (this.id == null) { return 0L; }
//        return this.id;
//    }
}
  