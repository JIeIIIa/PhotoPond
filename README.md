# PhotoPond

[![Build Status](https://travis-ci.org/JIeIIIa/PhotoPond.svg?branch=master)](https://travis-ci.org/JIeIIIa/PhotoPond)
[![Coverage Status](https://coveralls.io/repos/github/JIeIIIa/PhotoPond/badge.svg?branch=master)](https://coveralls.io/github/JIeIIIa/PhotoPond?branch=master)

PhotoPond is a java study project. It available by https://photopond.herokuapp.com

The purpose of the PhotoPond project was to develop a user-friendly web-application for storing images. 
After signing up the user can create directories and upload files. 
Each directory and file can be moved, renamed or deleted. 
A user can link his/her social (Facebook or Twitter) account to PhotoPond profile. 
If the social account is linked, user can authorize with it. 
When Twitter account has been linked a user can create a tweet and easily share images. 
Administrator can view basic information about all users, change user's login or role, delete users.

## Getting Started
These instructions will give you a copy of the project and running on your local machine for development

### Prerequisites
For building and running the application you need:
* [JDK 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
* MySQL 5.7 or PostgreSQL 10.6
* [Git Guide](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
* Maven 3.5.3 or later ([Installing Apache Maven](https://maven.apache.org/install.html))
 
### Installing

#### Clone repository
Clone this repository onto your local machine. You can do it like this:
```shell
$ git clone https://github.com/JIeIIIa/PhotoPond
```

#### Create database
Open database terminal and type in the following command:
```shell
CREATE DATABASE PhotoPond;
```

#### Create Facebook application
* Create an application following [App development](https://developers.facebook.com/docs/apps/)
* Open created Facebook app 
* Set up **Facebook login** (from **_Add a Product_** section)
* Open Settings->Basic and add **App Domains** (e.g. `localhost`)  
* Make a note of your **App ID** and **App Secret**

#### Create Twitter application
* Create an application following [How to create a Twitter application](https://docs.inboundnow.com/guide/create-twitter-application/)
* On the _Step 7_ choose `Read and Write` **Access permission**
* Open **App details** and add _Callback urls_ (where **<your_server_address>** is the address to listen by Photopond application, 
e.g. `http://127.0.0.1:8099`):
  ```
  <your_server_address>/public/twitter-auth/callback/login
  <your_server_address>/public/twitter-auth/callback/associate
  <your_server_address>/public/twitter-auth
  ``` 

### Running

#### Active profiles 
You should choose exact one of `prod` or `dev` profiles and 
one of `disk-database-storage` or `database-storage`. Also you are able to use another profiles if necessary.
See below full list of available profiles:  

* **_prod_** - if application run in production environment;
* **_dev_** - if application run in development environment (e.g. on your local pc);
* **_database-storage_** - if you want to store all information in database;  
* **_disk-database-storage_** - if you want to store pictures on the disk. 
In this case in database store symbolic link on the picture;
* **_postgresql_** - if you want to use **PostreSQL** database, otherwise **MySQL** will be used;
* **_ssl_** - if you want to use _https_ protocol. 
In this case you should replace **localhostKeystore.p12** file to your own certificate  

#### Environment Variables
Use environment variables below:
* **_JDBC_DATABASE_URL_** - the JDBC URL to the database instance 
(e.g. `jdbc:postgresql://localhost:5432/PhotoPond`)
* **_JDBC_DATABASE_USERNAME_** - the database username
* **_JDBC_DATABASE_PASSWORD_** - the database password
* **_FACEBOOK_APPLICATION_ID_** - _App ID_ of your Facebook App
* **_FACEBOOK_APPLICATION_SECRET_** - _App Secret_ of your Facebook App
* **_TWITTER_CONSUMER_KEY_** - _Consumer Key_ of your Twitter App 
* **_TWITTER_CONSUMER_SECRET_** - _Consumer Secret_ of your Twitter App
* **_KEY_STORE_PASSWORD_** - the password for the keystore (*.p12)

Also you can change corresponding variables in *.properties files

#### Running the application using the command-line
This project can be built with [Apache Maven](http://maven.apache.org/).

Use the following steps to run the application locally:

1. Execute next Maven goals to create the `target/photo-pond-1.0-SNAPSHOT.jar` file:
   ```bash
   $ mvn clean install -Dmaven.test.skip=true
   ```
2. Run the application using `java -jar`, as shown in the following example:
   ```bash
   java -Dspring.profiles.active=<ACTIVE_PROFILES_LIST> <OTHER_VARIABLES> -jar target/photo-pond-1.0-SNAPSHOT.jar
   ```
   where:
   * **_<ACTIVE_PROFILES_LIST>_** - list of spring boot active profiles 
   (e.g `dev,postgresql,database-storage`) 
   * **_<OTHER_VARIABLES>_** - list of environment variables that are required to run 
   and have not been set before 
   (e.g. `-DJDBC_DATABASE_USERNAME=username`) 
3. Once running with `dev` active profile, the application will be available at:
   ```
   http://localhost:8099/
   ```
   If you need to start your application on another port use `-Dserver.port=PORT` variable.

## Built With
* [Spring boot](https://docs.spring.io/spring-boot/docs/2.1.1.RELEASE/reference/htmlsingle/) - A framework that provides a simpler and faster way to set up, 
configure, and run both simple and web-based applications
* [Thymeleaf](https://www.thymeleaf.org/) - A server-side Java template engine
* [Vue.js](https://vuejs.org/v2/guide/) - An open-source JavaScript framework for building user interfaces and single-page applications
* [Maven](https://maven.apache.org/) - Dependency Management

## Troubleshooting
* Make sure that you are using java 8, and that maven version is appropriate.
  ```shell
  mvn -v
  ```
  should return something like:
  ```
  Apache Maven 3.5.3
  Maven home: C:\Program Files\Maven\bin\..
  Java version: 1.8.0_192, vendor: Oracle Corporation
  Java home: C:\Program Files\Java\jdk1.8.0_192\jre
  ```
* Make sure that you have set all necessary variables.

## Author
* **Oleksii Onishchenko** - *Initial work* - [JIeIIIa](https://github.com/JIeIIIa)