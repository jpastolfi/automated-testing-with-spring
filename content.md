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