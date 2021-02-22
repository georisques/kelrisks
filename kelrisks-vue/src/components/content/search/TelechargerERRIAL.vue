<template>
    <div>
        <section class="section section-white">

            <errors ref="resultsERRIAL"/>

            <br/>

            <div id="searchButtonsWrapper">
                <a @click="$emit('flow', -1)"
                   class="bouton">
                    <font-awesome-icon icon="chevron-left"/>
                    Modifier l'état des risques
                </a>

                <a :href="env.basePath"
                   class="bouton">
                    <font-awesome-icon icon="search"/>
                    Nouvelle recherche
                </a>
            </div>

            <div id="actionButtonsWrapper"></div>

            <div class="container bordered">
                <div>
                    <div style="margin-bottom: 20px"><span class="title">Parcelle(s) </span></div>
                    <span class="rightAlign">Adresse&nbsp;: </span><b><span v-if="avis.summary.adresse">{{ avis.summary.adresse }}, <br/><span class="rightAlign"/>{{ avis.summary.commune.codePostal }} {{
                        avis.summary.commune.nomCommune
                                                                                                        }}</span><span v-else-if="avis.summary.commune">{{
                        avis.summary.commune.codePostal
                                                                                                                                                        }}, {{ avis.summary.commune.nomCommune }}</span><span v-else><i>n/a</i></span></b><br/>
                    <span class="rightAlign">Code parcelle&nbsp;: </span><b><span v-if="avis.summary.codeParcelle && avis.summary.codeParcelle !== ''">{{
                        avis.summary.codeParcelle
                                                                                                                                                       }}</span><span v-else><i>n/a</i></span></b><br/>
                    <br>
                </div>
            </div>

            <div class="container bordered ppr">
                <div>
                    <div class="errial_title"><span class="title">Télécharger l'ERRIAL « état des risques réglementés pour l'information des acquéreurs et des locataires »</span></div>
                    <p>Il appartient au propriétaire du bien de vérifier l'exactitude de ces informations autant que de besoin et, le cas échéant, de le compléter à partir d'informations disponibles
                       sur le site internet de la préfecture ou d'informations dont il dispose sur le bien, notamment les sinistres que le bien a subis.</p>
                    <p v-if="hasPPR"><strong>Le propriétaire doit joindre les extraits de la carte réglementaire et du règlement du PPR qui concernent la parcelle.</strong></p>

                    <div style="margin: 0 auto 25px; width: 350px;">
                        <leaflet :data="currentData"
                                 :max-zoom-center="leaflet.center"
                                 :min-zoom="14"
                                 :parcelle="leaflet.data.parcelles"
                                 :png-name="currentPngName"
                                 @png="pngGenerated"
                                 :wms-layer="currentWmsLayer"
                                 :wms-servers="currentWmsServers"
                                 :attribution="currentAttribution"
                                 :codes-communes="this.avis.codesCommunes"
                                 :bbox-risque="this.avis.bboxParcelles"
                                 :id-ppr="currentIdPpr"
                                 v-show="currentData !== ''"/>
                    </div>
                    <div id="pdf">
                        <a @click="getPdf" style="display:inline-block; float:none; text-decoration: none !important;"><span style="font-size: 5em;"><font-awesome-icon icon="file-pdf"/></span><br/>
                        <span v-if="isLoading"><font-awesome-icon icon="spinner" style="margin : 0 10px 0 0;" spin/>Génération du PDF en cours...<br>Veuillez patienter plusieurs secondes...</span>
                        <span v-else>État des risques au format PDF</span>
                        </a>
                    </div>
                </div>
            </div>

        </section>
    </div>
</template>

<script>

import Errors from "../base/Errors";
import Leaflet from "../leaflet/LeafletPdf";
import fetchWithError from "../../../script/fetchWithError";
import moment from "moment"
import mixinAvisHas from "../../mixins/avisHas";
import * as conf from "../../../config.js";

