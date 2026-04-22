alter table tb_users
add column features varchar[] not null default '{}';
