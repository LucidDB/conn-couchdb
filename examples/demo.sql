create or replace server couchdb_server foreign data wrapper couchdb_wrapper options( username 'dynamobi', password 'removed', url 'http://dynamobi.cloudant.com/sw');

-- view is required, it is appended to the given URL with a preceding '/'.
-- all columns currently must be strings
create or replace foreign table car1(time_vv varchar(128), product_name varchar(128)) server couchdb_server options (view '_design/sw/_view/shipped');

select * from car1;


create or replace foreign table car1(time_vv varchar(128), product_name varchar(128)) server couchdb_server options (view '_design/sw/_view/shipped');

-- if you pass the view_def parameter as well, we will make a POST
-- call first to create the view, then when you select it will use that def
-- for couchdb's map function.
