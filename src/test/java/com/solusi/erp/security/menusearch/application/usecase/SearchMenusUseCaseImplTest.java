package com.solusi.erp.security.menusearch.application.usecase;

import com.solusi.erp.core.dto.MenuSearchDto;
import com.solusi.erp.security.menusearch.application.port.MenuQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchMenusUseCase Tests")
class SearchMenusUseCaseImplTest {

    @Mock
    private MenuQueryPort menuQueryPort;

    private SearchMenusUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new SearchMenusUseCaseImpl(menuQueryPort);
    }

    @Test
    @DisplayName("returns empty list when authorities is null")
    void execute_returnsEmpty_whenAuthoritiesNull() {
        List<MenuSearchDto> result = useCase.execute("tax", 10, null);

        assertThat(result).isEmpty();
        verify(menuQueryPort, never()).searchAllowedMenus(anyString(), any(), anyInt());
    }

    @Test
    @DisplayName("returns empty list when authorities is empty")
    void execute_returnsEmpty_whenAuthoritiesEmpty() {
        List<MenuSearchDto> result = useCase.execute("tax", 10, List.of());

        assertThat(result).isEmpty();
        verify(menuQueryPort, never()).searchAllowedMenus(anyString(), any(), anyInt());
    }

    @Test
    @DisplayName("returns mapped DTOs when authorities are provided")
    void execute_returnsMappedDtos_whenAuthoritiesProvided() {
        Collection<String> authorities = List.of("ROLE_ADMIN");
        MenuQueryPort.MenuItem item = new MenuQueryPort.MenuItem(
                "/master/tax", "Tax", "Master Data > Tax",
                "ti-receipt-tax", "Master Data > Tax", "Master Data > Tax"
        );
        when(menuQueryPort.searchAllowedMenus("tax", authorities, 5)).thenReturn(List.of(item));

        List<MenuSearchDto> result = useCase.execute("tax", 5, authorities);

        assertThat(result).hasSize(1);
        MenuSearchDto dto = result.get(0);
        assertThat(dto.urlPath()).isEqualTo("/master/tax");
        assertThat(dto.name()).isEqualTo("Tax");
        assertThat(dto.subText()).isEqualTo("Master Data > Tax");
    }

    @Test
    @DisplayName("returns empty list when port returns no items")
    void execute_returnsEmpty_whenNoMenuItemsFound() {
        Collection<String> authorities = List.of("ROLE_USER");
        when(menuQueryPort.searchAllowedMenus("xyz", authorities, 10)).thenReturn(List.of());

        List<MenuSearchDto> result = useCase.execute("xyz", 10, authorities);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("passes limit parameter to port correctly")
    void execute_passesLimitToPort() {
        Collection<String> authorities = List.of("ROLE_ADMIN");
        when(menuQueryPort.searchAllowedMenus("", authorities, 3)).thenReturn(List.of());

        useCase.execute("", 3, authorities);

        verify(menuQueryPort).searchAllowedMenus("", authorities, 3);
    }
}
