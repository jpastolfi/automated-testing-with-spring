package com.example.swplanetapi.jacoco;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Anotação vai ser aplicada em tempo de execução
@Retention(RetentionPolicy.RUNTIME)
// E em métodos que tiverem ela
@Target(ElementType.METHOD)
public @interface ExcludeFromJacocoGeneratedReport {

}
