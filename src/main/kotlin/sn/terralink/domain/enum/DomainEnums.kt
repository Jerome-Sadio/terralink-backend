package sn.terralink.domain.enum

enum class UserRole {
    PROPRIETAIRE,
    CONTROLEUR,
    ADMIN
}

enum class UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}

enum class FarmType {
    AGRICULTURE,
    ELEVAGE,
    MIXTE
}

enum class FarmStatus {
    ACTIVE,
    INACTIVE
}

enum class VisitStatus {
    DRAFT,
    SUBMITTED,
    VALIDATED,
    REJECTED
}

enum class OverallHealth {
    EXCELLENT,
    GOOD,
    AVERAGE,
    POOR
}

enum class ExpenseCategory {
    ENGRAIS,
    SEMENCES,
    MATERIEL,
    SALAIRES,
    AUTRES
}

enum class NotificationType {
    VISIT_PUBLISHED,
    VISIT_VALIDATED,
    VISIT_REJECTED,
    SYSTEM
}
