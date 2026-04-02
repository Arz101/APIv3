
-- 10 most popular posts liked by followeds 

SELECT DISTINCT 
    p.id,
    p.description,
    p.picture,
    p.datecreated,
	u.username, 
	COALESCE(lc.likes_count, 0) AS likes,
	COALESCE(c.comments_count,0) AS comments
FROM posts p
INNER JOIN likes l 
	ON l.publication_id = p.id
INNER JOIN (
	SELECT f.followed_id as followed
	FROM follows f
	WHERE f.follower_id = 994 -- Current User ID LOGGED
) fl ON fl.followed = l.user_id
INNER JOIN users u 
	ON u.id = p.user_id
INNER JOIN (
	SELECT publication_id AS post_id, COUNT(*) AS likes_count
	FROM likes
	GROUP BY publication_id
) lc ON lc.post_id = p.id
LEFT JOIN (
	SELECT post_id , COUNT(*) AS comments_count
	FROM comments 
	GROUP BY post_id
) c ON c.post_id = p.id
ORDER BY likes DESC
LIMIT (10)