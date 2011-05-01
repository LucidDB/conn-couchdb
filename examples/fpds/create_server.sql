create or replace server fpds_couch 
foreign data wrapper couchdb_wrapper 
options( username '', password '', url 'http://localhost:5984/fpds_test');

create or replace server nosql_dynamobi 
foreign data wrapper couchdb_wrapper 
options( username '', password '', url 'http://nosql.dynamobi.com:5984/fpds_2008');