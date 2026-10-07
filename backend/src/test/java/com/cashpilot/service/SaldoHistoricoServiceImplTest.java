package com.cashpilot.service;

import com.cashpilot.dto.response.SaldoHistoricoPontoDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.SaldoDiario;
import com.cashpilot.entity.enums.SaldoOrigem;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.SaldoDiarioRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.impl.SaldoHistoricoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaldoHistoricoService")
class SaldoHistoricoServiceImplTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private SaldoDiarioRepository saldoDiarioRepository;

    @Mock
    private SaldoCalculator saldoCalculator;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private SaldoHistoricoServiceImpl service;

    @Captor
    private ArgumentCaptor<List<SaldoDiario>> novosCaptor;

    private LocalDate hoje;
    private LocalDate ontem;
    private BankAccount conta;

    @BeforeEach
    void setUp() {
        hoje = LocalDate.now();
        ontem = hoje.minusDays(1);
        conta = conta(10L, hoje.minusDays(3), true);
    }

    private BankAccount conta(Long id, LocalDate dataSaldoInicial, boolean ativa) {
        return BankAccount.builder().id(id).saldoInicial(new BigDecimal("100.00"))
                .dataSaldoInicial(dataSaldoInicial).ativa(ativa).build();
    }

    private TreeMap<LocalDate, BigDecimal> serie(LocalDate inicio, String... saldos) {
        TreeMap<LocalDate, BigDecimal> serie = new TreeMap<>();
        for (int i = 0; i < saldos.length; i++) {
            serie.put(inicio.plusDays(i), new BigDecimal(saldos[i]));
        }
        return serie;
    }

    private SaldoDiario registro(LocalDate dia, String saldo, SaldoOrigem origem) {
        return SaldoDiario.builder().contaBancaria(conta).data(dia).saldo(new BigDecimal(saldo)).origem(origem).build();
    }

    @Test
    @DisplayName("Conta sem registros é reconstruída desde a data do saldo inicial; só ontem é CAPTURADO")
    void deveReconstruirDesdeADataDoSaldoInicial() {
        when(saldoDiarioRepository.findUltimaData(10L)).thenReturn(Optional.empty());
        when(saldoCalculator.serieDiaria(conta, hoje.minusDays(3), ontem))
                .thenReturn(serie(hoje.minusDays(3), "100", "150", "120"));

        int criados = service.capturarParaConta(conta, hoje);

        assertThat(criados).isEqualTo(3);
        verify(saldoDiarioRepository).saveAll(novosCaptor.capture());
        assertThat(novosCaptor.getValue()).extracting(SaldoDiario::getOrigem)
                .containsExactly(SaldoOrigem.RECONSTRUIDO, SaldoOrigem.RECONSTRUIDO, SaldoOrigem.CAPTURADO);
        assertThat(novosCaptor.getValue()).extracting(SaldoDiario::getData)
                .containsExactly(hoje.minusDays(3), hoje.minusDays(2), ontem);
        assertThat(novosCaptor.getValue().get(2).getSaldo()).isEqualByComparingTo("120");
    }

    @Test
    @DisplayName("Captura continua do dia seguinte ao último registro")
    void deveContinuarAPartirDoUltimoRegistro() {
        when(saldoDiarioRepository.findUltimaData(10L)).thenReturn(Optional.of(hoje.minusDays(2)));
        when(saldoCalculator.serieDiaria(conta, ontem, ontem)).thenReturn(serie(ontem, "120"));

        assertThat(service.capturarParaConta(conta, hoje)).isEqualTo(1);
    }

    @Test
    @DisplayName("Segunda execução no mesmo dia não grava nada")
    void naoDeveGravarQuandoOntemJaFoiRegistrado() {
        when(saldoDiarioRepository.findUltimaData(10L)).thenReturn(Optional.of(ontem));

        assertThat(service.capturarParaConta(conta, hoje)).isZero();

        verifyNoInteractions(saldoCalculator);
        verify(saldoDiarioRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Conta aberta hoje ainda não tem dia fechado para registrar")
    void naoDeveGravarParaContaAbertaHoje() {
        BankAccount nova = conta(11L, hoje, true);
        when(saldoDiarioRepository.findUltimaData(11L)).thenReturn(Optional.empty());

        assertThat(service.capturarParaConta(nova, hoje)).isZero();
    }

    @Test
    @DisplayName("Reconstrução é limitada ao período máximo de histórico")
    void deveLimitarReconstrucaoAoPeriodoMaximo() {
        BankAccount antiga = conta(12L, LocalDate.of(1990, 1, 1), true);
        LocalDate limite = hoje.minusDays(SaldoHistoricoServiceImpl.MAX_DIAS_HISTORICO);
        when(saldoDiarioRepository.findUltimaData(12L)).thenReturn(Optional.empty());
        when(saldoCalculator.serieDiaria(antiga, limite, ontem)).thenReturn(serie(limite, "100"));

        service.capturarParaConta(antiga, hoje);

        verify(saldoCalculator).serieDiaria(antiga, limite, ontem);
    }

    @Test
    @DisplayName("Histórico da conta marca como divergente o dia cujo registro não bate com o saldo recalculado")
    void deveMarcarDivergenciaQuandoRegistroDifereDoRecalculado() {
        when(currentUserProvider.getScopeUserIds()).thenReturn(List.of(1L));
        when(bankAccountRepository.findByIdAndUserIdIn(10L, List.of(1L))).thenReturn(Optional.of(conta));
        LocalDate inicio = hoje.minusDays(2);
        when(saldoCalculator.serieDiaria(conta, inicio, hoje)).thenReturn(serie(inicio, "150", "90", "90"));
        when(saldoDiarioRepository.findAllByContaBancariaIdAndDataBetween(10L, inicio, hoje)).thenReturn(List.of(
                registro(inicio, "150.00", SaldoOrigem.RECONSTRUIDO),
                registro(ontem, "120.00", SaldoOrigem.CAPTURADO)));

        List<SaldoHistoricoPontoDTO> pontos = service.getHistoricoConta(10L, 3);

        assertThat(pontos).extracting(SaldoHistoricoPontoDTO::data).containsExactly(inicio, ontem, hoje);
        assertThat(pontos.get(0).divergente()).isFalse();
        assertThat(pontos.get(0).origem()).isEqualTo(SaldoOrigem.RECONSTRUIDO);
        assertThat(pontos.get(1).divergente()).isTrue();
        assertThat(pontos.get(1).saldo()).isEqualByComparingTo("90");
        assertThat(pontos.get(1).saldoRegistrado()).isEqualByComparingTo("120");
        assertThat(pontos.get(2).saldoRegistrado()).isNull();
        assertThat(pontos.get(2).divergente()).isFalse();
    }

    @Test
    @DisplayName("Histórico da conta não recua para antes da data do saldo inicial")
    void naoDeveRecuarAntesDaDataDoSaldoInicial() {
        when(currentUserProvider.getScopeUserIds()).thenReturn(List.of(1L));
        when(bankAccountRepository.findByIdAndUserIdIn(10L, List.of(1L))).thenReturn(Optional.of(conta));
        LocalDate abertura = hoje.minusDays(3);
        when(saldoCalculator.serieDiaria(conta, abertura, hoje)).thenReturn(serie(abertura, "100", "100", "100", "100"));
        when(saldoDiarioRepository.findAllByContaBancariaIdAndDataBetween(10L, abertura, hoje)).thenReturn(List.of());

        List<SaldoHistoricoPontoDTO> pontos = service.getHistoricoConta(10L, 30);

        assertThat(pontos).hasSize(4);
        assertThat(pontos.get(0).data()).isEqualTo(abertura);
    }

    @Test
    @DisplayName("Histórico total soma as contas ativas e ignora as inativas")
    void deveSomarApenasContasAtivasNoHistoricoTotal() {
        BankAccount outra = conta(20L, hoje.minusDays(30), true);
        BankAccount inativa = conta(30L, hoje.minusDays(30), false);
        when(currentUserProvider.getScopeUserIds()).thenReturn(List.of(1L));
        when(bankAccountRepository.findAllByUserIdIn(List.of(1L))).thenReturn(List.of(conta, outra, inativa));
        when(saldoCalculator.serieDiaria(conta, ontem, hoje)).thenReturn(serie(ontem, "100", "80"));
        when(saldoCalculator.serieDiaria(outra, ontem, hoje)).thenReturn(serie(ontem, "500", "500"));
        when(saldoDiarioRepository.findAllByContaBancariaIdAndDataBetween(10L, ontem, hoje))
                .thenReturn(List.of(registro(ontem, "130.00", SaldoOrigem.CAPTURADO)));
        when(saldoDiarioRepository.findAllByContaBancariaIdAndDataBetween(20L, ontem, hoje))
                .thenReturn(List.of(registro(ontem, "500.00", SaldoOrigem.CAPTURADO)));

        List<SaldoHistoricoPontoDTO> pontos = service.getHistoricoTotal(2);

        assertThat(pontos).hasSize(2);
        assertThat(pontos.get(0).saldo()).isEqualByComparingTo("600");
        assertThat(pontos.get(0).saldoRegistrado()).isEqualByComparingTo("630");
        assertThat(pontos.get(0).origem()).isEqualTo(SaldoOrigem.CAPTURADO);
        assertThat(pontos.get(0).divergente()).isTrue();
        assertThat(pontos.get(1).saldo()).isEqualByComparingTo("580");
        assertThat(pontos.get(1).saldoRegistrado()).isNull();
        verify(saldoCalculator, never()).serieDiaria(eq(inativa), any(), any());
    }

    @Test
    @DisplayName("Período fora do intervalo permitido é rejeitado")
    void deveRejeitarPeriodoInvalido() {
        assertThatThrownBy(() -> service.getHistoricoTotal(0)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.getHistoricoTotal(SaldoHistoricoServiceImpl.MAX_DIAS_HISTORICO + 1))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Conta fora do escopo do usuário não expõe histórico")
    void deveLancarExcecaoParaContaForaDoEscopo() {
        when(currentUserProvider.getScopeUserIds()).thenReturn(List.of(1L));
        when(bankAccountRepository.findByIdAndUserIdIn(99L, List.of(1L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getHistoricoConta(99L, 30)).isInstanceOf(ResourceNotFoundException.class);
    }

}
