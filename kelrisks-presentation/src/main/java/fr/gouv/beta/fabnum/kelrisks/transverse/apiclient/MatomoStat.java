package fr.gouv.beta.fabnum.kelrisks.transverse.apiclient;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/** Matomo results **/
@Data
public class MatomoStat {
	private String label;
	private Integer nb_visits;
	private Integer nb_events;
	private Integer nb_events_with_value;
	private Integer sum_event_value;
	private Integer min_event_value;
	private Integer max_event_value;
	private Integer sum_daily_nb_uniq_visitors;
	private Integer avg_event_value;
	@JsonProperty("Events_EventAction")
	private String Events_EventAction;
	@JsonProperty("Events_EventName")
	private String Events_EventName;
}
