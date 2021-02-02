package fr.gouv.beta.fabnum.kelrisks.presentation.rest;

import fr.gouv.beta.fabnum.commun.facade.dto.JsonInfoDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.ParcelleDTO;
import fr.gouv.beta.fabnum.kelrisks.facade.frontoffice.referentiel.IGestionParcelleFacade;
import fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.qo.ParcelleQO;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Validated
public class AbstractBasicApi {

	private final static String DEFAULT_PREFIXE = "000";

	@Autowired
	IGestionParcelleFacade gestionParcelleFacade;

	public String getParcelleCode(String codeINSEE, String codeParcelle) {

		return getParcelleCode(codeParcelle + "@", codeINSEE);
	}

	public String getParcelleCode(String parcelleSecNumInseeList) {

		List<ParcelleDTO> parcelles = getParcelles(parcelleSecNumInseeList);

		if (parcelles == null) {
			return null;
		}
		if (parcelles.size() > 1) {
			return null;
		}
		return parcelles.get(0).getCode();
	}

	public List<ParcelleDTO> getParcelles(String codeINSEE, String codeParcelle) {

		return getParcelles(codeParcelle + "@" + codeINSEE);
	}

	public List<ParcelleDTO> getParcelles(String parcelleSecNumInseeList) {
		List<String> secNumList = Arrays.asList(parcelleSecNumInseeList.split(","));

		ParcelleQO parcelleQO = new ParcelleQO();

		secNumList.forEach(secNum -> {

			String[] prefSecNumANDInsee = secNum.split("@");
			// TODO : verifier la taille de secNumANDInsee
			String parcelle = prefSecNumANDInsee[0];
			String[] parcelleInfo = parcelle.split("-");

			if (prefSecNumANDInsee.length == 2) {
				String codeInsee = prefSecNumANDInsee[1];

				int indexSection = 1;
				int indexNumero = 2;
				String numero = "";
				String section = "";
				String prefixe = "";
				// section et numero je suppose... en theorie ne devrait pas arriver
				if (parcelleInfo.length == 2) {
					indexSection = 0;
					indexNumero = 1;
					prefixe = DEFAULT_PREFIXE;
				} else {
					prefixe = parcelleInfo[0];
					if (StringUtils.isBlank(prefixe)) {
						prefixe = DEFAULT_PREFIXE;
					}
				}
				
				if (parcelleInfo.length >= 2) {
					section = parcelleInfo[indexSection];
					numero = parcelleInfo[indexNumero];
				}

				if (StringUtils.isNotBlank(codeInsee) && StringUtils.isNotBlank(section)) {
					parcelleQO.getParcelles().add(new ParcelleQO.PrefSecNumInsee(prefixe, section, numero, codeInsee));

				}

			}
		});

		List<ParcelleDTO> parcelleDTOs = null;
		if (!parcelleQO.getParcelles().isEmpty()) {
			// sinon je recupere toute la base du cadastre...
			parcelleDTOs = gestionParcelleFacade.rechercherAvecCritere(parcelleQO);
		}

		return parcelleDTOs;
	}

	ParcelleDTO getParcelleDTO(String codeINSEE, String codeParcelle) {

		String parcelleCode = getParcelleCode(codeParcelle + '@' + codeINSEE);

		ParcelleQO parcelleQO = new ParcelleQO();
		parcelleQO.setCodeINSEE(codeINSEE);
		parcelleQO.setCode(parcelleCode);

		if (StringUtils.isNotBlank(codeINSEE) && StringUtils.isNotBlank(parcelleCode)) {
			return gestionParcelleFacade.rechercherResultatUniqueAvecCritere(parcelleQO);
		}

		return null;
	}

	// https://reflectoring.io/bean-validation-with-spring-boot/
	@ExceptionHandler({ ConstraintViolationException.class, MethodArgumentTypeMismatchException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ResponseEntity<JsonInfoDTO> handleConstraintViolationException(Exception e) {
		JsonInfoDTO jsonInfoDTO = new JsonInfoDTO();
		jsonInfoDTO.addError("Not valid due to validation error: " + e.getMessage());
		return new ResponseEntity<>(jsonInfoDTO, HttpStatus.BAD_REQUEST);
	}

}
