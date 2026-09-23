package com.powerfitness;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces the layered architecture (config / dto / entity / exception / mapper / repository /
 * controller / security / service). Rules may match zero classes while the codebase is
 * still being built out; each one starts biting as soon as its layer has classes.
 */
@AnalyzeClasses(packages = "com.powerfitness", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule controllers_go_through_services = noClasses()
            .that().resideInAPackage("com.powerfitness.controller..")
            .should().dependOnClassesThat().resideInAPackage("com.powerfitness.repository..")
            .as("controllers must call a service, never a repository directly")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controllers_do_not_return_entities = noClasses()
            .that().resideInAPackage("com.powerfitness.controller..")
            .should().dependOnClassesThat().resideInAPackage("com.powerfitness.entity..")
            .as("controllers exchange DTOs, not entities")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule repositories_are_interfaces = classes()
            .that().resideInAPackage("com.powerfitness.repository..")
            .and().haveSimpleNameEndingWith("Repository")
            .should().beInterfaces()
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule entities_have_no_service_dependency = noClasses()
            .that().resideInAPackage("com.powerfitness.entity..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.powerfitness.service..",
                    "com.powerfitness.controller..",
                    "com.powerfitness.mapper..")
            .as("entities are persistence state, not orchestration")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule service_contracts_do_not_know_implementations = noClasses()
            .that().resideInAPackage("com.powerfitness.service")
            .should().dependOnClassesThat().resideInAPackage("com.powerfitness.service.impl..")
            .as("a service interface must not depend on its implementation")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule mappers_have_no_business_logic = noClasses()
            .that().resideInAPackage("com.powerfitness.mapper..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.powerfitness.repository..",
                    "com.powerfitness.service..")
            .as("MapStruct mappers only translate between entities and DTOs")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domain_engines_are_framework_free = noClasses()
            .that().resideInAPackage("com.powerfitness.service")
            .and().haveSimpleNameEndingWith("Generator")
            .or().resideInAPackage("com.powerfitness.service").and().haveSimpleNameEndingWith("Analyzer")
            .or().resideInAPackage("com.powerfitness.service").and().haveSimpleNameEndingWith("Calculator")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..")
            .as("the assessment analyzer / roadmap generator must stay plain, unit-testable Java")
            .allowEmptyShould(true);
}
