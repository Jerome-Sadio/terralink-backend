package sn.terralink.service

import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sn.terralink.dto.AuthResponse
import sn.terralink.dto.VerifyPinRequest
import sn.terralink.repository.UserRepository
import sn.terralink.security.JwtTokenProvider

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: JwtTokenProvider
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun requestPinOtp(phoneNumber: String): Boolean {
        val user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow { IllegalArgumentException("Utilisateur non trouvé pour ce numéro de téléphone") }

        // In development / mock mode, we log the OTP pin check request
        logger.info("OTP PIN requested for phone number: {}. Mock SMS sent.", phoneNumber)
        return true
    }

    @Transactional(readOnly = true)
    fun verifyPinAndLogin(request: VerifyPinRequest): AuthResponse {
        val user = userRepository.findByPhoneNumber(request.phoneNumber)
            .orElseThrow { BadCredentialsException("Numéro de téléphone ou code PIN incorrect") }

        if (!passwordEncoder.matches(request.pin, user.pinHash)) {
            throw BadCredentialsException("Numéro de téléphone ou code PIN incorrect")
        }

        val token = tokenProvider.generateToken(user.id!!, user.phoneNumber, user.role.name)

        return AuthResponse(
            token = token,
            userId = user.id!!,
            phoneNumber = user.phoneNumber,
            firstName = user.firstName,
            lastName = user.lastName,
            role = user.role
        )
    }
}
