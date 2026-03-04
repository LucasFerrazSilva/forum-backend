package com.ferraz.forumbackend.status;

import com.ferraz.forumbackend.status.entity.DatabaseInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Component;

@Component
public class StatusDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public DatabaseInfo getDatabaseInfo() {
        String sql = """
            SELECT
                version() as version,
                (SELECT setting::int FROM pg_settings WHERE name = 'max_connections') as maxConnections,
                (SELECT count(*)::int FROM pg_stat_activity) as activeConnections
            """;

        Tuple result =
                (Tuple) entityManager.createNativeQuery(sql, Tuple.class).getSingleResult();

        return new DatabaseInfo(
                result.get("version", String.class),
                result.get("maxConnections", Integer.class),
                result.get("activeConnections", Integer.class)
                );
    }

}
