package sn.terralink.config

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import sn.terralink.domain.entity.Farm
import sn.terralink.domain.entity.User
import sn.terralink.domain.enum.FarmType
import sn.terralink.domain.enum.UserRole
import sn.terralink.dto.AssignControllerRequest
import sn.terralink.repository.FarmRepository
import sn.terralink.repository.UserRepository
import sn.terralink.service.FarmService
import java.math.BigDecimal

@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val farmRepository: FarmRepository,
    private val farmService: FarmService,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    override fun run(vararg args: String?) {
        if (userRepository.count() == 0L) {
            logger.info("Initializing default test users and farm data...")

            val defaultPinHash = passwordEncoder.encode("1234")

            // 1. Admin - Direction Générale
            val admin = userRepository.save(
                User(
                    phoneNumber = "+221770000000",
                    pinHash = defaultPinHash,
                    role = UserRole.ADMIN,
                    firstName = "Ulysse",
                    lastName = "Gueye"
                )
            )

            // 2. Controller
            val controller = userRepository.save(
                User(
                    phoneNumber = "+221771111111",
                    pinHash = defaultPinHash,
                    role = UserRole.CONTROLEUR,
                    firstName = "Moussa",
                    lastName = "Sow"
                )
            )

            // 3. Owner
            val owner = userRepository.save(
                User(
                    phoneNumber = "+221772222222",
                    pinHash = defaultPinHash,
                    role = UserRole.PROPRIETAIRE,
                    firstName = "Fatou",
                    lastName = "Ndiaye"
                )
            )

            // 4. Sample Farm for Owner
            val farm = farmRepository.save(
                Farm(
                    owner = owner,
                    name = "Ferme Agricole de Niayes",
                    address = "Niayes, Région de Thiès, Sénégal",
                    sizeHectares = BigDecimal("15.5"),
                    type = FarmType.AGRICULTURE
                )
            )

            // 5. Assign Controller to Farm
            farmService.assignController(
                AssignControllerRequest(
                    farmId = farm.id!!,
                    controllerId = controller.id!!
                )
            )

            logger.info("Default data initialized successfully!")
            logger.info("Admin Phone: +221770000000 | PIN: 1234")
            logger.info("Contrôleur Phone: +221771111111 | PIN: 1234")
            logger.info("Propriétaire Phone: +221772222222 | PIN: 1234")
        }
    }
}
