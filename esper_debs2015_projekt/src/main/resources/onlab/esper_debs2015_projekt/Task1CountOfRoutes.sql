select count(*) as frequency, pickup_cell, 
dropoff_cell, max(dropoff_datetime) as max_dropoff_datetime
from TaxiLog#time(30 min 1 millisecond) 
where pickup_cell is not null and dropoff_cell is not null 
and dropoff_datetime is not null group by pickup_cell, dropoff_cell