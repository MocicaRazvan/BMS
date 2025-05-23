package com.mocicarazvan.templatemodule.repositories.beans;


import com.mocicarazvan.templatemodule.repositories.impl.AssociativeEntityRepositoryImpl;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;

@Repository("associativeEntityBeanRepositoryImplImpl")
public class AssociativeEntityRepositoryImplImpl extends AssociativeEntityRepositoryImpl {
    public AssociativeEntityRepositoryImplImpl(DatabaseClient databaseClient, TransactionalOperator transactionalOperator) {
        super(databaseClient, transactionalOperator, "associative_entity");
    }
}
