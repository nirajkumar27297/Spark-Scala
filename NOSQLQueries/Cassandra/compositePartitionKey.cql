create table location (
                    vehicle_id text,
                    date text,
                    time timestamp,
                    latitiude double,
                    longtitude double,
                    primary key ((vehicle_id,date),time)
                    ) with clustering order by (time desc);
