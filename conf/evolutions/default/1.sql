# --- !Ups

create table "persons" (
  "id" bigserial primary key,
  "name" varchar not null,
  "age" int not null
);

# --- !Downs

drop table if exists "persons";
