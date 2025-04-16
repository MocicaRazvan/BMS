package com.mocicarazvan.templatemodule.repositories.beans;

import com.mocicarazvan.templatemodule.models.IdGeneratedImpl;
import com.mocicarazvan.templatemodule.repositories.IdGeneratedRepository;
import org.springframework.stereotype.Repository;

@Repository("IdGeneratedBeanRepository")
public interface IdGeneratedBeanRepository extends IdGeneratedRepository<IdGeneratedImpl> {
}
