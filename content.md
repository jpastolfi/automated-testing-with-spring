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