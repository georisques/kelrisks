package fr.gouv.beta.fabnum.kelrisks.presentation.rest;

import fr.gouv.beta.fabnum.commun.facade.dto.JsonInfoDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.avis.AvisDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.CommuneDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.InstallationClasseeDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.SiteIndustrielBasiasDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.SiteIndustrielBasolDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.avis.IGestionAvisFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionCommuneFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;

@RestController
@Api(tags = {"API Avis Generator"}, description = "API permettant la génération d'avis")
public class ApiAvis extends AbstractBasicApi {
    
    @Autowired
    IGestionAvisFacade    gestionAvisFacade;
    @Autowired
    IGestionCommuneFacade gestionCommuneFacade;
    //    @Value(value = "classpath:/avis.html")
    //    private File baseAvis;
    
    public ApiAvis() {
        // Rien à faire
    }
    
    @GetMapping("/api/avis")
    @ApiOperation(value = "Requête retournant un avis à partir des paramètres.", response = String.class)
    public Response avis(@ApiParam(required = true, name = "codeParcelle", value = "Code de la parcelle.")
                         @RequestParam("codeParcelle") String codeParcelle,
                         @ApiParam(required = true, name = "codeINSEE", value = "Code postal de la commune.")
                         @RequestParam("codeINSEE") String codeINSEE,
                         @ApiParam(name = "nomVoie", value = "Nom de la voie.")
                         @RequestParam(value = "nomVoie", required = false) String nomVoie,
                         @ApiParam(name = "idBAN", value = "Id BAN (e.g. ADRNIVX_0000002009679805).")
                         @RequestParam(value = "idBAN", required = false) String idBAN,
                         @ApiParam(name = "nomProprietaire", value = "Nom du propriétaire / Raison sociale.")
                         @RequestParam(value = "nomProprietaire", required = false) String nomProprietaire) {
    
        if (codeParcelle != null && !codeParcelle.equals("")) {
            
            codeParcelle = getParcelleCode(codeINSEE, codeParcelle);
        
            if (codeParcelle == null) {
                JsonInfoDTO jsonInfoDTO = new JsonInfoDTO();
                jsonInfoDTO.addError("Le code parcelle n'a pas été trouvé.");
                return Response.ok(jsonInfoDTO).build();
            }
        }
    
        if ((codeParcelle == null || codeParcelle.equals("")) && (idBAN == null || idBAN.equals(""))) {
        
            JsonInfoDTO jsonInfoDTO = new JsonInfoDTO();
            jsonInfoDTO.addError("Merci d'entrer un code parcelle ou une adresse complète.");
            return Response.ok(jsonInfoDTO).build();
        }
        
        AvisDTO avisDTO = gestionAvisFacade.rendreAvis(codeParcelle, codeINSEE, nomVoie, idBAN, nomProprietaire);
        
        return Response.ok(avisDTO).build();
    }
    
