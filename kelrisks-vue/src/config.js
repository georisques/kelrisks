export const config = {
    clefsIGN: {
        recette: 'itsz2r193k66t38020bv0lhg',
        production: 'yyq1c134vg412zue2ivlv4sd'
    },
    coucheIGN: 'GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN-EXPRESS.STANDARD',
    couchesRisques: {
        argile: {
            layer: 'risq_argiles',
            serveurs: {'recette':'https://mapsrefrec.brgm.fr/wxs/georisques/errial_risques?','prod':'https://mapsref.brgm.fr/wxs/georisques/errial_risques?'}
        },
        radon: {
            layer: 'risq_radon',
            serveurs: {'recette':'https://mapsrefrec.brgm.fr/wxs/georisques/errial_risques?','prod':'https://mapsref.brgm.fr/wxs/georisques/errial_risques?'}
        },
        sismicite: {
            layer: 'risq_zonage_sismique',
            serveurs: {'recette':'https://mapsrefrec.brgm.fr/wxs/georisques/errial_risques?','prod':'https://mapsref.brgm.fr/wxs/georisques/errial_risques?'}
        },
        peb: {
            layer: 'risq_peb',
            serveurs: {'recette':'https://mapsrefrec.brgm.fr/wxs/georisques/errial_risques?','prod':'https://mapsref.brgm.fr/wxs/georisques/errial_risques?'}
        },
        canalisations: {
            layer: 'CANALISATIONS',
            serveurs: {'recette':'https://mapsrefrec.brgm.fr/wxs/georisques/errial_risques?','prod':'https://mapsref.brgm.fr/wxs/georisques/errial_risques?'}
        }
    }
}