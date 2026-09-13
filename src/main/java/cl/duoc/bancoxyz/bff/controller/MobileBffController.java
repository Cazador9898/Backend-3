package cl.duoc.bancoxyz.bff.controller;

import cl.duoc.bancoxyz.bff.dto.MobileSummaryResponse;
import cl.duoc.bancoxyz.bff.service.MobileBffService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/bff/mobile")
public class MobileBffController {

    private final MobileBffService service;

    public MobileBffController(MobileBffService service) {
        this.service = service;
    }

    @GetMapping("/cuentas/{cuentaId}/resumen")
    public ResponseEntity<MobileSummaryResponse> resumen(@PathVariable Long cuentaId) {
        return ResponseEntity.ok()
                .header("X-BFF-Channel", "MOBILE")
                .cacheControl(CacheControl.maxAge(15, TimeUnit.SECONDS).cachePrivate())
                .body(service.obtenerResumen(cuentaId));
    }
}
