package com.fyndd;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
@Service class ShopService {
final org.springframework.context.ApplicationEventPublisher events; final Users users;final Products products;final Carts carts;final Orders orders;final Reviews reviews;
ShopService(Users u,Products p,Carts c,Orders o,Reviews r,org.springframework.context.ApplicationEventPublisher events){this.events=events;users=u;products=p;carts=c;orders=o;reviews=r;}
User me(){var a=SecurityContextHolder.getContext().getAuthentication();if(a==null||!(a.getPrincipal() instanceof User))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in");return (User)a.getPrincipal();}
void role(String...allowed){if(!List.of(allowed).contains(me().role))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Access denied");}
Product product(Long id){return products.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));}
void owns(Product p){role("SELLER","ADMIN");if(!me().role.equals("ADMIN")&&!p.seller.id.equals(me().id))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You can only manage your own products");}
@org.springframework.cache.annotation.CacheEvict(value="catalog",allEntries=true) @Transactional public PurchaseOrder checkout(String address){if(address==null||address.trim().length()<10||address.length()>500)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter a complete delivery address");User u=me();var cart=carts.findByOwnerId(u.id);if(cart.isEmpty())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Your bag is empty");var order=new PurchaseOrder();order.customer=u;order.address=address.trim();order.status="PLACED";for(var c:cart){var p=product(c.product.id);if(c.quantity>p.stock)throw new ResponseStatusException(HttpStatus.CONFLICT,p.name+" has insufficient stock");p.stock-=c.quantity;products.save(p);var line=new OrderLine();line.productId=p.id;line.sellerId=p.seller.id;line.name=p.name;line.image=p.image;line.quantity=c.quantity;line.price=p.price;order.items.add(line);order.total=Math.addExact(order.total,Math.multiplyExact(line.price,line.quantity));}orders.save(order);carts.deleteByOwnerId(u.id);events.publishEvent(new OrderPlaced(order.id,order.total));return order;}
}

