package be.valuya.lombok.optionalgetter.idea

import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.JavaFindUsagesHandler
import com.intellij.find.findUsages.JavaFindUsagesHandlerFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField

/**
 * Makes "Find Usages" on an `@OptionalGetter` field also search its generated `getXxxOptional()` accessor,
 * so the field surfaces the getter's call sites (the same approach the bundled Lombok plugin uses for its
 * generated accessors). Without this, Find Usages on the field only finds direct field references and the
 * standard accessors (e.g. a `@Setter`-generated setter) — never the custom-named Optional getter.
 */
class OptionalGetterFindUsagesHandlerFactory(project: Project) : JavaFindUsagesHandlerFactory(project) {

    override fun canFindUsages(element: PsiElement): Boolean =
        element is PsiField &&
            OptionalGetterSupport.appliesTo(element) &&
            OptionalGetterSupport.findGenerated(element) != null

    override fun createFindUsagesHandler(element: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler {
        val factory = this
        // Default Java field handler (searches the field + its standard accessors), plus our generated getter.
        return object : JavaFindUsagesHandler(element, factory) {
            override fun getSecondaryElements(): Array<PsiElement> {
                val base = super.getSecondaryElements()
                val field = psiElement as? PsiField ?: return base
                val generated = OptionalGetterSupport.findGenerated(field) ?: return base
                return if (base.contains(generated)) base else base + generated
            }
        }
    }
}
