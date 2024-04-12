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