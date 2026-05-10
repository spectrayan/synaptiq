package com.spectrayan.synaptiq.action.application.service;

import com.spectrayan.synaptiq.action.application.port.in.ExecuteActionUseCase;
import com.spectrayan.synaptiq.action.application.port.out.ActionLogPersistencePort;
import com.spectrayan.synaptiq.action.application.port.out.SavedItemPersistencePort;
import com.spectrayan.synaptiq.action.domain.model.ActionLog;
import com.spectrayan.synaptiq.action.domain.model.ActionOutcome;
import com.spectrayan.synaptiq.action.domain.model.SavedItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecuteActionService implements ExecuteActionUseCase {

    private final ActionLogPersistencePort actionLogPersistence;
    private final SavedItemPersistencePort savedItemPersistence;

    @Override
    public Mono<ActionResult> execute(ExecuteActionCommand command) {
        var actionLog = ActionLog.builder()
            .tenantId(command.tenantId())
            .appId(command.appId())
            .actionId(command.action())
            .sessionId(command.sessionId())
            .inputSnapshot(ActionLog.ActionInput.builder()
                .action(command.action())
                .itemId(command.itemId())
                .userUid(command.userUid())
                .message(command.message())
                .build())
            .outcome(ActionOutcome.SUCCESS)
            .build();

        return actionLogPersistence.save(actionLog)
            .then(dispatchAction(command))
            .onErrorResume(ex -> {
                log.error("Action '{}' failed: {}", command.action(), ex.getMessage());
                return Mono.just(new ActionResult(false, ex.getMessage(), null));
            });
    }

    private Mono<ActionResult> dispatchAction(ExecuteActionCommand command) {
        return switch (command.action()) {
            case "save_item" -> {
                var savedItem = SavedItem.builder()
                    .tenantId(command.tenantId())
                    .appId(command.appId())
                    .itemId(command.itemId())
                    .sessionId(command.sessionId())
                    .userUid(command.userUid())
                    .itemSnapshot(SavedItem.ItemSnapshot.builder()
                        .sourceItemId(command.itemId())
                        .build())
                    .build();
                yield savedItemPersistence.save(savedItem)
                    .map(item -> new ActionResult(true, "Item saved", item.getId()));
            }
            default -> Mono.just(new ActionResult(true, "Action executed", null));
        };
    }
}
