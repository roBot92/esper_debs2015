select pickup_cell, dropoff_cell, inserted as inserted_for_delay from TaxiLog#time(30 min 1 millisecond) 
where pickup_cell is not null and dropoff_cell is not null and dropoff_datetime is not null