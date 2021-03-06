<template>
    <section class="section section-white"
             id="section3"
             style="margin-bottom: 40px">
        <div class="container">
            <div class="panel noborder">
                <h3 class="">Localisez votre bien pour réaliser votre état des risques réglementés,<br/>pour l'information des acquéreurs et des locataires (ERRIAL)</h3>

                <errors ref="searchErrors"/>

                <br/>

                <div id="sfp_form_wrapper"
                     v-bind:class="{'has-geoloc':leaflet.hasGeoloc}">
                    <!--                <div style="width: 40%; float: left; margin-left: 5%">-->
                    <kr-input :get-option-label-function="(option => {return option['properties']['name'] + ', ' + option['properties']['postcode'] + ' ' + option['properties']['city']})"
                              :get-option-value-function="(option => {return option['properties']['id']})"
                              :get-results-list-function="(data => {return data['features']})"
                              :start-at="3"
                              @selected="onAdresseChanged"
                              label="Adresse complète"
                              name="adresse"
                              v-bind:source="'https://api-adresse.data.gouv.fr/search/?type=housenumber&limit=10&q='">
                        <template slot="kr-no-results"
                                  slot-scope="slotProps">
                            Aucune adresse trouvée pour "{{ slotProps.query }}"
                        </template>
                    </kr-input>

                    <div class="clearfix"></div>

                    <p class="subtitle"
                       id="or"><span>Ou</span></p>

                    <kr-input :errors="error.field.codeCommune"
                              :get-option-label-function="(option => {return option['properties']['postcode'] + ' - ' + option['properties']['city']})"
                              :get-option-value-function="(option => {return option['properties']['citycode']})"
                              :get-results-list-function="(data => {return data['features']})"
                              :start-at="3"
                              @selected="onCodePostalSelected"
                              label="Nom de la commune ou code postal"
                              name="codePostal"
                              v-bind:source="'https://api-adresse.data.gouv.fr/search/?type=municipality&limit=10&q='">
                        <!--suppress JSUnresolvedVariable -->
                        <template slot="kr-option-label"
                                  slot-scope="slotProps">
                            {{ slotProps.option.properties.postcode + ' - ' + slotProps.option.properties.city }}
                        </template>
                        <!--suppress JSUnresolvedVariable -->
                        <template slot="kr-helper"
                                  slot-scope="slotProps">
                            INSEE&nbsp;: {{ slotProps.option.properties.citycode }}
                        </template>
                        <template slot="kr-no-results"
                                  slot-scope="slotProps">
                            Aucune commune trouvé pour "{{ slotProps.query }}"
                        </template>
                    </kr-input>

                    <kr-input @delayedquery="onCodeParcelleChanged"
                              label="Code de la parcelle"
                              name="codeparcelle"
                              placeholder="BA-115 ou BA-115, BA-116.
                              Séparer les numéros des parcelles pour en saisir plusieurs.">
                        <template slot="kr-helper">BA-115 ou BA-115, BA-116. Séparer les numéros des parcelles pour en saisir plusieurs.
                        </template>
                    </kr-input>

                    <div class="clearfix"></div>

                    <div style="width: 100%; display: flex; justify-content: center; margin-top: 40px;">
                        <button class="button"
                                disabled="disabled"
                                name="subscribe"
                                type="submit"
                                v-if="querying">
                            <font-awesome-icon icon="spinner"
                                               spin/>
                            Recherche en cours...<br>
                            Veuillez patienter plusieurs secondes...
                        </button>
                        <button @click="search"
                                class="bouton"
                                id="submit"
                                name="subscribe"
                                type="submit"
                                v-bind:disabled="form.selectedParcellesList.length === 0"
                                v-else>
                            <font-awesome-icon icon="search"/>
                            Afficher le résultat
                        </button>
                    </div>

                    <div id="cgu">
                        <p>En poursuivant votre navigation, vous acceptez nos <a href="https://www.georisques.gouv.fr/cgu" rel="noopener" target="_blank" title="Conditions générales d’utilisation">CGU</a>.</p>
                    </div>
                </div>

                <div id="sfp_form_leaflet_wrapper"
                     v-bind:class="{'has-geoloc':leaflet.hasGeoloc}">
                    <p>Vous pouvez ajouter ou enlever une ou des parcelles en cliquant dessus</p>
                    <Leaflet id="sfp_form_leaflet"
                             ref="sfp_form_leaflet"
                             :center="leaflet.center"
                             :parcelle-found="this.leaflet.parcelleFound"
                             @parcelleselected="onSelectedParcellesChanged"/>
                </div>
            </div>

            <div class="clearfix"></div>

        </div>
    </section>
