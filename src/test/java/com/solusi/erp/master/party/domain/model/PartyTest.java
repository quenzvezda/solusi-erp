package com.solusi.erp.master.party.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.master.shared.model.PartyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Party Domain Model Tests")
class PartyTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata (id == null)")
    void createNew_setsAllFieldsWithEmptyMetadata() {
        Set<Long> roleIds = Set.of(1L, 2L);
        List<PartyContactData> contacts = new ArrayList<>();
        List<PartyAddressData> addresses = new ArrayList<>();
        List<PartyIdentificationData> identifications = new ArrayList<>();

        Party party = Party.createNew("PTY-001", "Acme Corp", "Mr.", PartyType.ORGANIZATION,
                "Notes here", true, "email@test.com", "0812345", contacts, addresses, identifications, roleIds);

        assertThat(party.getCode()).isEqualTo("PTY-001");
        assertThat(party.getName()).isEqualTo("Acme Corp");
        assertThat(party.getSalutation()).isEqualTo("Mr.");
        assertThat(party.getType()).isEqualTo(PartyType.ORGANIZATION);
        assertThat(party.getNotes()).isEqualTo("Notes here");
        assertThat(party.getIsActive()).isTrue();
        assertThat(party.getEmail()).isEqualTo("email@test.com");
        assertThat(party.getPhone()).isEqualTo("0812345");
        assertThat(party.getRoleIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(party.getRoleNames()).isEmpty();
        assertThat(party.getId()).isNull();
    }

    @Test
    @DisplayName("Full constructor preserves all fields including metadata")
    void constructor_preservesAllFields() {
        AuditMetadata metadata = new AuditMetadata(10L, 1L, null, null, null, null);
        Set<Long> roleIds = Set.of(3L);
        List<String> roleNames = List.of("Vendor");

        Party party = new Party(metadata, "PTY-001", "Dr.", "John Doe", PartyType.PERSON,
                "A note", true, "john@test.com", "08111", roleIds, roleNames,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        assertThat(party.getId()).isEqualTo(10L);
        assertThat(party.getMetadata()).isEqualTo(metadata);
        assertThat(party.getCode()).isEqualTo("PTY-001");
        assertThat(party.getSalutation()).isEqualTo("Dr.");
        assertThat(party.getName()).isEqualTo("John Doe");
        assertThat(party.getType()).isEqualTo(PartyType.PERSON);
        assertThat(party.getNotes()).isEqualTo("A note");
        assertThat(party.getIsActive()).isTrue();
        assertThat(party.getEmail()).isEqualTo("john@test.com");
        assertThat(party.getPhone()).isEqualTo("08111");
        assertThat(party.getRoleIds()).containsExactly(3L);
        assertThat(party.getRoleNames()).containsExactly("Vendor");
    }
}

