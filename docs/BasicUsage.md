# Basic Usage
For basic usage, you need create at least `Controller`, `Repository`, `Entity`, and create `VO` `DTO` optional, but I'm highly recommend that you use them.
BTW, I think it's still a little cumbersome to define even a few files like I'm saying, so I may build a code-generator future.

Okay, let's start from a module named `Book`.

## Create the VO & DTO
first step, I recommend you to define the `VO` and `DTO` for Book:
```java
@Data
public class BookVo implements ICrowVo {
    private Integer id;
    private String name;
}

// and dto:

@Data
public class BookDTO implements ICrowDTO {
    
    private String name;
    
}
```

## Create the Entity
second, create an entity for `Book` like the codes below here, I recommend you to configure your JPA `spring.jpa.hibernate.ddl-auto=update` to enable table struct auto update, or you can maintain your table struct manually.

```java
@Data
@Entity
public class Book extends BaseCrowEntity implements ICrowEntity<Integer, BookVo> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    //....other field you need
}
```

## Create the JPA Repository
Then, create a JPA repository for it like this:
```java
@Repository
public interface BookRepository extends ICrowRepositoryJpa<Integer, Book> {
    // need no codes here by default
}
```

## Create the Controller
At the end, let's build the hardest thing here, the controller:
```java
@RestController
@RequestMapping("/my/book")
public class BookController extends CrowControllerJpa<
        Integer, // this is the entity ID type
        Book,
        BookVo,
        BookDto
        > {
    // need no codes here by default
}
```

## That's all
Now you can do the CRUD operation to the Book via:
* `POST /my/book` for create a new book
* `PUT /my/book/{id}` for update book information
* `GET /my/book` for books list
* `PUT /my/book/restore/{id}` for restore the soft-deleted book
* `PUT /my/book/restoreBatch/{ids}` for restore the soft-deleted books via `ids` like `1,2,3`
* `DELETE /my/book/{id}` for soft-delete a book
* `DELETE /my/book/batch/{ids}` for batch soft-delete books via `ids` like `1,2,3`
* `DELETE /my/book/destroy/{id}` for destroy a book from database
* `DELETE /my/book/destroyBatch/{id}` for destroy books from database
