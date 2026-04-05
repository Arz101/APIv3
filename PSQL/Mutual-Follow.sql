


WITH following AS (
	SELECT follower_id AS id FROM follows
	WHERE followed_id = 994 AND status = 'active'
), followers AS (
	SELECT followed_id AS id FROM follows
	WHERE follower_id = 994 AND status = 'active'
)
SELECT u.username, u.id
FROM following fw
INNER JOIN followers fr
	ON fw.id = fr.id
INNER JOIN users u
	ON fr.id = u.id


	  
	  545,
    257,
    292,
    7,
    74,
    1516,
    878,
    15,
    943,
    145,
    2386,
    3123,
    436,
    1110,
    1398,
    279,
    3036,
    829,
    2591
