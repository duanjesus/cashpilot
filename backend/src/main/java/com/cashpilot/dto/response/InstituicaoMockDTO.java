package com.cashpilot.dto.response;

/**
 * Illustrative, fully fictional institution entry returned by the Open Finance
 * stub's {@code GET /instituicoes} endpoint. Not backed by any table — this is a
 * hardcoded static list, never a real bank integration.
 */
public record InstituicaoMockDTO(
        String id,
        String nome
) {
}
