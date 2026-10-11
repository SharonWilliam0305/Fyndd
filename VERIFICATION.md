# Verification

Backend: `mvn -f backend/pom.xml package` passed with four tests, zero failures and zero errors.

The integration scenarios verify input validation, registration role restrictions, hidden password fields, successful login, wrong-password and invalid-token rejection, anonymous cart protection, customer/seller admin-access rejection, stock limits, checkout totals, atomic cart clearing, inventory reduction, empty-cart rejection, order status transitions, duplicate review rejection, profile updates, wishlist add/remove, seller product CRUD and catalog assistant fallback.

The Mockito unit test verifies relevant vector retrieval and normalized similarity.

Frontend: `npm --prefix frontend run build` passed. Dependencies were audited by npm with zero reported vulnerabilities during installation. This is build verification, not a browser interaction test.

Not verified here: browser layout/interactions (browser tool could not initialize because of a Windows sandbox error), Docker, MySQL runtime, external LLM credentials, cloud hosting, and GitHub publishing.

The live local API uses port 8090. The dev frontend uses port 5173 and proxies `/api` to it. Other applications were already using ports 8080 and 8081 and were left running.

Live smoke checks verified customer authentication, cart and wishlist reads, frontend HTTP 200, the API proxy, and OpenAPI HTTP 200. An empty-catalog cache race during initial seeding was found and fixed by clearing the catalog cache after seed insertion.
