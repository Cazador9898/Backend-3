package cl.duoc.customer.controller; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/customers") public class CustomerController {
 public record Customer(Long accountId,String nombre,Integer edad){}
 private final List<Customer> data=List.of(new Customer(101L,"John Doe",30),new Customer(102L,"Jane Smith",25),new Customer(103L,"Bob Johnson",30),new Customer(105L,"Charlie Green",35));
 @GetMapping public List<Customer> all(){return data;} @GetMapping("/{id}") public Customer one(@PathVariable Long id){return data.stream().filter(c->c.accountId().equals(id)).findFirst().orElseThrow();}
}
