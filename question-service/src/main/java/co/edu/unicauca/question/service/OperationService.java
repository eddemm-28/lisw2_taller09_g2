package co.edu.unicauca.question.service;

import co.edu.unicauca.question.dto.OperationDTO;
import co.edu.unicauca.question.dto.OperationResultEvent;
import co.edu.unicauca.question.exception.ResourceNotFoundException;
import co.edu.unicauca.question.model.Operation;
import co.edu.unicauca.question.repository.OperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationService {

    private static final Logger log = LoggerFactory.getLogger(OperationService.class);

    private final OperationRepository operationRepository;

    public OperationService(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    @Transactional(readOnly = true)
    public OperationDTO findById(String operationId) {
        return operationRepository.findById(operationId)
                .map(OperationDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la operación " + operationId));
    }

    @Transactional
    public void applyResult(OperationResultEvent event) {
        operationRepository.findById(event.operationId()).ifPresentOrElse(operation -> {
            if ("COMPLETED".equals(event.status())) {
                operation.complete(event.message());
            } else {
                operation.fail(event.message());
            }
        }, () -> log.warn("[QuestionService] Resultado para una operación desconocida: {}", event.operationId()));
    }

    public Operation register(Operation operation) {
        return operationRepository.save(operation);
    }
}
