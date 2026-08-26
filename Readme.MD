# Spring Security
### For login we need to add spring security dependency in pom.xml file
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
### By default, spring security will create a user with username as "user" and password as random

### There are two ways to autherize , formbased and basic auth. By default, spring security will use formbased authentication. To use basic auth we need to add below code in application.properties file
```spring.security.user.name=admin
spring.security.user.password=admin
spring.security.user.roles=USER
``` 
### To use formbased authentication we need to add below code in application.properties file
```
spring.security.user.name=admin
spring.security.user.password=admin
spring.security.user.roles=USER
```
### By default, spring security will use formbased authentication. To use basic auth we need to add below code in application.properties file
```
spring.security.user.name=admin
spring.security.user.password=admin
spring.security.user.roles=USER
```
### To use formbased authentication we need to add below code in application.properties file         


by default its 
####
username = user 
####
password = random ( auto generated password will be printed in console when we run the application)

### For test purpose we can create our own user and password by creating a configuration class as below

```java
@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService users(){
        UserDetails user = User.builder()
                .username("user")
                .password("{noop}password123")
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}admin123")
                .roles("ADMIN", "USER")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

}
```

### For real world application we should use database to store user details and password should be encrypted. We can use BCryptPasswordEncoder to encrypt the password. Below is an example of how to use BCryptPasswordEncoder in a configuration class

```java
@Configuration
public class SecurityConfig {
    @Bean
    public UserDetailsService users(){
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("password123"))
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN", "USER")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
```

### For accessing the user from Database we need to create a class that implements UserDetailsService interface and override the loadUserByUsername method. Below is an example of how to use UserDetailsService to load user details from database

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }
}
```
## As we created the new users int the database , so to createUser we need to create a controller to handle the request and save the user in database. Below is an example of how to create a user in database

So first we to allow the createUser or Register user to access the endpoint without authentication. For that we need to create a configuration class and override the configure method as below

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {  
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/register").permitAll() // Allow access to /register endpoint without authentication
                .anyRequest().authenticated() // All other endpoints require authentication
            .and()
            .formLogin()
                .loginPage("/login") // Custom login page
                .permitAll()
            .and()
            .logout()
                .permitAll();
    }
}
```

## OR
Using SecurityFilterChain
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean    
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/register").permitAll() // Allow access to /register endpoint without authentication
                .anyRequest().authenticated() // All other endpoints require authentication
            .and()
            .formLogin()
                .loginPage("/login") // Custom login page
                .permitAll()
            .and()
            .logout()
                .permitAll();
        return http.build();
    }
```

A CSRF token (Cross-Site Request Forgery token) is a unique, secret, and unpredictable value generated by a server-side application to protect against CSRF attacks. It ensures that user-initiated actions, such as form submissions, are legitimate, preventing attackers from tricking browsers into performing unwanted action
### To enable CSRF protection in Spring Security, you can use the following configuration in your security configuration class:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // Enable CSRF protection and store the token in a cookie
            .and()
            .authorizeRequests()        
                .antMatchers("/register").permitAll() // Allow access to /register endpoint without authentication
                .anyRequest().authenticated() // All other endpoints require authentication
            .and()
            .formLogin()
                .loginPage("/login") // Custom login page       
                .permitAll()
            .and()
            .logout()
                .permitAll();
    }
}
```


##  Security Context 
Security Context is a fundamental concept in Spring Security that holds the security-related information for the current execution thread. It is used to store details about the authenticated user, their roles, and other security-related data. The SecurityContext is typically associated with the current thread of execution and can be accessed throughout the application.
```shell
[User Request] ➔ [SecurityContextPersistenceFilter / SecurityContextHolderFilter]
                            │
                            ▼
Looks for JSESSIONID or JWT Header
                            │
                            ▼
Found! Validates identity.
                            │
                            ▼
Recreates Brand New SecurityContext for this specific thread
                            │
                            ▼
Executes your Controller Logic
                            │
                            ▼
Clears and Destroys Context (Thread returns to pool empty)
```
