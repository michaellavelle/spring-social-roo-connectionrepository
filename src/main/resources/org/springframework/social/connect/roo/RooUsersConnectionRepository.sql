CREATE TABLE if not exists user_connection (id bigint(20) NOT NULL AUTO_INCREMENT,access_token varchar(255) DEFAULT NULL,display_name varchar(255) DEFAULT NULL,expire_time bigint(20) DEFAULT NULL,image_url varchar(255) DEFAULT NULL,profile_url varchar(255) DEFAULT NULL,provider_id varchar(255) DEFAULT NULL,provider_user_id varchar(255) DEFAULT NULL,rank int(11) NOT NULL,refresh_token varchar(255) DEFAULT NULL,secret varchar(255) DEFAULT NULL,user_id varchar(255) DEFAULT NULL,version int(11) DEFAULT NULL,PRIMARY KEY (id));
create unique index if not exists user_provider on user_connection (user_id, provider_id, provider_user_id);
create unique index if not exists user_connection_rank on user_connection (user_id, provider_id, rank);

