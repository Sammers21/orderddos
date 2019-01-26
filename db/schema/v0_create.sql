CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TYPE IF EXISTS OrderStatus CASCADE;
CREATE TYPE OrderStatus AS ENUM ('NEW', 'SCHEDULED', 'REJECTED', 'ONGOING', 'DONE');

DROP TABLE IF EXISTS Orders;
CREATE TABLE Orders
(
  uuid                       UUID PRIMARY KEY                  DEFAULT uuid_generate_v1(),
  t_submitted                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
  email                      VARCHAR(256)             NOT NULL,
  target_url                 VARCHAR(256)             NOT NULL,
  num_nodes_by_region        JSON                     NOT NULL,
  t_start                    TIMESTAMP WITH TIME ZONE          DEFAULT NULL,
  duration                   INTERVAL                 NOT NULL,
  status                     OrderStatus              NOT NULL DEFAULT 'NEW'
);
