package sn.terralink.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import sn.terralink.domain.entity.User
import sn.terralink.domain.enum.UserStatus
import java.util.UUID

class UserPrincipal(
    val id: UUID,
    val phoneNumber: String,
    private val pinHash: String,
    val role: String,
    private val status: UserStatus,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    override fun getPassword(): String = pinHash
    override fun getUsername(): String = phoneNumber
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = status != UserStatus.SUSPENDED
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = status == UserStatus.ACTIVE

    companion object {
        fun create(user: User): UserPrincipal {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
            return UserPrincipal(
                id = user.id!!,
                phoneNumber = user.phoneNumber,
                pinHash = user.pinHash,
                role = user.role.name,
                status = user.status,
                authorities = authorities
            )
        }
    }
}
