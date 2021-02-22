// Configuration
export const config = {
    clefsIGN: {
        recette: 'itsz2r193k66t38020bv0lhg',
        production: 'yyq1c134vg412zue2ivlv4sd'
    },
    coucheIGN: 'GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN-EXPRESS.STANDARD',
    attributions: {
        'PPR': '<a href=\'https://www.brgm.fr\' target="_blank">BRGM</a>',
        'SISMICITE': '<a href=\'https://www.brgm.fr\' target="_blank">BRGM</a>',
        'RADON': '<a href=\'https://www.irsn.fr\' target="_blank">IRSN</a>',
        'PEB': '<a href=\'https://www.ecologie.gouv.fr/direction-generale-laviation-civile-dgac\' target="_blank">DGAC</a>',
        'ARGILE': '<a href=\'https://www.brgm.fr\' target="_blank">BRGM</a>',
        'CANALISATIONS': '<a href=\'https://www.brgm.fr\' target="_blank">BRGM</a>', 
        'POLLUTION_NON_REG': '<a href=\'https://www.brgm.fr\'>BRGM</a> | <a href=\'https://www.ecologie.gouv.fr\'>MTE</a> | DREAL/DRIEE'
    },
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
        },
        ppr_georisques: {
            layer: 'ppr_georisques',
            serveurs: {'recette':'https://mapsrefrec.brgm.fr/wxs/georisques/errial_risques?','prod':'https://mapsref.brgm.fr/wxs/georisques/errial_risques?'}
        },    
        ppr_gpu: {
            layer: 'ppr_gpu',
            serveurs: {'recette':'https://mapsrefrec.brgm.fr/wxs/georisques/errial_risques?','prod':'https://mapsref.brgm.fr/wxs/georisques/errial_risques?'}
        }    
    }
}