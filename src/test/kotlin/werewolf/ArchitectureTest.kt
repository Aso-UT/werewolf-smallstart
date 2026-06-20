package werewolf

import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

@AnalyzeClasses(packages = ["werewolf"])
class ArchitectureTest {

    @ArchTest
    val gameMustNotDependOnOtherPackages: ArchRule =
        noClasses().that().resideInAPackage("werewolf.game..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "werewolf.phase..", "werewolf.cpu..", "werewolf.ai..",
                "werewolf.human..", "werewolf.lodge.."
            )

    @ArchTest
    val phaseMustNotDependOnCpuAiHumanOrLodge: ArchRule =
        noClasses().that().resideInAPackage("werewolf.phase..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "werewolf.cpu..", "werewolf.ai..", "werewolf.human..", "werewolf.lodge.."
            )

    @ArchTest
    val cpuMustNotDependOnPhaseAiHumanOrLodge: ArchRule =
        noClasses().that().resideInAPackage("werewolf.cpu..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "werewolf.phase..", "werewolf.ai..", "werewolf.human..", "werewolf.lodge.."
            )

    @ArchTest
    val aiMustNotDependOnPhaseCpuHumanOrLodge: ArchRule =
        noClasses().that().resideInAPackage("werewolf.ai..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "werewolf.phase..", "werewolf.cpu..", "werewolf.human..", "werewolf.lodge.."
            )

    @ArchTest
    val humanMustNotDependOnPhaseCpuAiLodgeOrWeb: ArchRule =
        noClasses().that().resideInAPackage("werewolf.human..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "werewolf.phase..", "werewolf.cpu..", "werewolf.ai..", "werewolf.lodge..", "werewolf.web.."
            )

    @ArchTest
    val webMustNotDependOnPhaseCpuAiLodgeOrHuman: ArchRule =
        noClasses().that().resideInAPackage("werewolf.web..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "werewolf.phase..", "werewolf.cpu..", "werewolf.ai..", "werewolf.lodge..", "werewolf.human.."
            )

    @ArchTest
    val lodgeMustNotDependOnPhase: ArchRule =
        noClasses().that().resideInAPackage("werewolf.lodge..")
            .should().dependOnClassesThat().resideInAPackage("werewolf.phase..")
}
