package com.example.marketplacemanagement.service;

import com.example.marketplacemanagement.entity.DatabaseSequence;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

// increments the counter and return the next ID
@Service
public class SequenceService {
    private final ReactiveMongoTemplate mongoTemplate;

    public SequenceService(ReactiveMongoTemplate mongoTemplate){
        this.mongoTemplate = mongoTemplate;
    }

    public Mono<Long> nextId(String sequenceName){
        Query query = Query.query(
                Criteria.where("_id").is(sequenceName)
        );

        Update update = new Update().inc("value", 1);

        FindAndModifyOptions options = FindAndModifyOptions.options()
                .upsert(true)
                .returnNew(true);

        return mongoTemplate.findAndModify(
                query,
                update,
                options,
                DatabaseSequence.class
        ).map(DatabaseSequence::getValue);
    }
}
