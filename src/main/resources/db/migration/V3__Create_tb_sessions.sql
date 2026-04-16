create table tb_sessions (
    id uuid primary key default gen_random_uuid(),
    token varchar(96) not null unique,
    user_id uuid not null,
    expires_at timestamptz not null,
    created_at timestamptz default now(),
    updated_at timestamptz default now(),
    foreign key (user_id) references tb_users(id)
);
