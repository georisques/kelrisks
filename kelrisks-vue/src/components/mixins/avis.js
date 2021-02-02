import fetchWithError from "../../script/fetchWithError";
import avisHelper from "../../script/avisHelper";
import functions from "../../script/fonctions";
import extent from "turf-extent";

export default {
    data: () => ({
        querying: false,
        form: {
            codeInsee: '',
            nomAdresse: '',
            codeParcelle: '',
            selectedParcellesList: []
        },
        leaflet: {
            data: undefined,
            center: [0, 0],
            parcelleFound: '',
            hasGeoloc: false
        },
        avis: {},
    }),
    methods: {
        initAvis () {
            return {
                summary: {
                    adresse: {
                        rue: {}
                    },
                    commune: {}
                },
                basiasNbOf: 0,
                basiasParcelle: {},
                basiasProximiteParcelle: {},
                basiasRayonParcelle: {},
                basiasRaisonSociale: {},
                basiasNonGeorerencee: {},
                basolNbOf: 0,
                basolParcelle: {},
                basolProximiteParcelle: {},
                basolRayonParcelle: {},
                basolNonGeorerencee: {},
                s3ICNbOf: 0,
                installationClasseeParcelle: {},
                installationClasseeProximiteParcelle: {},
                installationClasseeRayonParcelle: {},
                installationClasseeNonGeorerencee: {},
                sisParcelle: {},
                sisNonGeorerencee: {},
                lentillesArgile: {},
                niveauArgile: 0,
                codeSismicite: 0,
                potentielRadon: 0,
                ppr: {},
                TRIs: {},
                AZIs: {},
                canalisations: {},
                nucleaires: {},
                zonePlanExpositionBruit: '',
                plansExpositionBruit: {},
                codesCommunes: null,
                bboxParcelles: null,
                codeParcelles: null
            }
        },
        parseJSONMap (jsonMap) {
            if (jsonMap !== '' && jsonMap !== undefined && jsonMap.length > 0) {
                return jsonMap.map(x => JSON.parse(x))
            }
            return {
                "type": "FeatureCollection",
                "features": []
            }
        },
        getAvis () {
            this.$refs.searchErrors.clearAll()

            if (this.form.selectedParcellesList.length === 0) {
                this.$refs.searchErrors.sendError('Merci de bien vouloir vérifier qu\'au moins une parcelle est sélectionnée sur la carte.')
                return
            }

            let parcelle = this.form.selectedParcellesList.join(',')
            console.log("selectedParcellesList", this.form.selectedParcellesList)
            let insee = this.form.codeInsee
            let nom = this.form.nomAdresse

            if (!parcelle && !nom) {
                this.$refs.searchErrors.sendError('Merci de bien vouloir choisir une rue/numéro ou entrer une parcelle.')
                return
            }

            let url = this.env.apiPath + 'avis?codeINSEE=' + insee + '&codeParcelle=' + parcelle + '&nomAdresse=' + nom

            this.querying = true

            fetchWithError(url, null, 1000 * 90)
                .then(r => {
                    if (r.status !== 200) {
                        // console.log('Wrong code')
                        this.$refs.searchErrors.sendError("Le service de recherche n'est pas disponible pour le moment, veuillez réessayer plus tard.")
                        this.$emit('loaded')
                        this.querying = false
                        return
                    }
                    r.json().then(value => {

                        this.avis = this.initAvis()
    
                        this.$refs.searchErrors.checkInformations(value)
    
                        if (this.$refs.searchErrors.hasError) {
                            this._paq.push(['trackEvent', 'Flow', 'Informations', 'Erreur'])
                            this.querying = false
                            // this.$refs.searchErrors.emitErrors()
                            return
                        }
    
                        if (this.$refs.searchErrors.hasWarning) {
                            this._paq.push(['trackEvent', 'Flow', 'Informations', 'Warning'])
                            this.querying = false
                            // this.$refs.searchErrors.emitWarnings()
                            return
                        }
    
                        this.avis.summary = value.summary
                        this._paq.push(['trackEvent', 'Flow', 'Informations', 'OK'])
    
                        this.avis.basiasParcelle = avisHelper.getBasiasParcelle(value)
                        this.avis.basiasProximiteParcelle = avisHelper.getBasiasProximiteParcelle(value)
                        this.avis.basiasRaisonSociale = avisHelper.getBasiasRaisonSocialeParcelle(value)
                        this.avis.basiasRayonParcelle = avisHelper.getBasiasRayonParcelle(value)
                        this.avis.basiasNonGeorerencee = avisHelper.getBasiasNonGeoreferencees(value)
    
                        this.avis.basolParcelle = avisHelper.getBasolParcelle(value)
                        this.avis.basolProximiteParcelle = avisHelper.getBasolProximiteParcelle(value)
                        this.avis.basolRayonParcelle = avisHelper.getBasolRayonParcelle(value)
                        this.avis.basolNonGeorerencee = avisHelper.getBasolNonGeoreferencees(value)
    
                        this.avis.installationClasseeParcelle = avisHelper.getICSurParcelle(value)
                        this.avis.installationClasseeProximiteParcelle = avisHelper.getICProximiteParcelle(value)
                        this.avis.installationClasseeRayonParcelle = avisHelper.getICRayonParcelle(value)
                        this.avis.installationClasseeNonGeorerencee = avisHelper.getICNonGeoreferencees(value)
    
                        this.avis.sisParcelle = avisHelper.getSISSurParcelle(value)
                        this.avis.sisNonGeorerencee = avisHelper.getSISNonGeoreferencees(value)
    
                        this.avis.lentillesArgile = value.lentillesArgile
                        this.avis.niveauArgile = value.niveauArgile
    
                        this.leaflet.data = value.leaflet
                        this.leaflet.center = [parseFloat(value.leaflet.center.y), parseFloat(value.leaflet.center.x)]
    
                        this.avis.codeSismicite = value.summary.commune.codeZoneSismicite
                        this.avis.potentielRadon = value.summary.commune.classePotentielRadon
                        this.avis.communes = value.summary.commune
                        
                        this.avis.ppr = value.planPreventionRisquesDTOs
    
                        // pour les couches WMS parametres
                        // code commune + codes communes limitrophes
                        if(value.summary.commune){
                            let codesCommunes = [value.summary.commune.codeINSEE]
                            let communesLimitrophes = value.summary.commune.communesLimitrophes
                            communesLimitrophes.forEach(function (communeLimitrophe) {
                                codesCommunes.push(communeLimitrophe.codeINSEE)
                            });
    
                            this.avis.codesCommunes = codesCommunes
                        }
    
                        // bbox de ou des parcelles(s)
                        let parcellesGeoJson = this.parseJSONMap(value.leaflet.parcelles)
                        if(parcellesGeoJson.length && parcellesGeoJson.length == 1){
                            this.avis.bboxParcelles = extent(parcellesGeoJson[0])
                        } else if(parcellesGeoJson.length && parcellesGeoJson.length > 1){
                            this.avis.bboxParcelles = extent({
                                "type": "FeatureCollection",
                                "features": parcellesGeoJson
                            })
                        }
                        console.log("bboxParcelles",this.avis.bboxParcelles)
    
                        let codeParcelles = []
                        parcellesGeoJson.forEach(function (parcelle) {
                            console.log("parcelle",parcelle)
                            codeParcelles.push(parcelle.properties.parcelle+"@"+parcelle.properties.codeINSEE)
                        })
                        this.avis.codeParcelles = codeParcelles
                        
                        this.avis.TRIs = value.tris
                        this.avis.AZIs = value.azis
    
                        this.avis.canalisations = value.geogCanalisations
                        this.avis.nucleaires.positions = value.geogInstallationsNucleaires
                        this.avis.nucleaires.installations = value.installationNucleaireDTOS
                        this.avis.hasCentraleNucleaire = value.hasCentraleNucleaire
    
                        this.avis.zonePlanExpositionBruit = value.zonePlanExpositionBruit
                        this.avis.plansExpositionBruit = value.plansExpositionBruit
    
                        functions.scrollToElement('app', 500, 0, false)
                        this._paq.push(['trackEvent', 'Flow', 'Avis', 'Rendu'])
    
                        this.querying = false
    
                        this.$emit('flow', 1)
                        this.$emit('form', this.form)
                        this.$emit('avis', this.avis)
                        this.$emit('leaflet', this.leaflet)
                        this.$emit('loaded')
                    })
                    .catch(() => {
                        this.$emit('loaded')
    
                        this.$refs.searchErrors.sendError('Votre requête n’a pu aboutir dans un délai raisonnable.')
    
                        this.avis = this.initAvis()
    
                        this.querying = false
                    })
            }) 
                
        }
    }
}
