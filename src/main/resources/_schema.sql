-- Create sequence 
create sequence HIBERNATE_SEQUENCE
minvalue 1
maxvalue 9999999999999999999999999999
start with 1
increment by 1
cache 20;
create table PERSON
(
	id number not null primary key,
	name varchar2(20),
	age number,
	nation varchar2(20),
	address varchar2(20)
);	