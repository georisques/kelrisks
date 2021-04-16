package fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.avis.impl;

import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateSystemAxis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import fr.gouv.beta.fabnum.commun.facade.AbstractFacade;
import fr.gouv.beta.fabnum.commun.utils.GeoJsonUtils;
import fr.gouv.beta.fabnum.kelrisks.facade.avis.AvisDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.avis.AvisDTO.Summary;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.AbstractLocalisationAvecPrecision;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.AleaDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.ArgileDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.CommuneDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.InstallationClasseeDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.InstallationNucleaireDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.ParcelleDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.PlanExpositionBruitDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.PlanPreventionRisquesGasparDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.SecteurInformationSolDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.SiteIndustrielBasiasDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.SiteIndustrielBasolDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.avis.IGestionAvisFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionArgileFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionBRGMFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionCommuneFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionGPUFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionGeoDataGouvFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionGeorisquesFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionInstallationClasseeFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionParcelleFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionPlanExpositionBruitFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionPlanPreventionRisquesGasparFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionSiteIndustrielBasiasFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionSiteIndustrielBasolFacade;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.BRGMPaginatedCanalisation;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.BRGMPaginatedInstallationNuclaire;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedAZI;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedPPR;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedRadon;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedRadon.Radon;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedSIS;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedSismique;
import fr.gouv.beta.fabnum.kelrisks.transverse.apiclient.GeorisquePaginatedTRI;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.Assiette;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities.Generateur;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.enums.PrecisionEnum;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.qo.PlanPreventionRisquesGasparQO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class GestionAvisFacade extends AbstractFacade implements IGestionAvisFacade {
    
    @Autowired
    IGestionSiteIndustrielBasiasFacade        gestionSiteIndustrielBasiasFacade;
    @Autowired
    IGestionSiteIndustrielBasolFacade         gestionSiteIndustrielBasolFacade;
    @Autowired
    IGestionInstallationClasseeFacade         gestionInstallationClasseeFacade;
    @Autowired
    IGestionCommuneFacade                     gestionCommuneFacade;
    @Autowired
    IGestionParcelleFacade                    gestionParcelleFacade;
    @Autowired
    IGestionArgileFacade                      gestionArgileFacade;
    @Autowired
    IGestionGeorisquesFacade                  gestionGeorisquesFacade;
    @Autowired
    IGestionGeoDataGouvFacade                 gestionGeoDataGouvFacade;
    @Autowired
    IGestionGPUFacade                         gestionGPUFacade;
    @Autowired
    IGestionPlanExpositionBruitFacade         gestionPlanExpositionBruitFacade;
    @Autowired
    IGestionPlanPreventionRisquesGasparFacade gestionPlanPreventionRisquesGasparFacade;
    @Autowired
    IGestionBRGMFacade                        gestionBRGMFacade;
    
    
    private List<String> arrondissementsMarseille =
            Stream.of("13201", "13202", "13203", "13204", "13205", "13206", "13207", "13208", "13209", "13210", "13211", "13212", "13213", "13214", "13215", "13216").collect(Collectors.toList());
    private List<String> arrondissementsLyon =
            Stream.of("69381", "69382", "69383", "69384", "69385", "69386", "69387", "69388", "69389").collect(Collectors.toList());
    private List<String> arrondissementsParis =
            Stream.of("75101", "75102", "75103", "75104", "75105", "75106", "75107", "75108", "75109", "75110", "75111", "75112", "75113", "75114", "75115", "75116", "75117", "75118", "75119",
                      "75120").collect(Collectors.toList());    
    
    private static final List<String> PRECISION_CODES = Arrays.asList(PrecisionEnum.BASOL_COMMUNE.getCode(),
            PrecisionEnum.BASOL_RUE.getCode(),
            PrecisionEnum.BASIAS_RUE.getCode(),
            PrecisionEnum.S3IC_COMMUNE.getCode(),
            null);
    
    @Override
    @Cacheable(value = "avis", cacheManager = "cacheManagerAvis", keyGenerator = "customAvisKeyGenerator", unless = "#result.hasError || #result.hasWarning")
    public AvisDTO rendreAvis(List<ParcelleDTO> parcelleDTOs, CommuneDTO communeDTO, @NotNull String nomAdresse, boolean isIGNRequested, boolean isPPRRequested) {
        
        AvisDTO avisDTO = new AvisDTO();
        
        avisDTO.getSummary().setCommune(communeDTO);
        avisDTO.getSummary().setAdresse(nomAdresse);
        
        return getAvisFromParcelle(avisDTO, parcelleDTOs, communeDTO, isIGNRequested, isPPRRequested);
    }
    
    @Override
    public AvisDTO rendreAvis(String geoJson) {
    
        AvisDTO avisDTO = new AvisDTO();
    
        Geometry<?> geometry = GeoJsonUtils.fromGeoJson(geoJson);
    
        ParcelleDTO parcelleDTO = new ParcelleDTO();
        parcelleDTO.setMultiPolygon(geometry);
        parcelleDTO.setEwkt(GeoJsonUtils.toGeoJson(geometry));
    
        Point<?> centroid = (Point<?>) gestionParcelleFacade.rechercherCentroidParcelle(geometry);
        Position ptCentroid = ((Point) centroid).getPosition();
        CommuneDTO communeDTO = gestionGeoDataGouvFacade.rechercherCommune(Double.toString(ptCentroid.getCoordinate(CoordinateSystemAxis.mkLatAxis())),
                                                                           Double.toString(ptCentroid.getCoordinate(CoordinateSystemAxis.mkLonAxis())));
    
        avisDTO.getSummary().setCommune(communeDTO);
    
        return getAvisFromParcelle(avisDTO, Collections.singletonList(parcelleDTO), communeDTO, true, true);
    }
    
    private AvisDTO getAvisFromParcelle(AvisDTO avisDTO, List<ParcelleDTO> parcelleDTOs, CommuneDTO communeDTO, boolean isIGNRequested, boolean isPPRRequested) {
    
        try {
        
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
            log.info("------------- " + simpleDateFormat.format(new Date()) + " - " + parcelleDTOs.stream().map(parcelleDTO -> parcelleDTO.getPrefixe() + parcelleDTO.getSection() + parcelleDTO.getNumero()).collect(Collectors.joining(", ")) + " @ " + communeDTO.getNomCommune() + " - " + communeDTO.getCodePostal() + " (" + communeDTO.getCodeINSEE() + ") -------------");
            long startTime = System.currentTimeMillis();
            Geometry<?> parcellesUnion = gestionParcelleFacade.rechercherUnionParcelles(parcelleDTOs.stream()
                                                                                                .map(ParcelleDTO::getId)
                                                                                                .collect(Collectors.toList()));
            log.info((System.currentTimeMillis() - startTime) + " => " + "rechercherUnionParcelles");
            startTime = System.currentTimeMillis();
            Geometry<?> touchesParcelle = gestionParcelleFacade.rechercherUnionParcellesContigues(parcellesUnion);
            log.info((System.currentTimeMillis() - startTime) + " => " + "rechercherUnionParcellesContigues");
            startTime = System.currentTimeMillis();
            Geometry<?> expendedParcelle = gestionParcelleFacade.rechercherExpendedParcelle(parcellesUnion, 500);
            log.info((System.currentTimeMillis() - startTime) + " => " + "rechercherExpendedParcelle");
            startTime = System.currentTimeMillis();
            Point<?> centroid = (Point<?>) gestionParcelleFacade.rechercherCentroidParcelle(parcellesUnion);
            Position ptCentroid = centroid.getPositionN(0);
            double longitudeCentroid = ptCentroid.getCoordinate(CoordinateSystemAxis.mkLonAxis());
            double latitudeCentroid = ptCentroid.getCoordinate(CoordinateSystemAxis.mkLatAxis());
            
            log.info((System.currentTimeMillis() - startTime) + " => " + "rechercherCentroidParcelle (" +
            		longitudeCentroid + ", " +
            		latitudeCentroid+ ")");
            startTime = System.currentTimeMillis();
        
            avisDTO.getLeaflet().setCenter(new AvisDTO.Leaflet.Point(Double.toString(longitudeCentroid),
                                                                     Double.toString(latitudeCentroid)));
        
            avisDTO.getSummary().setCodeParcelle(parcelleDTOs.stream()
                                                         .map(parcelleDTO -> parcelleDTO.getPrefixe() + "-" + parcelleDTO.getSection() + "-" + parcelleDTO.getNumero())
                                                         .collect(Collectors.joining(", ")));
            avisDTO.getLeaflet().setParcelles(parcelleDTOs.stream()
                                                      .map(parcelleDTO -> GeoJsonUtils.toGeoJson(parcelleDTO.getMultiPolygon(),
                                                                                                 Stream.of(new SimpleEntry<>("parcelle", parcelleDTO.getPrefixe() + "-" + parcelleDTO.getSection() + "-" + parcelleDTO.getNumero()),
                                                                                                		   new SimpleEntry<>("codeINSEE", parcelleDTO.getCommune()))
                                                                                                         .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))))
                                                      .collect(Collectors.toList()));
        
            List<Geometry<?>> parcelleSitesSolsPolues = new ArrayList<>(); // TODO : Renommer la variable devenue inutile
            parcelleSitesSolsPolues.add(parcellesUnion);
        
            log.info((System.currentTimeMillis() - startTime) + " => " + "rechercherZoneContenantParcelle");
            startTime = System.currentTimeMillis();
        
            getAvisBasias(avisDTO, parcellesUnion, parcelleSitesSolsPolues, touchesParcelle, expendedParcelle, communeDTO.getCodeINSEE());
            log.info((System.currentTimeMillis() - startTime) + " => " + "getAvisBasias");
            startTime = System.currentTimeMillis();
        
            getAvisBasol(avisDTO, parcelleSitesSolsPolues, touchesParcelle, expendedParcelle, communeDTO.getCodeINSEE());
            log.info((System.currentTimeMillis() - startTime) + " => " + "getAvisBasol");
            startTime = System.currentTimeMillis();
        
            getAvisICPE(avisDTO, parcelleSitesSolsPolues, touchesParcelle, expendedParcelle, communeDTO.getCodeINSEE());
            log.info((System.currentTimeMillis() - startTime) + " => " + "getAvisICPE");
            startTime = System.currentTimeMillis();
        
            getAvisSis(avisDTO, longitudeCentroid, latitudeCentroid);
            log.info((System.currentTimeMillis() - startTime) + " => " + "getAvisSis");
            startTime = System.currentTimeMillis();
        
            getAvisPPR(avisDTO, parcelleSitesSolsPolues, longitudeCentroid, latitudeCentroid, communeDTO.getCodeINSEE(), isIGNRequested, isPPRRequested);
            log.info((System.currentTimeMillis() - startTime) + " => " + "getAvisPPR");
            startTime = System.currentTimeMillis();
        
            getAvisAZI(avisDTO, communeDTO.getCodeINSEE());
            log.info((System.currentTimeMillis() - startTime) + " => " + "getAvisAZI");
            startTime = System.currentTimeMillis();
        
            getAvisTRI(avisDTO, communeDTO.getCodeINSEE());
            log.info((System.currentTimeMillis() - startTime) + " => " + "getAvisTRI");
            startTime = System.currentTimeMillis();
        
            getAvisArgile(avisDTO, parcellesUnion);
            log.info((System.currentTimeMillis() - startTime) + " => " + "getAvisArgile");
            startTime = System.currentTimeMillis();
        
            getAvisSismicite(avisDTO, communeDTO);
            log.info((System.currentTimeMillis() - startTime) + " => " + "getAvisSismicite");
            startTime = System.currentTimeMillis();
        
            getAvisRadon(avisDTO, communeDTO);
            log.info((System.currentTimeMillis() - startTime) + " => " + "getAvisRadon");
            startTime = System.currentTimeMillis();
        
            getAvisCanalisations(avisDTO, longitudeCentroid, latitudeCentroid);
            log.info((System.currentTimeMillis() - startTime) + " => " + "getAvisCanalisations");
            startTime = System.currentTimeMillis();
        
            getAvisPlanExpositionBruit(avisDTO, centroid);
            log.info((System.currentTimeMillis() - startTime) + " => " + "getAvisPlanExpositionBruit");
            startTime = System.currentTimeMillis();
        
            getAvisInstallationsNucleaires(avisDTO, longitudeCentroid, latitudeCentroid);
            log.info((System.currentTimeMillis() - startTime) + " => " + "getAvisInstallationsNucleaires");
        
            return avisDTO;
        }
        catch (Exception e) {
        	String infoParcelle = "";
        	Summary summary = avisDTO.getSummary();
        	if(summary != null) {
        		infoParcelle = communeDTO.getCodeINSEE() + " - " + summary.getCodeParcelle();
        	}
        	
        	log.error("Fail de to create avis for: {}", infoParcelle, e);
            AvisDTO avis = new AvisDTO();
            avis.addError("Un fournisseur de données n'est pas joignable pour le moment ou a rencontré une erreur. Merci de réessayer ultérieurement.");
            return avis;
        }
    }
    
    private void getAvisSis(AvisDTO avisDTO, double longitudeCentroid, double latitudeCentroid) {
        
        GeorisquePaginatedSIS georisquePaginatedSisParcelle = gestionGeorisquesFacade.rechercherSisCoordonnees(longitudeCentroid,
        		latitudeCentroid);
        GeorisquePaginatedSIS georisquePaginatedSisRayonParcelle = gestionGeorisquesFacade.rechercherSisCoordonneesRayon(longitudeCentroid,
        		latitudeCentroid, 100);
        
        if (!georisquePaginatedSisParcelle.getData().isEmpty()) {
    
            SecteurInformationSolDTO secteurInformationSolDTO = new SecteurInformationSolDTO();
    
            secteurInformationSolDTO.setId(georisquePaginatedSisParcelle.getData().get(0).getId_sis());
            secteurInformationSolDTO.setNom(georisquePaginatedSisParcelle.getData().get(0).getNom());
            secteurInformationSolDTO.setFicheRisque(georisquePaginatedSisParcelle.getData().get(0).getFiche_risque());
    
            avisDTO.getSecteurInformationSolSurParcelleDTOs().add(secteurInformationSolDTO);
        }
        
        if (!georisquePaginatedSisRayonParcelle.getData().isEmpty()) {
            georisquePaginatedSisParcelle.getData().forEach(secteurInformationSols -> {
    
                avisDTO.getLeaflet().getSis().add(GeoJsonUtils.toGeoJson(secteurInformationSols.getGeom(),
                                                                         Stream.of(new SimpleEntry<>("Nom", secteurInformationSols.getNom()))
                                                                                 .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
    
                SecteurInformationSolDTO secteurInformationSolDTO = new SecteurInformationSolDTO();
                secteurInformationSolDTO.setId(secteurInformationSols.getId_sis());
                secteurInformationSolDTO.setNom(secteurInformationSols.getNom());
                secteurInformationSolDTO.setFicheRisque(secteurInformationSols.getFiche_risque());
    
                avisDTO.getSecteurInformationSolRayonParcelleDTOs().add(secteurInformationSolDTO);
            });
        }
    }
    
    private void getAvisPPR(AvisDTO avisDTO, List<Geometry<?>> parcelleSitesSolsPolues, double longitudeCentroid, double latitudeCentroid, String codeINSEE, boolean isIGNRequested, boolean isPPRRequested) {
        
        long startTime = System.currentTimeMillis();
        
        String parcelleSitesSolsPoluesGeoJson = GeoJsonUtils.getGeometryFromGeoJson(GeoJsonUtils.toGeoJson(parcelleSitesSolsPolues));
        
        List<PlanPreventionRisquesGasparDTO> gaspars = rechercherPprsGaspar(codeINSEE);
        String codeArrondissement = getEquivalenceInseeArrondissmentCommune(codeINSEE);
        if(!codeArrondissement.equals(codeINSEE)) {
        	gaspars.addAll(rechercherPprsGaspar(codeArrondissement));
        }
        List<PlanPreventionRisquesGasparDTO> planPreventionRisquesList = removeDuplicatePPR(gaspars);
        
        if(!planPreventionRisquesList.isEmpty()) {
            // 1) Verifier si il existe une carte dans le GPU ou dans Georisques
            List<String> idGasparGeorisques = new ArrayList<>();

            try {
                GeorisquePaginatedPPR pprGeorisques = gestionGeorisquesFacade.rechercherPprCommune(codeINSEE);
                // pour trouver les PPR dans Georisques
                if(pprGeorisques != null && !pprGeorisques.getData().isEmpty()) {
                	idGasparGeorisques = pprGeorisques.getData().stream().map(ppr -> ppr.getId_gaspar()).collect(Collectors.toList());
                }
            } catch (Exception e) {
				log.error("Fail to rechercherPprCommune", e);
			} 

            rechercheCartesPPR(planPreventionRisquesList, idGasparGeorisques);
            
            // 2) Recherche spatiale à la parcelle sur GPU et Georisques
            List<Assiette> assiettes = null;
            if(isIGNRequested) {
            	try {
                    startTime = System.currentTimeMillis();
                    assiettes = gestionGPUFacade.rechercherAssiettesContenantPolygon(parcelleSitesSolsPoluesGeoJson);
                    log.info("time-rechercherAssiettesContenantPolygon: {}", (System.currentTimeMillis() - startTime));
            	} catch (Exception e) {
            		log.error("Fail to rechercherAssiettesContenantPolygon", e);
    			}
            }
           
            // dans le cas ou le service de l'IGN a une reponse vide avec un code 200, les assiettes sont pas nulles mais les features oui.
            if(assiettes != null) {
                try {
                	// suppression des doublons et filtre pour ne conserver que PM1 et PM3
                	// cf mail de l'IGN :
                	// filtrer les résultats issus de la recherche d’assiettes de façon à n’envoyer des requêtes
                	// que sur les générateurs concerné par l’API Carto (PM1 et PM3)
                	Set<String> nameSet = new HashSet<>();
                	List<Assiette> featuresDistinctByPartition = assiettes.stream()
                	            .filter(e -> nameSet.add(e.getPartition()))
                	            .collect(Collectors.toList());
                	
                	// Ajout des geom du GPU au PPR
                    for (Assiette assiette : featuresDistinctByPartition) {
                        getGenerateurs(planPreventionRisquesList, assiette);
                    }
                }
                catch (TimeoutException e) {
            		log.error("Fail to getGenerateurs", e);
                }
            }
                  
            GeorisquePaginatedPPR georisquePaginatedPPR = null;
            // TODO : si tous les ppr gaspar on deja une geom, pas besoin d'appeler l'API Géorisques PPR
            if(isPPRRequested && CollectionUtils.isNotEmpty(idGasparGeorisques)) {
                startTime = System.currentTimeMillis();
                try {
					georisquePaginatedPPR = gestionGeorisquesFacade.rechercherPprCoordonnees(longitudeCentroid,
							latitudeCentroid);
				} catch (Exception e) {
            		log.error("Fail to rechercherPprCoordonnees", e);
				}
                 log.info("time-rechercherPprCoordonnees: {}", (System.currentTimeMillis() - startTime));
                 
         		if (georisquePaginatedPPR != null && !georisquePaginatedPPR.getData().isEmpty()) {
         	        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);

                    georisquePaginatedPPR.getData().forEach(geoRisquesPPR -> {
                    	planPreventionRisquesList.forEach(ppr -> {
                    		// si le PPR n'a pas deja les geom du GPU
                    		// Rue Basse Mouillere, orleans
                    		if(ppr.getAssiettes().isEmpty() && ppr.getIdGaspar().equals(geoRisquesPPR.getId_gaspar())) {
                    			ppr.setExistsInGeorisque(true);
                    			setGeomPPR(sdf, ppr, geoRisquesPPR.getGeom_perimetre());
                    		}
                    	});
                    });
               } 
            }
            
            // 3) supprimer les PPR qui on une carte mais dont la parcelle n'est pas dessus
            planPreventionRisquesList.removeIf(ppr -> (ppr.isExistsCarte() && (!ppr.isExistsInGpu() && !ppr.isExistsInGeorisque())));        	
        }

        avisDTO.setPlanPreventionRisquesDTOs(planPreventionRisquesList);
    }
    
    private List<PlanPreventionRisquesGasparDTO> rechercherPprsGaspar(String codeInsee) {
        
        PlanPreventionRisquesGasparQO planPreventionRisquesGasparQO = new PlanPreventionRisquesGasparQO();
        planPreventionRisquesGasparQO.setCodeINSEE(codeInsee);
        planPreventionRisquesGasparQO.setAnnuleOuAbroge(false);

        return gestionPlanPreventionRisquesGasparFacade.rechercherAvecCritere(planPreventionRisquesGasparQO);
    }
    
    private void getGenerateurs(List<PlanPreventionRisquesGasparDTO> planPreventionRisquesList, Assiette assiette) throws TimeoutException {
    	List<Generateur> generateurs = null;
    	try {
        	long startTime = System.currentTimeMillis();
            generateurs = gestionGPUFacade.rechercherGenerateur(assiette.getPartition());
            log.info("time-rechercherGenerateur: {}", (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
    		log.error("Fail to rechercherGenerateur",e);
		}


        if (generateurs == null) { throw new TimeoutException("timeout-generateurs-null"); }
        
        // Sécurisation de la jointure assiette / générateur qui ne peut être faite via l'API GpU
        generateurs.removeIf(generateur -> !generateur.getIdgen().equalsIgnoreCase(assiette.getIdgen()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);

        // Ajout la geometrie des assiettes au PPR
        generateurs.forEach(generateur -> {
        	planPreventionRisquesList.forEach(ppr -> {
        		if(ppr.getIdGaspar().equals(generateur.getIdGaspar())) {
        			ppr.setExistsInGpu(true);
        			ppr.setIdAssietteErrial(assiette.getIdgen()+"_"+assiette.getPartition());
        			setGeomPPR(sdf, ppr, assiette.getMultiPolygon());
        		}
        	});
        });
    }
    
    private void setGeomPPR( SimpleDateFormat sdf, PlanPreventionRisquesGasparDTO ppr, Geometry<?> geom) {
    	Map<String, Object> properties = Stream.of(new SimpleEntry<>("'PPR'", ppr.getAlea().getFamilleAlea().getLibelle()),
                new SimpleEntry<>("prescritLe", ppr.getDateDeprescription() != null ? sdf.format(ppr.getDateDeprescription()) : "n/a"),
                new SimpleEntry<>("approuvéLe", ppr.getDateApprobation() != null ? sdf.format(ppr.getDateApprobation()) : "n/a"))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	    ppr.getAssiettes().add(GeoJsonUtils.toGeoJson(geom, properties));
    }
    
    private void getAvisCanalisations(AvisDTO avisDTO, double longitudeCentroid, double latitudeCentroid) {
        
        BRGMPaginatedCanalisation brgmPaginatedCanalisation =
                gestionBRGMFacade.rechercherCanalisationsCoordonnees(String.valueOf(longitudeCentroid),
                                                                     String.valueOf(latitudeCentroid),
                                                                     500);
    
        if (brgmPaginatedCanalisation != null) {
            // can't return the geom from security rules, return just the bbox
        	brgmPaginatedCanalisation.getFeatures().forEach(canalisation -> avisDTO.getGeogCanalisations().add(GeoJsonUtils.convertToBBOX(canalisation.getGeometry())));
        }
    }
    
    private void getAvisInstallationsNucleaires(AvisDTO avisDTO, double longitudeCentroid, double latitudeCentroid) {
        
        List<InstallationNucleaireDTO> installationNucleaireDTOS = new ArrayList<>();
        
        installationNucleaireDTOS.addAll(getCentralesNucleaires(longitudeCentroid, latitudeCentroid));
        avisDTO.setHasCentraleNucleaire(!installationNucleaireDTOS.isEmpty());
        
        installationNucleaireDTOS.addAll(getInstallationsNucleaires(longitudeCentroid, latitudeCentroid));
        
        avisDTO.setInstallationNucleaireDTOS(installationNucleaireDTOS);
    }
    
    private List<InstallationNucleaireDTO> getCentralesNucleaires(double longitudeCentroid, double latitudeCentroid) {
        
        List<InstallationNucleaireDTO> installationNucleaireDTOS = new ArrayList<>();
        
        BRGMPaginatedInstallationNuclaire brgmPaginatedInstallationNuclaire20 =
                gestionBRGMFacade.rechercherInstallationsNucleairesCoordonnees(String.valueOf(longitudeCentroid),
                                                                               String.valueOf(latitudeCentroid),
                                                                               20000);
        
        if (brgmPaginatedInstallationNuclaire20 != null) {
            
            List<BRGMPaginatedInstallationNuclaire.InstallationNucleaire> installationsNucleaires = brgmPaginatedInstallationNuclaire20.getFeatures();
            
            installationsNucleaires.removeIf(installationNucleaire -> !installationNucleaire.getProperties().getType_leg().toLowerCase().contains("centrale"));
            
            installationsNucleaires.forEach(centralesNucleaires -> {
                
                InstallationNucleaireDTO installationNucleaireDTO = new InstallationNucleaireDTO();
                installationNucleaireDTO.setNomInstallation(centralesNucleaires.getProperties().getNom_inst());
                installationNucleaireDTO.setLibCommune(centralesNucleaires.getProperties().getNom_com());
                installationNucleaireDTO.setCentrale(true);
                installationNucleaireDTOS.add(installationNucleaireDTO);
            });
        }
        
        return installationNucleaireDTOS;
    }
    
    private List<InstallationNucleaireDTO> getInstallationsNucleaires(double longitudeCentroid, double latitudeCentroid) {
        
        List<InstallationNucleaireDTO> installationNucleaireDTOS = new ArrayList<>();
        
        BRGMPaginatedInstallationNuclaire brgmPaginatedInstallationNuclaire10 =
                gestionBRGMFacade.rechercherInstallationsNucleairesCoordonnees(String.valueOf(longitudeCentroid),
                                                                               String.valueOf(latitudeCentroid),
                                                                               10000);
        
        if (brgmPaginatedInstallationNuclaire10 != null) {
            
            List<BRGMPaginatedInstallationNuclaire.InstallationNucleaire> installationsNucleaires = brgmPaginatedInstallationNuclaire10.getFeatures();
            
            installationsNucleaires.forEach(installationNucleaire -> {
                
                InstallationNucleaireDTO installationNucleaireDTO = new InstallationNucleaireDTO();
                installationNucleaireDTO.setNomInstallation(installationNucleaire.getProperties().getNom_inst());
                installationNucleaireDTO.setLibCommune(installationNucleaire.getProperties().getNom_com());
                installationNucleaireDTO.setCentrale(false);
                installationNucleaireDTOS.add(installationNucleaireDTO);
            });
        }
        
        return installationNucleaireDTOS;
    }
          
    private void getAvisSismicite(AvisDTO avisDTO, CommuneDTO commune) {
        
        //TODO : Dans la mesure ou le front bouge encore beaucoup, on ne prend pour l'instant que 10 communes pour le pas multiplier inutilement le nombre d'appels à l'API
        String listeCodesINSEE = commune.getCodeINSEE() + "," + commune.getCommunesLimitrophes().stream().limit(9).map(CommuneDTO::getCodeINSEE).collect(Collectors.joining(","));
        
        GeorisquePaginatedSismique georisquePaginatedSismique = gestionGeorisquesFacade.rechercherSismiciteCommune(listeCodesINSEE);
        
        if (!georisquePaginatedSismique.getData().isEmpty()) {
            avisDTO.getSummary().getCommune().setCodeZoneSismicite(georisquePaginatedSismique.getData().stream()
                                                                           .filter(zs -> zs.getCode_insee().equals(commune.getCodeINSEE()))
                                                                           .findFirst().get().getCode_zone());
            
            avisDTO.getSummary().getCommune().getCommunesLimitrophes()
                    .forEach(communeDTO -> communeDTO.setCodeZoneSismicite(georisquePaginatedSismique.getData().stream()
                                                                                   .filter(zs -> zs.getCode_insee().equals(commune.getCodeINSEE()))
                                                                                   .findFirst().get().getCode_zone()));
        }
    }
    
    private void getAvisArgile(AvisDTO avisDTO, Geometry<?> parcelle) {
    
        long startTime = System.currentTimeMillis();
    
        int niveau = gestionArgileFacade.rechercherNiveauMaximumArgileDansPolygonEtendu(parcelle, 50);
        log.info(" V " + (System.currentTimeMillis() - startTime) + " => " + "gestionArgileFacade.rechercherNiveauMaximumArgileDansPolygonEtendu");
        startTime = System.currentTimeMillis();
        List<ArgileDTO> argileDTOs = gestionArgileFacade.rechercherLentillesDansPolygonEtendu(parcelle, 2000);
        log.info(" V " + (System.currentTimeMillis() - startTime) + " => " + "gestionArgileFacade.rechercherLentillesDansPolygonEtendu");
        startTime = System.currentTimeMillis();
    
        avisDTO.setNiveauArgile(niveau);
        avisDTO.setLentillesArgile(argileDTOs);
    }
    
    private void getAvisRadon(AvisDTO avisDTO, CommuneDTO commune) {
        
        //TODO : Dans la mesure ou le front bouge encore beaucoup, on ne prend pour l'instant que 10 communes pour le pas multiplier inutilement le nombre d'appels à l'API
        String listeCodesINSEE = commune.getCodeINSEE() + "," + commune.getCommunesLimitrophes().stream().limit(9).map(CommuneDTO::getCodeINSEE).collect(Collectors.joining(","));
        
        GeorisquePaginatedRadon georisquePaginatedRadon = gestionGeorisquesFacade.rechercherRadonCommune(listeCodesINSEE);
        
        if (!georisquePaginatedRadon.getData().isEmpty()) {
        	// il peut y avoir plusieurs fois la meme commune avec des valeur de radon differente, il faut prendre la plus forte
            // cf https://gitlab.brgm.fr/brgm/georisques/georisques-ministere/-/issues/1142
        	georisquePaginatedRadon.getData().stream()
        			.filter(zs -> zs.getCode_insee().equals(commune.getCodeINSEE()))
                    .sorted(Comparator.comparing(Radon::getClassePotentielAsInt)
                            .reversed()).findFirst().ifPresent(c -> avisDTO.getSummary().getCommune().setClassePotentielRadon(c.getClasse_potentiel()));
        	            
			avisDTO.getSummary().getCommune().getCommunesLimitrophes()
					.forEach(communeDTO -> georisquePaginatedRadon.getData().stream()
							.filter(zs -> zs.getCode_insee().equals(communeDTO.getCodeINSEE())).findFirst()
							.ifPresent(c -> communeDTO.setClassePotentielRadon(c.getClasse_potentiel())));
        }
    }
    
    private void getAvisTRI(AvisDTO avisDTO, String codeINSEE) {
        
        GeorisquePaginatedTRI georisquePaginatedRadon = gestionGeorisquesFacade.rechercherTRICommune(codeINSEE);
        
        if (!georisquePaginatedRadon.getData().isEmpty()) {
            avisDTO.setTRIs(georisquePaginatedRadon.getData());
        }
    }
    
    private void getAvisAZI(AvisDTO avisDTO, String codeINSEE) {
        
        GeorisquePaginatedAZI georisquePaginatedRadon = gestionGeorisquesFacade.rechercherAZICommune(codeINSEE);
        
        if (!georisquePaginatedRadon.getData().isEmpty()) {
            avisDTO.setAZIs(georisquePaginatedRadon.getData());
        }
    }
    
    private void getAvisPlanExpositionBruit(AvisDTO avisDTO, Point<?> centroid) {
        
        String zone = gestionPlanExpositionBruitFacade.rechercherZoneCentroid(centroid);
        avisDTO.setZonePlanExpositionBruit(zone);
        
        List<PlanExpositionBruitDTO> plansExposition = gestionPlanExpositionBruitFacade.rechercherPlanExpositionBruitDansRayon(centroid, 1000d);
        avisDTO.setPlansExpositionBruit(plansExposition);
    }
    
    private void getAvisICPE(AvisDTO avisDTO, List<Geometry<?>> parcelleSitesSolsPolues, Geometry<?> touchesParcelle, Geometry<?> expendedParcelle, String codeINSEE) {
        
        avisDTO.setInstallationClasseeSurParcelleDTOs((List<InstallationClasseeDTO>) removeLowPrecision(gestionInstallationClasseeFacade.rechercherInstallationsDansPolygons(parcelleSitesSolsPolues)));
        avisDTO.setInstallationClasseeProximiteParcelleDTOs((List<InstallationClasseeDTO>) removeLowPrecision(gestionInstallationClasseeFacade.rechercherSitesDansPolygon(touchesParcelle)));
        avisDTO.setInstallationClasseeRayonParcelleDTOs((List<InstallationClasseeDTO>) removeLowPrecision(gestionInstallationClasseeFacade.rechercherSitesDansPolygon(expendedParcelle)));
        
        avisDTO.getInstallationClasseeRayonParcelleDTOs().forEach(icpe -> avisDTO.getLeaflet().getIcpe().add(icpe.getEwkt()));
        
        avisDTO.getInstallationClasseeProximiteParcelleDTOs().removeAll(avisDTO.getInstallationClasseeSurParcelleDTOs());
        
        avisDTO.getInstallationClasseeRayonParcelleDTOs().removeAll(avisDTO.getInstallationClasseeSurParcelleDTOs());
        avisDTO.getInstallationClasseeRayonParcelleDTOs().removeAll(avisDTO.getInstallationClasseeProximiteParcelleDTOs());
        
        avisDTO.setInstallationClasseeNonGeorerenceesDTOs(gestionInstallationClasseeFacade.rechercherInstallationsAvecFaiblePrecisionDeGeolocalisation(codeINSEE));
    }
    
    private void getAvisBasol(AvisDTO avisDTO, List<Geometry<?>> parcelleSitesSolsPolues, Geometry<?> touchesParcelle, Geometry<?> expendedParcelle, String codeINSEE) {
        
        avisDTO.setSiteIndustrielBasolSurParcelleDTOs((List<SiteIndustrielBasolDTO>) removeLowPrecision(gestionSiteIndustrielBasolFacade.rechercherSitesDansPolygons(parcelleSitesSolsPolues)));
        avisDTO.setSiteIndustrielBasolProximiteParcelleDTOs((List<SiteIndustrielBasolDTO>) removeLowPrecision(gestionSiteIndustrielBasolFacade.rechercherSitesDansPolygon(touchesParcelle)));
        avisDTO.setSiteIndustrielBasolRayonParcelleDTOs((List<SiteIndustrielBasolDTO>) removeLowPrecision(gestionSiteIndustrielBasolFacade.rechercherSitesDansPolygon(expendedParcelle)));
        
        avisDTO.getSiteIndustrielBasolRayonParcelleDTOs().forEach(sib -> avisDTO.getLeaflet().getBasol().add(sib.getEwkt()));
        
        avisDTO.getSiteIndustrielBasolProximiteParcelleDTOs().removeAll(avisDTO.getSiteIndustrielBasolSurParcelleDTOs());
        
        avisDTO.getSiteIndustrielBasolRayonParcelleDTOs().removeAll(avisDTO.getSiteIndustrielBasolSurParcelleDTOs());
        avisDTO.getSiteIndustrielBasolRayonParcelleDTOs().removeAll(avisDTO.getSiteIndustrielBasolProximiteParcelleDTOs());
        
        avisDTO.setSiteIndustrielBasolNonGeorerenceesDTOs(gestionSiteIndustrielBasolFacade.rechercherSitesAvecFaiblePrecisionDeGeolocalisation(codeINSEE));
    }
    
    private void getAvisBasias(AvisDTO avisDTO, Geometry<?> parcelle, List<Geometry<?>> parcelleSitesSolsPolues, Geometry<?> touchesParcelle, Geometry<?> expendedParcelle,
                               String codeINSEE) {
    
        long startTime = System.currentTimeMillis();
    
        avisDTO.setSiteIndustrielBasiasSurParcelleDTOs((List<SiteIndustrielBasiasDTO>) removeLowPrecision(gestionSiteIndustrielBasiasFacade.rechercherSitesDansPolygons(parcelleSitesSolsPolues)));
        log.info(" V " + (System.currentTimeMillis() - startTime) + " => " + "gestionSiteIndustrielBasiasFacade.rechercherSitesDansPolygons");
        startTime = System.currentTimeMillis();
        avisDTO.setSiteIndustrielBasiasProximiteParcelleDTOs((List<SiteIndustrielBasiasDTO>) removeLowPrecision(gestionSiteIndustrielBasiasFacade.rechercherSitesDansPolygon(touchesParcelle)));
        log.info(" V " + (System.currentTimeMillis() - startTime) + " => " + "gestionSiteIndustrielBasiasFacade.rechercherSitesDansPolygon");
        startTime = System.currentTimeMillis();
        avisDTO.setSiteIndustrielBasiasRayonParcelleDTOs((List<SiteIndustrielBasiasDTO>) removeLowPrecision(gestionSiteIndustrielBasiasFacade.rechercherSitesDansPolygon(expendedParcelle)));
        log.info(" V " + (System.currentTimeMillis() - startTime) + " => " + "gestionSiteIndustrielBasiasFacade.rechercherSitesDansPolygon");
        startTime = System.currentTimeMillis();
    
        avisDTO.getSiteIndustrielBasiasRayonParcelleDTOs().forEach(sib -> avisDTO.getLeaflet().getBasias().add(sib.getEwkt()));
    
        avisDTO.getSiteIndustrielBasiasProximiteParcelleDTOs().removeAll(avisDTO.getSiteIndustrielBasiasSurParcelleDTOs());
    
        avisDTO.getSiteIndustrielBasiasRayonParcelleDTOs().removeAll(avisDTO.getSiteIndustrielBasiasSurParcelleDTOs());
        avisDTO.getSiteIndustrielBasiasRayonParcelleDTOs().removeAll(avisDTO.getSiteIndustrielBasiasProximiteParcelleDTOs());
    
        avisDTO.getSiteIndustrielBasiasParRaisonSocialeDTOs().removeAll(avisDTO.getSiteIndustrielBasiasSurParcelleDTOs());
        avisDTO.getSiteIndustrielBasiasParRaisonSocialeDTOs().removeAll(avisDTO.getSiteIndustrielBasiasProximiteParcelleDTOs());
        avisDTO.getSiteIndustrielBasiasParRaisonSocialeDTOs().removeAll(avisDTO.getSiteIndustrielBasiasRayonParcelleDTOs());
    
        avisDTO.setSiteIndustrielBasiasNonGeorerenceesDTOs(gestionSiteIndustrielBasiasFacade.rechercherSitesAvecFaiblePrecisionDeGeolocalisation(codeINSEE));
        log.info(" V " + (System.currentTimeMillis() - startTime) + " => " + "gestionSiteIndustrielBasiasFacade.rechercherSitesAvecFaiblePrecisionDeGeolocalisation");
    }
    
    private List<? extends AbstractLocalisationAvecPrecision> removeLowPrecision(List<? extends AbstractLocalisationAvecPrecision> sites) {
        sites.removeIf(site -> PRECISION_CODES.contains(site.getPrecision()));
    
        return sites;
    }
    
    /**
     * Pour ne pas afficher de "doublon" de PPR
     * COLMESNIL MANNEVILLE | 76550 | A-231 |
     * Cf https://gitlab.brgm.fr/brgm/georisques/georisques-ministere/-/issues/1130
     * @param planPreventionRisquesList
     */
	public List<PlanPreventionRisquesGasparDTO> removeDuplicatePPR(List<PlanPreventionRisquesGasparDTO> allPPRs) {
		List<PlanPreventionRisquesGasparDTO> filterPPRs = new ArrayList<>();
		List<String> idPPRs = new ArrayList<>();

		if (allPPRs != null) {
			for (PlanPreventionRisquesGasparDTO ppr : allPPRs) {
				AleaDTO alea = ppr.getAlea();
				String codeFamille = "";
				// en theorie pas de PPR sans alea
				if(alea != null && alea.getFamilleAlea() != null) {
					codeFamille = alea.getFamilleAlea().getCode();
				}
				
				String idPPR = ppr.getCodeINSEE() + "_" + ppr.getIdGaspar() + "_" + codeFamille;
				if (!idPPRs.contains(idPPR)) {
					// ne tient plus compte des infos boolean de la base car on ne sait pas remplir correctement les valeurs...
					ppr.setExistsInGeorisque(false);
					ppr.setExistsInGpu(false);
					filterPPRs.add(ppr);
					idPPRs.add(idPPR);
				}
			}
		}

		return filterPPRs;
	}
	
	public void rechercheCartesPPR(List<PlanPreventionRisquesGasparDTO> pprs, List<String> idGasparGeorisques) {
		for(PlanPreventionRisquesGasparDTO ppr : pprs) {
			String idGaspar = ppr.getIdGaspar();
			long nbCarteGeneration = gestionGPUFacade.rechercherCarteGenerateur(idGaspar);
			
			if(nbCarteGeneration > 0 || idGasparGeorisques.contains(idGaspar)) {
				ppr.setExistsCarte(true);
			}
		}
	}
	
	@Override
    public String getEquivalenceInseeArrondissmentCommune(String codeINSEE) {
        
        if (arrondissementsMarseille.contains(codeINSEE)) { return "13055"; }
        if (arrondissementsLyon.contains(codeINSEE)) { return "69123"; }
        if (arrondissementsParis.contains(codeINSEE)) { return "75056"; }
        
        return codeINSEE;
    }
}