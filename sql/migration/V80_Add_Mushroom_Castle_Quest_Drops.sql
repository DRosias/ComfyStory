insert into `mob_drops` (`mobid`, `itemid`, `chance`, `minquant`, `maxquant`)
select 3300100, 4034104, 10000, 1, 1
where not exists (select 1 from `mob_drops` where `mobid` = 3300100 and `itemid` = 4034104);

insert into `mob_drops` (`mobid`, `itemid`, `chance`, `minquant`, `maxquant`)
select 3300101, 4034105, 10000, 1, 1
where not exists (select 1 from `mob_drops` where `mobid` = 3300101 and `itemid` = 4034105);

insert into `mob_drops` (`mobid`, `itemid`, `chance`, `minquant`, `maxquant`)
select 3300105, 4034111, 10000, 1, 1
where not exists (select 1 from `mob_drops` where `mobid` = 3300105 and `itemid` = 4034111);

insert into `mob_drops` (`mobid`, `itemid`, `chance`, `minquant`, `maxquant`)
select 3300106, 4034114, 10000, 1, 1
where not exists (select 1 from `mob_drops` where `mobid` = 3300106 and `itemid` = 4034114);

insert into `mob_drops` (`mobid`, `itemid`, `chance`, `minquant`, `maxquant`)
select 3300107, 4034114, 10000, 1, 1
where not exists (select 1 from `mob_drops` where `mobid` = 3300107 and `itemid` = 4034114);

insert into `mob_drops` (`mobid`, `itemid`, `chance`, `minquant`, `maxquant`)
select 3300108, 4034112, 10000, 1, 1
where not exists (select 1 from `mob_drops` where `mobid` = 3300108 and `itemid` = 4034112);

insert into `mob_drops` (`mobid`, `itemid`, `chance`, `minquant`, `maxquant`)
select 3300108, 4034114, 10000, 1, 1
where not exists (select 1 from `mob_drops` where `mobid` = 3300108 and `itemid` = 4034114);
