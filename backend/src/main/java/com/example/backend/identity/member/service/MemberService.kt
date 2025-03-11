package com.example.backend.identity.member.service

import com.example.backend.entity.MemberEntity
import com.example.backend.entity.MemberRepository
import com.example.backend.global.exception.GlobalException
import com.example.backend.identity.member.exception.MemberErrorCode
import com.example.backend.identity.security.user.CustomUser
import lombok.RequiredArgsConstructor
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun join(username: String, password: String, email: String): MemberEntity {
        memberRepository
            .findByUsername(username)
            .ifPresent {
                throw GlobalException(
                    MemberErrorCode.CONFLICT_RESOURCE,
                    "해당 username은 이미 사용중입니다."
                )
            }

        memberRepository
            .findByEmail(email)
            .ifPresent {
                throw GlobalException(
                    MemberErrorCode.CONFLICT_RESOURCE,
                    "해당 email은 이미 사용중입니다."
                )
            }

        val encodedPassword = passwordEncoder.encode(password)

//        val member = MemberEntity.builder()
//            .username(username)
//            .password(encodedPassword)
//            .email(email)
//            .build()
        val member = MemberEntity(username,encodedPassword,email)

        return memberRepository.save(member)
    }

    fun login(username: String, password: String): CustomUser {
        val member = memberRepository
            .findByUsername(username)
            .orElseThrow {
                UsernameNotFoundException("해당 username을 가진 회원을 찾을 수 없습니다.")
            }

        if (!passwordEncoder.matches(password, member.password)) {
            throw BadCredentialsException("비밀번호가 일치하지 않습니다.")
        }

        val loginUser = CustomUser(
            member
        )

        loginUser.setLogin()

        return loginUser
    }

    fun findByUsername(username: String): Optional<MemberEntity> {
        return memberRepository.findByUsername(username)
    }

    fun findById(authorId: Long): Optional<MemberEntity> {
        return memberRepository.findById(authorId)
    }
}
