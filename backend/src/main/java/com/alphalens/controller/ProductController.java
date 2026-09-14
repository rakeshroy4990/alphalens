package com.alphalens.controller;

import com.alphalens.auth.CurrentUser;
import com.alphalens.service.AiExplanationService;
import com.alphalens.service.backtest.BacktestService;
import com.alphalens.service.broker.BrokerService;
import com.alphalens.service.portfolio.PortfolioService;
import com.alphalens.service.trading.TradingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final AiExplanationService aiExplanationService;
    private final PortfolioService portfolioService;
    private final BrokerService brokerService;
    private final TradingService tradingService;
    private final BacktestService backtestService;

    public ProductController(
            AiExplanationService aiExplanationService,
            PortfolioService portfolioService,
            BrokerService brokerService,
            TradingService tradingService,
            BacktestService backtestService) {
        this.aiExplanationService = aiExplanationService;
        this.portfolioService = portfolioService;
        this.brokerService = brokerService;
        this.tradingService = tradingService;
        this.backtestService = backtestService;
    }

    @GetMapping("/research/{instrumentId}/explain")
    public Map<String, Object> explain(@PathVariable long instrumentId) {
        return aiExplanationService.explain(instrumentId);
    }

    @GetMapping("/portfolio")
    public Map<String, Object> portfolio() {
        return portfolioService.snapshot(user());
    }

    @PostMapping("/portfolio/holdings")
    public Map<String, Object> addHolding(@RequestBody Map<String, Object> body) {
        return portfolioService.addManual(
                user(),
                ((Number) body.get("instrumentId")).longValue(),
                new BigDecimal(body.get("quantity").toString()),
                new BigDecimal(body.get("averagePrice").toString())
        );
    }

    @PostMapping("/portfolio/import")
    public Map<String, Object> importHoldings(@RequestBody Map<String, String> body) {
        return portfolioService.importCsv(user(), body.get("csv"));
    }

    @GetMapping("/broker/login")
    public Map<String, Object> brokerLogin() {
        return brokerService.login();
    }

    @PostMapping("/broker/connect")
    public Map<String, Object> brokerConnect(@RequestBody Map<String, Object> body) {
        boolean consent = Boolean.TRUE.equals(body.get("consent"));
        return brokerService.connect(user(), consent);
    }

    @PostMapping("/broker/sync")
    public Map<String, Object> brokerSync(@RequestParam(defaultValue = "false") boolean force) {
        return brokerService.sync(user(), force);
    }

    @PostMapping("/orders/preview")
    public Map<String, Object> preview(@RequestBody Map<String, Object> body) {
        BigDecimal price = body.get("price") == null ? null : new BigDecimal(body.get("price").toString());
        return tradingService.preview(
                user(),
                ((Number) body.get("instrumentId")).longValue(),
                (String) body.get("side"),
                new BigDecimal(body.get("quantity").toString()),
                price
        );
    }

    @PostMapping("/orders/confirm")
    public Map<String, Object> confirm(@RequestBody Map<String, String> body) {
        return tradingService.confirm(body.get("confirmationId"));
    }

    @PostMapping("/orders/cancel")
    public Map<String, Object> cancel(@RequestBody Map<String, String> body) {
        return tradingService.cancel(body.get("orderId"));
    }

    @PostMapping("/backtests")
    public Map<String, Object> backtest(@RequestBody Map<String, Object> body) {
        return backtestService.run(
                user(),
                ((Number) body.get("algorithmId")).longValue(),
                ((Number) body.get("instrumentId")).longValue(),
                LocalDate.parse(body.get("from").toString()),
                LocalDate.parse(body.get("to").toString())
        );
    }

    private UUID user() {
        return CurrentUser.requireId();
    }
}
