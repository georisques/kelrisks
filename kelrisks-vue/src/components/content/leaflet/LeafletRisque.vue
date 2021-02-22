<template>
    <div class="leaflet_wrapper">
        <l-map :center="maxZoomCenter"
               v-if="reference !== null"
               :id="'leafletMap_' + reference"
               :ref="'leafletMap_' + reference"
               :options="{attributionControl: false}"
               >
            <l-control-attribution position="bottomright" prefix="<a href='https://www.ign.fr/' target='_blank'>IGN</a> | <a href='https://cadastre.data.gouv.fr/datasets/cadastre-etalab' target='_blank'>Etalab</a>"></l-control-attribution>
            <l-tile-layer :url="url"/>
            <l-geo-json :geojson="getData(parcelle)"
                        :options="featureOptions"
                        :options-style="styleFunction('#455674')"
                        :ref="'lGeoJson_Parcelle_' + reference"/>
            <!-- un data sans color ? && wmsLayer == null"-->
            <l-geo-json :geojson="getData(data)"
                        :ref="'lGeoJson_' + reference"
                        :options="featureOptions"
                        :attribution="attribution"
                        :options-style="styleFunction('#455674')"
                        v-if="typeof data === 'string' "/>

                        <!-- v-else-if="wmsLayer == null" -->
                        <!-- v-else: -->
            <l-geo-json :geojson="getData(json.data)"
                        :ref="'lGeoJson_' + reference + '_' + index"
                        
                        v-else:
                        :key="json.color + '_' + index"
                        :options="featureOptions(json.color)"
                        :attribution="attribution"
                        :options-style="styleFunction(json.color, json.opacity)"
                        v-for="(json, index) in data"/>
        </l-map>
    </div>
</template>

<script>
import {divIcon, marker} from "leaflet";
import {LGeoJson, LMap, LTileLayer, LControlAttribution} from 'vue2-leaflet';
import mixinLeaflet from "./leaflet_common";

