package co.edu.unicauca.review.model;

/**
 * PENDING: registrada, esperando la confirmacion de Lifecycle Service.
 * ASSIGNED: confirmada (la pregunta paso a IN_REVIEW).
 * CANCELLED: compensada porque la transicion no se pudo realizar.
 * COMPLETED: el revisor ya registro su resultado.
 */
public enum AssignmentStatus {
    PENDING,
    ASSIGNED,
    CANCELLED,
    COMPLETED
}
