# FYNDD

A runnable e-commerce capstone built with React/Vite, Spring Boot, Spring Security, JWT, JPA and relational storage. There is no MongoDB dependency. The default database is persistent H2 for an easy local demo; a MySQL profile and Docker Compose are included.

## Start in PowerShell

Requires Node.js 20.19+ (24 recommended), Java 21, Maven 3.9+, and npm. The dependencies were installed in this workspace.

Terminal 1, from the project directory:

```powershell
Set-Location 'G:\data c\Desktop\FYNDD'
mvn -f backend/pom.xml spring-boot:run
```

Terminal 2:

```powershell
Set-Location 'G:\data c\Desktop\FYNDD'
npm --prefix frontend install
npm --prefix frontend run dev
```

Open http://127.0.0.1:5173. The API listens at http://localhost:8090. Vite forwards `/api` requests to it. Stop each terminal with Ctrl+C. `start.ps1` is an alternative that starts the backend hidden and writes logs; for easier process management, the two-terminal approach is recommended.

The app was also launched for this session. If you need to stop its packaged backend before using the commands above, run `Stop-Process -Id (Get-Content .backend.pid)` from this project root while this session's backend is still running. The PID file is local and ignored by Git. The first startup may take about a minute; wait for the catalog to appear before signing in.

## Demo accounts

| Role | Email | Password |
|---|---|---|
| Customer | customer@fyndd.local | FynddDemo123! |
| Seller | seller@fyndd.local | FynddDemo123! |
| Admin | admin@fyndd.local | FynddDemo123! |

Twenty products are seeded by name when `SEED_DEMO=true`, including into the existing local demo database. Existing products, inventory, orders and reviews are preserved; repeated starts do not duplicate the catalog. Demo accounts are created only when the database has no users. Registration always creates a customer; an admin can grant seller access. These accounts are for local demonstration.

## Use the application

1. Browse Discover, choose a category, search, sort prices, and use pagination. Click a product for its description and reviews.
2. Sign in or create an account. Add a product with the plus button. Open the bag, adjust quantity, enter a delivery address and place the order.
3. Checkout is **cash on delivery**. Prices and stock are checked by the server; inventory reduction, order creation and bag clearing occur in one database transaction. No card details or online money transfers are collected.
4. Open your name in the header for your profile and orders. Delivery progresses through Placed → Processing → Shipped → Delivered. Edit your name, phone and saved address. Use Save on products to build a wishlist; Saved in the header opens it.
5. Sign in as the seller and select Studio. Create, edit and delete your own products, manage stock, see relevant orders and view sales charts. Referenced products cannot be deleted; set their stock to zero instead.
6. Sign in as admin to manage all products and orders, advance delivery statuses and change user roles. You cannot change your own admin role.
7. Open the shopping guide in the bottom corner. It retrieves relevant catalog products, prices and availability even without an AI key.

## Optional LLM

Set environment variables in the PowerShell terminal **before** starting Spring Boot:

```powershell
$env:LLM_API_KEY='your-provider-key'
$env:LLM_MODEL='your-provider-model'
# Optional OpenAI-compatible chat-completions endpoint:
$env:LLM_API_URL='https://api.openai.com/v1/chat/completions'
mvn -f backend/pom.xml spring-boot:run
```

The key remains in the backend. A maximum of four catalog items is injected into the prompt and output is capped at 300 tokens. Provider failure falls back to catalog suggestions. Retrieval uses normalized 256-dimensional feature-hashed word vectors and cosine similarity: this is a small educational vector-search implementation, not a trained semantic embedding model or a separate vector database. External LLM requests may incur provider charges. No external provider has been verified without credentials.

## MySQL

Create a `fyndd` database and a database user with access to it. Set these in the backend terminal:

```powershell
$env:SPRING_PROFILES_ACTIVE='mysql'
$env:DATABASE_URL='jdbc:mysql://localhost:3306/fyndd'
$env:DATABASE_USER='fyndd'
$env:DATABASE_PASSWORD='your-database-password'
$env:JWT_SECRET='your-random-secret-of-at-least-32-bytes'
$env:SEED_DEMO='true' # only for the first demonstration startup
mvn -f backend/pom.xml spring-boot:run
```

Without the MySQL profile, data lives in `data/fyndd.mv.db` relative to the backend process working directory. Spring does not automatically load `.env`; example files document the settings. Keep the working directory consistent. Tests use a separate in-memory database and do not erase your demo data.

## Docker

Requires Docker Desktop with Linux containers. From the project root:

```powershell
$env:DATABASE_PASSWORD='choose-a-database-password'
$env:MYSQL_ROOT_PASSWORD='choose-a-different-root-password'
$env:JWT_SECRET='choose-a-random-secret-of-at-least-32-bytes'
docker compose up --build
```

Open http://localhost:3000. Nginx serves React and proxies the API to Spring Boot; MySQL has a persistent named volume. `docker compose down` stops the stack while retaining data. Docker/MySQL deployment has not been run in this environment.

## Verification and API reference

