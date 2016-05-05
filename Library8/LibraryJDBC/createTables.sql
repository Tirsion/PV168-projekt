/**
 * Author:  L
 * Created: 24.3.2016
 */

CREATE TABLE "READER" (
    "id" bigint primary key generated always as identity,
    "name" varchar(50),
    "address" varchar(100),
    "email" varchar(50),
    "note" varchar(255)
);
