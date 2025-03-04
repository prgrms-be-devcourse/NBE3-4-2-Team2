package com.example.backend.social.feed.implement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.CommentEntity;
import com.example.backend.entity.CommentRepository;
import com.example.backend.entity.FollowEntity;
import com.example.backend.entity.FollowRepository;
import com.example.backend.entity.HashtagEntity;
import com.example.backend.entity.HashtagRepository;
import com.example.backend.entity.ImageEntity;
import com.example.backend.entity.ImageRepository;
import com.example.backend.entity.LikeRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostHashtagEntity;
import com.example.backend.entity.PostHashtagRepository;
import com.example.backend.entity.PostRepository;
import com.example.backend.global.event.CommentEventListener;
import com.example.backend.global.event.FollowEventListener;
import com.example.backend.global.event.LikeEventListener;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.social.reaction.like.service.LikeService;

import jakarta.persistence.EntityManager;

@Component
public class FeedTestHelper {

	// @Autowired
	// private MemberRepository memberRepository;
	//
	// @Autowired
	// private HashtagRepository hashtagRepository;
	//
	// @Autowired
	// private PostRepository postRepository;
	//
	// @Autowired
	// private PostHashtagRepository postHashtagRepository;
	//
	// @Autowired
	// private ImageRepository imageRepository;
	//
	// @Autowired
	// private LikeRepository likeRepository;
	//
	// @Autowired
	// private CommentRepository commentRepository;
	//
	// @Autowired
	// private FollowRepository followRepository;
	//
	// @Autowired
	// private LikeService likeService;
	//
	// @Autowired
	// EntityManager entityManager;
	// @Autowired
	// private MemberService memberService;
	//
	// @MockitoBean
	// LikeEventListener likeEventListener;
	// @MockitoBean
	// FollowEventListener followEventListener;
	// @MockitoBean
	// CommentEventListener commentEventListener;
	//
	// @Transactional
	// public void setData() {
	//
	// 	entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate();
	//
	// 	// 1. 멤버 생성 (20명)
	// 	List<MemberEntity> members = new ArrayList<>();
	// 	for (int i = 1; i <= 20; i++) {
	// 		MemberEntity member = memberService.join("user" + i, "password" + i, "user" + i + "@test.com");
	// 		members.add(member);
	// 	}
	// 	memberRepository.flush();
	//
	// 	// 2. 해시태그 생성 (20개)
	// 	List<HashtagEntity> hashtags = Arrays.asList(
	// 			"Java", "Spring", "Coding", "Algorithm", "JPA",
	// 			"QueryDSL", "Database", "Test", "SpringBoot", "WebDev",
	// 			"Backend", "Frontend", "FullStack", "Cloud", "AWS",
	// 			"DevOps", "MSA", "Security", "Docker", "Kubernetes"
	// 		).stream()
	// 		.map(content -> HashtagEntity.builder().content(content).build())
	// 		.collect(Collectors.toList());
	// 	hashtagRepository.saveAll(hashtags);
	// 	hashtagRepository.flush();
	//
	// 	// 3. 팔로우 관계 생성 (user1이 모든 사용자를 팔로우)
	// 	List<FollowEntity> follows = new ArrayList<>();
	// 	MemberEntity user1 = members.get(0);
	// 	for (int i = 1; i < members.size() / 2; i++) {
	// 		FollowEntity follow = FollowEntity.builder()
	// 			.sender(user1)
	// 			.receiver(members.get(i))
	// 			.build();
	// 		follows.add(follow);
	//
	// 		// 양방향 관계 설정
	// 		user1.getFollowingList().add(follow);
	// 		members.get(i).getFollowerList().add(follow);
	// 	}
	// 	followRepository.saveAll(follows);
	// 	followRepository.flush();
	//
	// 	// 4. 게시글 생성 (각 유저별 5개, 시간순 정렬을 위한 일정한 간격)
	// 	List<PostEntity> posts = new ArrayList<>();
	// 	for (int memberIndex = 0; memberIndex < members.size(); memberIndex++) {
	// 		MemberEntity member = members.get(memberIndex);
	// 		for (int postIndex = 0; postIndex < 5; postIndex++) {
	// 			int totalIndex = (memberIndex * 5) + postIndex;
	// 			posts.add(PostEntity.builder()
	// 				.content("게시글 " + member.getUsername() + "-" + postIndex)
	// 				.member(member)
	// 				.isDeleted(false)
	// 				.build());
	// 		}
	// 	}
	// 	postRepository.saveAll(posts);
	// 	postRepository.flush();
	//
	// 	// 5. 해시태그 연결 (각 게시글마다 순차적으로 3개씩)
	// 	List<PostHashtagEntity> postHashtags = new ArrayList<>();
	// 	for (int i = 0; i < posts.size(); i++) {
	// 		PostEntity post = posts.get(i);
	// 		// 각 게시글마다 연속된 3개의 해시태그 할당
	// 		for (int j = 0; j < 3; j++) {
	// 			int hashtagIndex = (i + j) % hashtags.size();
	// 			postHashtags.add(PostHashtagEntity.builder()
	// 				.post(post)
	// 				.hashtag(hashtags.get(hashtagIndex))
	// 				.build());
	// 		}
	// 	}
	// 	postHashtagRepository.saveAll(postHashtags);
	// 	postHashtagRepository.flush();
	//
	// 	// 6. 이미지 추가 (각 게시글마다 2개씩)
	// 	List<ImageEntity> images = new ArrayList<>();
	// 	for (int i = 0; i < posts.size(); i++) {
	// 		PostEntity post = posts.get(i);
	// 		for (int j = 0; j < 2; j++) {
	// 			images.add(ImageEntity.builder()
	// 				.imageUrl("image_" + post.getMember().getUsername() + "_" + i + "_" + j + ".jpg")
	// 				.post(post)
	// 				.build());
	// 		}
	// 	}
	// 	imageRepository.saveAll(images);
	// 	imageRepository.flush();
	//
	// 	// 7. 좋아요 추가 (각 게시글에 첫 5명의 사용자가 좋아요)
	// 	for (long i = 1; i <= 20; i++) {
	// 		for (int j = 0; j < 5; j++) {
	// 			likeService.createLike(i, posts.get(((int)(i % 20) * 5 + j)).getId());
	// 		}
	// 	}
	//
	// 	// 8. 댓글 추가 (각 게시글에 첫 3명의 사용자가 댓글)
	// 	List<CommentEntity> comments = new ArrayList<>();
	// 	for (PostEntity post : posts) {
	// 		for (int i = 0; i < 3; i++) {
	// 			comments.add(CommentEntity.builder()
	// 				.content("댓글 " + members.get(i).getUsername() + " -> " + post.getMember().getUsername())
	// 				.post(post)
	// 				.member(members.get(i))
	// 				.build());
	// 		}
	// 	}
	// 	commentRepository.saveAll(comments);
	// 	commentRepository.flush();
	// }

}
