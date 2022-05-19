`Crow-Rest` is a quick-starter based on SpringBoot to handle common CRUD operations via RESTful API, it's easily integrated into your project especially new one, it's built a new realization via abstract the common operations in web development.

# Features
* Build a full-functionally module with almost no codes need to write
* It's easily to extend and override the default handlers
* Provide a service for all data struct from defined entities, so you can build the front-end application based on it in the same programming style of `Crow-Rest`, like [Crow-ExtJS](https://github.com/laofahai/crow-extjs) (not finish yet)
* more...

# Getting started
## Installation
This package is not publish to the Maven Repository yet, so you can [download the release] and import to your package manually like this:

### from maven repository
Not finish yet, I'll publish a release when it's works stable.
### from jar
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

### handler for database
crow-rest is now only provide a JPA handler for database things. so please just configure JPA as usually in your `application.yml`

### security framework
crow-rest wasn't provide a security framework in it by now, so just deal with it as you want.

## Basic usage
For usage, [follow this link](./docs/BasicUsage.md) to see the basic usage.
