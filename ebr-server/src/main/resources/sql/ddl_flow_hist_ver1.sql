create table flow_hist_ver1 (
    flow_instance_id text not null,
	flow_url text not null,
	timestamp_start integer not null default 0,
	timestamp_end integer not null default 0,
	task_state integer not null default -1,
	constraint flow_hist_ver1_pk primary key (flow_instance_id, flow_url)
);