</template>

<script>
import Errors from '../../../components/content/base/Errors'
import mixinAvis from '../../../components/mixins/avis'
import KrInput from '../../../components/ui/KrInput'
import Leaflet from "../leaflet/LeafletParcelle";
import fetchWithError from "../../../script/fetchWithError";

export default {
    name: 'SearchFormParcelle',
    mixins: [mixinAvis],
    components: {
        Leaflet,
        Errors,
        KrInput
    },
    computed: {
        _paq: function () {
            return window._paq
        }
    },
    props: {
        tinyUrl: {
            type: String,
            default: undefined
        }
    },
    data: () => ({
        cgu: true,
        error: {
            field: {
                codeCommune: []
            }
        },
        env: {
            basePath: process.env.VUE_APP_FRONT_PATH,
            apiPath: process.env.VUE_APP_BACK_API_PATH
        }
    }),
    methods: {
        onAdresseChanged (option) {
            if (option.properties) {
                this.form.codeInsee = option['properties']['citycode']
                this.form.nomAdresse = option['properties']['name']
                this.form.geolocAdresse = option['geometry']['coordinates']['0'] + '|' + option['geometry']['coordinates']['1']
                this.leaflet.center = [parseFloat(option['geometry']['coordinates']['1']), parseFloat(option['geometry']['coordinates']['0'])]
                this.showLeaflet(true)

                fetchWithError(this.env.apiPath + "cadastre/proximite/" + option['geometry']['coordinates']['0'] + "/" + option['geometry']['coordinates']['1'], null, 1000 * 10)
                .then(r => {
                   if (r.status !== 200) {
                        this.$refs.searchErrors.sendError("Le service de recherche n'est pas disponible pour le moment, veuillez réessayer plus tard.")
                        return
                    }             
                    r.json().then(data => {
                        if (data) {
                            this.leaflet.parcelleFound = data.prefixe + '-' + data.section + '-' + data.numero + '@' + data.commune
                        }
                    })
                })

            } else {
                this.form.codeInsee = ''
                this.form.nomAdresse = ''
                this.form.geolocAdresse = ''
                this.leaflet.center = [0, 0]
                this.clearSelectedParcelles()
            }
        },
        onCodeParcelleChanged (value) {
            this.form.codeParcelle = value
            this.checkAndGetParcelles()
        },
        onCodePostalSelected (option) {
            this.form.codeInsee = option['properties']['citycode']
            this.checkAndGetParcelles()
        },
        onSelectedParcellesChanged (array) {
            this.form.selectedParcellesList = array
        },
        search () {
            this.getAvis();
        },
        checkAndGetParcelles () {

            if (this.form.codeInsee && this.form.codeParcelle && this.form.codeParcelle.length >= 2) {

                this.$refs.searchErrors.clearWarnings()
                this.$refs.searchErrors.clearErrors()

                fetchWithError(this.env.apiPath + "cadastre/match/" + this.form.codeInsee + "/" + this.form.codeParcelle, null, 1000 * 10).then(r => {
                   if (r.status !== 200) {
                        this.$refs.searchErrors.sendError("Le service de recherche n'est pas disponible pour le moment, veuillez réessayer plus tard.")
                        return
                    }
                    r.json().then(data => {
                        if (data && data.parcelle) {
                            this.leaflet.center = [parseFloat(data.centroid.y), parseFloat(data.centroid.x)]
                            this.showLeaflet(true)
                            this.leaflet.parcelleFound = data.parcelle.prefixe + '-' + data.parcelle.section + '-' + data.parcelle.numero + '@' + data.parcelle.commune
                        } else {
                            this.clearSelectedParcelles()
                            if (this.form.codeParcelle !== '') this.$refs.searchErrors.sendWarning('Aucune parcelle n\'a été trouvé avec le code suivant : ' + this.form.codeParcelle + '.')
                            else this.$refs.searchErrors.sendWarning('Malheureusement, le service n\'est pas disponible pour votre adresse. Vous pouvez retrouver ces informations à votre préfecture.')
                        }
                    })
                    .catch((e) => {
                        console.log(e)
                        this.$refs.searchErrors.sendError('Une erreur est survenue. Merci de réessayer ultérieurement.')
                    })})
            }
            if (!this.form.codeParcelle) {
                this.clearSelectedParcelles()
            }
        },
        clearSelectedParcelles () {
            this.leaflet.parcelleFound = ''
            this.form.selectedParcellesList = []
            this.showLeaflet(false)
        },
        showLeaflet (visible) {
            setTimeout(() => {
                this.$refs.sfp_form_leaflet.invalidate()
            }, 1000);
            this.leaflet.hasGeoloc = visible
        },
        loadAvis (codeAvis) {
            console.log('loadAvis')
            // this.visibility.modifier = false
            this.$emit('loading')
            this.$emit('setflow', 0)
            fetch(this.env.apiPath + 'url?' + 'code=' + codeAvis)
               .then(r => {
                    if (r.status !== 200) {
                        // console.log('Wrong code')
                        if(r.status === 422){
                            this.$refs.searchErrors.sendError("Oups ! Votre recherche n'a pas été trouvée :-(.")
                            this.$refs.searchErrors.sendError("Si nous l'avons perdue, veuillez bien vouloir nous en excuser.")
                        } else { // 500
                            this.$refs.searchErrors.sendError("Le service de recherche n'est pas disponible pour le moment, veuillez réessayer plus tard.")
                        }

                        this.$emit('loaded')
                        return
                    }
                    r.json().then(value => {
                        console.log(value)
                        let array = value.url.split('|&|')
                        console.log(array)

                        this.form.selectedParcellesList = array[0].split(',')
                        this.form.codeInsee = array[1]
                        this.form.nomAdresse = array[2]

                        console.log(this.form)

                        this.getAvis()
                    })
               }) 
        }
    },
    watch: {
        tinyUrl: function () {
            console.log("watched")
            this.loadAvis(this.tinyUrl)
        }
    }
}
</script>

