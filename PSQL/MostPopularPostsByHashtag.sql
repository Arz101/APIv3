
--- MOST POPULAR POSTS BY HASHTAG FOR FOLLOWING ACCOUNT OR PUBLIC ACCOUNTS

SELECT 
    p.id,
    p.description,
    p.picture,
    p.datecreated,
    u.username,
    COALESCE(l.like_count, 0) AS likes,
	COALESCE(c.comments_count, 0) AS comments
FROM posts p

JOIN profiles pf 
    ON pf.user_id = p.user_id

JOIN users u
	ON u.id = p.user_id

LEFT JOIN follows f 
    ON f.followed_id = p.user_id 
   		AND f.follower_id = 994  --- CURRENT USER LOGGED
   		AND f.status = 'active'
JOIN post_hashtag ph 
    ON ph.post_id = p.id

LEFT JOIN (
    SELECT publication_id, COUNT(*) AS like_count
    FROM likes
    GROUP BY publication_id
) l ON l.publication_id = p.id

LEFT JOIN (
	SELECT post_id, COUNT(*) as comments_count
	FROM comments
	GROUP by post_id
) c ON c.post_id = p.id

WHERE ph.hashtag_id IN (17, 15, 47) -- 20 -- HASHTAG ID
AND (
    pf.private = false 
    OR f.follower_id IS NOT NULL
)
ORDER BY likes DESC
LIMIT 10;



SELECT * FROM hashtags 
ORDER BY post_count DESC
LIMIT 5
