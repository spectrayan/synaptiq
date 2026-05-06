package com.synaptiq.action.infrastructure.persistence.mongo.repository;

import com.synaptiq.action.infrastructure.persistence.mongo.entity.ActionLogDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ActionLogReactiveMongoRepository extends ReactiveMongoRepository<ActionLogDocument, String> {
}
