package com.fyndd;
import jakarta.persistence.*;
import java.util.*;
@Entity @Table(name="app_users",indexes=@Index(columnList="email",unique=true))
class User { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; public String name; @Column(unique=true) public String email; @com.fasterxml.jackson.annotation.JsonIgnore public String password; public String role; @OneToOne(cascade=CascadeType.ALL) public UserProfile profile; @ManyToMany @com.fasterxml.jackson.annotation.JsonIgnore public Set<Product> favorites=new HashSet<>(); }
@Entity @Table(indexes={@Index(columnList="name"),@Index(columnList="category")})
class Product { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; public String name,category; @Column(length=3000) public String description; public String image; public int price,stock; @ManyToOne @com.fasterxml.jackson.annotation.JsonIgnore public User seller; @Version public Long version; }
@Entity @Table(uniqueConstraints=@UniqueConstraint(columnNames={"owner_id","product_id"})) class CartItem { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @ManyToOne public User owner; @ManyToOne public Product product; public int quantity; }
@Entity class Review { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @ManyToOne @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"email","role","profile"}) public User author; @ManyToOne public Product product; public int rating; @Column(length=2000) public String text; }
@Entity @Table(name="purchase_orders") class PurchaseOrder { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @ManyToOne public User customer; public String status,address; public int total; public java.time.Instant createdAt=java.time.Instant.now(); @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER) public List<OrderLine> items=new ArrayList<>(); }
@Entity class OrderLine { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; public Long productId,sellerId; public String name,image; public int price,quantity; }

@Entity class UserProfile { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; public String phone; @Column(length=500) public String address; }

