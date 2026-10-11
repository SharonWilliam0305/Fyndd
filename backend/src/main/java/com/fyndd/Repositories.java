package com.fyndd;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
interface Users extends JpaRepository<User,Long>{Optional<User> findByEmail(String email);Optional<User> findFirstByRole(String role);}
interface Products extends JpaRepository<Product,Long>{}
interface Carts extends JpaRepository<CartItem,Long>{List<CartItem> findByOwnerId(Long id); void deleteByOwnerId(Long id);}
interface Reviews extends JpaRepository<Review,Long>{List<Review> findByProductId(Long id); boolean existsByAuthorIdAndProductId(Long user,Long product);}
interface Orders extends JpaRepository<PurchaseOrder,Long>{List<PurchaseOrder> findByCustomerIdOrderByIdDesc(Long id);}
