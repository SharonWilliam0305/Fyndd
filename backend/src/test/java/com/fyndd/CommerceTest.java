package com.fyndd;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc class CommerceTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json;
 String token(String email)throws Exception{return json.readTree(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\""+email+"\",\"password\":\"FynddDemo123!\"}")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("token").asText();}
 @Test void checkoutAndPermissions()throws Exception{
  mvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(20));
  mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
  String customer=token("customer@fyndd.local"),seller=token("seller@fyndd.local"),admin=token("admin@fyndd.local");
  mvc.perform(get("/api/manage/users").header("Authorization","Bearer "+customer)).andExpect(status().isForbidden());
  mvc.perform(get("/api/manage/users").header("Authorization","Bearer "+seller)).andExpect(status().isForbidden());
  mvc.perform(put("/api/cart").header("Authorization","Bearer "+customer).contentType(MediaType.APPLICATION_JSON).content("{\"productId\":1,\"quantity\":99}")).andExpect(status().isConflict());
  mvc.perform(put("/api/cart").header("Authorization","Bearer "+customer).contentType(MediaType.APPLICATION_JSON).content("{\"productId\":1,\"quantity\":2}")).andExpect(status().isOk());
  String order=mvc.perform(post("/api/orders").header("Authorization","Bearer "+customer).contentType(MediaType.APPLICATION_JSON).content("{\"address\":\"Alex, 12 Test Street, Bengaluru 560001\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(259800)).andReturn().getResponse().getContentAsString();
  long id=json.readTree(order).get("id").asLong();
  mvc.perform(get("/api/cart").header("Authorization","Bearer "+customer)).andExpect(jsonPath("$.length()").value(0));
  mvc.perform(get("/api/products/1")).andExpect(jsonPath("$.stock").value(23));
  mvc.perform(post("/api/orders").header("Authorization","Bearer "+customer).contentType(MediaType.APPLICATION_JSON).content("{\"address\":\"Alex, 12 Test Street, Bengaluru 560001\"}")).andExpect(status().isBadRequest());
  mvc.perform(put("/api/manage/orders/"+id).header("Authorization","Bearer "+seller).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"SHIPPED\"}")).andExpect(status().isForbidden());
  mvc.perform(put("/api/manage/orders/"+id).header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DELIVERED\"}")).andExpect(status().isBadRequest());
  mvc.perform(put("/api/manage/orders/"+id).header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PROCESSING\"}")).andExpect(status().isOk());
  mvc.perform(post("/api/products/1/reviews").header("Authorization","Bearer "+customer).contentType(MediaType.APPLICATION_JSON).content("{\"rating\":5,\"text\":\"A lovely everyday tote\"}")).andExpect(status().isOk());
  mvc.perform(post("/api/products/1/reviews").header("Authorization","Bearer "+customer).contentType(MediaType.APPLICATION_JSON).content("{\"rating\":5,\"text\":\"Duplicate\"}")).andExpect(status().isConflict());
 }
 @Test void profilesFavoritesAndSellerCrud()throws Exception{
  String customer=token("customer@fyndd.local"),seller=token("seller@fyndd.local");
  mvc.perform(put("/api/me").header("Authorization","Bearer "+customer).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Alex Morgan\",\"phone\":\"9876543210\",\"address\":\"12 Test Street\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.profile.phone").value("9876543210"));
  mvc.perform(post("/api/wishlist/1").header("Authorization","Bearer "+customer)).andExpect(status().isOk());
  mvc.perform(get("/api/wishlist").header("Authorization","Bearer "+customer)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));
  mvc.perform(delete("/api/wishlist/1").header("Authorization","Bearer "+customer)).andExpect(status().isOk());
  mvc.perform(get("/api/wishlist").header("Authorization","Bearer "+customer)).andExpect(jsonPath("$.length()").value(0));
  String body="{\"name\":\"Test find\",\"category\":\"Accessories\",\"description\":\"A test product\",\"image\":\"https://example.com/item.jpg\",\"price\":10000,\"stock\":3}";
  mvc.perform(post("/api/products").header("Authorization","Bearer "+customer).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
  long id=json.readTree(mvc.perform(post("/api/products").header("Authorization","Bearer "+seller).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("id").asLong();
  mvc.perform(put("/api/products/"+id).header("Authorization","Bearer "+seller).contentType(MediaType.APPLICATION_JSON).content(body.replace("10000","20000"))).andExpect(status().isOk()).andExpect(jsonPath("$.price").value(20000));
  mvc.perform(delete("/api/products/"+id).header("Authorization","Bearer "+seller)).andExpect(status().isOk());
  mvc.perform(get("/api/products/"+id)).andExpect(status().isNotFound());
  mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"headphones\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.mode").value("catalog"));
 }
 @Test void registrationValidationAndAuthentication()throws Exception{
  mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Test\",\"email\":\"bad\",\"password\":\"short\"}")).andExpect(status().isBadRequest());
  mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Test User\",\"email\":\"new@fyndd.local\",\"password\":\"ValidPass123\",\"role\":\"ADMIN\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.user.role").value("CUSTOMER")).andExpect(jsonPath("$.user.password").doesNotExist());
  mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"new@fyndd.local\",\"password\":\"wrong\"}")).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/cart").header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
 }
}

