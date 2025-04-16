package com.mocicarazvan.templatemodule.repositories.beans;

import com.mocicarazvan.templatemodule.models.ManyToOneUserImpl;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository("manyToOneUserBeanRepository")
public interface ManyToOneUserBeanRepository extends ManyToOneUserRepository<ManyToOneUserImpl> {
    @Query("""
            SELECT * FROM many_to_one_user
            WHERE EXTRACT(MONTH FROM created_at) = :month
            AND EXTRACT(YEAR FROM created_at) = :year
            ORDER BY created_at DESC
            """)
    Flux<ManyToOneUserImpl> findModelByMonth(int month, int year);
}
