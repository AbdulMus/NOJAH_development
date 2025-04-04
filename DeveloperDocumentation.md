# NOJAH's Makeup & Beauty Inventory Developer Documentation

## Table of Contents

- [Overview](#overview)
- [Tools and Techniques Employed](#tools-and-techniques-employed)
    - [Backend](#backend)
    - [Frontend](#frontend)
    - [Build Tools](#build-tools)
    - [Dependencies](#dependencies)
- [Core Components](#core-components)
    - [Main Application Class](#main-application-class)
    - [Controllers](#controllers)
    - [Models](#models)
        - [Product Model](#product-model)
        - [User Model](#user-model)
- [Data Storage](#data-storage)
    - [User Data](#user-data)
    - [Product Data](#product-data)
    - [Product Images](#product-images)
- [Controllers and Routing](#controllers-and-routing)
- [Frontend Implementation](#frontend-implementation)
    - [Thymeleaf Templates](#thymeleaf-templates)
    - [CSS Styling](#css-styling)
    - [JavaScript](#javascript)
- [Authentication and Authorization](#authentication-and-authorization)
    - [Authentication System](#authentication-system)
    - [Authentication Flow](#authentication-flow)
- [Development Environment Setup](#development-environment-setup)
    - [Prerequisites](#prerequisites)
    - [Setup Steps](#setup-steps)
- [Build Scripts](#build-scripts)
    - [For MacOS or Linux](#for-macos-or-linux)
    - [For Windows](#for-windows)
- [Testing](#testing)
    - [Writing New Tests](#writing-new-tests)
- [Project Structure](#project-structure)

## Overview

The NOJAH Makeup & Beauty Inventory Catalog is built using the Spring Boot framework following the Model-View-Controller (MVC) pattern:

- **Model**: Java classes representing data entities (Product, User)
- **View**: Thymeleaf templates for rendering HTML pages
- **Controller**: Spring controllers handling HTTP requests and responses

The application uses a file-based storage system rather than a traditional database, with data stored in CSV and TXT files.

## Tools and Techniques Employed

### Backend

- **Java 23**: Core programming language
- **Spring Boot 3.4.3**: Application framework
- **Spring MVC**: Web framework
- **Thymeleaf**: Server-side Java template engine


### Frontend

- **HTML5**: Structure
- **CSS3**: Styling
- **JavaScript**: Client-side functionality
- **Font Awesome 6.4.0**: Icon library used for Product Labels


### Build Tools

- **Maven**: Dependency management and build automation
- **Spring Boot Maven Plugin**: Spring Boot integration with Maven


### Dependencies

- **spring-boot-starter-thymeleaf**: Thymeleaf integration with Spring Boot
- **spring-boot-starter-web**: Web application development
- **spring-boot-starter-test**: Testing framework
- **spring-boot-starter-actuator**: Application monitoring and management

## Core Components

### Main Application Class

The `MakeupBeautyApplication.java` class serves as the entry point for the Spring Boot application. It contains the `main` method and is annotated with `@SpringBootApplication`.

```java
@SpringBootApplication
public class MakeupBeautyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MakeupBeautyApplication.class, args);
    }
}
```

### Controllers

The `HomeController.java` class handles HTTP requests and manages the application's routing. It contains methods for:

- Displaying the homepage
- Displaying product pages
- Handling product searches
- Handling product filtering
- Managing user authentication
- Processing favourites
- Handling Database


### Models

#### Product Model

The `Product.java` class represents a beauty product with properties such as:

- ID
- Name
- Brand
- Description
- Category
- Image path
- Labels


```java
public class Product {
    private int id;
    private String name;
    private String brand;
    private String description;
    private String category;
    private String image;
    private List<String> labels;
    
    // Constructors, getters, setters, other methods
}
```

#### User Model

The `User.java` class represents a user account with properties:

- Username
- Password
- User's Favoruite Products


```java
public class User {
    private String username;
    private String password;
    private List<Integer> favourites;
    
    // Constructors, getters, setters, other methods
}
```

## Data Storage

The application uses file-based storage instead of traditional databases:

### User Data

User information is stored in `users.csv` with the following format:

```plaintext
username,password,favorites
BARRY,$Uper2050,5;4
```

- Each line represents a user
- Columns are seperated by commas
- Favourite product IDs are seperated by semicolons


### Product Data

Product information is stored in `catalog.txt` with the following format:

```plaintext
id, name, brand, description, category, image, labels
1|,|Matte Revolution Hydrating Lipstick|,|Charlotte Tilbury|,|A matte lipstick with hydrating formula.|,|Lip|,|/uploads/1.jpg|,|
```

- Each line represents a product
- Fields are separated by `|,|` in order to allow commas within product descriptions
- Labels are separated by semicolons


### Product Images

Product images are stored in the `uploads/` directory with filenames corresponding to their references in the `catalog.txt` file.

## Controllers and Routing

The `HomeController` class handles all HTTP requests. Key endpoints include:

| Endpoint | HTTP Method | Description
|-----|-----|-----
| `/` | GET | Display homepage with product catalog
| `/search` | GET | Search products by query
| `/product/{id}` | GET | Display product details
| `/login` | GET/POST | Display login page / Process login
| `/create-account` | GET/POST | Display account creation page / Process account creation
| `/logout` | GET | Log out current user
| `/my-favorites` | GET | Display user's favourite products
| `/favorite` | POST | Add product to user's favourites
| `/unfavorite` | POST | Remove product from user's favorites
| `/admin` | GET | Display admin dashboard
| `/add-product` | POST | Add new product
| `/update-product/{id}` | GET/POST | Display/process product update page
| `/delete-product` | POST | Delete product


## Frontend Implementation

### Thymeleaf Templates

The application uses Thymeleaf for server-side rendering of HTML templates. Key Thymeleaf features used include:

- `th:each` for iteration
- `th:if` and `th:unless` for conditionals
- `th:text` for text references
- `th:href` and `th:src` for dynamic URLs
- `th:action` for form actions


Example from `index.html`:

```html
<div class="product-card-index" th:each="product : ${filteredResults}">
    <div class="product-info-index">
        <a th:href="@{/product/{id}(id=${product.id})}">
            <img th:src="@{${product.image}}" alt="Product Image">
            <h3 th:text="${product.name}"></h3>
        </a>
        <p><strong>Made By:</strong> <span th:text="${product.brand}"></span></p>
        <p><strong>Category:</strong> <span th:text="${product.category}"></span></p>
    </div>
</div>
```

### CSS Styling

The application uses multiple CSS files for styling different sections:

- `styles.css`: Global Styles
- `product.css`: Product Template Page
- `admin.css`: Admin Dashboard
- `favourites.css`: Favourites Page
- `loginstyle.css`: Login and Account Creation Pages
- `searchstyle.css`: Search Result Page
- `update-product.css`: Product Update Page


### JavaScript

The `script.js` file contains client-side functionality for:

- Image preview when uploading product images
- Label management (adding/removing labels)
- Font Awesome Icons (linked based on label content)


## Authentication and Authorization

### Authentication System:

1. User credentials are stored in `users.csv`
2. Passwords are stored in plaintext
3. Session-based authentication is used to maintain login state
4. Admin access is determined by username (hardcoded "ADMIN" user)


### Authentication flow:

1. User submits login details in the login page
2. Controller validates credentials against `users.csv`
3. If valid, user information is stored in session
4. If not, error message displays to user


## Development Environment Setup

### Prerequisites

- JDK 23
- Maven 3.6+
- Git (optional)
- IDE (IntelliJ IDEA is recommended)


### Setup Steps

1. Clone the repository:


```bash
git clone https://github.com/AbdulMus/NOJAH_development.git
cd NOJAH_development/NOJAH
```

2. Import the project into your IDE as a Maven project
3. Configure JDK 23 as the project SDK
4. Install Maven dependencies:


```bash
mvn clean install
```

5. Run the application:


```bash
mvn spring-boot:run
```

## Build Scripts

For convenience, you can use the included build scripts:

### For MacOS or Linux
```shellscript
chmod +x ./build-and-run.sh
./build-and-run.sh
```

### For Windows
Run the `build.bat` script


## Testing

The application includes test classes:

- `HomeController.Tests.java`: Tests for the HomeController
- `ProductTest.java`: Tests for the Product model
- `UserTest.java`: Tests for the User model


To run tests:

```shellscript
mvn test
```

### Writing New Tests

When adding new features, create corresponding test classes in the `src/test/java` directory. Use the Spring Boot testing framework with JUnit.

Example test structure:

```java
public class NewFeatureTest {
    
    @Test
    public void testNewFeature() {
        // Test implementation
        assertEquals(expectedValue, actualValue);
    }
}
```

## Project Structure

```plaintext
NOJAH/
├── .idea/
├── src/                                                    
│   ├── main/                                               
│   │   ├── java/                                           
│   │   │   └── com.makeupbeauty/                           
│   │   │       ├── controller/                             
│   │   │       │   └── HomeController.java                             
│   │   │       ├── model/                                  
│   │   │       │   ├── Product.java                          
│   │   │       │   └── User.java                       
│   │   │       └── MakeupBeautyApplication.java           
│   │   └── resources/                               
│   │       ├── static/    
│   │       │   ├── css/                       
│   │       │   │   ├── admin.css                       
│   │       │   │   ├── favourites.css                  
│   │       │   │   ├── loginstyle.css                  
│   │       │   │   ├── product.css                     
│   │       │   │   ├── searchstyle.css                 
│   │       │   │   ├── styles.css                      
│   │       │   │   └── update-product.css       
│   │       │   └── js/                       
│   │       │       └── script.js          
│   │       ├── templates/                           
│   │       │   ├── admin.html                       
│   │       │   ├── create-account.html                       
│   │       │   ├── favorites.html                 
│   │       │   ├── index.html                  
│   │       │   ├── login.html                    
│   │       │   ├── product.html                
│   │       │   ├── search-results.html              
│   │       │   └── update-product.html              
│   │       ├── application.properties               
│   │       ├── catalog.txt/                           
│   │       └── users.csv/                          
│   └── test/                                              
│       └── java/     
│           └── com.makeupbeauty
│               ├── HomeControllerTests.java                       
│               ├── ProductTests.java                       
│               └── UserTests.java   
├── target/ 
├── uploads/                           
├── .DS_Store
├── build.bat
├── build-and-run.sh
├── HOW-TO-RUN.md
├── mvnw
├── mvnw.cmd                                                
└── pom.xml                                             
```
