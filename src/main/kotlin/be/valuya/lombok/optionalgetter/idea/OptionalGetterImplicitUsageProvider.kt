package be.valuya.lombok.optionalgetter.idea

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField

/**
 * Tells IntelliJ that an `@OptionalGetter` field is *read* by its generated `getXxxOptional()` accessor,
 * so the "Private field is assigned but never accessed" inspection no longer fires on it.
 *
 * The augment provider injects the accessor as a synthetic [com.intellij.psi.PsiMethod] whose body does not
 * reference the field in the PSI tree (it is a light method with no source), so the unused-symbol inspection
 * cannot see the read on its own. This is the same mechanism the bundled Lombok plugin uses for `@Getter`.
 */
class OptionalGetterImplicitUsageProvider : ImplicitUsageProvider {

    override fun isImplicitUsage(element: PsiElement): Boolean = isImplicitRead(element)

    override fun isImplicitRead(element: PsiElement): Boolean =
        element is PsiField && OptionalGetterSupport.appliesTo(element)

    override fun isImplicitWrite(element: PsiElement): Boolean = false
}
