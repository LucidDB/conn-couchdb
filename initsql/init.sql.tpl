!set verbose true;

create or replace schema sys_couchdb;
set schema 'sys_couchdb';
set path 'sys_couchdb';

create or replace foreign data wrapper couchdb_wrapper
-- you may need to edit this path
library 'plugin/couchdbConnector.jar'
language java;

-- and this one
call sqlj.install_jar('file:${FARRAGO_HOME}/plugin/couchdbConnector.jar', 'couchdb_jar', 0);

-- helper udx for foreign tables
create or replace function sys_couchdb.wrapper_udx(
  username varchar(65535),
  password varchar(65535),
  url varchar(65535),
  view varchar(65535),
  "LIMIT" varchar(65535))
returns table(objects varchar(65535))
language java
parameter style system defined java
no sql
external name 'sys_couchdb.couchdb_jar:com.dynamobi.db.conn.couchdb.CouchUdx.query';

-- examples, all parameters are optional (url default localhost:5984)
create or replace server couchdb_server
foreign data wrapper couchdb_wrapper
options(
  username 'un',
  password 'pw',
  url 'http://couchdburl.com'
);

-- view is required. It will be appended to the URL in a smart way, you can
-- add further GET parameters to the end.
-- all columns currently must be strings and must have the same names as
-- their CouchDB counterparts.
create or replace foreign table car1("Time" varchar(128),
  "ProductName" varchar(128))
server couchdb_server
options (view '_design/sw/_view/shipped');

-- this will prepare a server that, when a foreign table of this server type
-- is first created, in couchdb a new document called 'rar' in the sw database
-- and a view called shipped4 will be created.
create or replace server couchdb_server2
foreign data wrapper couchdb_wrapper
options(
  username 'un',
  password 'pw',
  url 'http://dynamobi.cloudant.com/sw/_design/rar',
  view_def '{
  "views": {
    "shipped4": {
      "map": "function(doc) { if ( doc.order_line_item[15].PRODUCTNAME == ''1936 Chrysler Airflow'') emit(doc._id, {ProductName: doc.order_line_item[15].PRODUCTNAME} ); }"
    }
  }
}'
);
-- notice that the necessary _view (for the url) is not present;
-- it will be added automatically.
create or replace foreign table car2(
  "ProductName" varchar(128))
server couchdb_server2
options (
  "LIMIT" '20',
  view 'shipped4'
);

select * from car1;
select * from car2;
