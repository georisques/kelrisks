<template>
    <div class="leaflet">
        <l-map :center="center"
               :zoom="18"
               :options="{attributionControl: false, zoomControl: false}"
               ref="leafletParcelles">
            <l-tile-layer :url="url"/>
            <l-control-zoom zoomInTitle="Vue rapprochée" zoomOutTitle="Vue éloignée" position="topleft"></l-control-zoom>
            <l-control-attribution position="bottomright" prefix="<a href='https://www.ign.fr/' target='_blank'>IGN</a> | <a href='https://cadastre.data.gouv.fr/datasets/cadastre-etalab' target='_blank'>Etalab</a>"></l-control-attribution>
            <l-geo-json :geojson="parseJSONMap(data.parcelles)"
                        :options="parcellesOptions"
                        :options-style="parcellesStyleFunction"
                        :ref="'parcelle_' + reference"
                        v-if="data && currentZoom >= 17"/>
            <l-marker :icon="createAdresseIcon"
                      :lat-lng="center"
                      v-if="center"/>
        </l-map>
    </div>
</template>

<script>
import {LGeoJson, LMap, LMarker, LTileLayer, LControlZoom, LControlAttribution} from 'vue2-leaflet';
import fetchWithError from "../../../script/fetchWithError";
import {icon} from "leaflet";
import mixinLeaflet from "./leaflet_common";

export default {
    name: "LeafletParcelle",
    mixins: [mixinLeaflet],
    components: {
        LMap,
        LTileLayer,
        LMarker,
        LGeoJson,
        LControlZoom,
        LControlAttribution
    },
    props: {
        center: {
            type: Array,
            default: () => [48.8737762014, 2.2950365488]
        },
        parcelleFound: {
            type: String,
            default: undefined
        }
    },
    data: () => ({
        currentZoom: 18,
        data: null,
        selectedList: [],
        env: {
            apiPath: process.env.VUE_APP_BACK_API_PATH
        }
    }),
    methods: {
        invalidate () {
            // console.log('invalidated')
            this.$refs.leafletParcelles.mapObject.invalidateSize()
        },
        updateParcelles (x, y) {

            fetchWithError(this.env.apiPath + "cadastre/" + x + "/" + y, null, 1000 * 10).then(r => {
                   if (r.status !== 200) {
                        this.$refs.searchErrors.sendError("Le service de recherche n'est pas disponible pour le moment, veuillez réessayer plus tard.")
                        return
                    }             
                    r.json().then(data => {
                        this.data = {
                            parcelles: data
                        }
                    }
            )})
        },
        initLayerClick (layer) {
            if (this.selectedList.includes(layer.id)) {
                layer.setStyle({fillColor: '#FF0000', color: '#FF0000'})
                layer.selected = true
            } else {
                layer.selected = false
            }

            layer.on('click', () => {
                layer.selected = !layer.selected

                layer.selected ? layer.setStyle({fillColor: '#FF0000', color: '#FF0000'}) : layer.setStyle({fillColor: '#455674', color: '#455674'})

                if (layer.id) {
                    if (layer.selected) {
                        this.selectedList.push(layer.id)
                    } else {
                        this.selectedList = this.selectedList.filter(function (element) {
                            return element !== layer.id;
                        })
                    }
                }

                this.$emit('parcelleselected', this.selectedList)
            });

            this.$emit('parcelleselected', this.selectedList)
        }
    },
    computed: {
        createAdresseIcon () {
            return icon({
                iconUrl: '/images/leaflet/adresse.svg',
                shadowUrl: '/images/leaflet/shadow.png',
                iconSize: [35, 35], // width and height of the image in pixels
                shadowSize: [30, 22], // width, height of optional shadow image
                iconAnchor: [17, 35], // point of the icon which will correspond to marker's location
                shadowAnchor: [0, 24],  // anchor point of the shadow. should be offset
                popupAnchor: [0, 0] // point from which the popup should open relative to the iconAnchor
            })
        },
        parcellesOptions () {
            return {
                onEachFeature: this.onEachFeatureFunction,
            };
        },
        parcellesStyleFunction () {
            return () => {
                return {
                    weight: 2,
                    color: "#455674",
                    opacity: 0.6,
                    fillColor: "#455674",
                    fillOpacity: 0.1
                };
            };
        },
        onEachFeatureFunction () {
            return (feature, layer) => {
                layer.bindTooltip(
                    () => {
                        let divs = ''
                        divs = divs.concat(
                            '<div>', 'Code INSEE', ' : ', feature.properties['commune'], '</div>',
                            '<div>', 'Code parcelle', ' : ', feature.properties['code'], '</div>'
                            )
                        return divs
                    },
                    {permanent: false, sticky: true}
                );

                if (feature.properties['code']) {
                    layer.id = ''.concat(feature.properties['code'], '@', feature.properties['commune'])
                }

                this.initLayerClick(layer)
            };
        }
    },
    watch: {
        parcelleFound: function () {
            if (this.parcelleFound && this.parcelleFound !== '') {
                this.selectedList = [this.parcelleFound]
            } else this.selectedList = []
        }
    },
    mounted () {

        this.reference = this._uid

        this.$nextTick(() => {

            const map = this.$refs.leafletParcelles.mapObject

            map.on('moveend', () => {
                if (this.currentZoom >= 17) this.updateParcelles(map.getCenter().lng, map.getCenter().lat)
            })
            map.on('zoomend', () => {
                this.currentZoom = map.getZoom()
            })
        })

        // pour eviter au demarrage de l'appli d'appeler le cadastre avec 0,0
        let y = this.center[1]
        if(y != 0){
            this.updateParcelles(this.center[0], y)
        }
    }
}
</script>

<style scoped>
    .leaflet {
        width  : 100%;
        height : 100%;
    }
</style>