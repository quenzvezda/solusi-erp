package com.solusi.erp.security.user.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.security.user.domain.model.User;

@FunctionalInterface
public interface FindUsersUseCase {
    Page<User> execute(String keyword, Pageable pageable);
}
