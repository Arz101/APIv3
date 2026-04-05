


SELECT p.id,
       COUNT(CASE WHEN i.likes = true THEN 1 END) * 3 +
       COUNT(CASE WHEN i.comments = true THEN 1 END) * 2 +
       COUNT(CASE WHEN i.save = true THEN 1 END) * 1 AS score
FROM posts p
LEFT JOIN postviewed i ON i.post_id = p.id
WHERE p.datecreated >= NOW() - INTERVAL '30 Days'
GROUP BY p.id
ORDER BY score DESC
