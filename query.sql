SELECT 
lower(hex("uuid")) as "uuid", 
"name", 
datetime("changedToAt" / 1000, 'unixepoch', 'localtime') as "changedToAt",
"source"
FROM "player_name"
WHERE lower(hex("uuid")) == "8b56263625e54bd48e402d44ec7ca86a"


SELECT 
lower(hex("uuid")) as "uuid", 
"name", 
datetime(max("changedToAt") / 1000, 'unixepoch', 'localtime'),
"source"
FROM "player_name"
GROUP BY "uuid"
ORDER BY "name"


SELECT 
lower(hex("uuid")) as "uuid", 
"name", 
datetime(max("changedToAt") / 1000, 'unixepoch', 'localtime'),
"source"
FROM "player_name"
WHERE lower(hex("uuid")) == "8b56263625e54bd48e402d44ec7ca86a"


SELECT 
lower(hex("uuid")) as "uuid", 
"name", 
datetime(max("changedToAt") / 1000, 'unixepoch', 'localtime'),
"source",
count("name") as "count"
FROM "player_name"
GROUP BY "uuid"
ORDER BY "name"

SELECT 
lower(hex("uuid")) as "uuid", 
"name", 
datetime(max("changedToAt") / 1000, 'unixepoch', 'localtime'),
"source",
count("name") as "count"
FROM "player_name"
GROUP BY "uuid"
ORDER BY "count" DESC

SELECT
"uuid" IS NULL AS "invalid", 
"uuid",
"name",
max("changedToAt") AS "changedToAt",
"source"
FROM "player_name"
WHERE "uuid" == ?


INSERT INTO "player_name" 
("uuid","name","changedToAt","source")
VALUES (?,?,?,?)


SELECT
"index" 
"uuid",
"name",
max("changedToAt") as "changedToAt",
"source"
WHERE lower(hex("uuid")) == "8b56263625e54bd48e402d44ec7ca86a"