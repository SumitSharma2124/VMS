package com.vms.pass;

import com.vms.qr.QrService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PassQrTest {

    @Test
    void passQrContainsOnlyTheToken() {
        QrService qrService = new QrService();
        String token = new PassTokenGenerator().next();

        assertThat(qrService.decode(qrService.generatePng(token, 512))).isEqualTo(token);
    }
}
