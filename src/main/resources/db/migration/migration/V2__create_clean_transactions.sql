create table clean_transactions(
    id bigint generated always as identity primary key,
    sender_account_id bigint not null,
    receiver_account_id bigint not null,
    amount numeric(18, 2) not null,
    category varchar(50),
    description text,
    transaction_date timestamp not null,
    created_at timestamp default current_timestamp
);