export default {

    getBasiasParcelle (value) {
        // console.log('getBasiasParcelle')
        let avis = {
            lib: '',
            liste: []
        }
        avis.numberOf = value.siteIndustrielBasiasSurParcelleDTOs.length
        if (avis.numberOf === 0) {
            avis.lib = 'N’est pas référencée dans l\'inventaire des sites ayant accueilli par le passé une activité susceptible d\'avoir pu généré une pollution des sols (BASIAS).'
        } else {
            avis.lib = 'est référencée dans l\'inventaire des sites ayant accueilli par le passé une activité susceptible d\'avoir pu généré une pollution des sols (BASIAS).'
            value.siteIndustrielBasiasSurParcelleDTOs.forEach(function (element) {
                avis.liste.push(element)
            }, this)
        }
        return avis
    },

    getBasiasProximiteParcelle (value) {
        // console.log('getBasiasProximiteParcelle')
        let avis = {
            lib: '',
            liste: []
        }
        avis.numberOf = value.siteIndustrielBasiasProximiteParcelleDTOs.length
        if (avis.numberOf !== 0) {
            avis.lib = avis.numberOf === 1
                ? 'dans le voisinage immédiat de la (ou des) parcelle(s), un site  ayant accueilli par le passé une activité susceptible d\'avoir pu générer une pollution des sols (BASIAS). Vous pouvez consulter la fiche consacrée à cette activité industrielle à l\'adresse suivante : '
                : 'dans le voisinage immédiat de la (ou des) parcelle(s), des sites  ayant accueilli par le passé une activité susceptible d\'avoir pu générer une pollution des sols (BASIAS). Vous pouvez consulter les fiches consacrée à cette activité industrielle à l\'adresse suivante : '
            value.siteIndustrielBasiasProximiteParcelleDTOs.forEach(function (element) {
                avis.liste.push(element)
            }, this)
        }
        return avis
    },

    getBasiasRaisonSocialeParcelle (value) {
        // console.log('getBasiasRaisonSociale')
        let avis = {
            lib: '',
            liste: []
        }
        avis.numberOf = value.siteIndustrielBasiasParRaisonSocialeDTOs.length
        if (avis.numberOf !== 0) {
            avis.lib = 'un site dont la localisation est imprécise mais ayant potentiellement appartenu au même propriétaire (' + value.nomProprietaire + ') : '
            value.siteIndustrielBasiasParRaisonSocialeDTOs.forEach(function (element) {
                avis.liste.push(element)
            }, this)
        }
        return avis
    },

    getBasiasRayonParcelle (value) {
        let avis = {
            lib: '',
            liste: []
        }
        avis.numberOf = value.siteIndustrielBasiasRayonParcelleDTOs.length
        if (avis.numberOf > 0) {
            value.siteIndustrielBasiasRayonParcelleDTOs.forEach(function (element) {
                avis.liste.push(element)
            }, this)
        }
        // console.log(avis)
        return avis
    },

    getBasiasNonGeoreferencees (value) {
        let avis = {
            lib: 'Liste des sites BASIAS non géoréférencés',
            liste: []
        }
        avis.numberOf = value.siteIndustrielBasiasNonGeorerenceesDTOs.length
        if (avis.numberOf !== 0) {
            value.siteIndustrielBasiasNonGeorerenceesDTOs.forEach(function (element) {
                avis.liste.push(element)
            }, this)
        }
        return avis
    },

    getBasolParcelle (value) {
        let avis = {
            lib: '',
            liste: []
        }
        avis.numberOf = value.siteIndustrielBasolSurParcelleDTOs.length
        if (avis.numberOf === 0) {
            avis.lib = 'N’est pas référencée dans l\'inventaire des sites pollués BASOL.'
        } else {
            avis.lib = 'Est référencée dans l\'inventaire des sites pollués BASOL.'
            value.siteIndustrielBasolSurParcelleDTOs.forEach(function (element) {
                avis.liste.push(element)
            }, this)
        }
        return avis
    },

    getBasolProximiteParcelle () {
        return {numberOf: 0, lib: ''}
    },

    getBasolRayonParcelle (value) {
        let avis = {
            lib: '',
            liste: []
        }
        avis.numberOf = value.siteIndustrielBasolRayonParcelleDTOs.length
        if (avis.numberOf > 0) {
            value.siteIndustrielBasolRayonParcelleDTOs.forEach(function (element) {
                avis.liste.push(element)
            }, this)
        }
        return avis
    },

    getBasolNonGeoreferencees (value) {
        let avis = {
            lib: 'Liste des sites BASOL non géoréférencés',
            liste: []
        }
        avis.numberOf = value.siteIndustrielBasolNonGeorerenceesDTOs.length
        if (avis.numberOf !== 0) {
            value.siteIndustrielBasolNonGeorerenceesDTOs.forEach(function (element) {
                avis.liste.push(element)
            }, this)
        }
        return avis
    },

    getICSurParcelle (value) {
        let avis = {
            lib: '',
            liste: []
        }
        avis.numberOf = value.installationClasseeSurParcelleDTOs.length
        if (avis.numberOf === 0) {
            avis.lib = 'N’est pas référencée dans l\'inventaire des installations classées en fonctionnement ou arrêtées.'
        } else {
            avis.lib = 'Est référencée dans l\'inventaire des installations classées sous le nom de :'
            value.installationClasseeSurParcelleDTOs.forEach(function (element) {
                avis.liste.push(element)
            }, this)
        }
        return avis
    },

    getICProximiteParcelle () {
        return {numberOf: 0, lib: ''}
    },

    getICRayonParcelle (value) {
        let avis = {
            lib: '',
            liste: []
        }
        avis.numberOf = value.installationClasseeRayonParcelleDTOs.length
        if (avis.numberOf > 0) {
            value.installationClasseeRayonParcelleDTOs.forEach(function (element) {
                avis.liste.push(element)
            }, this)
        }
        return avis
    },

    getICNonGeoreferencees (value) {
        let avis = {
            lib: '',
            liste: []
        }
        avis.numberOf = value.installationClasseeNonGeorerenceesDTOs.length
        if (avis.numberOf === 0) {
            avis.lib = 'Aucune Installation Classée non géoréférencée dans la commune'
        } else {
            avis.lib = 'Le résultat de cette recherche ne tient pas compte des ' + avis.numberOf + ' sites identifiés sur la commune qui n’ont pu être géolocalisés faute d’une information suffisante : '
            value.installationClasseeNonGeorerenceesDTOs.forEach(function (element) {
                avis.liste.push(element)
            }, this)
        }
        return avis
    },

    getSISSurParcelle (value) {
        let avis = {
            lib: '',
            liste: []
        }
        avis.numberOf = value.secteurInformationSolSurParcelleDTOs.length
        if (avis.numberOf === 0) {
            avis.lib = 'N’est pas situé en secteur d’information sur les sols (SIS).'
        } else {
            avis.lib = 'Est référencée dans l\'inventaire des secteurs d\'informations sur les sols'
            value.secteurInformationSolSurParcelleDTOs.forEach(function (element) {
                avis.liste.push(element)
            }, this)
        }
        return avis
    },

    getSISNonGeoreferencees (value) {
        let avis = {
            lib: 'Liste des secteurs d\'information sur les sols non géoréférencés',
            liste: []
        }
        avis.numberOf = value.secteurInformationSolNonGeorerenceesDTOs.length
        if (avis.numberOf !== 0) {
            value.secteurInformationSolNonGeorerenceesDTOs.forEach(function (element) {
                avis.liste.push(element)
            }, this)
        }
        return avis
    }
}