<style scoped>

h3 {
	text-align : center;
}

#or {
	color    : #222933;
	margin   : 30px 0 15px 0;
	position : relative;
}

#or span:before,
#or span:after {
	border-bottom : 2px solid #0053B3;
	content       : '';
	overflow      : hidden;
	position      : absolute;
	top           : 50%;
	width         : 45%;
}

#or span:before {
	right : 55%;
}

#or span:after {
	left : 55%;
}

.container {
	max-width : unset;
}

#sfp_form_wrapper {
	float      : left;
	margin     : 80px calc(50% - 450px / 2) 0;
	transition : all 0.5s;
	width      : 450px;
}

#sfp_form_wrapper.has-geoloc {
	float  : left;
	margin : 80px 50px 0 0;
}

#sfp_form_leaflet_wrapper {
	float      : left;
	height     : 632px;
	transition : all 0.5s;
	visibility : hidden;
	width      : 0;
}

#sfp_form_leaflet {
	float  : left;
	height : 600px;
}

#sfp_form_leaflet_wrapper p {
	color      : white;
	margin     : 5px;
	text-align : center;
	transition : all 0.5s;
	width      : 100%;
}

#sfp_form_leaflet_wrapper.has-geoloc p {
	color : black;
}

#sfp_form_leaflet_wrapper.has-geoloc {
	visibility : visible;
	width      : calc(100% - 500px)
}

.panel.noborder {
	border     : none;
	box-shadow : unset;
}

#cgu {
	/*margin : 0px 5% 0;*/
}

#cgu p {
	color      : rgb(50, 50, 50);
	font-size  : 0.85em;
	margin     : 0;
	text-align : center;
}

#cgu p a {
	color           : rgb(50, 50, 50);
	text-decoration : underline;
}

#cgu p a:visited {
	color           : rgb(50, 50, 50);
	text-decoration : underline;
}
</style>
