select median(fare_amount+tip_amount) as med, pickup_cell 
from TaxiLog#time(15 min) where pickup_cell is not null group by pickup_cell
