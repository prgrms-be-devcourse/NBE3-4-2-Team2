package com.example.backend.social.reaction.like.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.example.backend.entity.LikeEntity;
import com.example.backend.entity.LikeRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.social.reaction.like.dto.LikeInfo;
import com.example.backend.social.reaction.like.scheduler.LikeSyncManager;
import com.example.backend.social.reaction.like.util.RedisKeyUtil;
import com.example.backend.social.reaction.like.util.component.RedisLikeService;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class LikeSyncServiceTest {

    @Autowired
    private LikeSyncService likeSyncService;
    
    @Autowired
    private LikeSyncManager likeSyncManager;
    
    @Autowired
    private RedisLikeService redisLikeService;
    
    @Autowired
    private LikeRepository likeRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private MemberService memberService;
    
    private MemberEntity testMember;
    private MemberEntity contentMember;
    private PostEntity testPost;
    
    @BeforeEach
    public void setup() {
        // 테스트 전에 데이터 초기화
        likeRepository.deleteAll();
        postRepository.deleteAll();
        memberRepository.deleteAll();

        // 시퀀스 초기화
        entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE post ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE likes ALTER COLUMN id RESTART WITH 1").executeUpdate();

        // 테스트용 멤버 추가
        testMember = memberService.join("testMember", "testPassword", "test@gmail.com");
        contentMember = memberService.join("contentMember", "testPassword", "content@gmail.com");
        
        // 테스트용 게시물 추가
        PostEntity post = PostEntity.builder()
            .content("testContent")
            .member(contentMember)  // 컨텐츠 작성자는 contentMember
            .build();
        testPost = postRepository.save(post);
    }
    
    @Test
    @DisplayName("1. 좋아요 5회 이상 시 벌크 쿼리 실행 테스트")
    public void t001() throws Exception {
        // Given - 좋아요를 누를 5명의 멤버 생성
        MemberEntity member1 = memberService.join("member1", "password", "member1@test.com");
        MemberEntity member2 = memberService.join("member2", "password", "member2@test.com");
        MemberEntity member3 = memberService.join("member3", "password", "member3@test.com");
        MemberEntity member4 = memberService.join("member4", "password", "member4@test.com");
        MemberEntity member5 = memberService.join("member5", "password", "member5@test.com");
        
        Long postId = testPost.getId();
        String resourceType = "POST";
        
        // 좋아요 정보 수동 생성 및 Redis에 저장
        for (MemberEntity member : Arrays.asList(member1, member2, member3, member4, member5)) {
            String likeKey = RedisKeyUtil.getLikeKey(resourceType, postId, member.getId());
            String countKey = RedisKeyUtil.getLikeCountKey(resourceType, postId);
            
            LikeInfo likeInfo = new LikeInfo(
                member.getId(),
                postId,
                resourceType,
                LocalDateTime.now(),
                LocalDateTime.now(),
                true
            );
            
            redisLikeService.updateLikeInfo(likeKey, likeInfo);
            redisLikeService.updateLikeCount(countKey, true);
            
            // 동기화 스케줄링
            likeSyncManager.scheduleSyncToDatabase(member.getId(), postId, resourceType, true, true);
        }
        
        // 잠시 대기 (자동 동기화 발생 기다림 - 배치 사이즈 5에 도달했을 때)
        Thread.sleep(1000);
        
        // Then - DB에 좋아요 정보가 저장되었는지 확인
        List<LikeEntity> likes = likeRepository.findAll();
        assertEquals(5, likes.size());
        
        // 각 사용자의 좋아요가 저장되었는지 확인
        Set<Long> memberIds = likes.stream()
            .map(like -> like.getMember().getId())
            .collect(Collectors.toSet());
            
        assertTrue(memberIds.contains(member1.getId()));
        assertTrue(memberIds.contains(member2.getId()));
        assertTrue(memberIds.contains(member3.getId()));
        assertTrue(memberIds.contains(member4.getId()));
        assertTrue(memberIds.contains(member5.getId()));
    }
    
    @Test
    @DisplayName("2. 30초 후 벌크 쿼리 실행 테스트 (수동 트리거)")
    public void t002() throws Exception {
        // Given
        MemberEntity likeMember = memberService.join("likeMember", "password", "likeMember@test.com");
        Long postId = testPost.getId();
        String resourceType = "POST";
        
        // 좋아요 정보 생성 및 Redis에 저장
        String likeKey = RedisKeyUtil.getLikeKey(resourceType, postId, likeMember.getId());
        String countKey = RedisKeyUtil.getLikeCountKey(resourceType, postId);
        
        LikeInfo likeInfo = new LikeInfo(
            likeMember.getId(),
            postId,
            resourceType,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );
        
        redisLikeService.updateLikeInfo(likeKey, likeInfo);
        redisLikeService.updateLikeCount(countKey, true);
        
        // 동기화 스케줄링
        likeSyncManager.scheduleSyncToDatabase(likeMember.getId(), postId, resourceType, true, true);
        
        // 시간 경과 시뮬레이션 (수동으로 syncToDatabase 호출)
        likeSyncService.syncToDatabase();
        
        // 잠시 대기
        Thread.sleep(500);
        
        // Then - DB에 좋아요 정보가 저장되었는지 확인
        List<LikeEntity> likes = likeRepository.findAll();
        assertEquals(1, likes.size());
        assertEquals(likeMember.getId(), likes.get(0).getMember().getId());
    }
    
    @Test
    @DisplayName("3. 여러 리소스 타입이 같은 배치에서 처리되는지 테스트")
    public void t003() throws Exception {
        // Given
        MemberEntity member1 = memberService.join("member1", "password", "member1@test.com");
        MemberEntity member2 = memberService.join("member2", "password", "member2@test.com");
        Long postId = testPost.getId();
        
        // POST, COMMENT, REPLY 리소스 타입에 대한 좋아요 등록
        String[] resourceTypes = {"POST", "COMMENT", "REPLY"};
        
        for (String resourceType : resourceTypes) {
            String likeKey1 = RedisKeyUtil.getLikeKey(resourceType, postId, member1.getId());
            String countKey1 = RedisKeyUtil.getLikeCountKey(resourceType, postId);
            
            LikeInfo likeInfo1 = new LikeInfo(
                member1.getId(),
                postId,
                resourceType,
                LocalDateTime.now(),
                LocalDateTime.now(),
                true
            );
            
            redisLikeService.updateLikeInfo(likeKey1, likeInfo1);
            redisLikeService.updateLikeCount(countKey1, true);
            
            // member1의 좋아요 동기화 스케줄링
            likeSyncManager.scheduleSyncToDatabase(member1.getId(), postId, resourceType, true, true);
            
            // member2도 같은 리소스에 좋아요
            String likeKey2 = RedisKeyUtil.getLikeKey(resourceType, postId, member2.getId());
            
            LikeInfo likeInfo2 = new LikeInfo(
                member2.getId(),
                postId,
                resourceType,
                LocalDateTime.now(),
                LocalDateTime.now(),
                true
            );
            
            redisLikeService.updateLikeInfo(likeKey2, likeInfo2);
            redisLikeService.updateLikeCount(countKey1, true);
            
            // member2의 좋아요 동기화 스케줄링
            likeSyncManager.scheduleSyncToDatabase(member2.getId(), postId, resourceType, true, true);
        }
        
        // 총 6개의 좋아요가 큐에 들어갔으므로 배치 임계값(5)을 넘어 자동 동기화되었을 것임
        Thread.sleep(1000);
        
        // Then - DB에 모든 좋아요 정보가 저장되었는지 확인
        List<LikeEntity> likes = likeRepository.findAll();
        assertEquals(6, likes.size());  // 3개 리소스 타입 x 2명 멤버 = 6개 좋아요
        
        // 리소스 타입별 좋아요 수 확인
        Map<String, Long> likeCountByResourceType = likes.stream()
            .collect(Collectors.groupingBy(
                like -> like.getResourceType(),
                Collectors.counting()
            ));
        
        assertEquals(2L, likeCountByResourceType.getOrDefault("POST", 0L));
        assertEquals(2L, likeCountByResourceType.getOrDefault("COMMENT", 0L));
        assertEquals(2L, likeCountByResourceType.getOrDefault("REPLY", 0L));
    }
    
    @Test
    @DisplayName("4. 좋아요 취소도 함께 동기화되는지 테스트")
    public void t004() throws Exception {
        // Given
        MemberEntity member = memberService.join("member", "password", "member@test.com");
        Long postId = testPost.getId();
        String resourceType = "POST";
        
        // 1. 먼저 좋아요 적용
        String likeKey = RedisKeyUtil.getLikeKey(resourceType, postId, member.getId());
        String countKey = RedisKeyUtil.getLikeCountKey(resourceType, postId);
        
        LikeInfo likeInfo1 = new LikeInfo(
            member.getId(),
            postId,
            resourceType,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true  // 좋아요 적용
        );
        
        redisLikeService.updateLikeInfo(likeKey, likeInfo1);
        redisLikeService.updateLikeCount(countKey, true);
        likeSyncManager.scheduleSyncToDatabase(member.getId(), postId, resourceType, true, true);
        
        // 수동 동기화
        likeSyncService.syncToDatabase();
        Thread.sleep(500);
        
        // 2. 좋아요가 DB에 저장되었는지 확인
        List<LikeEntity> initialLikes = likeRepository.findAll();
        assertEquals(1, initialLikes.size());
        assertTrue(initialLikes.get(0).isLiked());  // 좋아요 활성 상태 확인
        
        // 3. 좋아요 취소
        LikeInfo likeInfo2 = new LikeInfo(
            member.getId(),
            postId,
            resourceType,
            likeInfo1.createDate(),
            LocalDateTime.now(),
            false
        );
        
        redisLikeService.updateLikeInfo(likeKey, likeInfo2);
        redisLikeService.updateLikeCount(countKey, false);
        likeSyncManager.scheduleSyncToDatabase(member.getId(), postId, resourceType, false, false);
        
        // 수동 동기화
        likeSyncService.syncToDatabase();
        Thread.sleep(500);
        
        // 4. 좋아요 취소가 DB에 반영되었는지 확인
        List<LikeEntity> updatedLikes = likeRepository.findAll();
        assertEquals(1, updatedLikes.size());
        assertFalse(updatedLikes.get(0).isLiked());
    }
}
