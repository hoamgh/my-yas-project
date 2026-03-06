# Hướng dẫn Viết Unit Test và Kiến thức cần có cho Dự án YAS

## 📋 Mục lục

1. [Tổng quan về Testing trong YAS](#1-tổng-quan-về-testing-trong-yas)
2. [Kiến thức Java cần có](#2-kiến-thức-java-cần-có)
3. [Kiến thức Spring Boot cần có](#3-kiến-thức-spring-boot-cần-có)
4. [Cấu trúc Test trong dự án](#4-cấu-trúc-test-trong-dự-án)
5. [Unit Testing với Mockito](#5-unit-testing-với-mockito)
6. [Controller Testing với @WebMvcTest](#6-controller-testing-với-webmvctest)
7. [Integration Testing với Testcontainers](#7-integration-testing-với-testcontainers)
8. [Test Data Generation với Instancio](#8-test-data-generation-với-instancio)
9. [Best Practices](#9-best-practices)
10. [Các công cụ và thư viện hỗ trợ](#10-các-công-cụ-và-thư-viện-hỗ-trợ)

---

## 1. Tổng quan về Testing trong YAS

### 1.1 Phân loại Test

Dự án YAS chia tests thành 2 loại chính:

| Loại Test | Vị trí | Plugin | Annotation chính | Mục đích |
|-----------|--------|--------|------------------|----------|
| **Unit Test** | `src/test/java/` | Maven Surefire | `@WebMvcTest`, `@ExtendWith(MockitoExtension.class)` | Test từng unit riêng lẻ |
| **Integration Test** | `src/it/java/` | Maven Failsafe | `@SpringBootTest` + Testcontainers | Test tích hợp nhiều components |

### 1.2 Naming Convention

```
// Unit Test
*Test.java         → src/test/java/

// Integration Test  
*IT.java           → src/it/java/
```

### 1.3 Chạy Tests

```bash
# Chạy unit tests
mvn test -f [module-name]

# Chạy integration tests
mvn verify -f [module-name]

# Chạy tất cả tests
mvn clean verify -f [module-name]
```

---

## 2. Kiến thức Java cần có

### 2.1 Java Core Knowledge

```java
// 1. Lambda Expressions & Functional Interfaces
List<String> names = items.stream()
    .map(Item::getName)
    .filter(name -> name.startsWith("A"))
    .collect(Collectors.toList());

// 2. Optional
Optional<User> user = userRepository.findById(id);
user.orElseThrow(() -> new NotFoundException("User not found"));

// 3. Records (Java 14+)
public record ProductVm(Long id, String name, BigDecimal price) {}

// 4. Generics
public class GenericRepository<T, ID> {
    public Optional<T> findById(ID id) { ... }
}
```

### 2.2 Java Collections & Stream API

```java
// Stream operations thường dùng trong tests
List<ProductVm> products = productList.stream()
    .filter(p -> p.isPublished())
    .map(p -> ProductVm.fromModel(p))
    .sorted(Comparator.comparing(ProductVm::getName))
    .toList();

// Map operations
Map<Long, Product> productMap = products.stream()
    .collect(Collectors.toMap(Product::getId, Function.identity()));
```

### 2.3 Annotations (Java)

```java
// Thường gặp trong dự án
@Override      // Ghi đè method từ parent
@Deprecated    // Đánh dấu deprecated
@FunctionalInterface  // Interface có 1 abstract method
@SuppressWarnings("unchecked")  // Bỏ qua warning
```

---

## 3. Kiến thức Spring Boot cần có

### 3.1 Core Spring Concepts

#### Dependency Injection (DI)

```java
// Constructor Injection (khuyến nghị)
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final MediaService mediaService;
}

// Field Injection (không khuyến nghị)
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
}
```

#### Spring Beans & Components

```java
@Component      // Generic component
@Service        // Business logic layer
@Repository     // Data access layer
@Controller     // Web MVC controller
@RestController // REST API controller = @Controller + @ResponseBody
@Configuration  // Configuration class
```

### 3.2 Spring Data JPA

```java
// Entity
@Entity
@Table(name = "product")
@Getter @Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> images;
}

// Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
    List<Product> searchByName(@Param("name") String name);
    
    Page<Product> findByCategory(Category category, Pageable pageable);
}
```

### 3.3 Spring Web MVC

```java
@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    
    @GetMapping("/products")
    public ResponseEntity<List<ProductVm>> getProducts(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(productService.getProducts(pageNo, pageSize));
    }
    
    @PostMapping("/products")
    public ResponseEntity<ProductVm> createProduct(
            @Valid @RequestBody ProductPostVm productPostVm) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(productPostVm));
    }
    
    @PutMapping("/products/{id}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductPutVm productPutVm) {
        productService.updateProduct(id, productPutVm);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
```

### 3.4 Spring Security

```java
// OAuth2 Resource Server configuration
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/backoffice/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

### 3.5 Validation

```java
// ViewModel với validation
public record ProductPostVm(
    @NotBlank(message = "Name is required")
    String name,
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    BigDecimal price,
    
    @Size(max = 1000, message = "Description max 1000 characters")
    String description,
    
    @NotNull(message = "Category is required")
    Long categoryId
) {}
```

---

## 4. Cấu trúc Test trong dự án

### 4.1 Cấu trúc thư mục

```
src/
├── main/
│   └── java/com/yas/product/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       └── model/
├── test/              ← Unit Tests
│   └── java/com/yas/product/
│       ├── controller/
│       │   └── ProductControllerTest.java
│       └── service/
│           └── ProductServiceTest.java
└── it/                ← Integration Tests
    └── java/com/yas/product/
        └── controller/
            └── ProductControllerIT.java
```

### 4.2 Common Test Infrastructure

Dự án YAS sử dụng `common-library` cung cấp các base classes cho testing:

```java
// AbstractControllerIT - Base class cho Integration Tests
public class AbstractControllerIT {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    protected String authServerUrl;

    @LocalServerPort
    private int port;

    protected RequestSpecification getRequestSpecification() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        return new RequestSpecBuilder()
            .setPort(port)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    protected String getAccessToken(String username, String password) {
        return given()
            .contentType("application/x-www-form-urlencoded")
            .formParams(Map.of(
                "username", username,
                "password", password,
                "scope", "openid",
                "grant_type", "password",
                "client_id", "quarkus-service",
                "client_secret", "secret"
            ))
            .post(authServerUrl + "/protocol/openid-connect/token")
            .then().assertThat().statusCode(200)
            .extract().path("access_token");
    }

    protected RequestSpecification givenLoggedInAsAdmin() {
        return given(getRequestSpecification())
            .auth().oauth2(getAccessToken("admin", "admin"));
    }
}
```

```java
// IntegrationTestConfiguration - Cấu hình Testcontainers
@TestConfiguration
public class IntegrationTestConfiguration {

    @Bean(destroyMethod = "stop")
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:16")
            .withReuse(true);
    }

    @Bean(destroyMethod = "stop")
    public KeycloakContainer keycloakContainer() {
        return new KeycloakContainer()
            .withRealmImportFiles("/test-realm.json")
            .withReuse(true);
    }

    @Bean
    public DynamicPropertyRegistrar keycloakDynamicProperties(
            KeycloakContainer keycloakContainer) {
        return registry -> {
            registry.add(
                "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "/realms/quarkus"
            );
        };
    }
}
```

---

## 5. Unit Testing với Mockito

### 5.1 Service Layer Testing

```java
@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    WebhookEventNotificationRepository webhookEventNotificationRepository;
    
    @Mock
    WebhookApi webHookApi;

    @InjectMocks
    WebhookService webhookService;

    @Test
    void test_notifyToWebhook_ShouldNotException() {
        // Arrange
        WebhookEventNotificationDto notificationDto = WebhookEventNotificationDto
            .builder()
            .notificationId(1L)
            .url("http://example.com")
            .secret("secret")
            .build();

        WebhookEventNotification notification = new WebhookEventNotification();
        when(webhookEventNotificationRepository.findById(notificationDto.getNotificationId()))
            .thenReturn(Optional.of(notification));

        // Act
        webhookService.notifyToWebhook(notificationDto);

        // Assert
        verify(webhookEventNotificationRepository).save(notification);
        verify(webHookApi).notify(
            notificationDto.getUrl(), 
            notificationDto.getSecret(), 
            notificationDto.getPayload()
        );
    }
}
```

### 5.2 Mockito Annotations

```java
@Mock          // Tạo mock object
@InjectMocks   // Inject mocks vào class được test
@Spy           // Partial mock - gọi real method trừ khi stubbed
@Captor        // Capture arguments được pass vào mock
```

### 5.3 Mockito Methods phổ biến

```java
// Stubbing
when(repository.findById(1L)).thenReturn(Optional.of(entity));
when(service.create(any())).thenThrow(new RuntimeException("Error"));

// BDD Style
given(repository.findById(1L)).willReturn(Optional.of(entity));

// Verify
verify(repository).save(any(Product.class));
verify(repository, times(2)).findById(anyLong());
verify(repository, never()).delete(any());

// Argument Matchers
when(service.process(anyString(), eq("specific")))
    .thenReturn(result);

// Argument Captor
@Captor
ArgumentCaptor<Product> productCaptor;

verify(repository).save(productCaptor.capture());
Product savedProduct = productCaptor.getValue();
assertEquals("Expected Name", savedProduct.getName());
```

---

## 6. Controller Testing với @WebMvcTest

### 6.1 Cấu trúc cơ bản

```java
@WebMvcTest(controllers = WarehouseController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class WarehouseControllerTest {

    @MockitoBean  // Mock service layer
    private WarehouseService warehouseService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectWriter objectWriter;

    @BeforeEach
    void setUp() {
        objectWriter = new ObjectMapper()
            .writer()
            .withDefaultPrettyPrinter();
    }

    @Test
    void testGetProducts_whenValidRequest_thenReturnProducts() throws Exception {
        // Arrange
        Long warehouseId = 1L;
        List<ProductInfoVm> products = Arrays.asList(
            new ProductInfoVm(1L, "Product1", "SKU1", true),
            new ProductInfoVm(2L, "Product2", "SKU2", true)
        );

        given(warehouseService.getProductWarehouse(eq(warehouseId), any(), any(), any()))
            .willReturn(products);

        // Act & Assert
        mockMvc.perform(get("/backoffice/warehouses/{warehouseId}/products", warehouseId)
                .param("productName", "")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Product1"))
            .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void testCreateWarehouse_whenValidRequest_thenReturnCreated() throws Exception {
        // Arrange
        WarehousePostVm request = new WarehousePostVm("New Warehouse", 1L);
        WarehouseDetailVm response = new WarehouseDetailVm(1L, "New Warehouse", ...);
        
        given(warehouseService.create(any(WarehousePostVm.class)))
            .willReturn(response);

        // Act & Assert
        mockMvc.perform(post("/backoffice/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectWriter.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("New Warehouse"));
    }
}
```

### 6.2 MockMvc Request Builders

```java
// GET request
mockMvc.perform(get("/api/products/{id}", 1L)
        .param("includeDetails", "true"))

// POST request với JSON body
mockMvc.perform(post("/api/products")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonContent))

// PUT request
mockMvc.perform(put("/api/products/{id}", 1L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonContent))

// DELETE request
mockMvc.perform(delete("/api/products/{id}", 1L))

// With authentication
mockMvc.perform(get("/api/protected")
        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
```

### 6.3 MockMvc Result Matchers

```java
.andExpect(status().isOk())
.andExpect(status().isCreated())
.andExpect(status().isNoContent())
.andExpect(status().isBadRequest())
.andExpect(status().isNotFound())
.andExpect(status().isForbidden())

.andExpect(content().contentType(MediaType.APPLICATION_JSON))
.andExpect(content().json(expectedJson))

.andExpect(jsonPath("$.id").value(1))
.andExpect(jsonPath("$.name").value("Product"))
.andExpect(jsonPath("$[0].id").value(1))
.andExpect(jsonPath("$.items").isArray())
.andExpect(jsonPath("$.items", hasSize(3)))
```

---

## 7. Integration Testing với Testcontainers

### 7.1 Cấu trúc Integration Test

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration.class)
class CartItemControllerIT extends AbstractControllerIT {

    @Autowired
    private CartItemRepository cartItemRepository;

    @MockitoBean
    private ProductService productService;  // Mock external service

    private ProductThumbnailVm existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = ProductThumbnailVm
            .builder()
            .id(Long.MIN_VALUE)
            .name("product-name")
            .slug("product-slug")
            .build();
    }

    @AfterEach
    void tearDown() {
        cartItemRepository.deleteAll();  // Clean up after each test
    }

    @Nested  // Group related tests
    class AddCartItemTest {

        @Test
        void testAddCartItem_whenRequestIsValid_shouldReturnCartItemGetVm() {
            // Arrange
            CartItemPostVm cartItemPostVm = new CartItemPostVm(existingProduct.id(), 1);
            when(productService.existsById(cartItemPostVm.productId())).thenReturn(true);

            // Act & Assert
            givenLoggedInAsAdmin()
                .body(cartItemPostVm)
                .when()
                .post("/v1/storefront/cart/items")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("productId", is(cartItemPostVm.productId()))
                .body("quantity", equalTo(cartItemPostVm.quantity()))
                .log().ifValidationFails();
        }
    }
}
```

### 7.2 Testcontainers Setup

```java
@Testcontainers
@SpringBootTest
class LocationServiceIT {

    @Container
    @ServiceConnection  // Auto-configure datasource
    static PostgreSQLContainer<?> postgresContainer = 
        new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private LocationService locationService;

    @Test
    void test_getAddressesByIdList_shouldWork() {
        // Test với real database
        List<Long> addressIds = Collections.singletonList(1L);
        List<AddressDetailVm> result = locationService.getAddressesByIdList(addressIds);
        assertNotNull(result);
    }
}
```

### 7.3 Rest Assured với Integration Tests

```java
@Test
void getRatingList_whenProvidedAccessToken_shouldReturnData() {
    given(getRequestSpecification())
        .auth().oauth2(getAccessToken("admin", "admin"))
        .when()
        .get("/v1/backoffice/ratings")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("size()", Matchers.greaterThan(0))
        .log().ifValidationFails();
}

@Test
void createRating_whenValidData_shouldReturnCreated() {
    RatingPostVm ratingPostVm = new RatingPostVm(1L, 5, "Great product!");
    
    givenLoggedInAsAdmin()
        .body(ratingPostVm)
        .when()
        .post("/v1/storefront/ratings")
        .then()
        .statusCode(HttpStatus.CREATED.value())
        .body("id", Matchers.notNullValue())
        .body("ratingStar", equalTo(5));
}
```

---

## 8. Test Data Generation với Instancio

### 8.1 Basic Usage

```java
// Tạo object với random data
Product product = Instancio.create(Product.class);

// Tạo với customization
Product product = Instancio.of(Product.class)
    .set(field(Product::getName), "Custom Name")
    .set(field(Product::getPrice), BigDecimal.valueOf(100))
    .create();
```

### 8.2 Advanced Patterns

```java
// Ignore certain fields
Rating rating = Instancio.of(Rating.class)
    .ignore(Select.field(Rating::getId))  // Ignore auto-generated ID
    .generate(Select.field(Rating::getRatingStar), 
        gen -> gen.ints().min(1).max(5))  // Custom range
    .create();

// Create list
List<Product> products = Instancio.ofList(Product.class)
    .size(10)
    .create();

// Nested object customization
CheckoutPostVm checkoutPostVm = Instancio.of(CheckoutPostVm.class)
    .supply(field(CheckoutPostVm.class, "shippingAddressId"), 
        gen -> Long.toString(gen.longRange(1, 10000)))
    .create();
```

---

## 9. Best Practices

### 9.1 Test Naming Convention

```java
// Pattern: methodName_stateUnderTest_expectedBehavior
@Test
void createProduct_whenValidInput_shouldReturnCreatedProduct() { }

@Test
void getProductById_whenProductNotFound_shouldThrowNotFoundException() { }

@Test
void updateProduct_whenUnauthorized_shouldReturnForbidden() { }
```

### 9.2 AAA Pattern (Arrange-Act-Assert)

```java
@Test
void getCategoryById_Success() {
    // Arrange - Setup test data and mocks
    Category category = new Category();
    category.setId(1L);
    category.setName("Electronics");
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

    // Act - Execute the method under test
    CategoryGetDetailVm result = categoryService.getCategoryById(1L);

    // Assert - Verify the result
    assertNotNull(result);
    assertEquals("Electronics", result.name());
    verify(categoryRepository).findById(1L);
}
```

### 9.3 Test Data Cleanup

```java
@AfterEach
void tearDown() {
    // Clean up in correct order (respect foreign key constraints)
    orderItemRepository.deleteAll();
    orderRepository.deleteAll();
    productRepository.deleteAll();
}
```

### 9.4 Test Isolation

```java
// Mỗi test nên độc lập
@BeforeEach
void setUp() {
    // Reset state trước mỗi test
    testData = createTestData();
}

// Sử dụng random data để tránh conflicts
String uniqueName = "Product_" + UUID.randomUUID();
```

### 9.5 Assertions Best Practices

```java
// Sử dụng AssertJ cho readability
import static org.assertj.core.api.Assertions.assertThat;

assertThat(result)
    .isNotNull()
    .hasFieldOrPropertyWithValue("id", 1L)
    .hasFieldOrPropertyWithValue("name", "Expected Name");

assertThat(result.getItems())
    .hasSize(3)
    .extracting("name")
    .containsExactly("A", "B", "C");

// Exception testing
assertThatThrownBy(() -> service.process(null))
    .isInstanceOf(NotFoundException.class)
    .hasMessage("Product not found");
```

---

## 10. Các công cụ và thư viện hỗ trợ

### 10.1 Testing Libraries used in YAS

| Library | Purpose | Maven Dependency |
|---------|---------|------------------|
| JUnit 5 | Test framework | `spring-boot-starter-test` |
| Mockito | Mocking framework | `spring-boot-starter-test` |
| AssertJ | Fluent assertions | `spring-boot-starter-test` |
| Testcontainers | Container-based testing | `spring-boot-testcontainers` |
| Rest Assured | REST API testing | `io.rest-assured:rest-assured` |
| Instancio | Test data generation | `org.instancio:instancio-junit` |
| H2 Database | In-memory database for tests | `com.h2database:h2` |

### 10.2 Coverage và Quality Tools

```xml
<!-- JaCoCo for code coverage -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
        </execution>
    </executions>
</plugin>
```

### 10.3 Chạy Coverage Report

```bash
# Generate coverage report
mvn clean verify -f product

# Report location
target/site/jacoco/index.html
```

---

## 11. Checklist cho việc viết Test

### Unit Test Checklist
- [ ] Test tất cả public methods
- [ ] Test happy path (success scenarios)
- [ ] Test edge cases và error scenarios
- [ ] Mock tất cả dependencies
- [ ] Sử dụng meaningful test names
- [ ] Verify behavior, không verify implementation

### Integration Test Checklist
- [ ] Test end-to-end flow
- [ ] Test với real database (Testcontainers)
- [ ] Test authentication/authorization
- [ ] Test API response format
- [ ] Clean up data sau mỗi test
- [ ] Test error responses

---

## 12. Tổng kết kiến thức cần có

### Core Java
- [ ] OOP: Inheritance, Polymorphism, Encapsulation
- [ ] Collections Framework: List, Map, Set
- [ ] Stream API & Lambda
- [ ] Optional
- [ ] Records & Generics

### Spring Framework
- [ ] Dependency Injection
- [ ] Spring MVC: Controller, Service, Repository
- [ ] Spring Data JPA: Entity, Repository
- [ ] Spring Security: OAuth2, JWT
- [ ] Spring Boot Auto-configuration

### Testing
- [ ] JUnit 5: Annotations, Lifecycle
- [ ] Mockito: Mock, Stub, Verify
- [ ] MockMvc: Web layer testing
- [ ] Testcontainers: Database testing
- [ ] Rest Assured: API testing

### Database
- [ ] SQL basics
- [ ] JPA/Hibernate mappings
- [ ] Liquibase migrations

### DevOps/Tools
- [ ] Docker basics
- [ ] Maven: Build, Test commands
- [ ] Git: Branch, Merge, PR workflow

---

**Tài liệu tham khảo:**
- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Rest Assured Documentation](https://rest-assured.io/)
- [Instancio Documentation](https://www.instancio.org/)
