create table task_exec_hist_ver1 (
    workflow_instance_id text not null,
	task_url text not null,
	timestamp_start integer not null default 0,
	timestamp_end integer not null default 0,
	task_state integer not null default -1,
	constraint task_exec_hist_ver1_pk primary key (workflow_instance_id, task_url)
);