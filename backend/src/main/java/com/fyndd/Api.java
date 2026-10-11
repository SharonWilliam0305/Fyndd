package com.fyndd;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
@RestController @RequestMapping("/api") class Api {
final ShopService s;final Tokens tokens;final BCryptPasswordEncoder encoder;final Users users;final Products products;final Carts carts;final Orders orders;final Reviews reviews;final CatalogCache catalog;
Api(ShopService s,Tokens t,BCryptPasswordEncoder e,Users u,Products p,Carts c,Orders o,Reviews r,CatalogCache catalog){this.s=s;tokens=t;encoder=e;users=u;products=p;carts=c;orders=o;reviews=r;this.catalog=catalog;}
record Registration(@NotBlank @Size(max=80)String name,@Email @NotBlank String email,@Size(min=8,max=72)String password){}
record Login(@Email @NotBlank String email,@NotBlank String password){}
record ProductInput(@NotBlank @Size(max=120)String name,@NotBlank @Size(max=80)String category,@NotBlank @Size(max=3000)String description,@NotBlank @Pattern(regexp="https://.*")String image,@Min(1) @Max(10000000)int price,@Min(0) @Max(100000)int stock){}
record CartInput(@NotNull Long productId,@Min(0) @Max(99)int quantity){}
record ReviewInput(@Min(1) @Max(5)int rating,@NotBlank @Size(max=2000)String text){}
Map<String,Object> session(User u){return Map.of("user",u,"token",tokens.create(u));}
@PostMapping("/auth/register") Object register(@Valid @RequestBody Registration r){String email=r.email().toLowerCase(Locale.ROOT).trim();if(users.findByEmail(email).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered");var u=new User();u.name=r.name().trim();u.email=email;u.password=encoder.encode(r.password());u.role="CUSTOMER";return session(users.save(u));}
@PostMapping("/auth/login") Object login(@Valid @RequestBody Login r){var u=users.findByEmail(r.email().toLowerCase(Locale.ROOT).trim()).filter(x->encoder.matches(r.password(),x.password)).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Incorrect email or password"));return session(u);}
@GetMapping("/me") User me(){return users.findById(s.me().id).orElseThrow();}
record ProfileInput(@NotBlank @Size(max=80)String name,@Size(max=30)String phone,@Size(max=500)String address){}
@PutMapping("/me") User profile(@Valid @RequestBody ProfileInput r){var u=users.findById(s.me().id).orElseThrow();u.name=r.name().trim();if(u.profile==null)u.profile=new UserProfile();u.profile.phone=r.phone();u.profile.address=r.address();return users.save(u);}
@GetMapping("/wishlist") @org.springframework.transaction.annotation.Transactional Object wishlist(){return users.findById(s.me().id).orElseThrow().favorites.stream().toList();}
@PostMapping("/wishlist/{id}") @org.springframework.transaction.annotation.Transactional void favorite(@PathVariable Long id){var u=users.findById(s.me().id).orElseThrow();u.favorites.add(s.product(id));users.save(u);}
@DeleteMapping("/wishlist/{id}") @org.springframework.transaction.annotation.Transactional void unfavorite(@PathVariable Long id){var u=users.findById(s.me().id).orElseThrow();u.favorites.removeIf(p->p.id.equals(id));users.save(u);}
@GetMapping("/products") Object products(@RequestParam(defaultValue="")String q,@RequestParam(defaultValue="")String category,@RequestParam(defaultValue="featured")String sort,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="12")int size){var list=catalog.all().stream().filter(p->(p.name+" "+p.description).toLowerCase().contains(q.toLowerCase())&&(category.isEmpty()||category.equals(p.category))).sorted(sort.equals("price-low")?Comparator.comparingInt(p->p.price):sort.equals("price-high")?Comparator.comparingInt((Product p)->p.price).reversed():Comparator.comparing(p->p.id)).toList();int limit=Math.max(1,Math.min(48,size)),start=Math.min(list.size(),Math.min(100000,Math.max(0,page))*limit);return Map.of("items",list.subList(start,Math.min(start+limit,list.size())),"total",list.size(),"pages",(list.size()+limit-1)/limit);}
@GetMapping("/products/{id}") Product product(@PathVariable Long id){return s.product(id);}
@org.springframework.cache.annotation.CacheEvict(value="catalog",allEntries=true) @PostMapping("/products") Product create(@Valid @RequestBody ProductInput r){s.role("SELLER","ADMIN");var p=new Product();p.seller=s.me();return save(p,r);}
@org.springframework.cache.annotation.CacheEvict(value="catalog",allEntries=true) @PutMapping("/products/{id}") Product update(@PathVariable Long id,@Valid @RequestBody ProductInput r){var p=s.product(id);s.owns(p);return save(p,r);}
Product save(Product p,ProductInput r){p.name=r.name();p.category=r.category();p.description=r.description();p.image=r.image();p.price=r.price();p.stock=r.stock();return products.save(p);}
@org.springframework.cache.annotation.CacheEvict(value="catalog",allEntries=true) @DeleteMapping("/products/{id}") void delete(@PathVariable Long id){var p=s.product(id);s.owns(p);products.delete(p);}
@GetMapping("/products/{id}/reviews") Object reviews(@PathVariable Long id){return reviews.findByProductId(id);}
@PostMapping("/products/{id}/reviews") Review review(@PathVariable Long id,@Valid @RequestBody ReviewInput r){var u=s.me();if(reviews.existsByAuthorIdAndProductId(u.id,id))throw new ResponseStatusException(HttpStatus.CONFLICT,"You already reviewed this item");var v=new Review();v.author=u;v.product=s.product(id);v.rating=r.rating();v.text=r.text();return reviews.save(v);}
@GetMapping("/cart") Object cart(){return carts.findByOwnerId(s.me().id);}
@PutMapping("/cart") Object cart(@Valid @RequestBody CartInput r){var u=s.me();var p=s.product(r.productId());if(r.quantity()>p.stock)throw new ResponseStatusException(HttpStatus.CONFLICT,"Not enough stock");var c=carts.findByOwnerId(u.id).stream().filter(x->x.product.id.equals(p.id)).findFirst().orElseGet(CartItem::new);if(r.quantity()==0){if(c.id!=null)carts.delete(c);}else{c.owner=u;c.product=p;c.quantity=r.quantity();carts.save(c);}return cart();}
@PostMapping("/orders") Object checkout(@RequestBody Map<String,String> r){return s.checkout(r.get("address"));}
@PostMapping("/orders/{id}/cancel") Object cancelOrder(@PathVariable Long id){return s.cancelOrder(id);}
@GetMapping("/orders") Object orders(){return orders.findByCustomerIdOrderByIdDesc(s.me().id);}
@GetMapping("/manage/products") Object inventory(){s.role("SELLER","ADMIN");return products.findAll().stream().filter(p->s.me().role.equals("ADMIN")||p.seller.id.equals(s.me().id)).toList();}
@GetMapping("/manage/orders") Object allOrders(){s.role("SELLER","ADMIN");var user=s.me();return orders.findAll().stream().filter(o->user.role.equals("ADMIN")||o.items.stream().anyMatch(i->i.sellerId.equals(user.id))).map(o->{var visible=o.items.stream().filter(i->user.role.equals("ADMIN")||i.sellerId.equals(user.id)).toList();return Map.of("id",o.id,"customer",o.customer,"status",o.status,"address",o.address,"createdAt",o.createdAt,"items",visible,"total",visible.stream().mapToInt(i->i.price*i.quantity).sum());}).toList();}
@PutMapping("/manage/orders/{id}") Object status(@PathVariable Long id,@RequestBody Map<String,String> r){s.role("ADMIN");var o=orders.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));var next=r.get("status");var transitions=Map.of("PLACED","PROCESSING","PROCESSING","SHIPPED","SHIPPED","DELIVERED");if(!Objects.equals(transitions.get(o.status),next))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid order status transition");o.status=next;return orders.save(o);}
@GetMapping("/manage/users") Object users(){s.role("ADMIN");return users.findAll();}
@PutMapping("/manage/users/{id}/role") Object role(@PathVariable Long id,@RequestBody Map<String,String> r){s.role("ADMIN");String role=r.get("role");if(!List.of("CUSTOMER","SELLER","ADMIN").contains(role))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid role");if(id.equals(s.me().id))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"You cannot change your own role");var u=users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));u.role=role;return users.save(u);}
}
@RestControllerAdvice class Errors {
@ExceptionHandler(ResponseStatusException.class) ResponseEntity<?> response(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",Objects.requireNonNullElse(e.getReason(),"Request failed")));}
@ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class) ResponseEntity<?> validation(org.springframework.web.bind.MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("message",e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).findFirst().orElse("Invalid input")));}
@ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class) ResponseEntity<?> constraint(Exception e){return ResponseEntity.status(409).body(Map.of("message","This record is referenced by another record and cannot be removed."));}
@ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class) ResponseEntity<?> stock(Exception e){return ResponseEntity.status(409).body(Map.of("message","Inventory changed. Please refresh and try again."));}
}





