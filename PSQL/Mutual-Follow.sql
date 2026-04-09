


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

INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT p.id, 200 FROM posts p
WHERE NOT EXISTS (
	SELECT ph.post_id
	FROM post_hashtag ph
	WHERE p.id = ph.post_id
)


SELECT hashtag_id, COUNT(post_id) FROM post_hashtag
GROUP BY hashtag_id