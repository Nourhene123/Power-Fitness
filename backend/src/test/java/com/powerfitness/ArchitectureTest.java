package com.powerfitness;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces the layered architecture (Config / DTO / Entity / Exception / Mapper / Repository /
 * RestController / Security / Services). Rules may match zero classes while the codebase is
 * still being built out; each one starts biting as soon as its layer has classes.
 */
@AnalyzeClasses(packages = "com.powerfitness", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule controllers_go_through_services = noClasses()
            .that().resideInAPackage("com.powerfitness.RestController..")
            .should().dependOnClassesThat().resideInAPackage("com.powerfitness.Repository..")
            .as("controllers must call a service, never a repository directly")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controllers_do_not_return_entities = noClasses()
            .that().resideInAPackage("com.powerfitness.RestController..")
            .should().dependOnClassesThat().resideInAPackage("com.powerfitness.Entity..")
            .as("controllers exchange DTOs, not entities")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule repositories_are_interfaces = classes()
            .that().resideInAPackage("com.powerfitness.Repository..")
            .and().haveSimpleNameEndingWith("Repository")
            .should().beInterfaces()
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule entities_have_no_service_dependency = noClasses()
            .that().resideInAPackage("com.powerfitness.Entity..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.powerfitness.Services..",
                    "com.powerfitness.RestController..",
                    "com.powerfitness.Mapper..")
            .as("entities are persistence state, not orchestration")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule service_contracts_do_not_know_implementations = noClasses()
            .that().resideInAPackage("com.powerfitness.Services.Interface..")
            .should().dependOnClassesThat().resideInAPackage("com.powerfitness.Services.Implimentation..")
            .as("a service interface must not depend on its implementation")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule mappers_have_no_business_logic = noClasses()
            .that().resideInAPackage("com.powerfitness.Mapper..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.powerfitness.Repository..",
                    "com.powerfitness.Services..")
            .as("MapStruct mappers only translate between entities and DTOs")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domain_engines_are_framework_free = noClasses()
            .that().resideInAPackage("com.powerfitness.Services")
            .and().haveSimpleNameEndingWith("Generator")
            .or().resideInAPackage("com.powerfitness.Services").and().haveSimpleNameEndingWith("Analyzer")
            .or().resideInAPackage("com.powerfitness.Services").and().haveSimpleNameEndingWith("Calculator")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..")
            .as("the assessment analyzer / roadmap generator must stay plain, unit-testable Java")
            .allowEmptyShould(true);
}
