package ru.blog.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.blog.backend.dto.comment.CommentDto;
import ru.blog.backend.dto.comment.CommentCreateRequestDto;
import ru.blog.backend.dto.post.PostCreateRequestDto;
import ru.blog.backend.dto.post.PostPageResponseDto;
import ru.blog.backend.dto.post.PostResponseDto;
import ru.blog.backend.dto.post.PostUpdateRequestDto;
import ru.blog.backend.marker.SaveCommentMarker;
import ru.blog.backend.marker.SavePostMarker;
import ru.blog.backend.page.PageableRequest;
import ru.blog.backend.service.CommentService;
import ru.blog.backend.service.PostFileService;
import ru.blog.backend.service.PostService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostService postService;

    private final CommentService commentService;

    private final PostFileService postFileService;

    @PostMapping
    @Validated(value = SavePostMarker.class)
    public PostResponseDto savePost(@RequestBody @Valid PostCreateRequestDto requestDto) {
        return postService.save(requestDto);
    }

    @PutMapping
    @Validated(value = SavePostMarker.class)
    public PostResponseDto updatePost(@RequestBody @Valid PostUpdateRequestDto requestDto) {
        return postService.update(requestDto);
    }

    @DeleteMapping("/{postId}")
    public void deleteById(@PathVariable("postId") Long postId) {
        postService.deleteById(postId);
    }

    @PostMapping("/{postId}/likes")
    public Integer increaseLike(@PathVariable("postId") Long postId) {
        return postService.increaseLike(postId);
    }

    @GetMapping
    public PostPageResponseDto findPostsBySearchString(
        @RequestParam("search") @NotBlank String search,
        @Valid PageableRequest pageableRequest
    ) {
        return postService.findPostsBySearchString(search, pageableRequest);
    }

    @PutMapping("/{postId}/image")
    public void updatePostFile(@RequestParam("image") MultipartFile image, @PathVariable("postId") Long postId) {
        postFileService.updatePostFile(image, postId);
    }

    @GetMapping("/{postId}")
    public PostResponseDto findById(@PathVariable("postId") Long postId) {
        return postService.findById(postId);
    }

    @GetMapping("/{postId}/image")
    public byte[] findFileByPostId(@PathVariable("postId") Long postId) throws IOException {
        return postFileService.findFileByPostId(postId).getContentAsByteArray();
    }

    @PostMapping("/{postId}/comments")
    @Validated(value = SaveCommentMarker.class)
    public CommentDto addCommentToPost(@RequestBody @Valid CommentCreateRequestDto requestDto) {
        return commentService.addCommentToPost(requestDto);
    }

    @GetMapping("/{postId}/comments")
    public List<CommentDto> findCommentsByPostId(@PathVariable("postId") Long postId) {
        return commentService.findCommentsByPostId(postId);
    }

    @GetMapping("/{postId}/comments/{commentId}")
    public CommentDto findCommentByPostIdAndCommentId(
        @PathVariable("postId") Long postId,
        @PathVariable("commentId") Long commentId
    ) {
        return commentService.findCommentByPostIdAndCommentId(postId, commentId);
    }

    @PutMapping("/{postId}/comments/{commentId}")
    @Validated(value = SaveCommentMarker.class)
    public CommentDto updatePostComment(@RequestBody @Valid CommentDto request) {
        return commentService.updatePostComment(request);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public void deleteCommentById(@PathVariable("commentId") Long commentId) {
        commentService.deleteById(commentId);
    }
}
