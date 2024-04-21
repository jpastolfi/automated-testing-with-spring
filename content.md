## Testes de unidade:
- Pequenas partes
- Escrito pelos desenvolvedores
- Podem ser solitários (não interage com outras unidades) ou sociáveis (conversa com outras unidades)

## Testes de integração:
- Bordas da aplicação. Testa as unidades que interagem com camadas externas
- Podem ser restritos (trabalham com uma camada) ou amplos (cruzam outras camadas)
- Dublês de teste. Não sobe um banco para um teste, mas usa algo mais leve tal como o H2

## Testes de componente ou subcutâneo:
- São testes de integração amplos, ou seja, cruzam várias camadas do sistema.
- Ex.: chamar a API e checar o retorno. A chamada vai passar por todas as camadas da aplicação.

## Testes e2e:
- Sobe o sistema completo (servidor da aplicação, banco de dados, web service, etc)
- Simula jornada do usuário
- Lento, código sem dono pois depende de muitas coisas externas, frágil

## Padrão para nomear testes
operacao_estado_retorno
Ex.: cratePlanet_WithValidData_ReturnsPlanet

## SpringBootTest
Quando estamos fazendo testes unitários, não é eficiente carregar todo o contexto da aplicação. Para especificar quais Beans queremos que sejam carregados, podemos especificar na classe de testes usando
@SpringBootTest(classes = PlanetService.class)

## Dublês de teste
Usados pelos testes solitários para simular o comportamento de suas dependências.
- Dummy: Apenas para compilar, não é invocado. Normalmente usado quando desenvolvemos em TDD
- Fake:  implementação funcional, mas não usada em produção. Exemplo: usar uma List (banco em memória) para salvar dados ao invés de um banco de dados.
- Stub: responde de acordo com definições preestabilecidas: quando chamado com determinado(s) parâmetro(s), retorna um valor fixo (verificação de estado)
- Spy: implementação mais robusta do stub. Grava informações sobre como os métodos foram chamados (verificação de estado e comportamento)
- Mock: interação exata com os objetos que o usam, verifica se o fluxo desejado foi invocado (verificação de comportamento )
![img.png](img.png)

## Testes unitários usando @SpringBootTest
Normalmente quando criamos testes de unidade não usamos a notação @SpringBootTest porque ela carrega muitas coisas desnecessárias. Para isso, substituímos a notação @SpringBootTest pela @ExtendWith(MockitoExtension.class) para usarmos somente o Mockito. Como não vamos usar o Spring para testar, também não vamos usar @AutoWired, mas sim @InjectMocks para instanciar a classe que vamos testar e também injetar todas as dependências de que ela precisa. Quanto ao mock que precisamos criar, vamos usar a notação @Mock ao invés de @MockBean

## Testes de integração
Para testarmos o funcionamento da aplicação, precisamos montar a estrutura de um banco de dados. Podemos fazer isso usando a anotação @DataJpaTest. Essa notação configura um banco em memória H2, possibilitando testar o funcionamento real sem mocks.

Para testarmos a repository, não podemos usá-la para salvar uma entidade e depois verificar se ela foi salva pois não podemos usar o que estamos testando para ajudar no teste. Para isso, vamos usar um TestEntityManager. Ele possibilita interagir com o banco de dados sem usar a repository. Por exemplo, podemos usar a repository para salvar uma entidade no banco e e o TestEntityManager para verificar se essa entidade foi salva corretamente buscando pelo id que foi salvo.

## Constraints
Para evitar que dados nulos sejam salvos nas nossas tabelas, podemos usar a anotação @Column(nullable = false) nos atributos da entidade em que queremos dados nulos. 

Se quisermos evitar strings vazias no banco, podemos usar uma biblioteca de Bean Validation e a notação @NotEmpty nos atributos desejados. 

- NotNull: não permite valores null nos atributos indicados
- NotEmpty: não permite valores null nos atributos indicados e o seu comprimento deve ser maior que 0
- NotBlank: não permite valores null nos atributos indicados e o seu comprimento tirando espaços deve ser maior que 0
- Size: especifica um tamanho mínimo e máximo para o campo indicado

