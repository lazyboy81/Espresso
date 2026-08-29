# Espresso
### A lightweight HTTP library for Java inspired by Echo
![Logo.jpg](assets/Logo.jpg)

For as long as I have been programming in the Java community, the defacto way to create Web APIs and REST services in Java
has been the [Spring framework and ecosystem](https://spring.io/projects).

Although this project is pretty much the standard way of creating and maintaining complex applications, for simple web servers, it is a bit too much.
looking for an alternative solutions in Java's standard library or third party libraries didn't satisfy what I was looking for, so figuring that it would
a fun little challenge, I created this project.

Introducing Espresso, a lightweight HTTP library for Java inspired by the [Echo Web Framework](https://echo.labstack.com/).
In this project you can create a simple web server in a few lines of code:
```java
void main(String[] args) {
  Espresso espresso = new Espresso(JettyOptions.defaultOps());

  espresso.get("hello", (request, response) -> {
    response.text(HttpStatus.OK, "Hello World");
  });

  espresso.start();
}
```
And with this we have an HTTP server!!!

## Features

### Multiple Request and Response Formats
Espresso supports multiple request formats:
- JSON
- XML
- Text
- Form Values
- Files

And multiple response formats:
- JSON
- XML
- Text
- HTML

### Easy to Use Middlewares
Alongside these request and response formats, espresso offers some middlewares as well:
- Request Response Logger: Log request and response as JSON logs
- Request ID Generator: generate or use an existing `X-Request-ID` header in the response

Registering a middleware is easy as well:
```java
void main(String[] args) {
  Espresso espresso = new Espresso(JettyOptions.defaultOps());
  
  espresso.use(Middlewares.requestResponseLogger());

  espresso.get("hello", (request, response) -> {
    response.text(HttpStatus.OK, "Hello World");
  });

  espresso.start();
}
```
### Grouping URLs
You can group URLS and create subbranches of routeRegistry, for  example:
```java
void main(String[] args) {
  Espresso espresso = new Espresso(JettyOptions.defaultOps());

  Router userRoutes = espresso.group("/user");
  userRoutes.use(Middlewares.requestResponseLogger());
  userRoutes.get("/list", (request, response) -> {
  }); // /users/list

  Router orderRoutes = espresso.group("/order");
  orderRoutes.use(Middlewares.requestId());
  orderRoutes.get("/list", (request, response) -> {
  }); // /orders/list

  espresso.start();
}
```
### Many more...
Espresso also supports Query Parameters, Path Variables and Form Values

## Guide
### Supported versions

- As of now the latest version is 1.0.0

### Prerequisites

Java 21, That's it.

### Installation
Add the project dependency to your pom.xml file:
```xml
<dependency>
  <groupId>io.github.lazyboy81</groupId>
  <artifactId>espresso-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```


## Contribute

**Use issues for everything**

- For a small change, just send a PR.
- For bigger changes open an issue for discussion before sending a PR.
- PR should have:
    - Test case
    - Documentation
    - Example (If it makes sense)
- You can also contribute by:
    - Reporting issues
    - Suggesting new features or enhancements
    - Improve/fix documentation

>Disclaimer: The source was written mostly by hand but LLM assistance was used. It is okay to use LLMs and AI agents
> for contribution to this project. Please try to avoid spamming the issues section and if you have used an LLM, provide the model name and
> a good description of the problem and your solution for it.

## License

[MIT](LICENSE)
