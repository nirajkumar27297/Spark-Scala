CREATE KEYSPACE home_security WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE home_security.home (
    home_id text PRIMARY KEY,
    address text,
    alt_phone text,
    city text,
    contact_name text,
    email text,
    guest_code text,
    main_code text,
    phone text,
    phone_password text,
    state text,
    zip text
); 
