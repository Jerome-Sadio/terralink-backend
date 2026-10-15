package sn.terralink.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sn.terralink.domain.entity.Farm
import sn.terralink.domain.entity.User
import java.util.UUID

@Repository
interface FarmRepository : JpaRepository<Farm, UUID> {
    fun findAllByOwner(owner: User): List<Farm>
    fun findAllByOwnerId(ownerId: UUID): List<Farm>
}
