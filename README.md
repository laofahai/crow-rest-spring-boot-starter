Here is the document for crow-rest project usage.

## Installation
This package is not publish to the Maven Repository yet, so you can [download the release] and import to your package manually like this:

```xml
<dependency>
    <groupId>org.teamswift</groupId>
    <artifactId>crow-rest-spring-boot-starter</artifactId>
    <version>${crow-rest.version}</version>
    <systemPath>/path/to/the/jar</systemPath>
</dependency>
```
## Configuration

### in your main application
First of all, you should configure the `@SpringBootApplication` annotation for your application can scan crow beans.
```java
@SpringBootApplication(
        scanBasePackages = {
                "org.teamswift.crow",
                "com.your.application"
        }
)
public class CrowRestDemoApplication {
    // ...
}
```

### handler
crow-rest is now only provide a JPA handler for database things. so please just configure JPA as usually in your `application.yml`
### security framework
crow-rest wasn't provide a security framework in it, so just deal with it as you want.

## Basic Usage
For basic usage, you need `Controller`, `Repository`, `Entity`, and `VO` `DTO` are optional, but I highly recommend that you use them.
BTW, I think it's still a little cumbersome to define even a few files like I'm saying, so I may build a code-generator future.

Okay, let's start from a module named `Book`.
First step, create an entity for `Book` like this:

```java
@Data
@Entity
public class Book implements ICrowEntity<Integer> {

    @Id private Integer id;
    
    private String name;
    
    //....
}
```
Second, I recommend you to define the `VO` and `DTO` for Book:

```java
@Data
public class BookVo implements ICrowVo {
    private Integer id;
    private String name;
}

// and dto:

@Data
public class BookVo implements ICrowDTO {
    private Integer id;
    private String name;
}
```

Then, create a JPA repository for it like this:
```java
@Repository
public interface BookRepository extends ICrowRepositoryJpa<Integer, Book> {

}
```
At the end, let's build the hardest thing here, the controller:
```java
@RestController
@RequestMapping("/my/book")
public class UserController extends CrowControllerJpa<
        Integer,
        User,
        BookVo,
        BookDto
        > {
}
```

Now you can do the CRUD operation to the Book:
* `POST /my/book` for create a new book.
* `PUT /my/book/{id}` for update book information.
* `GET /my/book` for books list.
* `DELETE /my/book/{id}` for soft-delete or destroy a book from database.

for more usage, [follow this link](./doc.md) to see the full documents.
