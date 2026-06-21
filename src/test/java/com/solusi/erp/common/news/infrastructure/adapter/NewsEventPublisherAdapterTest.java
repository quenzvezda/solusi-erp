package com.solusi.erp.common.news.infrastructure.adapter;

import com.solusi.erp.core.event.ApprovalRequestedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NewsEventPublisherAdapterTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void publishApprovalRequestedShouldIncludeNewsDetailPath() {
        NewsEventPublisherAdapter adapter = new NewsEventPublisherAdapter(applicationEventPublisher);

        adapter.publishApprovalRequested(1L, "NEWS-001", 10L, 20L);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        ApprovalRequestedEvent event = (ApprovalRequestedEvent) captor.getValue();
        assertThat(event.getReferenceType()).isEqualTo("NEWS");
        assertThat(event.getReferenceId()).isEqualTo(1L);
        assertThat(event.getReferenceCode()).isEqualTo("NEWS-001");
        assertThat(event.getDocumentPath()).isEqualTo("/common/news/1");
    }
}
