package com.mocicarazvan.templatemodule.repositories;


import com.mocicarazvan.templatemodule.models.TitleBodyImages;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TitleBodyImagesRepository<M extends TitleBodyImages> extends TitleBodyRepository<M> {
}
