# --- !Ups

create table "persons" (
  "id" bigserial primary key,
  "name" varchar not null,
  "age" int not null
);

create table "users" (
  "id" uuid primary key,
  "username" varchar not null,
  "email" varchar not null unique,
  "role" varchar not null, -- could implement as enum
  "first_name" varchar,
  "last_name" varchar,
  "language" varchar,
  "time_zone" varchar,
  "enabled" boolean,
  "create_date_time" timestamp with time zone not null,
  "update_date_time" timestamp with time zone
);

create table "password_data" (
  "provider_id" varchar not null,
  "provider_key" varchar not null references users(email),
  "hasher" varchar not null,
  "password" varchar not null,
  "salt" varchar,
  PRIMARY KEY ("provider_id", "provider_key")
);

# --- !Downs

drop table if exists "password_data";

drop table if exists "users";

drop table if exists "persons";
