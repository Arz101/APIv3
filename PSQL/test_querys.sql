
-- Trae los 5 usuarios que más likes han recibido en total en sus posts, 
-- pero solo contando posts de los últimos 30 días. 
-- Incluye su username, foto de perfil, y total de likes.

WITH count_likes AS (
	SELECT l.publication_id AS post_id, COUNT(*) AS total_likes
	FROM likes l
	GROUP BY l.publication_id
)
SELECT 
	p.user_id,
	u.username,
	pf.avatar_url,
	SUM(cl.total_likes) AS ALL_Likes
FROM posts p
INNER JOIN count_likes cl ON cl.post_id = p.id
INNER JOIN users u ON u.id = p.user_id
INNER JOIN profiles pf ON pf.user_id = u.id
WHERE p.datecreated >= NOW() - INTERVAL '30 Days'
GROUP BY
	p.user_id,
	u.username,
	pf.avatar_url
ORDER BY ALL_LIKES DESC
LIMIT(5)

-- Trae el feed de un usuario: los posts de las cuentas que sigue, ordenados por fecha, 
-- con likes y comentarios 

WITH count_likes AS (
	SELECT l.publication_id AS post_id, COUNT(*) AS total_likes
	FROM likes l
	GROUP BY l.publication_id
), count_comments AS (
	SELECT post_id, COUNT(*) AS total_comments
	FROM comments
	GROUP BY post_id
)
SELECT 
	p.id,
	p.description,
	p.picture,
	u.username,
	p.datecreated,
	COALESCE(cl.total_likes, 0) AS likes,
	COALESCE(cc.total_comments, 0) AS comments
FROM posts p
INNER JOIN follows fl
	ON fl.followed_id = p.user_id 
		AND fl.follower_id = 994
INNER JOIN users u ON u.id = p.user_id
INNER JOIN count_likes cl ON cl.post_id = p.id
LEFT JOIN count_comments cc ON cc.post_id = p.id


-- Dado un hashtag, trae los hashtags relacionados — es decir, 
-- los hashtags que aparecen más frecuentemente en los mismos posts que ese hashtag. 
-- Básicamente "si buscas #gym, también verás #fitness, #workout". 
-- Esto es una query de co-ocurrencia pura, sin ninguna tabla extra.

SELECT 
	h.id,
	h.name,
	COUNT(*) occurrences
FROM hashtags h
INNER JOIN post_hashtag ph 
	ON ph.hashtag_id = h.id AND ph.hashtag_id <> 64
INNER JOIN (
	SELECT post_id FROM post_hashtag
	WHERE hashtag_id = 64
) x ON ph.post_id = x.post_id
GROUP BY 
	h.id,
	h.name
ORDER BY occurrences DESC
LIMIT 5


SELECT * FROM follows
WHERE followed_id = 994