## Fazendo testes de integração 
Na nossa aplicação, não é possível salvar dois planetas com o mesmo nome. Para testar essa funcionalidade, precisamos salvar um planeta uma vez e depois tentar salvá-lo de novo. Não podemos usar a repository duas vezes para fazer isso pois é ela que queremos testar. Vamos novamente usar o TestEntityManager para interagir com o banco sem usar a repository. Vamos usar um método chamado persistFlushFind que faz três operações. O persist salva o planeta, o flush atualiza o banco de dados e o find busca a entidade que foi salva. Esse método altera a instância do objeto que foi passada como parâmetro para a entidade que foi salva no banco. Ou seja, se passarmos um objeto sem id e tivermos GenerationType.IDENTITY, um id será atribuído ao objeto. O método save da repository decide se vai criar ou atualizar um planeta baseado na existência ou não de um id, então para testarmos a exceção ao tentar salvar dois planetas com o mesmo nome, precisamos recuperar o planeta que foi salvo, retirar o seu id, e tentar salvar esse objeto (agora sem id).

Porém, ainda temos um problema. Quando usamos um entity manager, ele está gerenciando as entidades de um banco de dados. O Hibernate monitora tudo que acontece com o banco, então mesmo setando o id do planeta como nulo, o Hibernate sabe que esse planeta já foi criado e tenta atualizar ao invés de criar um novo. Para resolver isso, temos que remover o gerenciamento do entity manager desse planeta usando o método detach do próprio entity manager.

## Testes de integração com requisições HTTP
Para testar a camada de controle, vamos precisar fazer requisições HTTP. Para isso, vamos usar a anotação @WebMvcTest na nossa classe de teste. Para evitar que todos os controllers sejam carregados, vamos passar como parâmetro o controller que vamos testar. Ex.:
```
@WebMvcTest(PlanetControllerTest.class)
public class PlanetControllerTest {
  @Test
  public void createPlanet_WithValidData_ReturnsCreated() {
    ...
  }
}
```
Além de injetar o controlador no contexto de aplicação Spring, isso também monta um contexto Web, incluindo um cliente HTTP para interagir com esse contexto, o MockMvc. Vamos usá-lo para construir uma requisição com um request builder usando o método POST do MockMvcRequestBuilder e o método .content para enviar o corpo da requisição. Um detalhe é que vamos precisar serializar o objeto antes de enviá-lo para o cliente HTTP. Vamos usar o ObjectMapper.writeValueAsString para fazer isso. Agora vamos usar o .andExpect para validar o retorno do status e o corpo da resposta.

Para testar corpos inválidos, vamos precisar inserir a validação no controlador, tendo em vista que as anotações de @NotEmpty estão na entidade em si. Para isso, podemos usar a anotação @Valid no parâmetro da controller. Isso vai espelhar as validações na própria controller.
```
@PostMapping
  public ResponseEntity<Planet> create(@RequestBody @Valid Planet planet) {
    // ...
  }
```

Isso vai fazer com que tentativas de criação de planetas com dados inválidos (como definido na entidade) retornem um status 400. Se quisermos um status diferente desse, podemos criar um handler de exceções com as anotações @ControllerAdvice, que vai aplicar esse handler para toda a camada controller, e extends @ResponseEntityExceptionHandler, para sobreescrever o comportamento de validação do corpo da requisição. O @ResponseEntityExceptionHandler é um exception handler padrão para lidar com entidades de resposta. Dentro dele já existe um método que lida com o problema de argumentos inválidos na requisição

## Executando um script com uma query SQL para popular uma tabela com dados
Podemos querer popular uma tabela com entidades para podermos executar testes. Para isso, podemos usar junto da anotação @Test a notação @Sql(scripts = path/arquivo.sql), passando o caminho do arquivo .sql. Se colocarmos o arquivo dentro do diretório test/resources, podemos acessar diretamente com /nome_do_arquivo.sql

# Testes e2e / subcutâneos
## Configurando o servidor de aplicação
Quando usamos o MockMvc, estamos usando um servidor mockado. Para testar a aplicação end to end, precisamos usar um servidor real. Na nossa pasta de testes, vamos criar uma classe chamda PlanetIT para abrigar esses testes, que são mais "caros" de usar pois levam mais tempo e usam mais recursos.

Para subir esse servidor de aplicação e criar o contexto de aplicação do Spring e colocar todos os Beans nele, vamos usar a anotação @SpringBootTest. Vamos criar um teste vazio chamado contextLoads apenas para testar o carregamento do contexto da aplicação. 

