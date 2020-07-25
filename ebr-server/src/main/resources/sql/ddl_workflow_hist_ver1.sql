create table workflow_hist_ver1 (
    workflow_instance_id text not null,
	workflow_url text not null,
	timestamp_start integer not null default 0,
	timestamp_end integer not null default 0,
	task_state integer not null default -1,
	constraint workflow_hist_ver1_pk primary key (workflow_instance_id, workflow_url)
);