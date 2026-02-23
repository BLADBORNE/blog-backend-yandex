-- Очистка всех таблиц схемы blog
TRUNCATE TABLE
    blog.post_file_info,
    blog.post_comment,
    blog.post_tag,
    blog.post,
    blog.tag
    RESTART IDENTITY
    CASCADE;
