!set verbose true;

create or replace schema couchdb;
set schema 'couchdb';
set path 'couchdb';

create or replace foreign data wrapper couchdb_wrapper
library '/Users/ngoodman/dev/eigenbase_workspace/extensions/conn/couchdb-connectors/deploy/couchdbConnector.jar'
language java;

-- and this one
call sqlj.install_jar('file:/Users/ngoodman/dev/eigenbase_workspace/extensions/conn/couchdb-connectors/deploy/couchdbConnector.jar', 'couchdb_jar', 0);

-- helper udx for foreign tables
create or replace function couchdb.wrapper_udx(
  username varchar(65535),
  password varchar(65535),
  url varchar(65535),
  view varchar(65535))
returns table(objects varchar(65535))
language java
parameter style system defined java
no sql
external name 'couchdb.couchdb_jar:com.dynamobi.db.conn.couchdb.CouchUdx.query';

