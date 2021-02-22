package fr.gouv.beta.fabnum.kelrisks.presentation.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;

import fr.gouv.beta.fabnum.commun.facade.dto.JsonInfoDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.avis.AvisDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.avis.ShortUrlDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.CommuneDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.ParcelleDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.avis.IGestionAvisFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionCommuneFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionGeoDataGouvFacade;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel_write.IGestionShortUrlFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@Api(tags = {"API Avis Generator"}, value = "API permettant la génération d'avis")
public class ApiAvis extends AbstractBasicApi {
    
    @Autowired
    IGestionAvisFacade        gestionAvisFacade;
    @Autowired
    IGestionCommuneFacade     gestionCommuneFacade;
    @Autowired
    IGestionShortUrlFacade    gestionShortUrlFacade;
    @Autowired
    IGestionGeoDataGouvFacade gestionGeoDataGouvFacade;
    @Autowired
    PdfRedactor               pdfRedactor;
    
    public ApiAvis() {
        // Rien à faire
    }
    
    @ApiOperation(value = "Requête retournant une recherche à partir d'une URL courte.", hidden = true)
    @GetMapping("/api/url")
    public ResponseEntity<?> getUrl(@RequestParam("code") @NotBlank @Size(max = 10) String code) {
        
        ShortUrlDTO shortUrlDTO = gestionShortUrlFacade.rechercherResultatAvecCode(code);
        
        if (shortUrlDTO == null) { return ResponseEntity.status(422).build(); }
        
        return ResponseEntity.ok(shortUrlDTO);
    }
    
//    @GetMapping("/api/avis/parcelle")
//    @ApiOperation(value = "Requête retournant un avis à partir de la parcelle.", response = AvisDTO.class)
    public ResponseEntity<?> avisParParcelle(@ApiParam(required = true, name = "codeParcelle", value = "Code de la parcelle.")
                                    @RequestParam("codeParcelle") String codeParcelle,
                                    @ApiParam(required = true, name = "codeINSEE", value = "Code INSEE de la commune.")
                                    @RequestParam("codeINSEE") String codeINSEE,
                                    @ApiParam(name = "nomProprietaire", value = "Nom du propriétaire / Raison sociale.")
                                    @RequestParam(value = "nomProprietaire", required = false) String nomProprietaire,
                                    @RequestParam(value = "isIGNRequested", required = false, defaultValue = "true") boolean isIGNRequested,
                                    @RequestParam(value = "isPPRRequested", required = false, defaultValue = "true") boolean isPPRRequested) {
    
        return avis(codeINSEE, null, codeParcelle, isIGNRequested, isPPRRequested);
    }
    
//    @GetMapping("/api/avis/coordonnees")
//    @ApiOperation(value = "Requête retournant un avis à partir de coordonnées (SRID 4326).", response = AvisDTO.class)
    public ResponseEntity<?> avisParCoordonnees(@ApiParam(required = true, name = "longitude", value = "Longitude.")
                                       @RequestParam("longitude") String longitude,
                                       @ApiParam(required = true, name = "latitude", value = "Latitude.")
                                       @RequestParam("latitude") String latitude) {
    
        CommuneDTO communeDTO = gestionGeoDataGouvFacade.rechercherCommune(latitude, longitude);
    
        if (communeDTO == null) { return ResponseEntity.badRequest().body("Les coordonnées sont probablement erronées, aucune commune n'a été trouvé."); }
    
        ParcelleDTO parcelleDTO = gestionParcelleFacade.rechercherParcelleAvecCoordonnees(Double.parseDouble(longitude), Double.parseDouble(latitude));
    
        return avis(communeDTO.getCodeINSEE(), "", parcelleDTO.getCode(), true, true);
    }
    
//    @GetMapping("/api/avis/surface")
//    @ApiOperation(value = "Requête retournant un avis à partir d'une géométrie au format GeoJSON (SRID 4326).", response = AvisDTO.class, hidden = true)
    public Response avisParSurface(@ApiParam(required = true, name = "geoJSON", value = "un geoJSON.")
                                   @RequestParam("geojson") String geojson) {
        
        AvisDTO avisDTO = gestionAvisFacade.rendreAvis(geojson);
        
        return Response.ok(avisDTO).build();
    }
    
    @ApiOperation(value = "Requête permettant de rendre un avis.", hidden = true)
    @GetMapping("/api/avis")
    public ResponseEntity<?> avis(@RequestParam("codeINSEE") @NotBlank @Size(max = 5) String codeINSEE,
                         @RequestParam(value = "nomAdresse", required = false) @Size(max = 255) String nomAdresse,
                         @RequestParam("codeParcelle") @NotBlank @Size(max = 255) String codeParcelle,
                         @RequestParam(value = "isIGNRequested", required = false, defaultValue = "true") boolean isIGNRequested,
                         @RequestParam(value = "isPPRRequested", required = false, defaultValue = "true") boolean isPPRRequested) {
        
        if (codeParcelle == null || codeParcelle.equals("")) {
            
            JsonInfoDTO jsonInfoDTO = new JsonInfoDTO();
            jsonInfoDTO.addError("Merci d'entrer un code parcelle ou de choisir une adresse parmi les résultats proposés dans le champ.");
            return ResponseEntity.ok(jsonInfoDTO);
        }
        
        String      url         = getUrl(codeINSEE, nomAdresse, codeParcelle);
        ShortUrlDTO shortUrlDTO = gestionShortUrlFacade.rechercherResultatAvecUrl(url);
        
        if (shortUrlDTO == null) {
            shortUrlDTO = new ShortUrlDTO();
            // TinyURLs utilise 8 caracteres            
            shortUrlDTO.setCode(RandomStringUtils.random(8, true, true));
            shortUrlDTO.setUrl(url);
        }
        
        List<ParcelleDTO> parcelleDTOs = getParcelles(codeParcelle);
        
        if (parcelleDTOs == null || parcelleDTOs.isEmpty()) {
            JsonInfoDTO jsonInfoDTO = new JsonInfoDTO();
            jsonInfoDTO.addError("Une parcelle n'a pas été trouvée ¯\\_(ツ)_/¯");
            jsonInfoDTO.addInfo("Il peut arriver que certaines parcelles n'existent pas encore dans Kelrisks. Merci de réessayer plus tard ou de nous le signaler.");
            return ResponseEntity.ok(jsonInfoDTO);
        }
        
        CommuneDTO communeDTO = gestionCommuneFacade.rechercherCommuneComplete(codeINSEE);
        
        AvisDTO avisDTO = gestionAvisFacade.rendreAvis(parcelleDTOs, communeDTO, nomAdresse, isIGNRequested, isPPRRequested);
        
        avisDTO.getSummary().setCodeUrl(shortUrlDTO.getCode());
        
        if (shortUrlDTO.getId() == null) { gestionShortUrlFacade.save(shortUrlDTO); }
        
        // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        return ResponseEntity.ok(avisDTO);
    }
    
