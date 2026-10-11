package com.fyndd;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
@Configuration class Security {
@Bean BCryptPasswordEncoder encoder(){return new BCryptPasswordEncoder();}
@Bean SecurityFilterChain chain(HttpSecurity http,TokenFilter filter)throws Exception{return http.csrf(c->c.disable()).cors(c->{}).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a->a.requestMatchers("/api/auth/**","/api/products/**","/api/chat","/actuator/health","/swagger-ui/**","/v3/api-docs/**").permitAll().anyRequest().authenticated()).exceptionHandling(e->e.authenticationEntryPoint((r,s,x)->s.sendError(401))).addFilterBefore(filter,UsernamePasswordAuthenticationFilter.class).build();}
@Bean CorsConfigurationSource cors(@Value("${app.cors-origin}")String origin){var c=new CorsConfiguration();var origins=new ArrayList<>(List.of(origin));if(origin.equals("http://localhost:5173"))origins.add("http://127.0.0.1:5173");c.setAllowedOrigins(origins);c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type"));var s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;}}
@Component class Tokens {
private final javax.crypto.SecretKey key;
Tokens(@Value("${app.jwt-secret}")String secret){key=Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
String create(User u){return Jwts.builder().subject(u.id.toString()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+86400000)).signWith(key).compact();}
Long parse(String token){return Long.valueOf(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());}}
@Component class TokenFilter extends OncePerRequestFilter {
final Tokens tokens; final Users users; TokenFilter(Tokens t,Users u){tokens=t;users=u;}
protected void doFilterInternal(HttpServletRequest r,HttpServletResponse s,FilterChain chain)throws ServletException,java.io.IOException{String h=r.getHeader("Authorization");if(h!=null&&h.startsWith("Bearer ")){try{User u=users.findById(tokens.parse(h.substring(7))).orElseThrow();SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u,null,List.of(new SimpleGrantedAuthority("ROLE_"+u.role))));}catch(Exception e){SecurityContextHolder.clearContext();}}chain.doFilter(r,s);}}