Como essa anotação cria um ambiente web mockado, vamos ajustar para que seja o servidor real (Tomcat). Vamos configurar o ambiente web que vai ser utilizado pelo @SpringBootTest como o parâmetro (webEnvironment = port), onde port é a porta que será usada para subir o Tomcat. Se não informarmos nada, será usada a porta 8080. Para evitar conflitos, vamos escolher uma porta aleatória usando WebEnvironment.RANDOM_PORT.
## Configurando um banco MySQL para os testes
Vamos criar um perfil para os nossos testes e2e. O nome do perfil será application-ti.properties e vamos usar a anotação @ActiveProfiles("it") para selecionar esse perfil criado. O arquivo de perfil ficará assim:
```
# Configuracao do DataSource
spring.datasource.url=${MYSQL_HOST:jdbc:mysql://localhost/starwars?useSSL=false}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:user}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:123456}

# Configuracao do Hibernate
spring.jpa.hibernate.ddl-auto=update

# Configuracao da JPA - Habilita Log na execucao de SQL
spring.jpa.show-sql=true
```
## Fazendo os testes
Para fazer as requisições vamos usar um componente chamado TestRestTemplate, que será injetado pelo Spring usando @Autowired. Para fazer o teste de criação de planeta, precisamos fazer uma requisição POST. O TestRestTemplate tem um método chamado postForEntity, que faz uma requisição POST e já traduz a resposta de JSON para o tipo de entidade passado por parâmetro. Passamos três parâmetros para esse método: url, corpo da requisição e tipo do corpo da resposta para serialização. No nosso caso, seria `testRestTemplate.postForEntity("/planets", PLANET, Planet.class);`. Salvamos isso em uma variável do tipo `ResponseEntity<Planet>`, afinal, é isso que o nosso controlador retorna. O rest template tem um método genérico para requisições HTTP que podemos usar para obter um retorno mesmo que ele seja um `ResponseEntity<Void>`: exchange. Para usá-lo, devemos passar a URL, o método da requisição, o corpo da requisição e o tipo da resposta.

