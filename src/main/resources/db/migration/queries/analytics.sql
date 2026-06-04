select * from raw_transactions_import;
select * from clean_transactions;

select count(*) from raw_transactions_import;
select count(*) from clean_transactions;

select column_name from information_schema.columns where table_name = 'raw_transactions_import';

select import_status, count(*) from raw_transactions_import group by import_status;

select * from raw_transactions_import where import_status = 'VALID';