```powershell
mvn -f backend/pom.xml test
npm --prefix frontend run build
mvn -f backend/pom.xml package
```

Swagger: http://localhost:8090/swagger-ui/index.html

OpenAPI JSON: http://localhost:8090/v3/api-docs

Health: http://localhost:8090/actuator/health

For protected APIs, send `Authorization: Bearer <token>` from the login response. Main endpoints: `/api/auth/register`, `/api/auth/login`, `/api/me`, `/api/products`, `/api/cart`, `/api/orders`, `/api/wishlist`, `/api/chat`, `/api/manage/products`, `/api/manage/orders`, `/api/manage/users`. Swagger lists exact request formats. Prices are integer paise in the API, INR in the UI.

## Architecture and course mapping

| Topic | Implementation |
|---|---|
| React foundations, forms, Axios | React components, controlled search, validated forms, API client |
| REST, service/repository layers | Api, ShopService, Spring Data repositories; standard status codes |
| DTOs, validation, exceptions | Request records, Jakarta validation, centralized Errors handler |
| Relational mappings | User/profile one-to-one; orders/lines one-to-many; products/seller many-to-one; user/favorites many-to-many |
| Authentication/authorization | BCrypt, signed 24-hour JWTs, stateless security filter, server-enforced roles and ownership |
| Catalog and CRUD | Search, categories, price sort, pagination, inventory management |
| RAG and LLM | Database catalog retrieval, local vectors, prompt context, optional provider integration |
| Performance | Catalog cache with mutation/checkout eviction, split frontend bundles, async post-commit order logs |
| Observability | Key-value Logback logs and Actuator health/metrics |
| Testing | MockMvc integration tests and isolated H2 database |
| Containers | Frontend/backend Dockerfiles, Nginx proxy, MySQL Compose |
| Documentation | README, example environments, generated Swagger/OpenAPI |

The capstone is local and deployment-ready as source; no cloud deployment or GitHub remote has been configured. Sites hosting cannot run this Java/MySQL backend. To publish, deploy the backend container with a MySQL service and deploy the React build with an API URL and matching backend `CORS_ORIGIN`. Use HTTPS and separate production credentials; disable demo seeding. The current schema uses `ddl-auto=update`; introduce versioned migrations for a production database.

Current limits: no payment gateway, coupon engine, email delivery, password-reset flow or shipment-provider integration. Order status advancement belongs to admins; sellers view their relevant orders. Charts show placed order value, not settled payment revenue. JWT sign-out removes the browser token but does not revoke it server-side. Browser visual verification and external deployments need a functioning browser runtime/provider access.

## Railway production deployment

Use three Railway services in the same project environment: a Railway MySQL database, a backend service from `/backend`, and a frontend service from `/frontend`. Both app services use their directory's Dockerfile. Set the backend and frontend root directories as described in [Railway's monorepo guide](https://docs.railway.com/deployments/monorepo). The database remains on Railway's managed persistent storage.

Set these variables on the backend service (replace `MySQL`, `backend`, and `frontend` with the exact Railway service names if they differ):

```text
SPRING_PROFILES_ACTIVE=mysql
DATABASE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}
DATABASE_USER=${{MySQL.MYSQLUSER}}
DATABASE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
JWT_SECRET=<generate a random secret of at least 32 bytes>
CORS_ORIGIN=https://${{frontend.RAILWAY_PUBLIC_DOMAIN}}
SEED_DEMO=false
BOOTSTRAP_ADMIN_EMAIL=24eg109a60@anurag.edu.in
BOOTSTRAP_ADMIN_PASSWORD=<generate a unique password of at least 16 characters>
```

The backend reads Railway's `PORT` environment variable and serves `/actuator/health` for its health check. In the frontend service, set `PORT=80` to match Nginx and set this build variable:

```text
VITE_API_URL=https://${{backend.RAILWAY_PUBLIC_DOMAIN}}/api
```

Railway provides HTTPS public domains and private MySQL connection variables; the frontend build argument is required so the deployed React bundle calls the backend service. [Railway variables](https://docs.railway.com/variables), [MySQL](https://docs.railway.com/databases/mysql), and [public domains](https://docs.railway.com/networking/domains/working-with-domains) document these settings. Nginx falls back to `index.html` for page refreshes. Keep all generated secrets in Railway variables and out of Git.

On the first backend start, the bootstrap variables create the specified admin account with a BCrypt-hashed password. The product catalog is seeded with 20 finds without creating demo logins. After confirming the admin can sign in, remove the two `BOOTSTRAP_ADMIN_*` variables; the account remains in MySQL. Register customers through the app. An admin can grant seller access from Studio. Never enable `SEED_DEMO` in production; it creates the local demo accounts and their shared demonstration password.

## GitHub

The local repository is initialized. When you have a GitHub repository URL:

```powershell
git add .
git commit -m "Create FYNDD ecommerce capstone"
git branch -M main
git remote add origin <your-repository-url>
git push -u origin main
```

Database files, build outputs, logs and `.env` files are ignored. Never put provider keys or database passwords into source files.


