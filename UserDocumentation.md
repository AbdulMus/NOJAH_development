# NOJAH's Makeup & Beauty Inventory User Documentation

A JAVA based web application for browsing, searching, and managing makeup and beauty products.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
  - [User Roles](#user-roles)
  - [Browsing Products](#browsing-products)
  - [Searching and Filtering](#searching-and-filtering)
  - [User Accounts](#user-accounts)
  - [Managing Favorites](#managing-favorites)
  - [Admin Dashboard](#admin-dashboard)
- [Technical Details](#technical-details)
- [Troubleshooting](#troubleshooting)
- [FAQs](#faqs)
- [Contributing](#contributing)
- [License](#license)

## Overview

The NOJAH Makeup & Beauty Inventory Catalog is a user-friendly Spring Boot web application crafted to assist users in exploring, searching, and organizing a wide range of makeup and beauty products. It offers an extensive catalog filled with in-depth product details, handy filtering options, and personalized user account features.


## Features

- **Product Catalog**: Browse a comprehensive collection of beauty products
- **Search & Filter**: Find products by keywords, name, brand, category, or distinct labels
- **User Accounts**: Register and login to access stored data
- **Favorites System**: Save and manage your favorite products
- **Admin Dashboard**: Add, update, and delete products (admin only)

## Installation

### Prerequisites

- Java Development Kit (JDK) 23
- Apache Maven
- Git (optional, for cloning)

### Setup Instructions

1. **Clone the repository** (or download the source code)

```bash
git clone https://github.com/AbdulMus/NOJAH_development.git
cd NOJAH_development
```

2. **Build the project**


```bash
mvn clean install
```

3. **Run the application**


Option 1: Using Maven

```shellscript
mvn spring-boot:run
```

Option 2: Using the JAR file

```shellscript
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

4. **Access the application**


Open your web browser and go to the link: [http://localhost:8080](http://localhost:8080)

### Build and Run Script

Next time, for convenience, you can use the included build-and-run script:

#### For MacOS/Linux
```shellscript
chmod +x ./build-and-run.sh
./build-and-run.sh
```

#### For Windows
```shellscript
chmod +x ./build.bat
./build.bat
```

## How To Use

### User Roles

The application supports three types of users:

1. **Guest Users**: Can browse products, filter and search the catalog without logging in, but cannot save or access stored data
2. **Registered Users**: Can browse products and search the catalog, while also being able to favourite specific products, which can be accessed in a later session
3. **Admin Users**: Have all user privileges, but with the bonus access to the admin dashboard for managing products (adding, updating or deleting)

### Browsing Products

#### Homepage

The homepage displays all products in a grid layout with basic information:

- Product's Image
- Product's Name
- Product's Brand
- Product's Category


Click on any product to view it in its personal product page.

#### Product Page

The product page provides key information about a selected product:

- Product's Image
- Product's Name and Brand
- Product's Detailed description
- Product's Category
- Products's Labels (Vegan, Cruelty-Free, Eco-Friendly or Natural)
- IF LOGGED IN: Option to add/remove from favorites

### Searching and Filtering

#### Search Function

1. Enter keywords in the search box
2. Click the **Search** button or press Enter/Return on the keyboard
3. View matching products in the results page

#### Filtering Products

The sidebar on the homepage and search results page offers multiple filtering options based on a products aspects

1. Category: Filter products by categories
2. Brand: Filter products by brand
3. Label: Filter product by specific labels
Click **Apply Filters** to see filtered results\
Click **Reset Filters** to clear all filters

### User Accounts

#### Creating an Account

1. Click **Login** in the top right of the header
2. Click **Create one** on the login page
3. Fill in the registration form:
* Username (Must be unqiue)
* Password (Must have 8+ characters, an uppercase letter, a lowercase letter, a number and a special character)
* Confirm Password (Must match Password)
4. Click **Create Account**



#### Logging In

1. Click **Login** in the top right of the header
2. Enter your username and password
3. Click **Login**


#### Logging Out

1. Click **Logout** in the top right of the header when logged in

### Managing Favourites

#### Adding Products to Favourites

When logged in:

1. Navigate to a product's personal page
2. Click **Add to Favorites**


#### Viewing Favorites

1. Click **My Favourites** in the top right of the header
2. View all your favorited products in a grid layout


#### Removing Products from Favorites

1. On the favorites page, click **Remove** under any product
* Alternatively, on a product details page, click **Remove from Favorites**

### Admin Dashboard

> **Note**: This section is only accessible to administrator accounts.



#### Accessing the Admin Dashboard

1. Log in with the given admin username and password
2. Click **Admin Dashboard** in the top right of the header

#### Adding New Products

1. In the Admin Dashboard, find the "Add Product" form on the top left of the page
2. Fill in the required fields:
* Name
* Brand
* Category
* Description
* Image (upload an image)
* Labels (optional)
3. Click **Add Product**

#### Managing Existing Products

The Admin Dashboard displays a list of all products with options to:

1. Search Products
* Use the search box in the product list to find specific products using keywoards
* Enter your search and click **Search**

2. Update Products:
* Click **Update** next to a product
* Change product information in the form
* Upload a new image (optional)
* Add or remove labels
* Click **Save Changes**

3. Delete Products:
* Click **Delete** next to the product that will be removed
* The product will be permanently removed from the catalog/system

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

### Tools and Techniques Employed

- **Backend**: Java with Spring Boot 3.4.3
- **Frontend**: HTML, CSS, JavaScript and Thymeleaf Templates
- **Data Storage**: File-based storage (CSV and TXT files)
- **Build Tool**: Maven
