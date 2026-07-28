package be.valuya.lombok.optionalgetter.idea

import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.find.findUsages.JavaFindUsagesHandler
import com.intellij.find.findUsages.JavaFindUsagesHandlerFactory
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField

private val LOG = Logger.getInstance("be.valuya.lombok.optionalgetter.idea")

/**
 * Makes "Find Usages" on an `@OptionalGetter` field also search its generated `getXxxOptional()` accessor,
 * so the field surfaces the getter's call sites (the same approach the bundled Lombok plugin uses for its
 * generated accessors). Without this, Find Usages on the field only finds direct field references and the
 * standard accessors (e.g. a `@Setter`-generated setter) — never the custom-named Optional getter.
 *
 * Extends the base [FindUsagesHandlerFactory] (registered `order="first"`) so it is consulted before the
 * platform's own Java factory, and delegates the actual field search to a real [JavaFindUsagesHandler],
 * only appending our generated getter to its secondary elements.
 */
class OptionalGetterFindUsagesHandlerFactory(private val project: Project) : FindUsagesHandlerFactory() {

    override fun canFindUsages(element: PsiElement): Boolean {
        val result = element is PsiField && OptionalGetterSupport.appliesTo(element)
        if (element is PsiField && result) LOG.info("OptionalGetter: canFindUsages=true for field '${element.name}'")
        return result
    }

    override fun createFindUsagesHandler(element: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler? {
        if (element !is PsiField) return null
        val javaFactory = JavaFindUsagesHandlerFactory.getInstance(project)
        LOG.info("OptionalGetter: createFindUsagesHandler for field '${element.name}', generated=${OptionalGetterSupport.findGenerated(element)?.name}")
        return object : JavaFindUsagesHandler(element, javaFactory) {
            override fun getSecondaryElements(): Array<PsiElement> {
                val base = super.getSecondaryElements()
                val generated = OptionalGetterSupport.findGenerated(element)
                LOG.info("OptionalGetter: getSecondaryElements base=${base.map { (it as? PsiField)?.name ?: it.toString() }} generated=${generated?.name}")
                if (generated == null) return base
                return if (base.contains(generated)) base else base + generated
            }
        }
    }
}
