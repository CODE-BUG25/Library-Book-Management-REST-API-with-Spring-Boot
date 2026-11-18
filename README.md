# Library Book Management REST API (Spring Boot)

Spring Boot 3 / Java 17+ / Maven. Base URL: `http://localhost:8080/books`
Books are stored in an in-memory `LinkedHashMap` (data resets on restart).

## Import into Eclipse
1. Install Java 17+ and a Spring-capable Eclipse (Eclipse IDE for Enterprise Java, or add Spring Tools 4 from the Marketplace). Maven (m2e) is built into Eclipse.
2. `File > Import > Maven > Existing Maven Projects` -> select the `library-book-api` folder -> Finish.
3. Right-click the project -> `Maven > Update Project` (Alt+F5) and wait for dependencies to download.
4. Right-click `LibraryApplication.java` -> `Run As > Java Application` (or `Spring Boot App` with Spring Tools).
5. Console shows `Started LibraryApplication` -> API is live on port 8080.

Command line alternative: `mvn spring-boot:run`

## Test with Postman
`File > Import` -> `postman/Library-Book-API.postman_collection.json`.
Run requests one by one, or use the Collection Runner (every request has a status-code test).
Collection variable `baseUrl` = `http://localhost:8080`.

## Endpoints
| Part | Method | URL | Behaviour |
|------|--------|-----|-----------|
| (a) | - | - | `Book` model + `@RestController @RequestMapping("/books")`, in-memory Map |
| (b) | POST | `/books` | Add book (`@RequestBody`). 409 if `bookId` exists, 400 if `availableCopies < 0`, else 201 |
| (c) | GET | `/books` | All books |
| (c) | GET | `/books/{id}` | One book (`@PathVariable`), 404 with message if absent |
| (d) | PUT | `/books/{id}/issue` | Issues one copy (`availableCopies - 1`). 404 if absent, 400 if 0 copies left |
| extra | PUT | `/books/{id}/return` | Returns one copy (+1) |
| extra | DELETE | `/books/{id}` | Removes a book |

Sample POST body:
```json
{ "bookId": 201, "title": "Effective Java", "author": "Joshua Bloch", "genre": "Programming", "availableCopies": 4 }
```

## Note on part (d)
The photographed question was cut off at "issues a book by ...". The issue endpoint assumes the standard meaning (decrease `availableCopies` by 1, reject when none are available or the book is missing). Adjust `issueBook()` in `BookController` if the full question says otherwise.
