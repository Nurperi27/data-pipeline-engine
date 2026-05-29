create table raw_transactions_import(
    id bigint generated always as identity primary key,
    sender_account_id varchar(50),
    receiver_account_id varchar(50),
    amount_amount varchar(50),
    category varchar(50),
    description text,
    transaction_date varchar(50),
    import_status varchar(50),
    error_message text,
    created_at timestamp default current_timestamp
);