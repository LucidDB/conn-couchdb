!set verbose true;

create or replace schema couchdb;
set schema 'couchdb';
set path 'couchdb';

create or replace foreign data wrapper couchdb_wrapper
-- you may need to edit this path
library 'plugin/couchdbConnector.jar'
language java;

-- and this one
call sqlj.install_jar('file:${FARRAGO_HOME}/plugin/couchdbConnector.jar', 'couchdb_jar', 0);

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

-- examples, only url is optional (default localhost:5984)
create or replace server couchdb_server
foreign data wrapper couchdb_wrapper
options(
  username 'un',
  password 'pw',
  url 'http://couchdburl.com'
);

-- view is required, it is appended to the given URL with a preceding '/'.
-- all columns currently must be strings
create or replace foreign table car1(time_vv varchar(128),
  product_name varchar(128))
server couchdb_server
options (view '_design/sw/_view/shipped');

select * from car1;

-- if you pass the view_def parameter as well, we will make a POST
-- call first to create the view, then when you select it will use that def
-- for couchdb's map function.
