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
        val defaultPinHash = passwordEncoder.encode("1234")

        // 1. Jérôme San (+221770000000) - Compte Admin invisible
        val jerome = userRepository.findByPhoneNumber("+221770000000").orElse(null)
        if (jerome != null) {
            jerome.firstName = "Jérôme"
            jerome.lastName = "San"
            userRepository.save(jerome)
        } else {
            userRepository.save(
                User(
                    phoneNumber = "+221770000000",
                    pinHash = defaultPinHash,
                    role = UserRole.ADMIN,
                    firstName = "Jérôme",
                    lastName = "San"
                )
            )
        }

        // Nettoyage éventuel du numéro temporaire
        userRepository.findByPhoneNumber("+221773333333").ifPresent {
            userRepository.delete(it)
        }

        // 1b. Ulysse Gueye (+221778605182) - Admin principal
        val ulysse = userRepository.findByPhoneNumber("+221778605182").orElse(null)
        if (ulysse != null) {
            ulysse.firstName = "Ulysse"
            ulysse.lastName = "Gueye"
            ulysse.role = UserRole.ADMIN
            userRepository.save(ulysse)
        } else {
            userRepository.save(
                User(
                    phoneNumber = "+221778605182",
                    pinHash = defaultPinHash,
                    role = UserRole.ADMIN,
                    firstName = "Ulysse",
                    lastName = "Gueye"
                )
            )
            logger.info("Admin Ulysse Gueye configuré avec succès (+221778605182 / 1234)")
        }

        // Nettoyer et normaliser tous les numéros existants en base de données
        userRepository.findAll().forEach { u ->
            val clean = sn.terralink.service.AuthService.normalizePhoneNumber(u.phoneNumber)
            if (clean != u.phoneNumber) {
                logger.info("Normalisation numéro pour ${u.firstName} ${u.lastName}: '${u.phoneNumber}' -> '$clean'")
                u.phoneNumber = clean
                userRepository.save(u)
            }
        }

        if (userRepository.count() <= 2L) {
            logger.info("Initializing default test users and farm data...")

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
