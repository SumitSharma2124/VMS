package com.vms.qr;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QrServiceTest {

    @Test
    void generatedPngDecodesToOriginalPayload() {
        QrService service = new QrService();
        String payload = "http://localhost:5173/register?org=abc123";

        byte[] png = service.generatePng(payload, 256);

        assertThat(service.decode(png)).isEqualTo(payload);
    }
}
