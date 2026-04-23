alter table tb_activation_tokens
    add column expires_at timestamptz not null default now();