export default {
    name: 'TelechargerERRIAL',
    mixins: [mixinAvisHas],
    components: {
        Leaflet,
        Errors
    },
    props: {
        leaflet: {
            type: Object,
            default: () => {
            }
        },
        form: {
            type: Object,
            default: () => {
            }
        },
        errial: {
            type: String,
            default: ""
        }
    },
    data: () => ({
        env: {
            basePath: process.env.VUE_APP_FRONT_PATH,
            apiPath: process.env.VUE_APP_BACK_API_PATH
        },
        dataList: [],
        isDownload: false,
        isLoading: false,
        currentData: '',
        currentPngName: '',
        currentWmsLayer: null,
        currentWmsServers: null,
        currentIdPpr: null,
        currentAttribution: null,
        pngList: [],
    }),
    methods: {
        generatePngs () {
            console.log('generatePngs')

            this.dataList = []

            this.dataList.push([
                [{
                    data: this.leaflet.data.parcelles,
                    color: '#455674'
                }],
                'carte-parcelle'])

            for (let plan in this.avis.ppr) {
                plan = this.avis.ppr[plan]
                // id ppr en plus
                this.dataList.push([
                    [{
                        data: plan.assiettes,
                        color: '#840505',
                        opacity: 0
                    }],
                    plan.alea.familleAlea.code,// FIXME peut etre lie au bug #82
                    (!plan.existsInGeorisque && !plan.existsInGpu) ? null : (plan.existsInGeorisque ? conf.config.couchesRisques.ppr_georisques.layer : conf.config.couchesRisques.ppr_gpu.layer),
                    (!plan.existsInGeorisque && !plan.existsInGpu) ? null : (plan.existsInGeorisque ? conf.config.couchesRisques.ppr_georisques.serveurs : conf.config.couchesRisques.ppr_gpu.serveurs),                    
                    (!plan.existsInGeorisque && !plan.existsInGpu) ? null : (plan.existsInGeorisque ? plan.idGaspar : plan.idAssietteErrial)])
            }

            if (this.hasSismicite) this.dataList.push([
                typeof this.avis.summary.commune.communesLimitrophes.map === 'function' ?
                    [{data: this.avis.summary.commune.codeZoneSismicite === '1' ? [this.avis.summary.commune.multiPolygon] : [], color: '#D8D8D8', opacity: 0},
                        {data: this.avis.summary.commune.codeZoneSismicite === '2' ? [this.avis.summary.commune.multiPolygon] : [], color: '#FFD332', opacity: 0},
                        {data: this.avis.summary.commune.codeZoneSismicite === '3' ? [this.avis.summary.commune.multiPolygon] : [], color: '#FF8000', opacity: 0},
                        {data: this.avis.summary.commune.codeZoneSismicite === '4' ? [this.avis.summary.commune.multiPolygon] : [], color: '#E02B17', opacity: 0},
                        {data: this.avis.summary.commune.codeZoneSismicite === '5' ? [this.avis.summary.commune.multiPolygon] : [], color: '#840505', opacity: 0},
                        {data: this.avis.summary.commune.communesLimitrophes.filter(x => x.codeZoneSismicite === '1').map(x => x.multiPolygon), color: '#D8D8D8', opacity: 0},
                        {data: this.avis.summary.commune.communesLimitrophes.filter(x => x.codeZoneSismicite === '2').map(x => x.multiPolygon), color: '#FFD332', opacity: 0},
                        {data: this.avis.summary.commune.communesLimitrophes.filter(x => x.codeZoneSismicite === '3').map(x => x.multiPolygon), color: '#FF8000', opacity: 0},
                        {data: this.avis.summary.commune.communesLimitrophes.filter(x => x.codeZoneSismicite === '4').map(x => x.multiPolygon), color: '#E02B17', opacity: 0},
                        {data: this.avis.summary.commune.communesLimitrophes.filter(x => x.codeZoneSismicite === '5').map(x => x.multiPolygon), color: '#840505', opacity: 0}] :
                    undefined,
                "SISMICITE", conf.config.couchesRisques.sismicite.layer, conf.config.couchesRisques.sismicite.serveurs])

            if (this.hasRadonHaut || this.hasRadonMoyen) this.dataList.push([
                typeof this.avis.summary.commune.communesLimitrophes.map === 'function' ?
                    [{data: this.avis.summary.commune.classePotentielRadon === '1' ? [this.avis.summary.commune.multiPolygon] : [], color: '#FFD334', opacity: 0},
                        {data: this.avis.summary.commune.classePotentielRadon === '2' ? [this.avis.summary.commune.multiPolygon] : [], color: '#FF8002', opacity: 0},
                        {data: this.avis.summary.commune.classePotentielRadon === '3' ? [this.avis.summary.commune.multiPolygon] : [], color: '#840507', opacity: 0},
                        {data: this.avis.summary.commune.communesLimitrophes.filter(x => x.classePotentielRadon === '1').map(x => x.multiPolygon), color: '#FFD334', opacity: 0},
                        {data: this.avis.summary.commune.communesLimitrophes.filter(x => x.classePotentielRadon === '2').map(x => x.multiPolygon), color: '#FF8004', opacity: 0},
                        {data: this.avis.summary.commune.communesLimitrophes.filter(x => x.classePotentielRadon === '3').map(x => x.multiPolygon), color: '#840507', opacity: 0}] :
                    undefined,
                "RADON", conf.config.couchesRisques.radon.layer, conf.config.couchesRisques.radon.serveurs])

            if (this.hasPEB) this.dataList.push([
                typeof this.avis.plansExpositionBruit.map === 'function' ?
                    [{data: this.avis.plansExpositionBruit.filter(x => x.zone === 'D').map(x => x.multiPolygon), color: '#058E0C', opacity: 0},
                        {data: this.avis.plansExpositionBruit.filter(x => x.zone === 'C').map(x => x.multiPolygon), color: '#FFD332', opacity: 0},
                        {data: this.avis.plansExpositionBruit.filter(x => x.zone === 'B').map(x => x.multiPolygon), color: '#FF8000', opacity: 0},
                        {data: this.avis.plansExpositionBruit.filter(x => x.zone === 'A').map(x => x.multiPolygon), color: '#840505', opacity: 0}] :
                    undefined,
                "PEB", conf.config.couchesRisques.peb.layer, conf.config.couchesRisques.peb.serveurs])

            if (this.hasPollutionNonReglementaire) this.dataList.push([
                typeof this.avis.plansExpositionBruit.map === 'function' ?
                    [{data: this.avis.installationClasseeRayonParcelle.liste.map(x => x.ewkt), color: '#8E0800'},
                        {data: this.avis.basiasRayonParcelle.liste.map(x => x.ewkt), color: '#9E9E00'},
                        {data: this.avis.basolRayonParcelle.liste.map(x => x.ewkt), color: '#925600'}] :
                    undefined,
                "POLLUTION_NON_REG"])

            if (this.hasArgile) this.dataList.push([
                typeof this.avis.lentillesArgile.map === 'function' ?
                    [{data: this.avis.lentillesArgile.filter(x => x.niveauAlea === 1).map(x => x.multiPolygon), color: '#FFE340', opacity: 0},
                        {data: this.avis.lentillesArgile.filter(x => x.niveauAlea === 2).map(x => x.multiPolygon), color: '#FF9020', opacity: 0},
                        {data: this.avis.lentillesArgile.filter(x => x.niveauAlea === 3).map(x => x.multiPolygon), color: '#841520', opacity: 0}] :
                    undefined,
                "ARGILE", conf.config.couchesRisques.argile.layer, conf.config.couchesRisques.argile.serveurs])

            if (this.hasCanalisations) this.dataList.push([
                typeof this.avis.canalisations.map === 'function' ?
                    [{data: this.avis.canalisations, color: '#2A4999', opacity: 0}] :
                    undefined,
                "CANALISATIONS", conf.config.couchesRisques.canalisations.layer, conf.config.couchesRisques.canalisations.serveurs])

            this.feedLeaflet();
        },
        pngGenerated (png) {
            console.log('pngGenerated')
            this.pushCurrentPng(png)
        },
        debounceFetchPdf () {
            if (this.debounce) clearTimeout(this.debounce);
            this.debounce = setTimeout(() => {
                // do the work here
                this.fetchPdf()
            }, 100);
        },
        getPdf () {
            console.log('getPdf')
            // État des risques au format PDF
            this.isDownload = false
            this.isLoading = true
            this.pngList = []
            this.generatePngs()
        },
        feedLeaflet () {
            console.log("feedLeaflet")

            console.log("dataList",this.dataList)

            while (this.dataList.length !== 0) {

                let data = this.dataList.shift()

                console.log("dataList data",data)

                if (data && data.length > 0) {
                    this.currentPngName = data[1]
                    this.currentData = data[0]
                    // add WMS layer
                    if(data.length > 2){
                        this.currentWmsLayer = data[2]
                        this.currentWmsServers = data[3]
                        if(data.length > 3){
                            this.currentIdPpr = data[4]
                        }
                    } else {
                        this.currentWmsLayer = null
                        this.currentWmsServers = null
                        this.currentIdPpr = null
                    }

                    // add attribution
                    if(this.currentIdPpr != null){
                        this.currentAttribution = conf.config.attributions.PPR
                    } else if (this.currentPngName != 'carte-parcelle'){
                        this.currentAttribution = conf.config.attributions[this.currentPngName]
                    }

                    return
                }
            }

            this.currentData = ''
            this.currentWmsLayer = null
            this.currentWmsServers = null
            this.currentIdPpr = null
            this.currentAttribution = null
            this.debounceFetchPdf()
        },
        fetchPdf () {
            // FIXME : pour eviter de lancer 2 appels de l'API PDF pour une demande de PDF
            // le 1er PDF est OK mais le second a des cartes incompletes
            if(!this.isDownload){
                this._paq.push(['trackEvent', 'Flow', 'Pdf'])
                console.log('fetchPdf')
                this.isDownload = true

                let parcelles = this.form.selectedParcellesList
                let parcellesValues = ""

                // si la demande provient d'un lien partagé, la liste parcelles est vide FIXME : bug ?
                console.log("parcelles.length ",parcelles.length )
                if (parcelles.length == 0){
                    parcellesValues = this.avis.codeParcelles.join(',')
                } else {
                    parcellesValues = parcelles.join(',')
                }

                fetchWithError(this.env.apiPath + 'avis/pdf?' +
                    'codeINSEE=' + this.form.codeInsee + '&' +
                    'codeParcelle=' + parcellesValues + '&' +
                    'errial=' + this.avis.errial,
                    {
                        method: "POST",
                        headers: {
                            'Accept': 'application/json',
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(this.pngList)
                    },
                    1000 * 90)
                    .then(resp => resp.arrayBuffer())
                    .then(resp => {
                        return new Blob([resp], {type: "application/pdf"})
                    })
                    .then(resp => {
                        this.isLoading = false
                        const fileURL = window.URL.createObjectURL(resp)
                        const link = document.createElement('a')
                        link.href = fileURL
                        link.download = "ERRIAL_Parcelle_" + parcellesValues + "_" + moment(new Date()).format('DDMMYYYY') + ".pdf"
                        link.click()
                        // window.location.assign(fileURL);
                    })
            }

        },
        pushCurrentPng (png) {
            console.log('Push currentPng')

            if (png) this.pngList.push({name: this.currentPngName, png: png})
            this.feedLeaflet()
        }
    },
    computed: {
        _paq: function () {
            return window._paq
        }
    }
}
</script>

<style scoped>

#searchButtonsWrapper {
	float : left;
}

