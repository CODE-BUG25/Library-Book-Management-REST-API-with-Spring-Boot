package com.college.library.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import com.college.library.model.Book;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/books")
public class BookController {

    private final JdbcTemplate db;

    private final RowMapper<Book> mapper = (rs, i) -> new Book(
            rs.getInt("book_id"), rs.getString("title"), rs.getString("author"),
            rs.getString("genre"), rs.getInt("available_copies"));

    public BookController(JdbcTemplate db) {
        this.db = db;
    }

    // Creates the table (and sample data) when the app starts
    @PostConstruct
    public void init() {
        db.execute("CREATE TABLE IF NOT EXISTS book ("
                + "book_id INT PRIMARY KEY, title VARCHAR(200), author VARCHAR(200), "
                + "genre VARCHAR(100), available_copies INT)");
        Integer count = db.queryForObject("SELECT COUNT(*) FROM book", Integer.class);
        if (count != null && count == 0) {
            db.update("INSERT INTO book VALUES (101,'Clean Code','Robert C. Martin','Programming',5)");
            db.update("INSERT INTO book VALUES (102,'Wings of Fire','A.P.J. Abdul Kalam','Autobiography',3)");
            db.update("INSERT INTO book VALUES (103,'The Pragmatic Programmer','Andrew Hunt','Programming',1)");
        }
    }

    private Book find(int id) {
        List<Book> r = db.query("SELECT * FROM book WHERE book_id = ?", mapper, id);
        return r.isEmpty() ? null : r.get(0);
    }

    private ResponseEntity<Map<String, Object>> message(HttpStatus status, String text) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("message", text);
        return ResponseEntity.status(status).body(body);
    }

    // (b) add a book
    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody Book b) {
        if (find(b.getBookId()) != null) {
            return message(HttpStatus.CONFLICT, "Book with bookId " + b.getBookId() + " already exists");
        }
        if (b.getAvailableCopies() < 0) {
            return message(HttpStatus.BAD_REQUEST, "availableCopies cannot be negative");
        }
        db.update("INSERT INTO book VALUES (?,?,?,?,?)",
                b.getBookId(), b.getTitle(), b.getAuthor(), b.getGenre(), b.getAvailableCopies());
        return ResponseEntity.status(HttpStatus.CREATED).body(b);
    }

    // (c) get all books
    @GetMapping
    public List<Book> getAllBooks() {
        return db.query("SELECT * FROM book ORDER BY book_id", mapper);
    }

    // (c) get one book
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable("id") int id) {
        Book b = find(id);
        if (b == null) {
            return message(HttpStatus.NOT_FOUND, "Book with bookId " + id + " not found");
        }
        return ResponseEntity.ok(b);
    }

    // (d) issue a book
    @PutMapping("/{id}/issue")
    public ResponseEntity<?> issueBook(@PathVariable("id") int id) {
        Book b = find(id);
        if (b == null) {
            return message(HttpStatus.NOT_FOUND, "Book with bookId " + id + " not found");
        }
        if (b.getAvailableCopies() <= 0) {
            return message(HttpStatus.BAD_REQUEST, "No copies of '" + b.getTitle() + "' are available to issue");
        }
        db.update("UPDATE book SET available_copies = available_copies - 1 WHERE book_id = ?", id);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Book issued successfully");
        body.put("book", find(id));
        return ResponseEntity.ok(body);
    }

    // extra: return a book
    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable("id") int id) {
        if (find(id) == null) {
            return message(HttpStatus.NOT_FOUND, "Book with bookId " + id + " not found");
        }
        db.update("UPDATE book SET available_copies = available_copies + 1 WHERE book_id = ?", id);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Book returned successfully");
        body.put("book", find(id));
        return ResponseEntity.ok(body);
    }

    // extra: delete a book
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable("id") int id) {
        if (find(id) == null) {
            return message(HttpStatus.NOT_FOUND, "Book with bookId " + id + " not found");
        }
        db.update("DELETE FROM book WHERE book_id = ?", id);
        return message(HttpStatus.OK, "Book " + id + " deleted");
    }
}