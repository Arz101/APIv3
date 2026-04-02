-- FOLLOWS IDX
CREATE INDEX idx_follows_follower_id ON follows(follower_id);
CREATE INDEX idx_follows_followed_id ON follows(followed_id);
CREATE INDEX idx_follows_composite ON follows(follower_id, followed_id);

-- POSTS IDX
CREATE INDEX idx_posts_datecreated ON posts(datecreated)
CREATE INDEX idx_posts_user_id ON posts(user_id)
CREATE INDEX idx_posts_composite ON posts(id, user_id)

-- POST_HASHTAG IDX
CREATE INDEX idx_post_hashtag_post_id ON post_hashtag(post_id)

-- HASHTAGS IDX
CREATE INDEX idx_hashtags_name ON hashtags(name)

-- TOKENS IDX
CREATE INDEX idx_tokens_composite ON tokens(assigned_to, revoked)

-- LIKES IDX
CREATE INDEX idx_likes_composite ON likes (publication_id, user_id)

-- PROFILES IDX
CREATE INDEX idx_profiles_composite ON profiles (user_id, private)

-- COMMENTS IDX
CREATE INDEX idx_comment_parent ON comments(parent_id);