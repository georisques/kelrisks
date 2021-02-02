<!-- Il y a une seule Map qui affiche toutes les donnees -->
<template>
    <div class="leaflet_wrapper leaflet_pdf">
        <l-map :center="maxZoomCenter"
               :id="'leafletMap_' + reference"
               :ref="'leafletMap_' + reference"
               :zoom="zoom"
               :options="{attributionControl: false}"
               v-if="reference !== null">
            <l-tile-layer :ref="'lTile_' + reference"
                          :url="url"
                          @load="tilesLoaded = true"
                          @loading="tilesLoaded = false"/>
            <l-geo-json :geojson="getData(parcelle)"
                        :options="featureOptions"
                        :options-style="styleFunction('#455674')"
                        :ref="'lGeoJson_Parcelle_' + reference"
                        v-if="parcelle && parcelle.length !== 0"/>
            <l-geo-json :geojson="getData(data)"
                        :options="featureOptions"
                        :options-style="styleFunction('#455674')"
                        :ref="'lGeoJson_' + reference"
                        v-if="typeof data === 'string' && data.length !== 0"/>
            <l-geo-json :geojson="getData(json.data)"
                        :key="json.color + '_' + index"
                        :options="featureOptions(json.color)"
                        :options-style="styleFunction(json.color, json.opacity)"
                        :ref="'lGeoJson_' + reference + '_' + index"
                        v-else-if="data.length !== 0"
                        v-for="(json, index) in data"/>
        </l-map>
    </div>
</template>

<script>
import {divIcon, marker} from "leaflet";
import {LGeoJson, LMap, LTileLayer} from 'vue2-leaflet';
import mixinLeaflet from "./leaflet_common";
import domtoimage from "dom-to-image";

