-- view is required, it is appended to the given URL with a preceding '/'.
-- all columns currently must be strings
create or replace foreign table couchdb.FY2007 
-- ( fiscalYear varchar(128) , obligatedAmount varchar(128) ,agencyID varchar(255))
( fiscalYear INTEGER, obligatedAmount DECIMAL(13,3) ,agencyID varchar(255)) 
 
server fpds_couch 
options (username '', password '', view '_design/fy2007/_view/fy2007');
 
select * from car1;

-- if you pass the view_def parameter as well, we will make a POST
-- call first to create the view, then when you select it will use that def
-- for couchdb's map function.
