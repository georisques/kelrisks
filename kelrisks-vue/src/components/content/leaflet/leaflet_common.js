import JQuery from 'jquery'
// Pour charger le plugin Leaflet.NonTiledLayer
// https://github.com/ptv-logistics/Leaflet.NonTiledLayer
import L from 'leaflet';
//import 'leaflet.nontiledlayer/dist/NonTiledLayer.js';
// version custom car sinon erreur au moment de l'export oops, something went wrong! DOMException: The operation is insecure.
// cf les TODO dans le fichier
import './NonTiledLayer-src.js';
import * as conf from "../../../config.js";

let $ = JQuery

export default {
    data:  function () { 
        let url;
        // FIXME : mettre adresse service carto en config
        if (document.domain === 'localhost'){
            // https://geoservices.ign.fr/blog/2017/06/28/geoportail_sans_compte.html
            // y a pas toutes les couches avec la clef pratique
            url = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
        } else {
            let key;
            if(window.DOMAIN_MATOMO === 'brgm-rec'){
                key = conf.config.clefsIGN.recette
            } else {
                key = conf.config.clefsIGN.production
            }
            // ne gere pas encore les DOM 'GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2'; 
            let layer = conf.config.coucheIGN
            url = "https://wxs.ign.fr/"+key+"/geoportail/wmts?" +
            "&REQUEST=GetTile&SERVICE=WMTS&VERSION=1.0.0" +
            "&STYLE=normal" +
            "&TILEMATRIXSET=PM" +
            //"&FORMAT=image/png"+
            "&FORMAT=image/jpeg" +
            "&LAYER=" + layer + 
            "&TILEMATRIX={z}" +
            "&TILEROW={y}" +
            "&TILECOL={x}";
        }
        return { 
        url: url,
        zoom: 16,
        bounds: null,
        reference: null,
        interval: null,
        currentZoom: null,
        minZoomCenter: {x: null, y: null},
        intermediateZoomLevels: [],
        mapCentered: false,
        tilesLoaded: true,
        zooming: false,
        currentMaxZoom: 18,
        currentMinZoom: null
        }
    },
    props: {
        maxZoomCenter: {
            type: Array,
            default: () => [0, 0]
        },
        maxZoom: {default: () => null},
        minZoom: {default: () => null},
        wmsLayer: {default: () => null},
        // urls des serveurs WMS de recette et prod
        wmsServers: {
            type: Object,
            default: () => null
        },
        codesCommunes: {
            type: Array,
            default: () => []
        },
        bboxRisque: {
            type: Array,
            default: () => []
        }
    },
    methods: {
        crippleMap (ref) {

            let map = this.$refs[ref].mapObject

            map.zoomControl.disable()
            map.zoomControl.remove()
            map.touchZoom.disable()
            map.doubleClickZoom.disable()
            map.scrollWheelZoom.disable()
            map.boxZoom.disable()
            map.keyboard.disable()

            map.dragging.disable()
        },
        zoomIn (map, zoom) {

            this.zooming = true

            let zoomInControl = $("#leafletMap_" + this.reference + " a.leaflet-control-zoom-in")
            let zoomOutControl = $("#leafletMap_" + this.reference + " a.leaflet-control-zoom-out");

            if (this.currentZoom >= this.currentMaxZoom) return

            if (zoomOutControl.hasClass('leaflet-disabled')) {

                zoomOutControl.removeClass('leaflet-disabled')
            }

            if (zoom) this.currentZoom = zoom
            else ++this.currentZoom

            if (this.currentZoom === this.currentMaxZoom) zoomInControl.addClass('leaflet-disabled')
            if (this.currentZoom === this.currentMinZoom) zoomOutControl.addClass('leaflet-disabled')

            console.log(this.intermediateZoomLevels)
            console.log(this.currentZoom)

            map.setView({lat: this.intermediateZoomLevels[this.currentZoom].y, lng: this.intermediateZoomLevels[this.currentZoom].x}, this.currentZoom, {animation: true});
        },
        zoomOut (map, zoom) {

            this.zooming = true

            let zoomInControl = $("#leafletMap_" + this.reference + " a.leaflet-control-zoom-in")
            let zoomOutControl = $("#leafletMap_" + this.reference + " a.leaflet-control-zoom-out");

            if (this.currentZoom <= this.currentMinZoom) return

            if (zoomInControl.hasClass('leaflet-disabled')) {

                zoomInControl.removeClass('leaflet-disabled')
            }

            if (zoom) this.currentZoom = zoom
            else --this.currentZoom

            if (this.currentZoom === this.currentMaxZoom) zoomInControl.addClass('leaflet-disabled')
            if (this.currentZoom === this.currentMinZoom) zoomOutControl.addClass('leaflet-disabled')

            map.setView({lat: this.intermediateZoomLevels[this.currentZoom].y, lng: this.intermediateZoomLevels[this.currentZoom].x}, this.currentZoom, {animation: true});
        },
        injectCustomZoomControl (ref) {

            let map = this.$refs[ref].mapObject

            let control = "<div class=\"leaflet-control-zoom leaflet-bar leaflet-control\">" +
                "   <a class=\"leaflet-control-zoom-in  leaflet-disabled\" title=\"Vue rapprochée\" role=\"button\" aria-label=\"Vue rapprochée\">+</a>" +
                "   <a class=\"leaflet-control-zoom-out leaflet-disabled\" title=\"Vue éloignée\" role=\"button\" aria-label=\"Vue éloignée\">−</a>" +
                "</div>"

            $("#leafletMap_" + this.reference + " div.leaflet-top.leaflet-left").html(control)

            let zoomInControl = $("#leafletMap_" + this.reference + " a.leaflet-control-zoom-in")
            let zoomOutControl = $("#leafletMap_" + this.reference + " a.leaflet-control-zoom-out");

            if (this.currentMaxZoom > this.currentZoom) zoomInControl.removeClass('leaflet-disabled')
            if (this.currentMinZoom < this.currentZoom) zoomOutControl.removeClass('leaflet-disabled')

            zoomInControl.click((e) => {

                e.stopPropagation()
                this.zoomIn(map);
            });

            zoomOutControl.click((e) => {

                e.stopPropagation()
                this.zoomOut(map);
            });
        },
        parseJSON (json) {
            if (json !== '' && json !== undefined) {
                return JSON.parse(json)
            }
            return {
                "type": "FeatureCollection",
                "features": []
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
        getBoundsCenter (bounds) {
            let center = {x: null, y: null}

            center.x = bounds._southWest.lng + Math.abs(bounds._northEast.lng - bounds._southWest.lng) / 2
            center.y = bounds._southWest.lat + Math.abs(bounds._northEast.lat - bounds._southWest.lat) / 2

            return center
        },
        initIntermediateZoomLevels () {

            console.log("initIntermediateZoomLevels")

            console.log(this.currentMaxZoom)

            this.intermediateZoomLevels[this.currentMaxZoom] = {x: this.maxZoomCenter[1], y: this.maxZoomCenter[0]}

            let deltaX = Math.abs(this.minZoomCenter.x - this.maxZoomCenter[1])
            let deltaY = Math.abs(this.minZoomCenter.y - this.maxZoomCenter[0])
            let remainingX = deltaX
            let remainingY = deltaY
            let currentDeltaX = 0
            let currentDeltaY = 0

            const increaseRatio = 3 / 4

            console.log(this.currentMinZoom)

            for (let zoomLevel = this.currentMinZoom; zoomLevel < this.currentMaxZoom; zoomLevel++) {

                let currentCenter = {x: null, y: null}

                currentDeltaX += remainingX * increaseRatio
                currentDeltaY += remainingY * increaseRatio

                currentCenter.x = this.minZoomCenter.x + (this.minZoomCenter.x < this.maxZoomCenter[1] ? currentDeltaX : -currentDeltaX)
                currentCenter.y = this.minZoomCenter.y + (this.minZoomCenter.y < this.maxZoomCenter[0] ? currentDeltaY : -currentDeltaY)

                remainingX = deltaX - currentDeltaX
                remainingY = deltaY - currentDeltaY

                this.intermediateZoomLevels[zoomLevel] = currentCenter
            }
        },
        initMapZoom (map, mapRef, injectZoomControls) {

            if (!this.currentMinZoom) {

                this.currentMinZoom = map.getZoom()

                this.minZoomCenter = this.getBoundsCenter(map.getBounds())
                this.intermediateZoomLevels[this.currentMinZoom] = this.minZoomCenter

                this.currentZoom = this.currentMinZoom

                if (injectZoomControls) this.injectCustomZoomControl(mapRef)
            }

            this.initIntermediateZoomLevels();

            if (this.minZoom) {
                console.log("zoomIn")
                this.currentMinZoom = this.minZoom
                console.log(this.currentZoom)
                console.log(this.currentMinZoom)
                if (this.currentZoom < this.currentMinZoom) this.zoomIn(map, this.currentMinZoom)
            }
            if (this.maxZoom) {
                this.currentMaxZoom = this.maxZoom
                if (this.currentZoom > this.currentMaxZoom) this.zoomOut(map, this.currentMaxZoom)
            }
            if (!this.zooming) this.mapCentered = true
        },
        updateMapUntilFitsBounds (map, mapRef, bounds, initZoom, injectZoomControls) {

            this.mapCentered = false

            if (!this.interval) {

                this.interval = setInterval(() => {

                    map.fitBounds(bounds)
                    map.invalidateSize()

                    let mapWidth = map.getBounds()._northEast.lng - map.getBounds()._southWest.lng
                    let boundWidth = bounds._northEast.lng - bounds._southWest.lng
                    let widthFillPercent = boundWidth / mapWidth

                    let mapHeight = map.getBounds()._northEast.lat - map.getBounds()._southWest.lat
                    let boundHeight = bounds._northEast.lat - bounds._southWest.lat
                    let heightFillPercent = boundHeight / mapHeight

                    if (widthFillPercent > 0.25 || heightFillPercent > 0.25 || this.tilesLoaded) {

                        clearInterval(this.interval)
                        this.interval = null

                        setTimeout(() => {

                            if (!this.isCenterDefault() && this.maxZoomCenter && (initZoom || injectZoomControls)) {
                                console.log("(!this.isCenterDefault() && this.maxZoomCenter && (initZoom || injectZoomControls))")
                                this.initMapZoom(map, mapRef, injectZoomControls)
                            } else {
                                this.mapCentered = true;
                            }
                        }, 1000);
                    }
                }, 1000);
            }
        },
        isCenterDefault () {
            return this.maxZoomCenter && this.maxZoomCenter[0] === 0 && this.maxZoomCenter[1] === 0
        },
        // ajout couche WMS a la carte
        addWmsLayer (ref, isDeletePreviousWmsLayer) {
            console.log("addWmsLayer",ref)

            // pour les PDF carte il y a une seule carte, il faut virer la couche WMS si elle deja presente sur la carte
            let map = this.$refs[ref].mapObject
            if(isDeletePreviousWmsLayer){
                map.eachLayer(function(layer){
                    if (layer.options.layers){ 
                        map.removeLayer(layer)
                    }
                });
            }

            if(this.wmsLayer != null && this.wmsServers != null){
                console.log("wmsLayer",this.wmsLayer)
                console.log("codesCommunes", this.codesCommunes)

                let wmsServerUrl = window.DOMAIN_MATOMO === 'brgm-rec' ? this.wmsServers['recette'] : this.wmsServers['prod']
                console.log("wmsServerUrl",wmsServerUrl)

                let paramsWms =  {
                    layers: this.wmsLayer,
                    format: 'image/png',
                    transparent: true,
                    opacity: 0.8
                }

                console.log("bboxRisque", this.wmsLayer, this.bboxRisque)
                let confLayers = conf.config.couchesRisques

                if(this.wmsLayer == confLayers.sismicite.layer || this.wmsLayer == confLayers.radon.layer){
                    let codesCommunesQuoted = []
                    this.codesCommunes.forEach(function (codeCommune) {
                        codesCommunesQuoted.push("'"+codeCommune+"'")
                    })
 
                    paramsWms.codes_communes = codesCommunesQuoted.join()
                } else if(this.wmsLayer == confLayers.argile.layer){
                    paramsWms.xmin = this.bboxRisque[0]
                    paramsWms.ymin = this.bboxRisque[1]
                    paramsWms.xmax = this.bboxRisque[2]
                    paramsWms.ymax = this.bboxRisque[3]
                } else if(this.wmsLayer == confLayers.peb.layer){
                    paramsWms.lon = this.maxZoomCenter[1];
                    paramsWms.lat = this.maxZoomCenter[0];
                    paramsWms.distance = 1000; // metres
                } else if(this.wmsLayer == confLayers.canalisations.layer){
                    paramsWms.x = this.maxZoomCenter[1];
                    paramsWms.y = this.maxZoomCenter[0];
                    paramsWms.rayon = 500; // metres
                }

                L.nonTiledLayer.wms(wmsServerUrl,paramsWms).addTo(map)
                // let that = this
                // layer.on("load",function() { 
                //     console.log("all visible tiles have been loaded 2",that.zooming,this.mapCentered)
                // });
            } 
        }
    },
    watch: {
        tilesLoaded: function () {

            console.log("tilesLoaded : this.tilesLoaded = " + this.tilesLoaded + ", this.zooming = " + this.zooming)

            if (this.tilesLoaded && this.zooming) {
                this.zooming = false
                this.mapCentered = true
            }
        }
    }

}
