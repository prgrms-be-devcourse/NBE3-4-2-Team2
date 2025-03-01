package com.example.backend.social.reaction.like.controller;

/*
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LikeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private MemberService memberService;

	@Autowired
	private AccessTokenService accessTokenService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private LikeRepository likeRepository;
	@MockitoBean
	private LikeEventListener likeEventListener;

	private String accessToken;
	private MemberEntity testMember;
	private MemberEntity anotherMember;
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
		entityManager.createNativeQuery("ALTER TABLE like ALTER COLUMN id RESTART WITH 1").executeUpdate();

		// 테스트용 멤버 추가
		testMember = memberService.join("testMember", "testPassword", "test@gmail.com");
		anotherMember = memberService.join("anotherMember", "testPassword", "another@gmail.com");
		accessToken = accessTokenService.genAccessToken(testMember);

		// 테스트용 게시물 추가
		testPost = PostEntity.builder()
			.content("testContent")
			.member(anotherMember)
			.build();
		testPost = postRepository.save(testPost);

		// SecurityContext 설정
		// SecurityUser securityUser = new SecurityUser(testMember.getId(), testMember.getUsername(), testMember.getPassword(), new ArrayList<>());
		CustomUser securityUser = new CustomUser(testMember, null);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	@DisplayName("1. 좋아요 적용 테스트")
	public void t001() throws Exception {
		// When
		ResultActions resultActions = mockMvc.perform(post("/api-v1/like/{postId}", testPost.getId())
			.header("Authorization", "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isOk()) // SSE 세션이 추가되지 않습니다.
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 적용되었습니다."))
			.andExpect(jsonPath("$.data").exists());
	}

	@Test
	@DisplayName("2. 좋아요 취소 테스트")
	public void t002() throws Exception {
		// When & Then First
		MvcResult likeResult = mockMvc.perform(post("/api-v1/like/{postId}", testPost.getId())
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// Given Second
		String likeResponse = likeResult.getResponse().getContentAsString();
		JsonNode likeRoot = objectMapper.readTree(likeResponse);
		Long likeId = likeRoot.path("data").path("likeId").asLong();

		DeleteLikeRequest deleteRequest = DeleteLikeRequest.builder()
			.likeId(likeId)
			.build();
		String deleteRequestJson = objectMapper.writeValueAsString(deleteRequest);

		// When Second
		ResultActions resultActions = mockMvc.perform(delete("/api-v1/like/{postId}", testPost.getId())
			.content(deleteRequestJson)
			.header("Authorization", "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then Second
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 취소되었습니다."))
			.andExpect(jsonPath("$.data").exists());
	}

	@Test
	@DisplayName("3. 존재하지 않는 게시물에 좋아요 적용 테스트")
	public void t003() throws Exception {
		// Given
		Long nonExistentPostId = 99L;

		// When
		ResultActions resultActions = mockMvc.perform(post("/api-v1/like/{postId}", nonExistentPostId)
			.header("Authorization", "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("게시물 정보를 찾을 수 없습니다."));
	}

	@Test
	@DisplayName("4. 좋아요가 이미 적용된 게시물에 좋아요 중복 적용 테스트")
	public void t004() throws Exception {
		// Given
		mockMvc.perform(post("/api-v1/like/{postId}", testPost.getId())
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		// When
		ResultActions resultActions = mockMvc.perform(post("/api-v1/like/{postId}", testPost.getId())
			.header("Authorization", "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isConflict())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("이미 좋아요를 눌렀습니다."));
	}

	@Test
	@DisplayName("5. 좋아요를 누르지 않은 게시물에 좋아요 취소 요청 테스트")
	public void t005() throws Exception {
		// Given
		DeleteLikeRequest deleteRequest = DeleteLikeRequest.builder()
			.likeId(1L)
			.build();
		String deleteRequestJson = objectMapper.writeValueAsString(deleteRequest);

		// When
		ResultActions resultActions = mockMvc.perform(delete("/api-v1/like/{postId}", testPost.getId())
			.header("Authorization", "Bearer " + accessToken)
			.content(deleteRequestJson)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("좋아요 정보를 찾을 수 없습니다."));
	}

	@Test
	@DisplayName("6. 좋아요 취소시 다른 유저가 요청하는 테스트")
	public void t006() throws Exception {
		// Given First
		SecurityUser testSecurityUser = new SecurityUser(testMember.getId(), testMember.getUsername(), testMember.getPassword(), new ArrayList<>());

		// When First
		ResultActions resultActions = mockMvc.perform(post("/api-v1/like/{postId}", testPost.getId())
			.with(user(testSecurityUser))
			.header("Authorization", "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then First
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		// Given Second
		// 좋아요 정보 추출
		String likeResponse = resultActions.andReturn().getResponse().getContentAsString();
		JsonNode likeRoot = objectMapper.readTree(likeResponse);
		Long likeId = likeRoot.path("data").path("likeId").asLong();

		// 새로운 멤버 추가 및 토큰 발급
		MemberEntity otherMember = memberService.join("otherMember", "otherPassword", "other@gmail.com");
		String otherAccessToken = accessTokenService.genAccessToken(otherMember);

		SecurityUser otherSecurityUser = new SecurityUser(otherMember.getId(), otherMember.getUsername(), otherMember.getPassword(), new ArrayList<>());

		// Request DTO 정보 빌드
		DeleteLikeRequest deleteRequest = DeleteLikeRequest.builder()
			.likeId(likeId)
			.build();
		String deleteRequestJson = objectMapper.writeValueAsString(deleteRequest);

		// When Second
		ResultActions resultActions2 = mockMvc.perform(delete("/api-v1/like/{postId}", testPost.getId())
			.with(user(otherSecurityUser))
			.content(deleteRequestJson)
			.header("Authorization", "Bearer " + otherAccessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then Second
		resultActions2.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("좋아요를 취소할 권한이 없습니다."));
	}

	@Test
	@DisplayName("7. DB에 등록된 좋아요와 해당 게시물이 다른 경우 테스트")
	public void t007() throws Exception {
		// Given First
		SecurityUser testSecurityUser = new SecurityUser(testMember.getId(), testMember.getUsername(), testMember.getPassword(), new ArrayList<>());

		// When & Then First
		ResultActions likeResult = mockMvc.perform(post("/api-v1/like/{postId}", testPost.getId())
				.with(user(testSecurityUser))
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());

		// Given Second
		// 좋아요 ID 추출
		String likeResponse = likeResult.andReturn().getResponse().getContentAsString();
		JsonNode likeRoot = objectMapper.readTree(likeResponse);
		Long likeId = likeRoot.path("data").path("likeId").asLong();

		// 새로운 게시물 생성
		PostEntity otherPost = PostEntity.builder()
			.content("otherContent")
			.member(testMember)
			.build();
		otherPost = postRepository.save(otherPost);

		// Request DTO 정보 빌드
		DeleteLikeRequest deleteRequest = DeleteLikeRequest.builder()
			.likeId(likeId)
			.build();
		String deleteRequestJson = objectMapper.writeValueAsString(deleteRequest);

		// When Second
		ResultActions resultActions = mockMvc.perform(delete("/api-v1/like/{postId}", otherPost.getId())
			.with(user(testSecurityUser))
			.content(deleteRequestJson)
			.header("Authorization", "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then Second
		resultActions.andExpect(status().isConflict())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("좋아요 정보와 요청 게시물 정보가 다릅니다."));
	}
}


*/
