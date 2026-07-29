package org.acme.shortener.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.shortener.UrlMapping;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.enhanced.dynamodb.Expression;

import java.util.Optional;

@ApplicationScoped
public class UrlRepository {

    private final DynamoDbTable<UrlMapping> table;

    @Inject
    public UrlRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table(
                resolveTableName(),
                TableSchema.fromBean(UrlMapping.class));
    }

    private String resolveTableName() {
        String fromEnv = System.getenv("APP_DYNAMODB_TABLE_NAME");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        String fromProp = System.getProperty("app.dynamodb.table-name");
        return (fromProp != null && !fromProp.isBlank()) ? fromProp : "url-shortener";
    }

    /**
     * Salva o item apenas se o código ainda não existir (evita sobrescrever
     * um par código->URL já existente em caso de colisão).
     */
    public boolean saveIfAbsent(UrlMapping mapping) {
        try {
            PutItemEnhancedRequest<UrlMapping> request = PutItemEnhancedRequest.builder(UrlMapping.class)
                    .item(mapping)
                    .conditionExpression(Expression.builder()
                            .expression("attribute_not_exists(#c)")
                            .putExpressionName("#c", "code")
                            .build())
                    .build();
            table.putItem(request);
            return true;
        } catch (ConditionalCheckFailedException e) {
            return false; // código já existia (colisão) -> chamador tenta outro código
        }
    }

    public Optional<UrlMapping> findByCode(String code) {
        UrlMapping mapping = table.getItem(r -> r.key(k -> k.partitionValue(code)));
        return Optional.ofNullable(mapping);
    }

    public void incrementClicks(UrlMapping mapping) {
        mapping.setClicks(mapping.getClicks() + 1);
        table.updateItem(mapping);
    }
}