    @GetMapping(value = "/api/avis/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseBody
    public byte[] avisPdf(@ApiParam(required = true, name = "codeParcelle", value = "Code de la parcelle.")
                          @RequestParam("codeParcelle") String codeParcelle,
                          @ApiParam(required = true, name = "codeINSEE", value = "Code postal de la commune.")
                          @RequestParam("codeINSEE") String codeINSEE,
                          @ApiParam(name = "nomVoie", value = "Nom de la voie.")
                          @RequestParam(value = "nomVoie", required = false) String nomVoie,
                          @ApiParam(name = "idBAN", value = "Id BAN (e.g. ADRNIVX_0000002009679805).")
                          @RequestParam(value = "idBAN", required = false) String idBAN,
                          @ApiParam(name = "nomProprietaire", value = "Nom du propriétaire / Raison sociale.")
                          @RequestParam(value = "nomProprietaire", required = false) String nomProprietaire) {
    
        codeParcelle = getParcelleCode(codeINSEE, codeParcelle);
        
        AvisDTO avisDTO = gestionAvisFacade.rendreAvis(codeParcelle, codeINSEE, nomVoie, idBAN, nomProprietaire);
        
        try {
            File baseAvis = ResourceUtils.getFile("classpath:avis.html");
            
            org.jsoup.nodes.Document htmlDocument = Jsoup.parse(baseAvis, StandardCharsets.UTF_8.name());
    
            redigerAnalyse(htmlDocument, avisDTO, codeINSEE);
            
            String html = htmlDocument.outerHtml();
            
            ByteArrayInputStream  byteArrayInputStream  = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            
            WriterProperties writerProperties = new WriterProperties();
            PdfWriter        pdfWriter        = new PdfWriter(byteArrayOutputStream, writerProperties);
            
            ConverterProperties converterProperties = new ConverterProperties();
            converterProperties.setCharset(StandardCharsets.UTF_8.name());
            
            HtmlConverter.convertToPdf(byteArrayInputStream, pdfWriter, converterProperties);
            
            return byteArrayOutputStream.toByteArray();
        }
        catch (Exception e) {
            // TODO : Catcher cette exception correctement !
            e.printStackTrace();
        }
        
        return null;
    }
    
    private void redigerAnalyse(Document htmlDocument, AvisDTO avisDTO, String codeINSEE) {
        
        Element element = htmlDocument.select("#infos").first();
        
        CommuneDTO communeDTO = gestionCommuneFacade.rechercherCommuneAvecCodeINSEE(codeINSEE);
        element.append("n°" + avisDTO.getParcelle() + " à " + communeDTO.getNomCommune() + " (" + communeDTO.getCodePostal() + ")");
        
        redigerAnalyseParcelle(htmlDocument, avisDTO);
        
        redigerAnalyseComplementaire(htmlDocument, avisDTO);
    }
    
    private void redigerAnalyseParcelle(Document htmlDocument, AvisDTO avisDTO) {
        
        redigerBasiasParcelle(htmlDocument, avisDTO);
        
        redigerBasiasProximiteParcelle(htmlDocument, avisDTO);
        
        redigerBasolParcelle(htmlDocument, avisDTO);
        
        redigerInstallationClasseeParcelle(htmlDocument, avisDTO);
    }
    
    private void redigerInstallationClasseeParcelle(Document htmlDocument, AvisDTO avisDTO) {
        
        Element element;
        element = htmlDocument.select("#icParcelle").first();
        int numberOf = avisDTO.getInstallationClasseeSurParcelleDTOs().size();
        if (numberOf == 0) {
            element.append("N’est pas référencée dans l\'inventaire des installations classées en fonctionnement ou arrêtées.");
        }
        else {
            element.append("Est référencée dans l\'inventaire des installations classées sous le nom de :");
            element = element.appendElement("ul");
            for (InstallationClasseeDTO site : avisDTO.getInstallationClasseeSurParcelleDTOs()) {
                element.appendElement("li").append(" - " + site.getRaisonSociale());
            }
        }
    }
    
    private void redigerBasolParcelle(Document htmlDocument, AvisDTO avisDTO) {
        
        Element element;
        element = htmlDocument.select("#basolParcelle").first();
        int numberOf = avisDTO.getSiteIndustrielBasolSurParcelleDTOs().size();
        if (numberOf == 0) {
            element.append("N’est pas référencée dans l\'inventaire des sites pollués BASOL.");
        }
        else {
            element.append("Est référencée dans l\'inventaire des sites pollués BASOL.");
            element = element.appendElement("ul");
            for (SiteIndustrielBasolDTO site : avisDTO.getSiteIndustrielBasolSurParcelleDTOs()) {
                element.appendElement("li").append(" - <a href='https://basol.developpement-durable.gouv.fr/fiche.php?page=1&index_sp=" + site.getNumerobasol() + "'>" +
                                                   "https://basol.developpement-durable.gouv.fr/fiche.php?page=1&index_sp=" + site.getNumerobasol() + "</a>");
            }
        }
    }
    
    private void redigerBasiasProximiteParcelle(Document htmlDocument, AvisDTO avisDTO) {
        
        Element element;
        element = htmlDocument.select("#basiasProximiteParcelle").first();
        int numberOf = avisDTO.getSiteIndustrielBasiasProximiteParcelleDTOs().size();
        if (numberOf == 0) {
            element.remove();
        }
        else {
            element.append("Par ailleurs, nous identifions dans le voisinage immédiat de la (ou des) parcelle(s), un site ayant accueilli par le passé une activité susceptible d\'avoir pu générer " +
                           "une pollution des sols (BASIAS). Vous pouvez consulter la fiche consacrée à cette activité industrielle à l\'adresse suivante : .");
            element = element.appendElement("ul");
            for (SiteIndustrielBasiasDTO site : avisDTO.getSiteIndustrielBasiasProximiteParcelleDTOs()) {
                element.appendElement("li").append(" - <a href='http://fiches-risques.brgm.fr/georisques/basias-synthetique/" + site.getIdentifiant() + "'>" +
                                                   "http://fiches-risques.brgm.fr/georisques/basias-synthetique/" + site.getIdentifiant() + "</a>");
            }
        }
    }
    
    private void redigerBasiasParcelle(Document htmlDocument, AvisDTO avisDTO) {
        
        Element element;
        element = htmlDocument.select("#basiasParcelle").first();
        int numberOf = avisDTO.getSiteIndustrielBasiasSurParcelleDTOs().size();
        if (numberOf == 0) {
            element.append("n’est pas référencée dans l\'inventaire des sites ayant accueilli par le passé une activité susceptible d\'avoir pu généré une pollution des sols (BASIAS).");
        }
        else {
            element.append("est référencée dans l\'inventaire des sites ayant accueilli par le passé une activité susceptible d\'avoir pu généré une pollution des sols (BASIAS).");
            element = element.appendElement("ul");
            for (SiteIndustrielBasiasDTO site : avisDTO.getSiteIndustrielBasiasSurParcelleDTOs()) {
                element.appendElement("li").append(" - <a href='http://fiches-risques.brgm.fr/georisques/basias-synthetique/" + site.getIdentifiant() + "'>" +
                                                   "http://fiches-risques.brgm.fr/georisques/basias-synthetique/" + site.getIdentifiant() + "</a>");
            }
        }
    }
    
    private void redigerAnalyseComplementaire(Document htmlDocument, AvisDTO avisDTO) {
        
        if (avisDTO.getSiteIndustrielBasiasRayonParcelleDTOs().size() == 0 &&
            avisDTO.getSiteIndustrielBasolRayonParcelleDTOs().size() == 0 &&
            avisDTO.getInstallationClasseeRayonParcelleDTOs().size() == 0 &&
            avisDTO.getInstallationClasseeNonGeorerenceesDTOs().size() == 0) {
            
            htmlDocument.select("#analyseComplementaireWrapper").first().remove();
        }
        else {
            
            if (avisDTO.getSiteIndustrielBasiasRayonParcelleDTOs().size() == 0 &&
                avisDTO.getSiteIndustrielBasolRayonParcelleDTOs().size() == 0 &&
                avisDTO.getInstallationClasseeRayonParcelleDTOs().size() == 0) {
                
                htmlDocument.select("#rayonWrapper").first().remove();
            }
            else {
                redigerAvisRayonParcelle(htmlDocument, avisDTO);
            }
            
            if (avisDTO.getInstallationClasseeNonGeorerenceesDTOs().size() == 0) {
                htmlDocument.select("#icCommuneWrapper").first().remove();
            }
            else {
                redigerAvisInstallationsClasseesCommune(htmlDocument, avisDTO);
            }
        }
    }
    
    private void redigerAvisInstallationsClasseesCommune(Document htmlDocument, AvisDTO avisDTO) {
        
        Element element;
        int     numberOf = avisDTO.getInstallationClasseeNonGeorerenceesDTOs().size();
        element = htmlDocument.select("#icCommune").first();
        element.append("Il est à noter que nous avons trouvé " + numberOf + " installation(s) Classée(s) non géoréférencée(s) dans la commune : ");
        element = element.appendElement("ul");
        for (InstallationClasseeDTO site : avisDTO.getInstallationClasseeNonGeorerenceesDTOs()) {
            element.appendElement("li").append(" - " + site.getRaisonSociale());
        }
    }
    
    private void redigerAvisRayonParcelle(Document htmlDocument, AvisDTO avisDTO) {
        
        Element element;
        element = htmlDocument.select("#basiasRayon").first();
        int numberOf = avisDTO.getSiteIndustrielBasiasRayonParcelleDTOs().size();
        if (numberOf == 0) {
            element.remove();
        }
        else {
            if (numberOf == 1) {
                element.append("Se trouve 1 site Basias dont la fiche est consultable en cliquant sur le lien suivant :");
            }
            else {
                element.append("Se trouvent " + numberOf + " sites Basias dont les fiches sont consultables en cliquant sur les liens suivants :");
            }
            element = element.appendElement("ul");
            for (SiteIndustrielBasiasDTO site : avisDTO.getSiteIndustrielBasiasRayonParcelleDTOs()) {
                element.appendElement("li").append(" - <a href='http://fiches-risques.brgm.fr/georisques/basias-synthetique/" + site.getIdentifiant() + "'>" +
                                                   "http://fiches-risques.brgm.fr/georisques/basias-synthetique/" + site.getIdentifiant() + "</a>");
            }
        }
        
        element = htmlDocument.select("#basolRayon").first();
        numberOf = avisDTO.getSiteIndustrielBasolRayonParcelleDTOs().size();
        if (numberOf == 0) {
            element.remove();
        }
        else {
            if (numberOf == 1) {
                element.append("Se trouve 1 site Basol dont la fiche est consultable en cliquant sur le lien suivant :");
            }
            else {
                element.append("Se trouvent " + numberOf + " sites Basol dont les fiches sont consultables en cliquant sur les liens suivants :");
            }
            element = element.appendElement("ul");
            for (SiteIndustrielBasolDTO site : avisDTO.getSiteIndustrielBasolRayonParcelleDTOs()) {
                element.appendElement("li").append(" - <a href='https://basol.developpement-durable.gouv.fr/fiche.php?page=1&index_sp=" + site.getNumerobasol() + "'>" +
                                                   "https://basol.developpement-durable.gouv.fr/fiche.php?page=1&index_sp=" + site.getNumerobasol() + "</a>");
            }
        }
        
        element = htmlDocument.select("#icRayon").first();
        numberOf = avisDTO.getInstallationClasseeRayonParcelleDTOs().size();
        if (numberOf == 0) {
            element.remove();
        }
        else {
            if (numberOf == 1) {
                element.append("Se trouve 1 installation classée :");
            }
            else {
                element.append("Se trouvent " + numberOf + " installations classées :");
            }
            element = element.appendElement("ul");
            for (InstallationClasseeDTO site : avisDTO.getInstallationClasseeRayonParcelleDTOs()) {
                element.appendElement("li").append(" - " + site.getRaisonSociale());
            }
        }
    }
}