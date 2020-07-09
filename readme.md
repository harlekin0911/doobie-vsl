# Doobie-vsl

 access to VSL using scala and doobie

## Option bei leeren Sql-Queries Results
Damit Query0.option funktioniert muss in der Connection URL der Parameter allowNextOnExhaustedResultSet mit Wert 1 gesetzt werden,
z.B.: "jdbc:db2://172.17.4.39:50001/vslt01:allowNextOnExhaustedResultSet=1;"