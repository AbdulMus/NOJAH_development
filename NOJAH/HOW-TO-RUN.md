
# How To Run NOJAH's Makeup And Beauty Inventory Catalog

## Prerequisites

Before you begin, ensure you have the following installed on your system:

### Java Development Kit (JDK)
- Version 23 (as specified in `pom.xml`).
- Download and install the JDK from Oracle or OpenJDK.
- Verify the installation:

```bash
java -version
```

### Apache Maven
- Used to build and manage dependencies.
- Download and install Maven from [Maven's official website](https://maven.apache.org/).
- Verify the installation:

```bash
mvn -v
```

### Git
- To clone the repository.
- Download and install Git from [Git's official website](https://git-scm.com/).
- Verify the installation:

```bash
git --version
```

## How To Build & Run 

### 1. Clone the Repository
If you haven't already, clone the repository to your local machine:

```bash
git clone https://github.com/AbdulMus/NOJAH_development.git
cd NOJAH_development
```

### 2. Build the Project
To compile the project and package it into a .jar file, run the following command:

```bash
mvn clean install
```

This will:
- Clean the project (remove any previously compiled files).
- Compile the source code.
- Run tests.
- Package the application into a .jar file located in the `target` directory.

### 3. Run the Application
Once the project is built, you can run the application using one of the following methods:

#### Option 1: Run with Maven
Use Maven to start the Spring Boot application:

```bash
mvn spring-boot:run
```

#### Option 2: Run the JAR File
Alternatively, you can run the packaged `.jar` file directly:

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### 4. Access the Application
Once the application is running, open a web browser and navigate to [localhost:8080](http://localhost:8080)

You should see the homepage of the Makeup Beauty Application.

### 5. Stopping the Application
To stop the application, press `Ctrl + C` in the terminal where the application is running.

### Build-And-Run Script
To run the build-and-run.sh file, first make sure the right permissions are set:

```bash
chmod +x ./build-and-run.sh
```

Then run the script. Make sure to refresh the page when loaded in.
```bash
./build-and-run.sh
```

## Testing the Application
To run the unit tests included in the project, use the following command:

```bash
mvn test
```

This will execute all the tests and display the results in the terminal.

## Project Structure
Here’s an overview of the project structure:

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
│   │       ├── static.css/                           
│   │       │   ├── admin.css                       
│   │       │   ├── favourites.css                  
│   │       │   ├── loginstyle.css                  
│   │       │   ├── product.css                     
│   │       │   ├── searchstyle.css                 
│   │       │   ├── styles.css                      
│   │       │   └── update-product.css              
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
│           ├── ProductTest.java                          
│           └── UserTest.java
├── target/ 
├── uploads/                           
├── .DS_Store
├── build-and-run.sh
├── HOW-TO-RUN.md
├── mvnw
├── mvnw.cmd                                                
└── pom.xml                                             
```
