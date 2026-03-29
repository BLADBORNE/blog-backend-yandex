CREATE SCHEMA IF NOT EXISTS blog;

CREATE TABLE IF NOT EXISTS blog.tag
(
    ID    BIGSERIAL    NOT NULL,
    TITLE VARCHAR(100) NOT NULL,

    CONSTRAINT blog_tag_pkey PRIMARY KEY (ID),
    CONSTRAINT blog_tag_name_not_blank CHECK (TRIM(TITLE) <> ''),
    CONSTRAINT blog_tag_name_unique UNIQUE (TITLE)
);

CREATE TABLE IF NOT EXISTS blog.post
(
    ID    BIGSERIAL        NOT NULL,
    TITLE VARCHAR(100)     NOT NULL,
    TEXT  VARCHAR(1000)    NOT NULL,
    LIKES BIGINT DEFAULT 0 NOT NULL,

    CONSTRAINT blog_post_pkey PRIMARY KEY (ID),
    CONSTRAINT blog_post_title_not_blank CHECK (TRIM(TITLE) <> ''),
    CONSTRAINT blog_post_text_not_blank CHECK (TRIM(TEXT) <> ''),
    CONSTRAINT blog_post_likes_is_positive CHECK (LIKES >= 0)
);

CREATE TABLE IF NOT EXISTS blog.post_tag
(
    POST_ID BIGINT NOT NULL,
    TAG_ID  BIGINT NOT NULL,

    CONSTRAINT blog_post_tag_pkey PRIMARY KEY (POST_ID, TAG_ID),
    CONSTRAINT blog_post_post_fkey FOREIGN KEY (POST_ID) REFERENCES blog.post (ID) ON DELETE CASCADE,
    CONSTRAINT blog_post_tag_fkey FOREIGN KEY (TAG_ID) REFERENCES blog.tag (ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS blog.post_comment
(
    ID      BIGSERIAL     NOT NULL,
    POST_ID BIGINT        NOT NULL,
    TEXT    VARCHAR(1000) NOT NULL,

    CONSTRAINT blog_post_comment_pkey PRIMARY KEY (ID),
    CONSTRAINT blog_post_comment_fkey FOREIGN KEY (POST_ID) REFERENCES blog.post (ID) ON DELETE CASCADE,
    CONSTRAINT blog_post_comment_text_not_blank CHECK (TRIM(TEXT) <> '')
);

CREATE TABLE IF NOT EXISTS blog.post_file_info
(
    FILE_NAME VARCHAR(100) NOT NULL,
    POST_ID   BIGINT       NOT NULL,

    CONSTRAINT blog_post_file_info_pkey PRIMARY KEY (FILE_NAME),
    CONSTRAINT blog_post_file_info_fkey FOREIGN KEY (POST_ID) REFERENCES blog.post (ID) ON DELETE CASCADE,
    CONSTRAINT blog_post_file_info_not_blank CHECK (TRIM(FILE_NAME) <> ''),
    CONSTRAINT blog_post_file_info_unique UNIQUE (POST_ID)
);
