Create TABLE logData (
Date_Time  timestamp ,
Cpu_Count int ,
Cpu_Working_Time DOUBLE ,
Cpu_idle_Time DOUBLE ,
cpu_percent DOUBLE ,
Usage_cpu_count int ,
number_of_software_interrupts_since_boot DOUBLE ,
number_of_system_calls_since_boot int ,
number_of_interrupts_since_boot int ,
cpu_avg_load_over_1_min DOUBLE ,
cpu_avg_load_over_5_min   DOUBLE ,
cpu_avg_load_over_15_min DOUBLE ,
system_total_memory BIGINT,
system_used_memory BIGINT ,
system_free_memory BIGINT ,
system_active_memory BIGINT ,
system_inactive_memory BIGINT,
system_buffers_memory BIGINT ,
system_cached_memory BIGINT ,
system_shared_memory BIGINT ,
system_avalible_memory BIGINT ,
disk_total_memory BIGINT ,
disk_used_memory BIGINT ,
disk_free_memory BIGINT ,
disk_read_count BIGINT ,
disk_write_count BIGINT ,
disk_read_bytes BIGINT ,
disk_write_bytes BIGINT ,
time_spent_reading_from_disk BIGINT ,
time_spent_writing_to_disk BIGINT ,
time_spent_doing_actual_Input_Output BIGINT ,
number_of_bytes_sent BIGINT ,
number_of_bytes_received BIGINT ,
number_of_packets_sent BIGINT ,
number_of_packets_recived BIGINT ,
total_number_of_errors_while_receiving BIGINT ,
total_number_of_errors_while_sending BIGINT ,
total_number_of_incoming_packets_which_were_dropped BIGINT ,
total_number_of_outgoing_packets_which_were_dropped BIGINT ,
boot_time text ,
user_name text ,
keyboard double ,
mouse double ,
technology text ,
files_changed int ,
PRIMARY KEY (user_name,Date_Time)
);

copy logData (Date_Time, Cpu_Count, Cpu_Working_Time, Cpu_idle_Time, cpu_percent, Usage_Cpu_Count , number_of_software_interrupts_since_boot, number_of_system_calls_since_boot, number_of_interrupts_since_boot, cpu_avg_load_over_1_min, cpu_avg_load_over_5_min, cpu_avg_load_over_15_min, system_total_memory, system_used_memory, system_free_memory, system_active_memory, system_inactive_memory, system_buffers_memory, system_cached_memory, system_shared_memory, system_avalible_memory, disk_total_memory, disk_used_memory, disk_free_memory, disk_read_count, disk_write_count, disk_read_bytes, disk_write_bytes, time_spent_reading_from_disk, time_spent_writing_to_disk, time_spent_doing_actual_Input_Output, number_of_bytes_sent, number_of_bytes_received, number_of_packets_sent, number_of_packets_recived, total_number_of_errors_while_receiving, total_number_of_errors_while_sending, total_number_of_incoming_packets_which_were_dropped, total_number_of_outgoing_packets_which_were_dropped, boot_time, user_name, keyboard, mouse, technology, files_changed) from  '/home/niraj/Downloads/LMS DB/CpuLogData2019-09-16.csv'  with delimiter = ',' and header = true;

