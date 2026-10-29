package pt.matriz;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Regra de dependências da clean architecture (docs/architecture.md §6): as camadas só dependem das que
 * estão mais para dentro. controllers → services → repositories → domain; dtos e mappers ficam na fronteira HTTP.
 */
@AnalyzeClasses(packages = "pt.matriz", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule dominioNaoDependeDasOutrasCamadas = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..controllers..", "..services..",
                    "..repositories..", "..dtos..", "..mappers..", "..security..", "..config..");

    @ArchTest
    static final ArchRule controladoresNaoUsamRepositorios = noClasses().that().resideInAPackage("..controllers..")
            .should().dependOnClassesThat().resideInAPackage("..repositories..");

    @ArchTest
    static final ArchRule servicosNaoConhecemHttp = noClasses().that().resideInAPackage("..services..")
            .should().dependOnClassesThat().resideInAnyPackage("..controllers..", "..dtos..", "..mappers..");

    @ArchTest
    static final ArchRule repositoriosSoConhecemODominio = noClasses().that().resideInAPackage("..repositories..")
            .should().dependOnClassesThat().resideInAnyPackage("..controllers..", "..services..", "..dtos..",
                    "..mappers..");

    @ArchTest
    static final ArchRule dtosSaoSoDados = noClasses().that().resideInAPackage("..dtos..")
            .should().dependOnClassesThat().resideInAnyPackage("..controllers..", "..services..",
                    "..repositories..", "..mappers..");

    @ArchTest
    static final ArchRule mappersNaoAcedemADados = noClasses().that().resideInAPackage("..mappers..")
            .should().dependOnClassesThat().resideInAnyPackage("..controllers..", "..services..",
                    "..repositories..");
}
