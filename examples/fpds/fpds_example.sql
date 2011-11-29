0: jdbc:luciddb:> create or replace server couchdb_server foreign data wrapper couchdb_wrapper options( username 'dynamobi', password 'removed', url 'http://dynamobi.cloudant.com/fpds_test');
No rows affected (0.112 seconds)
0: jdbc:luciddb:> create or replace foreign table applib.year_agency (key0 varchar(100), key1 varchar(100), value0 integer) server couchdb_server options (view '_design/summarized_keys_only/_view/year_agency');
No rows affected (0.226 seconds)
0: jdbc:luciddb:> select * from applib.year_agency where KEY1 = '4900: NATIONAL SCIENCE FOUNDATION';
+-------+------------------------------------+---------+
| KEY0  |                KEY1                | VALUE0  |
+-------+------------------------------------+---------+
| 2007  | 4900: NATIONAL SCIENCE FOUNDATION  | 55650   |
+-------+------------------------------------+---------+
1 row selected (1.1 seconds)