    @ApiOperation(value = "Requête permettant de rendre un avis pdf.", hidden = true)
    @PostMapping("/api/avis/pdf")
    @ResponseBody
    public ResponseEntity<byte[]> avisPdf(@RequestBody @Size(max = 45) List<PdfRedactor.Png> pngs,
                                          @RequestParam("codeINSEE") @NotBlank @Size(max = 5) String codeINSEE,
                                          @RequestParam(value = "nomAdresse", required = false) @Size(max = 255) String nomAdresse,
                                          @RequestParam("codeParcelle") @NotBlank @Size(max = 255) String codeParcelle,
                                          @RequestParam(value = "errial", required = false) @Size(max = 255) String choixErrial,
                                          @RequestParam(value = "isIGNRequested", required = false, defaultValue = "true") boolean isIGNRequested,
                                          HttpServletRequest req) {
        // TODO definir le nombre max de pngs
        if (codeParcelle == null || codeParcelle.equals("")) {
            JsonInfoDTO jsonInfoDTO = new JsonInfoDTO();
            jsonInfoDTO.addError("Merci d'entrer un code parcelle ou de choisir une adresse parmi les résultats proposés dans le champ.");
            return null;
        }
    
        List<ParcelleDTO> parcelleDTOs = getParcelles(codeParcelle);
    
        if (parcelleDTOs == null || parcelleDTOs.isEmpty()) {
            return null;
        }
    
        CommuneDTO communeDTO = gestionCommuneFacade.rechercherCommuneComplete(codeINSEE);
    
        AvisDTO avisDTO = gestionAvisFacade.rendreAvis(parcelleDTOs, communeDTO, nomAdresse, isIGNRequested, true);
    
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy");
        String           fileName         = "ERRIAL_Parcelle_" + codeParcelle + "_" + simpleDateFormat.format(new Date());
    
        try {
            File baseAvis = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "avis.html");
        
            org.jsoup.nodes.Document htmlDocument = Jsoup.parse(baseAvis, StandardCharsets.UTF_8.name());
        
            // Get remote host URL https://stackoverflow.com/a/19598848
            StringBuffer url = req.getRequestURL();
            String uri = req.getRequestURI();
            int idx = (((uri != null) && (uri.length() > 0)) ? url.indexOf(uri) : url.length());
            String host = url.substring(0, idx); //base url
            host = host.replace("http:", "https:").replace(":8080", ""); // remove port
            // http://localhost
            
            pdfRedactor.redigerAnalyse(htmlDocument, avisDTO, codeINSEE);
            pdfRedactor.ajouterImages(htmlDocument, pngs);
            pdfRedactor.ajouterQRCode(htmlDocument, avisDTO, host);
            pdfRedactor.ajouterChoixUtilisateur(htmlDocument, choixErrial);
            pdfRedactor.setFileName(htmlDocument, fileName);
            
            String html = htmlDocument.outerHtml();
            
            ByteArrayInputStream  byteArrayInputStream  = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            
            WriterProperties writerProperties = new WriterProperties();
            PdfWriter        pdfWriter        = new PdfWriter(byteArrayOutputStream, writerProperties);
            
            ConverterProperties converterProperties = new ConverterProperties();
            converterProperties.setCreateAcroForm(true);
            converterProperties.setCharset(StandardCharsets.UTF_8.name());
            
            HtmlConverter.convertToPdf(byteArrayInputStream, pdfWriter, converterProperties);
            
            return ResponseEntity.ok()
                           .header("Content-Disposition",
                                   "attachment; filename=" + fileName + ".pdf")
                           .body(byteArrayOutputStream.toByteArray());
        }
        catch (Exception e) {
            // TODO : Catcher cette exception correctement !
            log.error("Fail to create PDF",e);
        }
        
        return null;
    }
    
    private String getUrl(String codeINSEE,
                          String nomAdresse,
                          String codeParcelle) {
        
        codeINSEE = codeINSEE == null || codeINSEE.equalsIgnoreCase("null") || codeINSEE.equalsIgnoreCase("undefined") ? "" : codeINSEE;
        nomAdresse = nomAdresse == null || nomAdresse.equalsIgnoreCase("null") || nomAdresse.equalsIgnoreCase("undefined") ? "" : nomAdresse;
        codeParcelle = codeParcelle == null || codeParcelle.equalsIgnoreCase("null") || codeParcelle.equalsIgnoreCase("undefined") ? "" : codeParcelle;
        
        return codeParcelle + "|&|" + codeINSEE + "|&|" + nomAdresse;
    }
    
}