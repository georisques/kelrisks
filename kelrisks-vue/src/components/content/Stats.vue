<template>
    <div id="stats_wrapper">
        <h2 class="section__title">Statistiques</h2><br>
        <br>
        <!-- <div class="histogram_wrapper"> -->
            <!-- <div class="histogram_title_wrapper">
                Utilisateurs
            </div>
            <div class="histogram_bars_wrapper">
                <div class="histogram_bar"
                     v-bind:style="{height: professionnel_pct + '%'}">
                    <div class="histogram_bar_text">{{professionnel_nb}}</div>
                </div>
                <div class="histogram_bar"
                     v-bind:style="{height: particulier_pct + '%'}">
                    <div class="histogram_bar_text">{{particulier_nb}}</div>
                </div>
            </div>
            <div class="histogram_labels_wrapper">
                <div class="histogram_label">Professionnels</div>
                <div class="histogram_label">Particuliers</div>
            </div> -->
        <!-- </div> -->
        <div class="value_wrapper">
            <div class="value_title_wrapper">
                Avis rendus
            </div>
            <div class="value_text_wrapper">
                {{ avis_rendu_nb }}
            </div>
        </div>
        <div class="value_wrapper">
            <div class="value_title_wrapper">
                Avis téléchargés
            </div>
            <div class="value_text_wrapper">
                {{ pdf_nb }}
            </div>
        </div>
        <!-- <div class="value_wrapper push-2">
            <div class="value_title_wrapper">
                Bureaux d'Étude consultés
            </div>
            <div class="value_text_wrapper">
                {{ bureau_etude_nb }}
            </div>
        </div> -->
        <div class="value_wrapper half">
            <div class="value_title_wrapper"
                 style="margin-top: -5px; margin-bottom: 5px;">
                Jours de travail à faible valeur ajoutée économisés par l’administration <sup>(1)</sup>
            </div>
            <div class="value_text_wrapper">
                {{ Math.round(avis_rendu_nb / 7) }}
            </div>
        </div>
        <div class="value_wrapper half">
            <div class="value_title_wrapper"
                 style="margin-top: -5px; margin-bottom: 5px;">
                Jours de travail à faible valeur ajoutée économisés par les professionnels du secteur <sup>(2)</sup>
            </div>
            <div class="value_text_wrapper">
                {{ Math.round(avis_rendu_nb / 5) }}
            </div>
        </div>
        <div style="clear: both; margin-bottom: 100px;"></div>
        <p class="renvoi">
            <sup>(1)</sup> - L'automatisation de la recherche sur le passé industriel d'une parcelle permet en moyenne l’économie d’une heure de travail de traitement et re-saisie de données par un
                           agent de l’administration. Le gain de temps est réinvesti sur l’accompagnement et l'approfondissement des dossiers qui le demandent.</p>
        <p class="renvoi">
            <sup>(2)</sup> - L'automatisation de la recherche sur le passé industriel d'une parcelle permet en moyenne l’économie d’une heure et demie de travail de traitement pour un professionnel du
                           secteur (notaire ou bureau d'études par exemple) Cette tâche implique bien souvent une interaction écrite avec l'administration ce qui prend du temps de suivi.</p>
    </div>
</template>
<script>
import fetchWithError from '@/script/fetchWithError'

export default {
    name: 'Stats',
    data: () => ({
        stats: {},
        // professionnel_pct: 0,
        // professionnel_nb: 0,
        // particulier_pct: 0,
        // particulier_nb: 0,
        avis_rendu_nb: 0,
        pdf_nb: 0,
        // bureau_etude_nb: 0,
        env: {
            presentationVersion: process.env.VUE_APP_VERSION,
            version: '',
            frontPath: process.env.VUE_APP_FRONT_PATH,
            backPath: process.env.VUE_APP_BACK_STATIC_PATH,
            apiPath: process.env.VUE_APP_BACK_API_PATH
        }
    }),
    methods: {
        getStats () {
            //let period = 'range'
            //let date = '2019-02-21,2022-12-31'
            let url = this.env.apiPath + "stats";
            //'https://wwwstats.' + window.DOMAIN_MATOMO + '.fr/piwik.php?module=API&idSite=' + window.ID_SITE_MATOMO + '&method=Events.getAction&secondaryDimension=eventName&flat=1&period=' + period + '&date=' + date + '&format=json'
            fetchWithError(url, null, 1000 * 20)
                .then(stream => stream.json())
                .then(value => {
                    this.stats = value
                    // console.log(this.stats)
                    //let demandeurs = this.getEventAction('Demandeur')
                    // console.log(demandeurs)/
                    // this.professionnel_nb = demandeurs.events.Professionnel.nb_events
                    // this.professionnel_pct = 100 / demandeurs.nb_events_sum * demandeurs.events.Professionnel.nb_events
                    // this.particulier_nb = demandeurs.events.Particulier.nb_events
                    // this.particulier_pct = 100 / demandeurs.nb_events_sum * demandeurs.events.Particulier.nb_events

                    let avis = this.getEventAction('Avis')
                    if (avis.events.Rendu) this.avis_rendu_nb = avis.events.Rendu.nb_events

                    let pdf = this.getEventAction('Pdf')
                    // console.log(pdf)
                    if (pdf.events.undefined) this.pdf_nb = pdf.events.undefined.nb_events

                    // let bureauEtude = this.getEventAction('Bureau_Etude')
                    // console.log(bureauEtude)
                    //if (avis.events.Bureau_Etude) this.bureau_etude_nb = avis.events.Bureau_Etude.nb_events
                })
        },
        getEventAction (actionName) {
            let action = {
                name: actionName,
                nb_events_sum: 0,
                nb_events_max: 0,
                events: {}
            }
            for (let key in this.stats) {
                let statAction = this.stats[key]
                if (statAction.Events_EventAction === actionName) {
                    let event = {}

                    event.nb_events = statAction.nb_events
                    event.nb_visits = statAction.nb_visits

                    action.nb_events_sum += event.nb_events
                    action.nb_events_max = event.nb_events > action.nb_events_max ? event.nb_events : action.nb_events_max

                    action.events[statAction.Events_EventName] = event
                }
            }
            if (action.name) return action
            return null
        }
    },
    mounted () {
        // TODO : cf ticket https://gitlab.brgm.fr/brgm/georisques/georisques-brgm/kelrisks/-/issues/70
        // this.getStats()
    }
}
</script>

<style scoped>

    #stats_wrapper {
        font-weight : bold;
        padding     : 30px 0 0;
    }

    @media (min-width : 1200px) {
        #stats_wrapper {
            padding : 30px 140px 0;
        }
    }

    .histogram_wrapper {
        margin           : 20px;
        width            : calc(40% - 40px);
        height           : 290px;
        background-color : white;
        box-shadow       : #999999 0 2px 4px;
        border-radius    : 2px;
        padding          : 20px;
        float            : left;
    }

    .value_wrapper {
        margin           : 20px;
        width            : calc(50% - 40px);
        height           : 125px;
        background-color : white;
        box-shadow       : #999999 0 2px 4px;
        border-radius    : 2px;
        padding          : 20px;
        float            : left;
    }

    .value_wrapper.half {
        width : calc(50% - 40px);
    }

    .histogram_bars_wrapper {
        width           : 100%;
        height          : calc(100% - 45px);
        display         : flex;
        align-items     : flex-end;
        justify-content : flex-end;
        padding         : 10px 0 15px 0;
    }

    .histogram_bar {
        width                   : 50%;
        background-color        : #4B9EFF;
        margin                  : 0 10px;
        border-top-left-radius  : 5px;
        border-top-right-radius : 5px;
    }

    .histogram_bar_text {
        margin-top : -25px;
    }

    .histogram_labels_wrapper {
        width  : 100%;
        height : 25px;
    }

    .histogram_label {
        height        : 25px;
        width         : 50%;
        float         : left;
        margin-bottom : 10px;
    }

    .value_text_wrapper {
        width         : 100%;
        height        : 25px;
        font-size     : 3em;
        margin-bottom : 10px;
    }

    .histogram_title_wrapper {
        width       : 100%;
        height      : 25px;
        font-weight : initial;
        line-height : 10px;
    }

    .value_title_wrapper {
        width       : 100%;
        height      : 45px;
        font-weight : initial;
        line-height : 15px;
    }

    .push-2 {
        margin-left : 17.5%;
    }

    @media (max-width : 700px) {

        .value_wrapper,
        .value_wrapper.half,
        .value_wrapper.push-2,
        .histogram_wrapper {
            width       : calc(100% - 40px);
            margin-left : 0;
        }
    }

    sup {
        font-size : 0.7em;
    }

    .renvoi {
        font-size   : 0.8em;
        margin      : 0 auto 5px auto;
        max-width   : 1000px;
        text-align  : left;
        color       : rgb(153, 153, 153);
        line-height : 1.1em;
    }
</style>
