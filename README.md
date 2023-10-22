## Spring Initializer
- Dependency: Spring Web, Spring Dta JPA, MySQL Driver, Lombok, JDBC API
- Project: Maven
- Spring Boot: 3.1.3
- JAVA 17

## Create Database in Mysql CLI or Workbench
- CLI
  - `Create database <RideSystem>`
  - `use <RideSystem>`
  - `show tables`

## User Model
Properties Listed:
- automatic insert, update
- use jakara api to manage data
- use lombok to minimize code getters and setters



- What is Jakara persistence?
    - Jakarta Persistence API (JPA; formerly Java Persistence API) is a Jakarta EE application programming interface specification that describes the management of relational data in enterprise Java applications.

    - `@Entity`
    - `@Table(name="user")`

- What is Serialization?
  - When transmitting data object through the internet or another process
  - Convert data object to another format
  - Serialization allows the developer to save the state of an object and recreate it as needed, providing storage of objects as well as data exchange. 

- What is Lombok?
  - import `lombok.Data` free developers from adding getters and setters.


- How to Configure Id in User Model?
  - implement `Serializable` for persistence
  - mapping the id private member in `User` using `@Id` (`javax.persistence.Id`)
  - set `serialVersionUID` for deserialization. 
    - JVM is based on the `serialzable uid ` to determine the version of class
    - when JVM deserializes, it compares whether the serial version uid in data stream is the same as its of customer class, if the class is considered not changed, the deserialization is successful.
  
  - configure auto increment with `@GeneratedValue(strategy=GenerationType.IDENTITY` annotation acknowledges to use database server side strategy.


- Use`Lombok.Data` or `Intellij` to create constructor, getters and setters:
  1. `@Data`: implements constructor, getters, setters for use
  2. Intellij
    - `ALT+INSERT` https://www.jetbrains.com/idea/guide/tips/generate-getters-and-setters/ (or Code->Generate->Constructor)
      - select all members except id
    - also create `getter and setter` using the same method
  
## Verify User Model Created a Table
- Run application
```

Hibernate: 
    create table user (
        id integer not null auto_increment,
        city varchar(255),
        contact_number varchar(255),
        identity varchar(255),
        license_plate_number varchar(255),
        mileage bigint,
        secret_key varchar(255),
        state varchar(255),
        user_name varchar(255),
        vehicle_type varchar(255),
        primary key (id)
    ) engine=InnoDB
```
- verify table created in my-sql workbench

## Clear design pattern - Interface  & Class
- In <REST> interface, define the routing method. In <REST_IMPL> class, define the method to be implemented.


## User - Sign Up
- Interface: UserRest
- Location: /REST
1. UserRest overall route: `@RequestMapping(path="/user")`

2. Signup Mapping: `@PostMapping(path="/user/signup")`
    - Argument: JSON BODY
    - Return Type: Response Message

- Implementation: UserRestImpl
- Location: /REST_IMPL

1. Map all Rest Methods inside the `UserRestImpl` with `@RestController`
2. `UserRestImpl` implements the `UserRest` interface
3. `ALT+ENTER` implement override method `signUp`
4. `@Autowired UserService` in UserRestImpl
  - you never need to explicitly new a UserService Object, but can use the method inside directly
  

### signUp Method:
  - try{ }catch(Exception ex){ ex.springStackTrace() }
  - calls `userService.signUp()` method, through the `requestMap` as parameter
  
```
@Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        try{
            return userService.signUp(requestMap);

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
```

- handle `error message` generically
  - utils: create a method that generates template for error response message
    - constants: create a constant string that replies `Something went wrong`

          return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

### UserServiceImpl.signUp 

### Validate User

### Check if User exists - JpaRepository

"Contact Number"
1. Create a query to find user contact number
- Location: DAO
  - Interface: UserDAO

        User findByContactNumber(@Param("contactNumber") String contactNumber);
  - Map: POJO/User
        - match String "contactNumber" in DAO findByContactNumber param to User Model definition "contactNumber"
  
             @NamedQuery(name="User.findByContactNumber", query="select u from User u where u.contactNumber=:contactNumber);


2. Use DAO object in signUp method (UserServiceImpl) to check for existence
   - Location: Service_IMPL
     - UserServiceImpl
       ```
       @Slf4j
        @Service
        public class UserServiceImpl implements UserService {

       @Override
       public ResponseEntity<String> signUp(Map<String, String> requestMap){
           log.info("Inside signup {}", requestMap);
           if(validateSignUpMap(requestMap)){
            
           }else{
               return RideUtils.getResponseEntity(RideConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
           }
       }

       ```
       - use queryObj:
          ```
           UserDao userDao;
           @Override
           public ResponseEntity<String> signUp(Map<String, String> requestMap){
              log.info("Inside signup {}", requestMap);
              if(validateSignUpMap(requestMap)){
                // create a user object if already exists
                User user = userDao.findByContactNumber(requestMap.get("contactNumber"));
                if(Objects.isNull(user)){

                }else{
                    return RideUtils.getResponseEntity("Contact Number already exists", HttpStatus.BAD_REQUEST);
                }
              }else{
                return RideUtils.getResponseEntity(RideConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
              }
           }
           ```
         

3. Create a user if does not exist and save to database
- Map attributes from ResponseMap to user object
  - Location: UserServiceImpl.java
  - create a method to set attributes
  - use JpaRepository method `.save()` to save user information into database

        User user = userDao.findByContactNumber(requestMap.get("contactNumber"));
            if(Objects.isNull(user)){
                userDao.save(getUserFromMap(requestMap));
            }
4. Exceptional Handling
  - Wrap try{}, catch{} around the signUp function
  - return `INTERNAL SERVER ERROR`
# RideSystem

## Authentication - CheckoutController
1. issue: CheckoutController is a @Controller type that maps to "checkout.html" by its @PostMapping(path="/checkout)
   - Thymeleaf searches for "checkout" named html based on "/checkout" path
   - Cannot use @RestController nor @RequestMap()
   - solution: use @RequestParam() to send by key in url ex: http://localhost:8089/checkout?order_id=1
2. issue: CheckoutController need to bypass JWT token authentication: 
    - solution: `JwtFilter.java` defines matching path:  request.getServletPath().matches("/checkout")
      - `SecurityFilterChain.java` add `.securityMatcher("/checkout")`         
        ```agsl
          .securityMatcher("/checkout")
        .authorizeHttpRequests((auths) -> auths
        .requestMatchers("/user/**", "/checkout").permitAll()
        .anyRequest().authenticated())

        ```
        