create table tb_teste(
    id_teste serial primary key,
    valor integer not null
);

insert into tb_teste values (1, 0);
commit;