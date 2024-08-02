package com.mocicarazvan.templatemodule.repositories;

import com.mocicarazvan.templatemodule.models.TitleBody;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TitleBodyRepository<M extends TitleBody> extends ManyToOneUserRepository<M> {
}
