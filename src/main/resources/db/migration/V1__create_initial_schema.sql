create sequence hibernate_sequence start 1 increment 1;

create table authority (
  authority_id int8 not null,
  created_at timestamp not null,
  updated_at timestamp not null,
  name varchar(255) not null,
  primary key (authority_id)
);

create table member (
  member_id int8 not null,
  created_at timestamp not null,
  updated_at timestamp not null,
  email varchar(255) not null,
  full_name varchar(255),
  password varchar(255) not null,
  wallet_wallet_id int8,
  primary key (member_id)
);

create table user_authority (
  member_id int8 not null,
  authority_id int8 not null
);

create table wallet (
  wallet_id int8 not null,
  created_at timestamp not null,
  updated_at timestamp not null,
  address varchar(255),
  primary key (wallet_id)
);

alter table authority
  add constraint UK_jdeu5vgpb8k5ptsqhrvamuad2 unique (name);

alter table member
  add constraint UK_mbmcqelty0fbrvxp1q58dn57t unique (email);

alter table member
  add constraint FKp8fsc9xwq6v5ykpissv1dt9ma
foreign key (wallet_wallet_id)
references wallet;

alter table user_authority
  add constraint FKgvxjs381k6f48d5d2yi11uh89
foreign key (authority_id)
references authority;

alter table user_authority
  add constraint FKb27h85xa52amfh4qk85mlxudl
foreign key (member_id)
references member;
