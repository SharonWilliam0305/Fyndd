package com.fyndd;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionalEventListener;
import org.slf4j.LoggerFactory;
record OrderPlaced(Long id,int total){}
@Component class OrderEvents {
 @Async @TransactionalEventListener public void placed(OrderPlaced event){LoggerFactory.getLogger(OrderEvents.class).atInfo().addKeyValue("orderId",event.id()).addKeyValue("totalPaise",event.total()).log("order_placed");}
}
