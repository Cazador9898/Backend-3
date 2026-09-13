package cl.duoc.bancoxyz.bff.controller;

import cl.duoc.bancoxyz.bff.dto.WebDashboardResponse;
import cl.duoc.bancoxyz.bff.service.WebBffService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/bff/web")
public class WebBffController {

    private final WebBffService service;

    public WebBffController(WebBffService service) {
        this.service = service;
    }

    @GetMapping("/cuentas/{cuentaId}/dashboard")
    public ResponseEntity<WebDashboardResponse> dashboard(@PathVariable Long cuentaId) {
        return ResponseEntity.ok()
                .header("X-BFF-Channel", "WEB")
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate())
                .body(service.obtenerDashboard(cuentaId));
    }
}
