package com.solusi.erp.security.role.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.security.role.infrastructure.persistence.Role;
import com.solusi.erp.security.role.infrastructure.persistence.RoleJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleLookupProviderImplTest {

    private final RoleJpaRepository roleJpaRepository = mock(RoleJpaRepository.class);
    private final RoleLookupProviderImpl provider = new RoleLookupProviderImpl(roleJpaRepository);

    @Test
    @DisplayName("resolve returns LookupDto with name and description as subText")
    void resolve_found_returnsLookupDto() {
        Role entity = new Role();
        entity.setId(1L);
        entity.setName("ADMIN");
        entity.setDescription("System Administrator");

        when(roleJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        LookupDto result = provider.resolve(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("ADMIN");
        assertThat(result.subText()).isEqualTo("System Administrator");
    }

    @Test
    @DisplayName("resolve returns null when roleId is null")
    void resolve_nullId_returnsNull() {
        LookupDto result = provider.resolve(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolve returns null when role not found")
    void resolve_notFound_returnsNull() {
        when(roleJpaRepository.findById(999L)).thenReturn(Optional.empty());

        LookupDto result = provider.resolve(999L);
        assertThat(result).isNull();
    }
}
