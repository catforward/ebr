create table task_detail_ver1 (
    task_url text not null,
    task_group_id text not null,
    task_id text not null,
	task_desc text not null default "none",
	task_cmd_line text not null default "none",
	task_depends text not null default "none",
	constraint task_detail_ver1_pk primary key (task_url)
);