package com.spectrayan.synaptiq.workflow.application.service;

import com.spectrayan.synaptiq.shared.exception.ResourceNotFoundException;
import com.spectrayan.synaptiq.workflow.application.port.in.WorkflowSharingUseCase;
import com.spectrayan.synaptiq.workflow.application.port.out.WorkflowPersistencePort;
import com.spectrayan.synaptiq.workflow.domain.model.Workflow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowSharingService implements WorkflowSharingUseCase {
    private final WorkflowPersistencePort persistence;

    @Override
    public Mono<Workflow> share(String tenantId, String wid) {
        return persistence.findById(wid, tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Workflow not found")))
            .flatMap(w -> {
                w.setShareToken(UUID.randomUUID().toString());
                w.setPublic(true);
                return persistence.save(w);
            });
    }

    @Override
    public Mono<Workflow> getShared(String shareToken) {
        return persistence.findByShareToken(shareToken)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Shared workflow not found")));
    }
}
