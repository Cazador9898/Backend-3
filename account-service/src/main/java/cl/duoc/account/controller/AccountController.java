package cl.duoc.account.controller;
import cl.duoc.account.model.Account; import org.springframework.web.bind.annotation.*; import org.springframework.http.ResponseEntity; import java.math.BigDecimal; import java.util.*;
@RestController @RequestMapping("/api/accounts") public class AccountController {
 private final List<Account> accounts=List.of(new Account(101L,"John Doe","ahorro",new BigDecimal("5000")),new Account(102L,"Jane Smith","prestamo",new BigDecimal("8000")),new Account(103L,"Bob Johnson","prestamo",new BigDecimal("12000")),new Account(105L,"Charlie Green","hipoteca",new BigDecimal("7000")));
 @GetMapping public List<Account> all(){return accounts;}
 @GetMapping("/{id}") public ResponseEntity<Account> one(@PathVariable Long id){return accounts.stream().filter(a->a.id().equals(id)).findFirst().map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());}
}