#searchButtonsWrapper a,
#actionButtonsWrapper a,
#bottomButtonsWrapper a {
	display : inline-block;
	float   : none;
}

#bottomButtonsWrapper {
	flex       : 0 0 100%;
	margin-top : 25px;
	text-align : center;
}

#actionButtonsWrapper {
	float : right;
}

@media (min-width : 630px) {
	#searchButtonsWrapper a:last-of-type,
	#actionButtonsWrapper a:last-of-type {
		margin-right : 0;
	}
}

@media (max-width : 1350px) {

	#searchButtonsWrapper {
		text-align : center;
		width      : 100%;
	}

	#actionButtonsWrapper {
		text-align : center;
		width      : 100%;
	}
}

@media (max-width : 630px) {
	#searchButtonsWrapper a,
	#actionButtonsWrapper a {
		margin-left  : 10px;
		margin-right : 10px;
	}
}

.container {
	max-width : unset;
}

.container.bordered {
	background-color : #FFFFFF;
	border           : 1px solid #CCCCCC;
	border-radius    : 2px;
	display          : flex;
	margin-bottom    : 20px;
	padding          : 20px;
	text-align       : left;
	width            : 100%;
}

.container.bordered span {
	line-height : 25px;
}

.container.bordered span.rightAlign {
	display       : inline-block;
	padding-right : 5px;
	text-align    : right;
	width         : 150px;
}

.container.bordered.ppr > div {
	display   : flex;
	flex-wrap : wrap;
}

.container.bordered.ppr div.errial_title {
	flex          : 1 0 100%;
	margin-bottom : 20px;
}

.container.bordered.ppr p {
	flex : 1 0 100%;
}

#pdf {
	margin     : 25px;
	text-align : center;
	width      : 100%;
}
</style>
