create table tb_activation_tokens(
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null,
    used_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);
