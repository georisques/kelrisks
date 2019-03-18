package fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.qo;


import fr.gouv.beta.fabnum.commun.transverse.qo.AbstractQO;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.QAdresse;
import lombok.Data;

import com.querydsl.core.BooleanBuilder;

@Data
public class AdresseQO extends AbstractQO {
    
    private String nomVoie;
    private String numero;
    private String nomVoiePartiel;
    private String idBan;
    
    @Override
    public void feedBuilder(BooleanBuilder builder) {
        
        if (id != null) {builder.and(QAdresse.adresse.id.eq(id));}
        if (idBan != null) {builder.and(QAdresse.adresse.idBAN.eq(idBan));}
        if (nomVoie != null) {builder.and(QAdresse.adresse.nomVoie.equalsIgnoreCase(nomVoie));}
        if (numero != null) {builder.and(QAdresse.adresse.numero.contains(numero));}
        if (nomVoiePartiel != null) {
            BooleanBuilder orBuilder = new BooleanBuilder();
            for (String s : nomVoiePartiel.split(" ")) {
                orBuilder.or(QAdresse.adresse.nomVoie.containsIgnoreCase(s));
            }
            builder.and(orBuilder);
        }
    }
}
  