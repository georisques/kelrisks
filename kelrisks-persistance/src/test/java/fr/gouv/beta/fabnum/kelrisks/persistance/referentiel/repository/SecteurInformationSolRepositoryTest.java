package fr.gouv.beta.fabnum.kelrisks.persistance.referentiel.repository;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.Parcelle;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.SecteurInformationSol;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestEntityManager
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SecteurInformationSolRepositoryTest {
    
    //    @Autowired
    //    TestEntityManager                entityManager;
    @Autowired
    SecteurInformationSolRepository secteurInformationSolRepository;
    @Autowired
    ParcelleRepository              parcelleRepository;
    
    @Test
    public void doitRetournerUnSecteurInformationSols() {
    
        Parcelle parcelle = parcelleRepository.getOne(1045474L);
    
        assertThat(parcelle).isNotNull();
    
        List<SecteurInformationSol> SecteurInformationSols = secteurInformationSolRepository.rechercherSecteursDansPolygon(parcelle.getMultiPolygon());
    
        assertThat(SecteurInformationSols).hasSize(1);
    }
    
    @Test
    public void neDoitRetournerAucunSecteurInformationSols() {
    
        Parcelle parcelle = parcelleRepository.getOne(1045760L);
    
        assertThat(parcelle).isNotNull();
    
        List<SecteurInformationSol> SecteurInformationSols = secteurInformationSolRepository.rechercherSecteursDansPolygon(parcelle.getMultiPolygon());
    
        assertThat(SecteurInformationSols).isEmpty();
    }
}