export default {
    name: "LeafletPdf",
    mixins: [mixinLeaflet],
    components: {
        LMap,
        LTileLayer,
        LGeoJson
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
    data: () =>({
        // image de la carte pour pdf quand le temps pour charger la carte depasse 30s
        // image dans le dossier public/images/map_hors_service.png
        // https://www.base64-image.de/
        errorMapImgBase64: "data:image/png;base64,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9SqtVou1a9dCp9PhN7/5DZqammC1WtHR0YG8vDy8+OKLAIAXXnjB6+G/detWHDp0CK+//jouXryImzdvwmq1or29HY2NjfjnP/+J/fv3D9k6bcecy8vLnQbvhx9+CI1Gg7/85S/8BBANM5978UX5+vrizTffREtLC3bv3o3du3c7POf48S/+GYkAAAKBSURBVOOqvRSGkkajwa5du2CxWLB//36n4ZqUlDRk63zmmWdw4MABLFmyxG46r0gjYuh6VVBQEHQ6nXIZcFFREYKDg7Fs2TKsXLlS9TJgb/D398ehQ4eQlJSEvLw8FBYWoq6uDgsXLsSsWbOwePFi5TDEUIiKioJer0dGRgYKCgpQXV3NrZxoBOFowEREw4inuomIGLpERAxdIiJi6BIRMXSJiIihS0TE0CUiYugSERFDl4iIoUtERAxdIiKGLhERQ5eIiBi6REQMXSIiYugSETF0iYiIoUtExNAlImLoEhERQ5eIiKFLREQMXSIihi4REUOXiIgYukREDF0iImLoEhExdImIiKFLRMTQJSJi6BIREUOXiIihS0REDF0iIoYuERFDl4iIGLpERAxdIiJi6BIRMXSJiIihS0TE0CUiYugSERFDl4iIoUtERAxdIiKGLhERQ5eIiBi6REQMXSIiYugSETF0iYgYukRExNAlImLoEhERQ5eIiKFLREQMXSIihi4REUOXiIgYukREDF0iImLoEhExdImIGLpERMTQJSJi6BIREUOXiIihS0REDF0iIoYuERFDl4iIGLpERAxdIiJi6BIRMXSJiBi6RETE0CUiYugSERFDl4iIoUtERAxdIiKGLhERQ5eIiBi6REQMXSIiYugSETF0iYgYukRExNAlImLoEhERQ5eIiKFLRMTQJSIihi4REUOXiIgYukREDF0iImLoEhExdImIGLpERMTQJSJi6BIREUOXiIihS0TE0CUiIoYuERFDl4iIGLpERAxdIiJi6BIRMXSJiBi6RETE0CUiYugSERFDl4iIoUtExNAlIiKGLhERQ5eIiNz0/wCHAnaMF4s4RAAAAABJRU5ErkJggg=="
    }),
    methods: {
        centerMap () {
            console.log("centerMap PDF")
            let map = this.$refs['leafletMap_' + this.reference].mapObject

            let bounds = null

            for (let ref in this.$refs) {

                if (!ref.includes('lGeoJson_')) continue

                let lGeoJson = Array.isArray(this.$refs[ref]) ? this.$refs[ref][0] : this.$refs[ref]

                if (lGeoJson && typeof lGeoJson.getBounds === 'function') {

                    if (Object.prototype.hasOwnProperty.call(lGeoJson.getBounds(), "_northEast")) {
                        if (bounds === null) bounds = lGeoJson.getBounds()

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

                this.updateMapUntilFitsBounds(map, 'leafletMap_' + this.reference, bounds, true, false)
            }
            // pas le temps de virer les couches WMS
            //this.addWmsLayer('leafletMap_' + this.reference)
        },
        getData (data) {
            console.log(('getData : ' + data).substr(0, 100))
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
        exportToPng (timeout) {
            console.log('exportToPng')
            
            if (this.mapCentered) {
                console.log('export !')
                setTimeout(() => {
                    domtoimage.toPng(document.getElementById('leafletMap_' + this.reference), {
                        // FIXME : bizarre avec une taille j'ai des tuiles qui plantent ('trou' gris) mais l'image fait la bonne taille (pas de whitespace)...
                        // width: 349,
                        // height: 300
                        }).then((dataUrl) => {
                            let img = new Image()
                            img.src = dataUrl
                            // document.body.appendChild(img);
                            this.$emit('png', dataUrl)
                        })
                        .catch((error) => {
                            let img = new Image()
                            let dataUrl = this.errorMapImgBase64
                            img.src = dataUrl
                            this.$emit('png', dataUrl)
                            console.error('oops, something went wrong!', error);
                        })
                }, timeout)
            }
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
        console.log("mounted pdf")
        this.reference = this._uid

        this.$nextTick(() => {
            console.log("mounted pdf2")

            this.crippleMap('leafletMap_' + this.reference)

            let map = this.$refs['leafletMap_' + this.reference].mapObject

            map.on('zoomend', () => {
                this.currentZoom = map.getZoom()
            })
        })
    },
    watch: {
        data: function () {
            console.log("watch PDF",this.data)

            this.mapCentered = false

            if (this.data !== []) {
                setTimeout(() => {

                    this.$refs['leafletMap_' + this.reference].mapObject.invalidateSize()
                    this.centerMap()
                }, 250);
            }
        },
        mapCentered: function () {
            if (this.mapCentered) {
                console.log("mapCentered PDF");
                let mapRef = 'leafletMap_' + this.reference
                let hasWmsLayer = false

                // pour ajuster le time out en fonction de la carte, si WMS, 
                // timeout plus grand pour avoir le temps de charger et afficher les couches WMS sur la carte
                // dans le code d'origine c'est 250
                let map = this.$refs[mapRef].mapObject
                map.eachLayer(function(layer){
                    if (layer.options.layers){ 
                        hasWmsLayer = true
                        return
                    }
                })
                console.log("hasWmsLayer",hasWmsLayer)
                let timeout = hasWmsLayer ? 4000 : 1000

                this.addWmsLayer(mapRef, true)
                this.exportToPng(timeout)
            }
        }
    }
}
</script>

<style>
.leaflet_wrapper {
	height : 300px;
	width  : 349px;
}

.leaflet_pdf div.leaflet-control-attribution {
	display : none !important;
}
</style>