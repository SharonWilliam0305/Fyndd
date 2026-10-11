package com.fyndd;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Configuration class Seed {
@Bean CommandLineRunner seedData(Users users,Products products,BCryptPasswordEncoder encoder,org.springframework.cache.CacheManager cache,@Value("${app.seed}")boolean enabled,@Value("${BOOTSTRAP_ADMIN_EMAIL:}")String bootstrapAdminEmail,@Value("${BOOTSTRAP_ADMIN_PASSWORD:}")String bootstrapAdminPassword){return args->{boolean hasAdminEmail=!bootstrapAdminEmail.isBlank(),hasAdminPassword=!bootstrapAdminPassword.isBlank();if(hasAdminEmail!=hasAdminPassword)throw new IllegalStateException("Set both BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD, or neither");if(hasAdminEmail){if(bootstrapAdminPassword.length()<16)throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD must contain at least 16 characters");if(users.findFirstByRole("ADMIN").isEmpty()){String email=bootstrapAdminEmail.trim().toLowerCase(java.util.Locale.ROOT);if(users.findByEmail(email).isPresent())throw new IllegalStateException("BOOTSTRAP_ADMIN_EMAIL already belongs to a non-admin account");var admin=new User();admin.name="FYNDD Administrator";admin.email=email;admin.password=encoder.encode(bootstrapAdminPassword);admin.role="ADMIN";users.save(admin);}}if(enabled&&users.count()==0){for(String role:new String[]{"ADMIN","SELLER","CUSTOMER"}){var u=new User();u.name=role.equals("CUSTOMER")?"Alex Morgan":role.equals("SELLER")?"Studio Seller":"FYNDD Admin";u.email=role.toLowerCase()+"@fyndd.local";u.password=encoder.encode("FynddDemo123!");u.role=role;users.save(u);}}var seller=users.findByEmail("seller@fyndd.local").orElseGet(()->users.findByEmail("catalog@fyndd.local").orElseGet(()->{var catalogSeller=new User();catalogSeller.name="FYNDD Store";catalogSeller.email="catalog@fyndd.local";catalogSeller.password=encoder.encode(java.util.UUID.randomUUID().toString());catalogSeller.role="SELLER";return users.save(catalogSeller);}));var names=new java.util.HashSet<String>();products.findAll().forEach(p->names.add(p.name));String[][] data={
{"Everyday Canvas Tote","Accessories","129900","photo-1553062407-98eeb64c6a62","A beautifully practical carryall. Durable cotton canvas, generous space, and thoughtfully placed pockets."},
{"Studio Wireless Headphones","Electronics","599900","photo-1505740420928-5e560c06d30e","Make room for your favorite sounds. Comfortable over-ear cushions and rich, balanced audio."},
{"Minimal Ceramic Vase","Home & Living","189900","photo-1578749556568-bc2c40e68b61","Sculptural ceramic with a warm matte finish. A quiet statement for your shelf or table."},
{"Classic Everyday Sneakers","Fashion","349900","photo-1549298916-b41d501d3772","Clean lines, a cushioned footbed, and an effortless everyday silhouette."},
{"Analog Leather Watch","Accessories","429900","photo-1524805444758-089113d48a6d","Timeless design with a soft leather strap. A considered finishing touch."},
{"Pour-over Coffee Set","Home & Living","249900","photo-1495474472287-4d71bcdd2085","Slow mornings start here. Everything you need for a satisfying daily coffee ritual."},
{"Pocket Digital Camera","Electronics","1299900","photo-1516035069371-29a1b244cc32","Capture the little things. Compact design and simple controls for everyday storytelling."},
{"Essential Cotton Tee","Fashion","99900","photo-1521572163474-6864f9cf17ab","Soft breathable cotton in a relaxed fit. The foundation of a considered wardrobe."},
{"Urban Travel Backpack","Accessories","279900","photo-1622260614153-03223fb72052","A spacious everyday backpack with a padded laptop compartment and adjustable shoulder straps."},
{"Everyday Sunglasses","Accessories","149900","photo-1511499767150-a48a237f0083","Lightweight frames and a timeless silhouette for sunny days and weekend adventures."},
{"Compact Bluetooth Speaker","Electronics","229900","photo-1608043152269-423dbba4e7e1","Portable wireless sound with easy pairing and a compact shape for your desk or travels."},
{"Mechanical Desk Keyboard","Electronics","459900","photo-1587829741301-dc798b83add3","A satisfying typing experience with responsive mechanical keys and a clean desktop footprint."},
{"Adjustable Reading Lamp","Home & Living","199900","photo-1507473885765-e6ed057f782c","Warm focused light and an adjustable shade for reading corners, desks, and evening routines."},
{"Soft Woven Cushion","Home & Living","89900","photo-1584100936595-c0654b55a2e2","An inviting textured cushion that adds a soft touch to your sofa or favorite chair."},
{"Relaxed Denim Jacket","Fashion","299900","photo-1543076447-215ad9ba6923","An easy layering essential with classic denim texture, practical pockets, and a relaxed fit."},
{"Everyday Running Shoes","Fashion","399900","photo-1542291026-7eec264c27ff","Lightweight cushioning and breathable construction for active mornings and everyday movement."},
{"Stainless Steel Water Bottle","Accessories","79900","photo-1602143407151-7111542de6e8","A reusable stainless steel bottle with a secure lid for commutes, workouts, and daily hydration."},
{"Over-ear Studio Headset","Electronics","329900","photo-1546435770-a3e426bf472b","Comfortable padded earcups and a clear listening experience for calls, music, and focused work."},
{"Indoor Planter Set","Home & Living","159900","photo-1416879595882-3373a0480b5b","Bring a little green indoors with a coordinated set of planters for your favorite houseplants."},
{"Linen Weekend Shirt","Fashion","179900","photo-1596755094514-f87e34085b2c","An airy linen-blend shirt with an easy fit for relaxed weekends and warm-weather outings."}};for(var d:data){if(names.contains(d[0]))continue;var p=new Product();p.name=d[0];p.category=d[1];p.price=Integer.parseInt(d[2]);p.image="https://images.unsplash.com/"+d[3]+"?auto=format&fit=crop&w=900&q=80";p.description=d[4];p.stock=25;p.seller=seller;products.save(p);}cache.getCache("catalog").clear();};}}



