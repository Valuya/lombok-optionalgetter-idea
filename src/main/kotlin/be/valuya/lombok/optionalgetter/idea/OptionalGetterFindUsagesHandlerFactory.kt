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
 * generated accessors). Without this, Find Usages on the field only finds direct field references.
 */
class OptionalGetterFindUsagesHandlerFactory(project: Project) : JavaFindUsagesHandlerFactory(project) {

    override fun canFindUsages(element: PsiElement): Boolean =
        element is PsiField && OptionalGetterSupport.appliesTo(element)

    override fun createFindUsagesHandler(element: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler? {
        if (element !is PsiField || !OptionalGetterSupport.appliesTo(element)) return null
        val generatedGetter = OptionalGetterSupport.findGenerated(element)
            ?: return JavaFindUsagesHandler(element, this)
        // Add the synthetic getter as a secondary element so its usages are included in the field's search.
        return JavaFindUsagesHandler(element, arrayOf<PsiElement>(generatedGetter), this)
    }
}
