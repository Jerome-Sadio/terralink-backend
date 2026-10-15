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

    companion object {
        fun normalizePhoneNumber(phone: String): String {
            val cleaned = phone.replace("[\\s\\-\\.\\(\\)]".toRegex(), "")
            return if (cleaned.startsWith("+")) {
                cleaned
            } else if (cleaned.startsWith("00")) {
                "+" + cleaned.substring(2)
            } else {
                "+221$cleaned"
            }
        }
    }

    fun requestPinOtp(phoneNumber: String): Boolean {
        val cleanPhone = normalizePhoneNumber(phoneNumber)
        val user = userRepository.findByPhoneNumber(cleanPhone)
            .or { userRepository.findByPhoneNumber(phoneNumber) }
            .orElseThrow { IllegalArgumentException("Utilisateur non trouvé pour ce numéro de téléphone") }

        logger.info("OTP PIN requested for phone number: {}. Mock SMS sent.", cleanPhone)
        return true
    }

    @Transactional(readOnly = true)
    fun verifyPinAndLogin(request: VerifyPinRequest): AuthResponse {
        val cleanPhone = normalizePhoneNumber(request.phoneNumber)
        val user = userRepository.findByPhoneNumber(cleanPhone)
            .or { userRepository.findByPhoneNumber(request.phoneNumber) }
            .or { userRepository.findByPhoneNumber(request.phoneNumber.trim().replace("\\s+".toRegex(), "")) }
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
