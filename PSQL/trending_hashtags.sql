

-- TRENDING HASHTAGS

SELECT h.name, COUNT(ph.hashtag_id) AS Total 
FROM post_hashtag ph
INNER JOIN (
	SELECT p.id FROM posts p
	WHERE p.datecreated >= NOW() - INTERVAL '30 Days'
) x ON x.id = ph.post_id
INNER JOIN hashtags h
	ON h.id = ph.hashtag_id
GROUP BY h.name
ORDER BY Total DESC
LIMIT 5

