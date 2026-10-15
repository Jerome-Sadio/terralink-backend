package sn.terralink.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import sn.terralink.dto.AuthResponse
import sn.terralink.dto.LoginRequest
import sn.terralink.dto.VerifyPinRequest
import sn.terralink.service.AuthService

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun requestOtp(@Valid @RequestBody request: LoginRequest): ResponseEntity<Map<String, Any>> {
        val success = authService.requestPinOtp(request.phoneNumber)
        return ResponseEntity.ok(
            mapOf(
                "success" to success,
                "message" to "Veuillez entrer votre code PIN pour vous connecter."
            )
        )
    }

    @PostMapping("/verify-pin")
    fun verifyPin(@Valid @RequestBody request: VerifyPinRequest): ResponseEntity<AuthResponse> {
        val response = authService.verifyPinAndLogin(request)
        return ResponseEntity.ok(response)
    }
}
