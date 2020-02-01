# --- !Ups

CREATE EXTENSION IF NOT EXISTS hstore;

CREATE TABLE "persons" (
  "id" BIGSERIAL PRIMARY KEY,
  "name" VARCHAR NOT NULL,
  "age" INT NOT NULL
);

CREATE TABLE "users" (
  "id" UUID PRIMARY KEY,
  "username" VARCHAR NOT NULL,
  "email" VARCHAR NOT NULL UNIQUE,
  "role" VARCHAR NOT NULL, -- could implement as enum
  "first_name" VARCHAR,
  "last_name" VARCHAR,
  "language" VARCHAR,
  "time_zone" VARCHAR,
  "enabled" BOOLEAN,
  "create_date_time" TIMESTAMP WITH TIME ZONE NOT NULL,
  "update_date_time" TIMESTAMP WITH TIME ZONE
);

CREATE TABLE "password_data" (
  "provider_id" VARCHAR NOT NULL, -- could implement as enum
  "provider_key" VARCHAR NOT NULL REFERENCES users(email),
  "hasher" VARCHAR NOT NULL,
  "password" VARCHAR NOT NULL,
  "salt" VARCHAR,
  PRIMARY KEY ("provider_id", "provider_key")
);

CREATE TABLE "oauth2_data" (
  "provider_id" VARCHAR NOT NULL, -- could implement as enum
  "provider_key" VARCHAR NOT NULL,
  "access_token" VARCHAR NOT NULL,
  "token_type" VARCHAR,
  "expires_in" INT,
  "refresh_token" VARCHAR,
  "params" HSTORE,
  PRIMARY KEY ("provider_id", "provider_key")
);

CREATE TABLE "user_oauth2_providers" (
  "user_id" UUID NOT NULL,
  "provider_id" VARCHAR NOT NULL, -- could implement as enum
  "provider_key" VARCHAR NOT NULL,
  PRIMARY KEY ("user_id", "provider_id", "provider_key"),
  FOREIGN KEY ("provider_id", provider_key) REFERENCES oauth2_data("provider_id", "provider_key")
);

# --- !Downs

DROP TABLE IF EXISTS "user_oauth2_providers";

DROP TABLE IF EXISTS "oauth2_data";

DROP TABLE IF EXISTS "password_data";

DROP TABLE IF EXISTS "users";

DROP TABLE IF EXISTS "persons";

DROP EXTENSION hstore;
