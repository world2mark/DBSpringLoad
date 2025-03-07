create table my_students (
    id uuid default gen_random_uuid() primary key,
    name string not null,
    dob timestamptz not null,
    email string not null
);

create table my_accounts (
    id uuid default gen_random_uuid() primary key,
    account_type varchar(10) NOT NULL,
    account_balance NUMERIC(23, 5) NOT NULL
);
