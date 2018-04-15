select median(fare_amount+tip_amount) as med, pickup_cell, max(dropoff_datetime) as lastInserted
from TaxiLog#time(15 min 1 millisecond) where pickup_cell is not null and dropoff_cell is not null and fare_amount > 0 group by pickup_cell