export default {
    name: "LeafletRisque",
    mixins: [mixinLeaflet],
    components: {
        LMap,
        LTileLayer,
        LGeoJson,
        LControlAttribution
    },
    props: {
        parcelle: {
            type: [String, Array],
            default: () => []
        },
        data: {
            type: [String, Array],
            default: () => []
        }
    },
    data: () => ({}),
    methods: {
        centerMap () {
            console.log("centerMap")
            let map = this.$refs['leafletMap_' + this.reference].mapObject
            let bounds = null

            // FIXME : peut être un bug, si on a une liste de geom, il zoom sur la derniere geom
            for (let ref in this.$refs) {
                if (!ref.includes('lGeoJson_')) {
                    continue
                }
                let lGeoJson = Array.isArray(this.$refs[ref]) ? this.$refs[ref][0] : this.$refs[ref]

                if (typeof lGeoJson.getBounds === 'function') {

                    if (Object.prototype.hasOwnProperty.call(lGeoJson.getBounds(), "_northEast")) {
                        if (bounds === null) bounds = lGeoJson.getBounds()
                        console.log(this.wmsLayer,bounds)
                        if (bounds._northEast.lat < lGeoJson.getBounds()._northEast.lat) bounds._northEast.lat = lGeoJson.getBounds()._northEast.lat
                        if (bounds._northEast.lng < lGeoJson.getBounds()._northEast.lng) bounds._northEast.lng = lGeoJson.getBounds()._northEast.lng
                        if (bounds._southWest.lat > lGeoJson.getBounds()._southWest.lat) bounds._southWest.lat = lGeoJson.getBounds()._southWest.lat
                        if (bounds._southWest.lng > lGeoJson.getBounds()._southWest.lng) bounds._southWest.lng = lGeoJson.getBounds()._southWest.lng

                    }
                }
            }

            if (bounds !== null) {

                if (!this.isCenterDefault() && this.maxZoomCenter) {

                    if (bounds._northEast.lat < this.maxZoomCenter[0]) bounds._northEast.lat = this.maxZoomCenter[0] + 0.0015
                    if (bounds._northEast.lng < this.maxZoomCenter[1]) bounds._northEast.lng = this.maxZoomCenter[1] + 0.0015
                    if (bounds._southWest.lat > this.maxZoomCenter[0]) bounds._southWest.lat = this.maxZoomCenter[0] - 0.0015
                    if (bounds._southWest.lng > this.maxZoomCenter[1]) bounds._southWest.lng = this.maxZoomCenter[1] - 0.0015
                }

                this.updateMapUntilFitsBounds(map, 'leafletMap_' + this.reference, bounds, true, true)
                // pas mieux
                //this.addWmsLayer('leafletMap_' + this.reference)
            }
        },
        getData (data) {
            if (typeof data === 'string') return this.parseJSON(data)
            return this.parseJSONMap(data)
        },
        styleFunction (color, opacity) {
            let fillOpacity
            if (!color) color = "#455674"

            // opacity a 0 pour ne pas afficher la couche geojson
            if (opacity !== 0) {
                opacity = 0.8
                fillOpacity = 0.2
            } else {
                fillOpacity = 0
            }

            return () => {
                return {
                    weight: 2,
                    color: color,
                    opacity: opacity,
                    fillColor: color,
                    fillOpacity: fillOpacity
                };
            };
        },
        onEachFeatureFunction () {
            return (feature, layer) => {
                // TODO a supprimer ne semble plus utiliser
                layer.bindTooltip(
                    () => {
                        let divs = ''
                        for (let property in feature.properties) {
                            let value = feature.properties[property]
                            let label = "";

                            if (property.startsWith("'")) {
                                label = property.substring(1, property.length - 1)
                            } else {
                                label = property.replace(/([A-Z])/gm, function (v) {
                                    return ' ' + v.toLowerCase()
                                }).replace(/(^.)/gm, function (v) {
                                    return v.toUpperCase()
                                })
                            }
                            divs = divs.concat('<div>', label, ' : ', value, '</div>')
                        }

                        return divs
                    },
                    {permanent: false, sticky: true}
                );
                layer.on('click', function (e) {
                    console.log(e)
                })
            };
        }
    },
    computed: {
        featureOptions () {
            return color => ({
                onEachFeature: this.onEachFeatureFunction,
                pointToLayer: this.createIcon(color)
            });
        },
        createIcon () {

            return myCustomColour => {

                const markerHtmlStyles =
                    ' background-color: ' + myCustomColour + '99;' +
                    ' width: 1.5rem;' +
                    ' height: 1.5rem;' +
                    ' display: block;' +
                    ' left: -0.75rem;' +
                    ' top: -0.75rem;' +
                    ' position: relative;' +
                    ' border-radius: 1.5rem 1.5rem 0;' +
                    ' transform: rotate(45deg);' +
                    ' border: 1px solid #FFFFFF99'

                return (feature, latlng) => {
                    let myIcon = divIcon({
                        className: "my-custom-pin",
                        iconAnchor: [0, 24],
                        labelAnchor: [-6, 0],
                        popupAnchor: [0, -36],
                        html: '<span style="' + markerHtmlStyles + '" />'
                    })
                    return marker(latlng, {icon: myIcon})
                };
            }
        }
    },
    mounted () {

        this.reference = this._uid
        console.log(this.reference + " => mounted")

        this.$nextTick(() => {
            console.log("$nextTick")
            // console.log(this.reference + " => $nextTick")
            let ref = 'leafletMap_' + this.reference
            this.crippleMap(ref)
            this.centerMap()

            // FIXME time out pour eviter de charger la couche WMS a chaque fois que la carte chanque emprise a cause du centerMap()
            // 2000 ce n'est pas assez
            setTimeout(() => {
                this.addWmsLayer(ref)
            }, 2500);    
            // if (!this.isCenterDefault()) this.injectCustomZoomControl('leafletMap_' + this.reference)
        })
        window.addEventListener('resize', this.centerMap)
    },
    beforeDestroy: function () {
        window.removeEventListener('resize', this.centerMap)
    },
    watch: {
        data: function () {

            setTimeout(() => {

                this.$refs['leafletMap_' + this.reference].mapObject.invalidateSize()
                this.centerMap()
            }, 2000);
        }
    }
}
</script>

<style scoped>
.leaflet_wrapper {
	height : 100%;
	width  : 100%;
}
</style>