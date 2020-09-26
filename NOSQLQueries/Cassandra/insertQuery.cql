insert into home_security.activity(home_id,datetime,event,code_used)
                  values('H0147477','2014-05-21 07:32:16','alarm_set','5509');

copy activity(home_id,datetime,code_used,event) FROM 'D:/input.csv' with header = true and delimiter = ',';
