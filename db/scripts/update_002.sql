create table if not exists post (
	id serial primary key,
	name text not null,
	description text not null,
	link text not null unique,
	created timestamp not null
);