## Configurando rollback para os testes
Se estivermos fazendo um teste de criação com algum atributo unique, não vamos conseguir rodar esse teste mais de uma vez porque já haverá uma entidade com esse atributo no banco de dados (partindo da ideia que no perfil de testes temos spring.jpa.hibernate.ddl-auto=update). Para resolver isso, podemos criar um script SQL que vai limpar o banco após cada teste. O script em si será `TRUNCATE TABLE planets`. Truncate é um comando que remove os registros da tabela informada de forma mais ágil que o DELETE. Agora precisamos avisar a classe de testes que esse script deve ser executado após cada teste. Para isso, vamos anotar a classe com @Sql(scripts = { "/remove_planets.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD). O segundo parâmetro especifica em qual fase de execução o script deve ser executado.

## Utilizando o WebTestClient (substituto do TestRestTemplate)
No Spring 5, foi introduzido um cliente web reativo (parte do módulo Webflux), o WebClient, e sua versão para testes, o WebTestClient. Ele surgiu como um substituto para o RestTemplate, pois utiliza uma abordagem não bloqueante para fazer requisições e ainda permite utilizar uma linguagem fluente, bem mais tranquila de entender.

Vamos observar o exemplo de teste feito com o TestRestTemplate:
```
@Test
public void createPlanet_ReturnsCreated() {
  ResponseEntity<Planet> sut = 
    restTemplate.postForEntity("/planets", PLANET, Planet.class);
 
  assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  assertThat(sut.getBody().getId()).isNotNull();
  assertThat(sut.getBody().getName()).isEqualTo(PLANET.getName());
  // Omitidos por simplicidade
}
```
Observe que o método postForEntity recebe vários parâmetros para fazer uma requisição post para o serviço web que estamos testando. Agora olha a versão com o WebTestClient:
```
@Test
public void createPlanet_ReturnsCreated() {
  Planet sut = webTestClient.post().uri("/planets").bodyValue(PLANET)
    .exchange().expectStatus().isCreated().expectBody(Planet.class)
    .returnResult().getResponseBody();
 
  assertThat(sut.getId()).isNotNull();
  assertThat(sut.getName()).isEqualTo(PLANET.getName());
  // Omitidos por simplicidade
}
```
A requisição é construída de forma fluente, onde cada parâmetro é informado num método específico que o utiliza, trazendo uma espécie de semântica melhor à requisição HTTP.

Para usar o WebTestCliente é necessário adicionar a dependência do webflux:
```
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
  <scope>test</scope>
</dependency>
```

## Separando os testes em fases
Vamos usar o Maven para dividir os testes em duas fases: uma com os mais leves e outra  com os mais pesados. Podemos configurar quais testes rodar em cada etapa do CI/CD.

Por padrão, os arquivos que terminam com Test rodam no build da aplicação. Vamos configurar para que os testes de integração (localizados no arquivo PlanetIT) também rodem com o build do Maven. Vamos fazer isso em duas etapas: configurar um plugin para os testes de unidade e outro para os testes de integração. Fazemos isso no pom.xml.

O primeiro plugin será para a execução dos testes de unidade:
```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <skip>${surefire.skip}</skip>
    </configuration>
</plugin>
```
Para os testes de integração usaremos o failsafe:
```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Após isso, se quisermos rodar todos os testes da aplicação, podemos usar o comando `./mvnw clean verify`. Se quisermos rodar apenas os de integração, podemos adicionar `-Dsurefire.skip=true`para pular os testes de integração. Podemos fazer isso porque incluímos `<skip>${surefire.skip}</skip>` no nosso plugin dos testes de unidade. Para rodar apenas os testes de unidade podemos usar o comando `./mvnw clean verify -DskipITs=true`. Esse sufixo funciona porque é suportado por padrão pelo failsafe. É o equivalente a rodar `./mvnw clean test`.

## Cobertura de testes com o Jacoco
Cobertura de testes serve para verificar se todos os caminhos possíveis da aplicação foram testados, se todos os fluxos de código estão sendo exercitados pelos testes. Para fazer isso usamos um plugin chamado Jacoco, que mede a cobertura de testes e gera um relatório dizendo o que não foi testado:
```
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
</plugin>
```
Agora precisamos informar quando esse plugin deve ser executado. Fazemos isso configurando executions:
```
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
Quando executarmos o comando `./mvnw clear test`, um arquivo jacoco.exec será criado na pasta target. Vamos adicionar mais uma execution ao nosso plugin para que um relatório seja gerado com essa cobertura de testes:
```
<execution>
    <id>report</id>
    <phase>prepare-package</phase>
    <goals>
        <goal>report</goal>
    </goals>
</execution>
```
Agora, ao executarmos os testes com o comando `./mvnw clear test jacoco:report` , será criada uma pasta site dentro da target com um HTML. Esse arquivo contém uma tabela mostrando a cobertura da aplicação, linhas testadas, etc. É possível navegar pela aplicação verificando o que foi testado.

É comum o Jacoco identificar que o método main não foi testado. Para resolver isso, vamos retirá-lo da cobertura alterando o arquivo pom.xml. Dentro do plugin do jacoco, vamos adicionar uma configuration com um exclude:
```
...
<groupId>org.jacoco</groupId>
<artifactId>jacoco-maven-plugin</artifactId>
<configuration>
    <excludes>
        <exclude>com/example/swplanetapi/SwPlanetApiApplication.class</exclude>
    </excludes>
</configuration>
...
```
Também podemos excluir métodos da cobertura de testes diretamente no código. Essa exclusão de método nativamente não é permitida pelo Jacoco. Vamos fazer isso criando uma anotação customizada na pasta jacoco dentro da aplicação principal:
```
package com.example.swplanetapi.jacoco;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Anotação vai ser aplicada em tempo de execução
@Retention(RetentionPolicy.RUNTIME)
// E em métodos que tiverem ela
@Target(ElementType.METHOD)
public @interface ExcludeFromJacocoGeneratedReport {}
```
Ao adicionar essa anotação em um método, ele será excluído da cobertura de testes.

Como a classe QueryBuilder nunca é construída (seu construtor nunca é chamado pois o seu único método é estático), o Jacoco identifica que ela não foi testada. Nesses casos podemos tornar o construtor da classe em um construtor privado.

## Testes mutante com Pitest
A ideia do teste é que, se houver alguma mudança no código testado, o teste não deve passar. Para availar isso, vamos usar testes mutante. Basicamente mudamos o código testado e rodamos o teste, que não deve passar. Se algum teste passar, ele é um teste mutante. Para testar isso, vamos usar o Pitest. Vamos usar um plugin do Maven para especificar uma fase de teste para fazer esse teste mutante.
```
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.8.0</version>
    <dependencies>
        <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>0.14</version>
        </dependency>
    </dependencies>
    <configuration>
        <targetTests>
            <param>com.example.swplanetapi.*Test</param>
        </targetTests>
    </configuration>
</plugin>
```
Esses testes mutante são muito demorados pois criam combinações de código para mutar o código principal. Normalmente são usados nos testes de unidade.

Os testes mutantes podem ser executados com o comando `./mvnw test-compile org.pitest:pitest-maven:mutationCoverage`. Será gerado um relatório dentro da pasta target/pit-reports/index.html.

Para excluir testes dos testes mutantes, podemos excluir classes inteiras no pom.xml:
```
...
<configuration>
    <targetTests>
        <param>com.example.swplanetapi.*Test</param>
    </targetTests>
    <excludedClasses>
        <param>com.example.swplanetapi.domain.Planet</param>
    </excludedClasses>
</configuration>
...
```
Classes que só tem métodos estáticos e possuem um construtor privado não contam para a cobertura no relatório do Pitest.

## TestContainers
Vamos configurar um container para rodar os nossos testes usando a biblioteca TestContainers. Para isso, vamos adicionar as dependências do TestContainers e a integração com o JUnit:
```
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-bom</artifactId>
    <version>1.16.1</version>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.18.3</version>
</dependency>
```
Também vamos adicionar a dependência para especificar que esse container será um MySQL:
```
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <version>1.18.3</version>
</dependency> 
```
Agora só precisamos alterar o application-it.properties, que é o perfil usado para os testes de integração:
```
# Configuracao do DataSource
# Precisamos adicionar :tc após o jbdc para especificar que é um test container. Após isso dizemos qual o tipo de base de dados e a versão. Damos um nome para o container (Nesse caso, vamos escolher o nome db para o container que será criado). 
spring.datasource.url=jdbc:tc:mysql:8.0:///db?TC_IMAGE_TAG=8.0

# Configuracao do Hibernate
spring.jpa.hibernate.ddl-auto=update
```
## Testes parametrizados
Geração de dados para testes, como por exemplo planetas vazios, com atributos sendo Strings vazias, etc.

Vamos criar um método no nosso teste de repository:
```
private static Stream<Arguments> providesInvalidPlanets() {
    return Stream.of(
        Arguments.of(new Planet(null, "climate", "terrain")),
        Arguments.of(new Planet("name", null, "terrain")),
        Arguments.of(new Planet("name", "climate", null)),
        Arguments.of(new Planet(null, null, "terrain")),
        Arguments.of(new Planet(null, "climate", null)),
        Arguments.of(new Planet("name", null, null)),
        Arguments.of(new Planet(null, null, null)),
        Arguments.of(new Planet("", "climate", "terrain")),
        Arguments.of(new Planet("name", "", "terrain")),
        Arguments.of(new Planet("name", "climate", "")),
        Arguments.of(new Planet("", "", "terrain")),
        Arguments.of(new Planet("", "climate", "")),
        Arguments.of(new Planet("name", "", "")),
        Arguments.of(new Planet("", "", ""))
    );
  }
```
Esses serão os dados parametrizados para o nosso teste. Agora vamos usá-los para verificar que não é possível criar um planeta com nenhum dos dados acima pois nenhum dos atributos pode ser nulo ou vazio:
```
@ParameterizedTest
@MethodSource("providesInvalidPlanets")
public void createPlanet_WithInvalidData_ThrowsException(Planet planet) {
    assertThatThrownBy(() -> planetRepository.save(planet)).isInstanceOf(RuntimeException.class);
}
```
Usamos a anotação @ParameterizedTest para dizer que o teste recebe parâmetros. Adicionamos o método que vai fornecer os dados dentro da anotação @MethodSource e adicionamos o tipo de parâmetro e o seu nome no método (nesse caso Planet planet). Passamos esse parâmetro no teste.