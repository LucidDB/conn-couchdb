-- view is required, it is appended to the given URL with a preceding '/'.
-- all columns currently must be strings
create or replace foreign table couchdb.FY2007 
-- ( fiscalYear varchar(128) , obligatedAmount varchar(128) ,agencyID varchar(255))
( fiscalYear INTEGER, obligatedAmount DECIMAL(13,3) ,agencyID varchar(255)) 
 
server fpds_couch 
options (username '', password '', view '_design/fy2007/_view/fy2007');
 
select * from car1;


create or replace foreign table couchdb.CA 
( "fiscalYear" INTEGER, "obligatedAmount" DECIMAL(13,3) ,"agencyID" varchar(255), "principalState" varchar(255)) 
server nosql_dynamobi 
options (view '_design/CA/_view/CA');

create or replace foreign table couchdb.all_contracts
(AMOUNT_OBLIGATED DECIMAL(19,2)
, COMPETE_CAT VARCHAR(255)
, SIGNED_DATE VARCHAR(100)
, CURRENT_COMPLETION_DATE VARCHAR(100)
, ULTIMATE_COMPLETION_DATE VARCHAR(100)
, CONT_ANNUAL_REV DECIMAL(19,2) 
, CONT_NUM_EMPLOYEES INTEGER 
, CONT_SMALL_BUS VARCHAR(10)
, CONT_WOMENOWNED_BUS VARCHAR(10)
, CONT_CITY VARCHAR(255)
, CONT_NAME VARCHAR(255)
, CONT_COUNTRY VARCHAR(100)
, CONT_STATE VARCHAR(50)
, PROD_OR_SERVICE_CODE VARCHAR(255)
, AGENCY_FUNDINGID VARCHAR(255)
, AGENCY_CAT VARCHAR(100)
, AGENCY_MOD  VARCHAR(100)
, FISCAL_YEAR INTEGER
, AGENCY_ID VARCHAR(255) 
)
server fpds_couch 
options (view '_design/all/_view/all');