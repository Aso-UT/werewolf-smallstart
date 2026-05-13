package werewolf

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import kotlin.test.Test

class ArchitectureTest {
    private val classes = ClassFileImporter().importPackages("werewolf")

    @Test
    fun `game must not depend on any other werewolf package`() {
        noClasses().that().resideInAPackage("werewolf.game..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "werewolf.phase..", "werewolf.cpu..", "werewolf.ai..",
                "werewolf.human..", "werewolf.lodge.."
            ).check(classes)
    }

    @Test
    fun `phase must not depend on cpu, ai, human, or lodge`() {
        noClasses().that().resideInAPackage("werewolf.phase..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "werewolf.cpu..", "werewolf.ai..", "werewolf.human..", "werewolf.lodge.."
            ).check(classes)
    }

    @Test
    fun `cpu must not depend on phase, ai, human, or lodge`() {
        noClasses().that().resideInAPackage("werewolf.cpu..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "werewolf.phase..", "werewolf.ai..", "werewolf.human..", "werewolf.lodge.."
            ).check(classes)
    }

    @Test
    fun `ai must not depend on phase, cpu, human, or lodge`() {
        noClasses().that().resideInAPackage("werewolf.ai..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "werewolf.phase..", "werewolf.cpu..", "werewolf.human..", "werewolf.lodge.."
            ).check(classes)
    }

    @Test
    fun `human must not depend on phase, cpu, ai, or lodge`() {
        noClasses().that().resideInAPackage("werewolf.human..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "werewolf.phase..", "werewolf.cpu..", "werewolf.ai..", "werewolf.lodge.."
            ).check(classes)
    }

    @Test
    fun `lodge must not depend on phase`() {
        noClasses().that().resideInAPackage("werewolf.lodge..")
            .should().dependOnClassesThat().resideInAPackage("werewolf.phase..")
            .check(classes)
    }
}
