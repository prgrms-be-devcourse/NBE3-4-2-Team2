package com.example.backend.content.image.controller;

import com.example.backend.content.image.service.ImageService;
import com.example.backend.entity.ImageEntity;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ImageService imageService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	private PostEntity testPost;

	@BeforeEach
	void setUp() {
		// ✅ 테스트용 사용자(MemberEntity) 저장 (refresh_token 추가)
		MemberEntity testMember = memberRepository.save(
			MemberEntity.builder()
				.username("testUser")
				.email("test@example.com")
				.password("password")
				.refreshToken("testRefreshToken") // ✅ refreshToken 값 설정
				.build()
		);

		// ✅ 테스트용 게시물(PostEntity) 저장
		testPost = postRepository.save(
			PostEntity.create("테스트 게시물", testMember) // ✅ 회원을 포함하여 저장
		);
	}


	@Test
	void 이미지_업로드_성공() throws Exception {
		// Given: Mock 이미지 파일 생성
		MockMultipartFile imageFile = new MockMultipartFile(
			"images", "testImage.jpg", "image/jpeg", "test data".getBytes()
		);

		Long postId = testPost.getId();

		// When: API 요청 수행
		var result = mockMvc.perform(multipart("/api-v1/images")
				.file(imageFile)
				.param("postId", String.valueOf(postId))
				.contentType(MediaType.MULTIPART_FORM_DATA))
			.andExpect(status().isCreated())
			.andReturn();

		// Then: 업로드된 이미지가 저장되었는지 확인
		List<ImageEntity> savedImages = testPost.getImages();
		assertThat(savedImages).isNotEmpty();
		assertThat(savedImages.get(0).getImageUrl()).contains("/uploads/");
	}
}
